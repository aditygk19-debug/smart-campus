import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SearchHandler — POST /search
 *
 * Filters available resources from MySQL, computes Dijkstra distance
 * from a source node, and returns results sorted by distance ascending.
 */
public class SearchHandler implements HttpHandler {

    private final Graph graph;
    private final RoutingEngine routingEngine;

    public SearchHandler(Graph graph, RoutingEngine routingEngine) {
        this.graph = graph;
        this.routingEngine = routingEngine;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = readBody(ex);
        String date = extractString(body, "date");
        String startTime = extractString(body, "startTime");
        String endTime = extractString(body, "endTime");
        String resourceType = extractString(body, "resourceType");
        Integer minCapacity = extractInt(body, "minCapacity");
        String sourceNodeId = extractString(body, "sourceNodeId");

        if (date == null || startTime == null || endTime == null) {
            sendJson(ex, 400, "{\"error\":\"date, startTime, endTime required\"}");
            return;
        }
        if (minCapacity == null) minCapacity = 0;
        if (sourceNodeId == null || sourceNodeId.isEmpty()) sourceNodeId = "N1";

        try {
            int slotId = Db.findSlotId(startTime, endTime);
            if (slotId < 0) {
                sendJson(ex, 400, "{\"error\":\"No matching time slot for " + startTime + " - " + endTime + "\"}");
                return;
            }

            List<Map<String, Object>> rows = Db.searchAvailable(slotId, date, resourceType, minCapacity);

            List<Map<String, Object>> results = new ArrayList<>();

            for (Map<String, Object> row : rows) {
                String nodeId = (String) row.get("nodeId");
                if (nodeId == null || graph.getNode(nodeId) == null) {
                    // Resource has no matching campus node — skip
                    continue;
                }

                RouteResult rr = routingEngine.findRoute(graph, sourceNodeId, nodeId, "DIJKSTRA");

                Map<String, Object> enriched = new HashMap<>(row);
                enriched.put("nodeName", row.get("resourceName"));
                if (rr.isRouteFound()) {
                    enriched.put("distance", rr.getTotalCost());
                    enriched.put("latency", rr.getEstimatedLatency());
                    enriched.put("path", rr.getOrderedNodes());
                } else {
                    enriched.put("distance", Integer.MAX_VALUE);
                    enriched.put("latency", 0);
                    enriched.put("path", new ArrayList<String>());
                }
                results.add(enriched);
            }

            results.sort(Comparator.comparingInt(m -> (int) m.get("distance")));

            sendJson(ex, 200, toJsonArray(results));

        } catch (Exception e) {
            sendJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String extractString(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static Integer extractInt(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private static String toJsonArray(List<Map<String, Object>> rows) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map<String, Object> row : rows) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{");
            boolean f2 = true;
            for (Map.Entry<String, Object> e : row.entrySet()) {
                if (!f2) sb.append(",");
                f2 = false;
                sb.append("\"").append(e.getKey()).append("\":");
                Object v = e.getValue();
                if (v == null) {
                    sb.append("null");
                } else if (v instanceof Number || v instanceof Boolean) {
                    sb.append(v);
                } else if (v instanceof List) {
                    sb.append("[");
                    boolean f3 = true;
                    for (Object item : (List<?>) v) {
                        if (!f3) sb.append(",");
                        f3 = false;
                        sb.append("\"").append(escape(String.valueOf(item))).append("\"");
                    }
                    sb.append("]");
                } else {
                    sb.append("\"").append(escape(String.valueOf(v))).append("\"");
                }
            }
            sb.append("}");
        }
        return sb.append("]").toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void sendJson(HttpExchange ex, int status, String body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}