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
 * TeamAHttpServer — HTTP service for Team A (OS Scheduling engine).
 *
 * Talks directly to MySQL (smartcampus) via JDBC.
 *
 * Endpoints:
 *   GET  /health    — service status + DB status
 *   GET  /labs      — list all registered lab/classroom resources
 *   POST /schedule  — create a booking request (FCFS + priority)
 *   POST /waitlist  — add a waitlist entry for an occupied resource
 *   POST /promote   — promote highest-priority waiter after a cancellation
 *   GET  /queue     — list current waitlist for a resource+slot+date
 */
public class TeamAHttpServer {

    private static final int PORT = 8090;

    public static void main(String[] args) throws IOException {
        System.out.println("DB connectivity: " + (Db.testConnection() ? "OK" : "FAILED"));

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/health",   new HealthHandler());
        server.createContext("/labs",     new LabsHandler());
        server.createContext("/schedule", new ScheduleHandler());
        server.createContext("/waitlist", new WaitlistHandler());
        server.createContext("/promote",  new PromoteHandler());
        server.createContext("/queue",    new QueueHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("Team A HTTP Server running on http://localhost:" + PORT);
        System.out.println("   GET  /health");
        System.out.println("   GET  /labs");
        System.out.println("   POST /schedule");
        System.out.println("   POST /waitlist");
        System.out.println("   POST /promote");
        System.out.println("   GET  /queue");
    }

    // ----------------------------------------------------------------
    // /health
    // ----------------------------------------------------------------
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            boolean db = Db.testConnection();
            String json = "{\"status\":\"ok\",\"service\":\"team-a-scheduler\",\"db\":" + db + "}";
            sendJson(ex, 200, json);
        }
    }

    // ----------------------------------------------------------------
    // /labs
    // ----------------------------------------------------------------
    static class LabsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
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

    // ----------------------------------------------------------------
    // /schedule
    // Body: { userId, labId, date, startTime, endTime, purpose, priority }
    // ----------------------------------------------------------------
    static class ScheduleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(ex);
            Integer userId = extractInt(body, "userId");
            String labId = extractString(body, "labId");
            String date = extractString(body, "date");
            String startTime = extractString(body, "startTime");
            String endTime = extractString(body, "endTime");
            String purpose = extractString(body, "purpose");
            Integer priority = extractInt(body, "priority");

            if (userId == null || labId == null || date == null
                    || startTime == null || endTime == null) {
                sendJson(ex, 400, "{\"error\":\"userId, labId, date, startTime, endTime required\"}");
                return;
            }
            if (priority == null) priority = 2;
            if (purpose == null) purpose = "Booking";

            BookingService.Result r = BookingService.createOrWaitlist(
                    userId, labId, date, startTime, endTime, purpose, priority);

            String json = "{"
                + "\"ok\":" + r.ok + ","
                + "\"bookingId\":" + r.bookingId + ","
                + "\"status\":\"" + escape(r.status) + "\","
                + "\"message\":\"" + escape(r.message) + "\","
                + "\"waitlistPosition\":" + r.waitlistPosition + ","
                + "\"priority\":" + r.priority
                + "}";

            sendJson(ex, r.ok ? 201 : 409, json);
        }
    }

    // ----------------------------------------------------------------
    // /waitlist — explicit waitlist add (same as schedule but forces Waitlisted)
    // ----------------------------------------------------------------
    static class WaitlistHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(ex);
            Integer userId = extractInt(body, "userId");
            String labId = extractString(body, "labId");
            String date = extractString(body, "date");
            String startTime = extractString(body, "startTime");
            String endTime = extractString(body, "endTime");
            String purpose = extractString(body, "purpose");
            Integer priority = extractInt(body, "priority");

            if (userId == null || labId == null || date == null
                    || startTime == null || endTime == null) {
                sendJson(ex, 400, "{\"error\":\"userId, labId, date, startTime, endTime required\"}");
                return;
            }
            if (priority == null) priority = 2;
            if (purpose == null) purpose = "Waitlist request";

            BookingService.Result r = BookingService.createOrWaitlist(
                    userId, labId, date, startTime, endTime, purpose, priority);

            String json = "{"
                + "\"ok\":" + r.ok + ","
                + "\"bookingId\":" + r.bookingId + ","
                + "\"status\":\"" + escape(r.status) + "\","
                + "\"message\":\"" + escape(r.message) + "\","
                + "\"waitlistPosition\":" + r.waitlistPosition
                + "}";

            sendJson(ex, r.ok ? 201 : 409, json);
        }
    }

    // ----------------------------------------------------------------
    // /promote
    // Body: { labId, date, startTime, endTime }
    // ----------------------------------------------------------------
    static class PromoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(ex);
            String labId = extractString(body, "labId");
            String date = extractString(body, "date");
            String startTime = extractString(body, "startTime");
            String endTime = extractString(body, "endTime");

            if (labId == null || date == null || startTime == null || endTime == null) {
                sendJson(ex, 400, "{\"error\":\"labId, date, startTime, endTime required\"}");
                return;
            }

            Map<String, Object> out = BookingService.promoteAfterCancel(labId, date, startTime, endTime);

            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> e : out.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(e.getKey()).append("\":");
                Object v = e.getValue();
                if (v instanceof Boolean || v instanceof Integer) sb.append(v);
                else sb.append("\"").append(escape(String.valueOf(v))).append("\"");
            }
            sb.append("}");
            sendJson(ex, 200, sb.toString());
        }
    }

    // ----------------------------------------------------------------
    // /queue?labId=L1&date=2026-10-15&startTime=10:00&endTime=12:00
    // ----------------------------------------------------------------
    static class QueueHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> q = parseQuery(ex.getRequestURI().getQuery());
            String labId = q.get("labId");
            String date = q.get("date");
            String startTime = q.get("startTime");
            String endTime = q.get("endTime");
            if (labId == null || date == null || startTime == null || endTime == null) {
                sendJson(ex, 400, "{\"error\":\"labId, date, startTime, endTime required\"}");
                return;
            }

            try {
                int resourceId = Db.findResourceIdByNode(labId);
                int slotId = Db.findSlotId(startTime, endTime);
                if (resourceId < 0 || slotId < 0) {
                    sendJson(ex, 200, "[]");
                    return;
                }
                String sql =
                    "SELECT BookingId, UserId, Priority, WaitlistPosition " +
                    "FROM BOOKINGS " +
                    "WHERE ResourceId=? AND SlotId=? AND BookingDate=? AND Status='Waitlisted' " +
                    "ORDER BY Priority ASC, BookingId ASC";
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                try (java.sql.Connection c = Db.get();
                     java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, resourceId);
                    ps.setInt(2, slotId);
                    ps.setString(3, date);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (!first) sb.append(",");
                            first = false;
                            sb.append("{")
                              .append("\"bookingId\":").append(rs.getInt("BookingId")).append(",")
                              .append("\"userId\":").append(rs.getInt("UserId")).append(",")
                              .append("\"priority\":").append(rs.getInt("Priority")).append(",")
                              .append("\"position\":").append(rs.getInt("WaitlistPosition"))
                              .append("}");
                        }
                    }
                }
                sb.append("]");
                sendJson(ex, 200, sb.toString());
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

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            int i = pair.indexOf('=');
            if (i > 0) {
                map.put(pair.substring(0, i), pair.substring(i + 1));
            }
        }
        return map;
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