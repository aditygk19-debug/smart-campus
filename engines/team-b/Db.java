import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Db {

    private static final String URL =
        System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:mysql://localhost:3306/smartcampus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");

    private static final String USER =
        System.getenv().getOrDefault("DB_USER", "root");

    private static final String PASS =
        System.getenv().getOrDefault("DB_PASSWORD", "satoru87");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found on classpath.");
            e.printStackTrace();
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static boolean testConnection() {
        try (Connection c = get()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("DB test failed: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------
    // Finds SlotId by matching StartTime and EndTime
    // ------------------------------------------------------------
    public static int findSlotId(String startTime, String endTime) throws SQLException {
        String s = normalizeTime(startTime);
        String e = normalizeTime(endTime);
        String sql = "SELECT SlotId FROM TIME_SLOT WHERE StartTime = ? AND EndTime = ? LIMIT 1";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s);
            ps.setString(2, e);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("SlotId");
            }
        }
        return -1;
    }

    // ------------------------------------------------------------
    // Search available resources matching filters.
    // A resource is "available" if it has no Confirmed/Pending booking
    // for that SlotId + BookingDate.
    // ------------------------------------------------------------
    public static List<Map<String, Object>> searchAvailable(
            int slotId,
            String date,
            String resourceType,   // null = any
            int minCapacity
    ) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append(
            "SELECT r.ResourceId, r.ResourceName, r.Capacity, r.Status, " +
            "       d.DepartmentName, rt.TypeName, " +
            "       l.RoomNumber AS NodeId " +
            "FROM RESOURCES r " +
            "JOIN DEPARTMENT d ON d.DepartmentId = r.DepartmentId " +
            "JOIN RESOURCE_TYPE rt ON rt.TypeId = r.TypeId " +
            "JOIN LOCATION l ON l.LocationId = r.LocationId " +
            "WHERE r.Status = 'Available' " +
            "  AND r.Capacity >= ? "
        );

        if (resourceType != null && !resourceType.isEmpty()) {
            sql.append(" AND rt.TypeName = ? ");
        }

        sql.append(
            "  AND r.ResourceId NOT IN ( " +
            "    SELECT b.ResourceId FROM BOOKINGS b " +
            "    WHERE b.SlotId = ? AND b.BookingDate = ? " +
            "      AND b.Status IN ('Pending','Confirmed') " +
            "  ) "
        );

        List<Map<String, Object>> out = new ArrayList<>();

        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setInt(idx++, minCapacity);
            if (resourceType != null && !resourceType.isEmpty()) {
                ps.setString(idx++, resourceType);
            }
            ps.setInt(idx++, slotId);
            ps.setString(idx, date);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("resourceId", rs.getInt("ResourceId"));
                    row.put("resourceName", rs.getString("ResourceName"));
                    row.put("capacity", rs.getInt("Capacity"));
                    row.put("status", rs.getString("Status"));
                    row.put("departmentName", rs.getString("DepartmentName"));
                    row.put("typeName", rs.getString("TypeName"));
                    row.put("nodeId", rs.getString("NodeId"));
                    out.add(row);
                }
            }
        }
        return out;
    }

    private static String normalizeTime(String t) {
        if (t == null) return null;
        t = t.trim();
        if (t.matches("\\d{2}:\\d{2}")) return t + ":00";
        return t;
    }
}