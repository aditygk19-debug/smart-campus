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

/**
 * TeamAHttpServer — HTTP wrapper for Team A's OS Scheduler Engine.
 *
 * Exposes:
 *   GET  /health   → basic status
 *   GET  /labs     → list all registered labs
 *   POST /schedule → submit a request + run scheduler + return decision
 *
 * Built by Team D (Engine 4) for integration. Delegates all logic to TeamAEngine.
 */
public class TeamAHttpServer {

    private static final int PORT = 8090;
    private static final TeamAEngine engine = new TeamAEngine();

    public static void main(String[] args) throws IOException {

        registerAllResources();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/health", new HealthHandler());
        server.createContext("/labs", new LabsHandler());
        server.createContext("/schedule", new ScheduleHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("✅ Team A HTTP Server running on http://localhost:" + PORT);
        System.out.println("   Endpoints:");
        System.out.println("   GET  http://localhost:" + PORT + "/health");
        System.out.println("   GET  http://localhost:" + PORT + "/labs");
        System.out.println("   POST http://localhost:" + PORT + "/schedule");
    }

    // ----------------------------------------------------------------
    // Register all 23 resources (same as Main.java)
    // ----------------------------------------------------------------
    private static void registerAllResources() {

        // CSE Labs
        engine.addLab("L1", "CSE", "CSE Lab 1", 30);
        engine.addLab("L2", "CSE", "CSE Lab 2", 30);
        engine.addLab("L3", "CSE", "CSE Lab 3", 30);
        engine.addLab("L4", "CSE", "CSE Lab 4", 30);

        // Electrical Labs
        engine.addLab("EL1", "ELECTRICAL", "Electrical Lab 1", 30);
        engine.addLab("EL2", "ELECTRICAL", "Electrical Lab 2", 30);

        // AIML Labs
        engine.addLab("AL1", "AIML", "AIML Lab 1", 30);
        engine.addLab("AL2", "AIML", "AIML Lab 2", 30);
        engine.addLab("AL3", "AIML", "AIML Lab 3", 30);

        // Mechanical Labs
        engine.addLab("ML1", "MECHANICAL", "Mechanical Lab 1", 30);
        engine.addLab("ML2", "MECHANICAL", "Mechanical Lab 2", 30);

        // CSE Classrooms + Conference Hall
        engine.addLab("C1", "CSE", "CSE Classroom 1", 60);
        engine.addLab("C2", "CSE", "CSE Classroom 2", 60);
        engine.addLab("C3", "CSE", "CSE Classroom 3", 60);
        engine.addLab("CH1", "CSE", "Conference Hall", 100);

        // Electrical Classrooms
        engine.addLab("EC1", "ELECTRICAL", "Electrical Classroom 1", 60);
        engine.addLab("EC2", "ELECTRICAL", "Electrical Classroom 2", 60);
        engine.addLab("EC3", "ELECTRICAL", "Electrical Classroom 3", 60);

        // AIML Classrooms
        engine.addLab("AC1", "AIML", "AIML Classroom 1", 60);
        engine.addLab("AC2", "AIML", "AIML Classroom 2", 60);
        engine.addLab("AC3", "AIML", "AIML Classroom 3", 60);

        // Mechanical Classrooms
        engine.addLab("MC1", "MECHANICAL", "Mechanical Classroom 1", 60);
        engine.addLab("MC2", "MECHANICAL", "Mechanical Classroom 2", 60);

        System.out.println("Registered 23 resources in TeamAEngine.");
    }

    // ----------------------------------------------------------------
    // Handlers
    // ----------------------------------------------------------------

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            sendJson(ex, 200, "{\"status\":\"ok\",\"service\":\"team-a-scheduler\"}");
        }
    }

    static class LabsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            // Static list (built from the same data above) — simple & fast
            String json = "[" +
                "{\"id\":\"L1\",\"dept\":\"CSE\",\"name\":\"CSE Lab 1\",\"capacity\":30}," +
                "{\"id\":\"L2\",\"dept\":\"CSE\",\"name\":\"CSE Lab 2\",\"capacity\":30}," +
                "{\"id\":\"L3\",\"dept\":\"CSE\",\"name\":\"CSE Lab 3\",\"capacity\":30}," +
                "{\"id\":\"L4\",\"dept\":\"CSE\",\"name\":\"CSE Lab 4\",\"capacity\":30}," +
                "{\"id\":\"EL1\",\"dept\":\"ELECTRICAL\",\"name\":\"Electrical Lab 1\",\"capacity\":30}," +
                "{\"id\":\"EL2\",\"dept\":\"ELECTRICAL\",\"name\":\"Electrical Lab 2\",\"capacity\":30}," +
                "{\"id\":\"AL1\",\"dept\":\"AIML\",\"name\":\"AIML Lab 1\",\"capacity\":30}," +
                "{\"id\":\"AL2\",\"dept\":\"AIML\",\"name\":\"AIML Lab 2\",\"capacity\":30}," +
                "{\"id\":\"AL3\",\"dept\":\"AIML\",\"name\":\"AIML Lab 3\",\"capacity\":30}," +
                "{\"id\":\"ML1\",\"dept\":\"MECHANICAL\",\"name\":\"Mechanical Lab 1\",\"capacity\":30}," +
                "{\"id\":\"ML2\",\"dept\":\"MECHANICAL\",\"name\":\"Mechanical Lab 2\",\"capacity\":30}," +
                "{\"id\":\"C1\",\"dept\":\"CSE\",\"name\":\"CSE Classroom 1\",\"capacity\":60}," +
                "{\"id\":\"C2\",\"dept\":\"CSE\",\"name\":\"CSE Classroom 2\",\"capacity\":60}," +
                "{\"id\":\"C3\",\"dept\":\"CSE\",\"name\":\"CSE Classroom 3\",\"capacity\":60}," +
                "{\"id\":\"CH1\",\"dept\":\"CSE\",\"name\":\"Conference Hall\",\"capacity\":100}," +
                "{\"id\":\"EC1\",\"dept\":\"ELECTRICAL\",\"name\":\"Electrical Classroom 1\",\"capacity\":60}," +
                "{\"id\":\"EC2\",\"dept\":\"ELECTRICAL\",\"name\":\"Electrical Classroom 2\",\"capacity\":60}," +
                "{\"id\":\"EC3\",\"dept\":\"ELECTRICAL\",\"name\":\"Electrical Classroom 3\",\"capacity\":60}," +
                "{\"id\":\"AC1\",\"dept\":\"AIML\",\"name\":\"AIML Classroom 1\",\"capacity\":60}," +
                "{\"id\":\"AC2\",\"dept\":\"AIML\",\"name\":\"AIML Classroom 2\",\"capacity\":60}," +
                "{\"id\":\"AC3\",\"dept\":\"AIML\",\"name\":\"AIML Classroom 3\",\"capacity\":60}," +
                "{\"id\":\"MC1\",\"dept\":\"MECHANICAL\",\"name\":\"Mechanical Classroom 1\",\"capacity\":60}," +
                "{\"id\":\"MC2\",\"dept\":\"MECHANICAL\",\"name\":\"Mechanical Classroom 2\",\"capacity\":60}" +
                "]";
            sendJson(ex, 200, json);
        }
    }

    static class ScheduleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(ex);

            String userId   = extractString(body, "userId");
            String labId    = extractString(body, "labId");
            Integer computers = extractInt(body, "computers");
            Integer duration  = extractInt(body, "duration");
            Integer priority  = extractInt(body, "priority");

            if (userId == null || labId == null || computers == null
                    || duration == null || priority == null) {
                sendJson(ex, 400, "{\"error\":\"userId, labId, computers, duration, priority required\"}");
                return;
            }

            try {
                // Submit request
                SchedulingRequest req = engine.submitRequest(
                        userId, labId, computers, duration, priority
                );

                if (req == null) {
                    sendJson(ex, 400, "{\"error\":\"Lab does not exist\"}");
                    return;
                }

                // Run PRIORITY scheduler for that lab
                engine.runScheduler(labId, SchedulingPolicy.PRIORITY);

                // Build response
                String json = String.format(
                        "{\"requestId\":\"%s\",\"userId\":\"%s\",\"labId\":\"%s\","
                      + "\"state\":\"%s\",\"queuePosition\":%d,\"priority\":%d,"
                      + "\"computers\":%d,\"duration\":%d}",
                        req.getRequestId(),
                        req.getUserId(),
                        req.getLabId(),
                        req.getState(),
                        req.getQueuePosition(),
                        req.getPriority(),
                        req.getRequiredComputers(),
                        req.getDurationMinutes()
                );

                sendJson(ex, 201, json);

            } catch (Exception e) {
                sendJson(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
            }
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