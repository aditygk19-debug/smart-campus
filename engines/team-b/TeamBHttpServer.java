import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeamBHttpServer {

    private static final int PORT = 8091;
    private static Graph graph;
    private static RoutingEngine routingEngine;

    public static void main(String[] args) throws IOException {

        buildGraph();
        routingEngine = new RoutingEngine();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/health", new HealthHandler());
        server.createContext("/route", new RouteHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("Team B HTTP Server running on http://localhost:" + PORT);
        System.out.println("  GET  http://localhost:" + PORT + "/health");
        System.out.println("  POST http://localhost:" + PORT + "/route");
    }

    private static void buildGraph() {
        graph = new Graph();

        graph.addNode(new Node("C1", "Classroom", "CSE Classroom 1", "ACTIVE"));
        graph.addNode(new Node("C2", "Classroom", "CSE Classroom 2", "ACTIVE"));
        graph.addNode(new Node("C3", "Classroom", "CSE Classroom 3", "ACTIVE"));
        graph.addNode(new Node("L1", "Lab", "CSE Lab 1", "ACTIVE"));
        graph.addNode(new Node("L2", "Lab", "CSE Lab 2", "ACTIVE"));
        graph.addNode(new Node("L3", "Lab", "CSE Lab 3", "ACTIVE"));
        graph.addNode(new Node("L4", "Lab", "CSE Lab 4", "ACTIVE"));
        graph.addNode(new Node("N4", "Department", "CSE Department", "ACTIVE"));

        graph.addNode(new Node("AC1", "Classroom", "AIML Classroom 1", "ACTIVE"));
        graph.addNode(new Node("AC2", "Classroom", "AIML Classroom 2", "ACTIVE"));
        graph.addNode(new Node("AC3", "Classroom", "AIML Classroom 3", "ACTIVE"));
        graph.addNode(new Node("AL1", "Lab", "AIML Lab 1", "ACTIVE"));
        graph.addNode(new Node("AL2", "Lab", "AIML Lab 2", "ACTIVE"));
        graph.addNode(new Node("AL3", "Lab", "AIML Lab 3", "ACTIVE"));
        graph.addNode(new Node("N7", "Department", "AIML Building", "ACTIVE"));

        graph.addNode(new Node("EC1", "Classroom", "Electrical Classroom 1", "ACTIVE"));
        graph.addNode(new Node("EC2", "Classroom", "Electrical Classroom 2", "ACTIVE"));
        graph.addNode(new Node("EC3", "Classroom", "Electrical Classroom 3", "ACTIVE"));
        graph.addNode(new Node("EL1", "Lab", "Electrical Lab 1", "ACTIVE"));
        graph.addNode(new Node("EL2", "Lab", "Electrical Lab 2", "ACTIVE"));
        graph.addNode(new Node("N6", "Department", "Electrical Department", "ACTIVE"));

        graph.addNode(new Node("MC1", "Classroom", "Mechanical Classroom 1", "ACTIVE"));
        graph.addNode(new Node("MC2", "Classroom", "Mechanical Classroom 2", "ACTIVE"));
        graph.addNode(new Node("ML1", "Lab", "Mechanical Lab 1", "ACTIVE"));
        graph.addNode(new Node("ML2", "Lab", "Mechanical Lab 2", "ACTIVE"));
        graph.addNode(new Node("N8", "Department", "Mechanical Department", "ACTIVE"));

        graph.addNode(new Node("N1", "Gate", "Main Gate", "ACTIVE"));
        graph.addNode(new Node("N2", "Gate", "Gate 1", "ACTIVE"));
        graph.addNode(new Node("N3", "Library", "Library", "ACTIVE"));
        graph.addNode(new Node("N5", "Office", "Office", "ACTIVE"));
        graph.addNode(new Node("CH1", "Conference Hall", "Conference Hall", "ACTIVE"));

        graph.addBidirectionalEdge("E00",  "E00R",  "N1", "N4", 55, 10, true);
        graph.addBidirectionalEdge("E00A", "E00AR", "N1", "N5", 70, 15, true);
        graph.addBidirectionalEdge("E00B", "E00BR", "N2", "N5", 80, 18, true);
        graph.addBidirectionalEdge("E00C", "E00CR", "N4", "N6", 10, 5, true);
        graph.addBidirectionalEdge("E00D", "E00DR", "N6", "N7", 30, 8, true);
        graph.addBidirectionalEdge("E00E", "E00ER", "N7", "N8", 30, 8, true);
        graph.addBidirectionalEdge("E00F", "E00FR", "N8", "N2", 50, 12, true);

        graph.addBidirectionalEdge("E01", "E01R", "N4", "L1", 10, 3, true);
        graph.addBidirectionalEdge("E03", "E03R", "N4", "L2", 15, 4, true);
        graph.addBidirectionalEdge("E05", "E05R", "N4", "L3", 20, 5, true);
        graph.addBidirectionalEdge("E07", "E07R", "N4", "L4", 22, 5, true);
        graph.addBidirectionalEdge("E09", "E09R", "N4", "C1", 15, 4, true);
        graph.addBidirectionalEdge("E11", "E11R", "N4", "C2", 24, 5, true);
        graph.addBidirectionalEdge("E13", "E13R", "N4", "C3", 25, 6, true);
        graph.addBidirectionalEdge("E15", "E15R", "N4", "CH1", 22, 5, true);

        graph.addBidirectionalEdge("E17", "E17R", "N4", "EL1", 70, 12, true);
        graph.addBidirectionalEdge("E19", "E19R", "N4", "EL2", 80, 14, true);
        graph.addBidirectionalEdge("E21", "E21R", "N4", "EC1", 75, 13, true);
        graph.addBidirectionalEdge("E23", "E23R", "N4", "EC2", 83, 15, true);
        graph.addBidirectionalEdge("E25", "E25R", "N4", "EC3", 85, 16, true);

        graph.addBidirectionalEdge("E27", "E27R", "N6", "AC1", 70, 12, true);
        graph.addBidirectionalEdge("E29", "E29R", "N6", "AC2", 75, 13, true);
        graph.addBidirectionalEdge("E31", "E31R", "N6", "AC3", 80, 14, true);
        graph.addBidirectionalEdge("E33", "E33R", "N6", "AL1", 90, 15, true);
        graph.addBidirectionalEdge("E35", "E35R", "N6", "AL2", 92, 16, true);
        graph.addBidirectionalEdge("E37", "E37R", "N6", "AL3", 94, 17, true);

        graph.addBidirectionalEdge("E39", "E39R", "N7", "MC1", 70, 12, true);
        graph.addBidirectionalEdge("E41", "E41R", "N7", "MC2", 75, 13, true);
        graph.addBidirectionalEdge("E43", "E43R", "N7", "ML1", 82, 15, true);
        graph.addBidirectionalEdge("E45", "E45R", "N7", "ML2", 84, 16, true);

        System.out.println("Graph loaded: 31 nodes, 60 edges.");
    }

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            sendJson(ex, 200,
                "{\"status\":\"ok\",\"service\":\"team-b-routing\",\"graphVersion\":\""
                + graph.getGraphVersion() + "\"}");
        }
    }

    static class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(ex);
            String source = extractString(body, "sourceNodeId");
            String dest   = extractString(body, "destNodeId");
            String algo   = extractString(body, "algorithm");
            if (algo == null) algo = "DIJKSTRA";

            if (source == null || dest == null) {
                sendJson(ex, 400, "{\"error\":\"sourceNodeId and destNodeId required\"}");
                return;
            }

            try {
                RouteResult r = routingEngine.findRoute(graph, source, dest, algo);

                StringBuilder sb = new StringBuilder();
                sb.append("{")
                  .append("\"found\":").append(r.isRouteFound()).append(",")
                  .append("\"algorithm\":\"").append(escape(r.getAlgorithm())).append("\",")
                  .append("\"graphVersion\":\"").append(escape(r.getGraphVersion())).append("\",")
                  .append("\"path\":").append(toJsonArray(r.getOrderedNodes())).append(",")
                  .append("\"edges\":").append(toJsonArray(r.getOrderedEdges())).append(",")
                  .append("\"totalDistance\":").append(r.getTotalCost()).append(",")
                  .append("\"totalLatency\":").append(r.getEstimatedLatency()).append(",")
                  .append("\"nodesExplored\":").append(r.getExploredNodeCount())
                  .append("}");

                sendJson(ex, 200, sb.toString());

            } catch (Exception e) {
                sendJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
            }
        }
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String extractString(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static String toJsonArray(List<String> items) {
        if (items == null || items.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(items.get(i))).append("\"");
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