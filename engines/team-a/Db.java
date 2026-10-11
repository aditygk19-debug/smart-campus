import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

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
    // Finds ResourceId from a campus node id like "L1"
    // ------------------------------------------------------------
    public static int findResourceIdByNode(String nodeId) throws SQLException {
        String sql = "SELECT r.ResourceId FROM RESOURCES r " +
                     "JOIN LOCATION l ON l.LocationId = r.LocationId " +
                     "WHERE l.RoomNumber = ? LIMIT 1";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nodeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("ResourceId");
            }
        }
        return -1;
    }

    // ------------------------------------------------------------
    // Count confirmed bookings for a resource+slot+date
    // ------------------------------------------------------------
    public static int countConfirmed(int resourceId, int slotId, String date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM BOOKINGS " +
                     "WHERE ResourceId = ? AND SlotId = ? AND BookingDate = ? " +
                     "AND Status IN ('Pending','Confirmed')";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            ps.setInt(2, slotId);
            ps.setString(3, date);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ------------------------------------------------------------
    // Check if user already has an active booking for resource+slot+date
    // ------------------------------------------------------------
    public static boolean userHasActiveBooking(int userId, int resourceId, int slotId, String date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM BOOKINGS " +
                     "WHERE UserId = ? AND ResourceId = ? AND SlotId = ? AND BookingDate = ? " +
                     "AND Status IN ('Pending','Confirmed','Waitlisted')";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, resourceId);
            ps.setInt(3, slotId);
            ps.setString(4, date);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // ------------------------------------------------------------
    // Insert booking (Confirmed or Waitlisted)
    // Returns new BookingId, or -1 on failure
    // ------------------------------------------------------------
    public static int insertBooking(int userId, int resourceId, int slotId,
                                    String date, String purpose,
                                    String status, int priority) throws SQLException {
        String sql = "INSERT INTO BOOKINGS " +
                     "(UserId, ResourceId, SlotId, BookingDate, Purpose, Status, Priority) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, resourceId);
            ps.setInt(3, slotId);
            ps.setString(4, date);
            ps.setString(5, purpose);
            ps.setString(6, status);
            ps.setInt(7, priority);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // ------------------------------------------------------------
    // Compute waitlist position for a booking
    // ------------------------------------------------------------
    public static int computeWaitlistPosition(int resourceId, int slotId, String date,
                                              int priority, int myBookingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM BOOKINGS " +
                     "WHERE ResourceId = ? AND SlotId = ? AND BookingDate = ? " +
                     "AND Status = 'Waitlisted' " +
                     "AND (Priority < ? OR (Priority = ? AND BookingId < ?))";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            ps.setInt(2, slotId);
            ps.setString(3, date);
            ps.setInt(4, priority);
            ps.setInt(5, priority);
            ps.setInt(6, myBookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) + 1;
            }
        }
        return 1;
    }

    public static void updateWaitlistPosition(int bookingId, int position) throws SQLException {
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE BOOKINGS SET WaitlistPosition = ? WHERE BookingId = ?")) {
            ps.setInt(1, position);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------
    // Pick the highest-priority waiter for resource+slot+date
    // Returns BookingId or -1
    // ------------------------------------------------------------
    public static int pickNextWaiter(int resourceId, int slotId, String date) throws SQLException {
        String sql = "SELECT BookingId FROM BOOKINGS " +
                     "WHERE ResourceId = ? AND SlotId = ? AND BookingDate = ? " +
                     "AND Status = 'Waitlisted' " +
                     "ORDER BY Priority ASC, BookingId ASC LIMIT 1";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            ps.setInt(2, slotId);
            ps.setString(3, date);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("BookingId");
            }
        }
        return -1;
    }

    public static int getBookingUserId(int bookingId) throws SQLException {
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT UserId FROM BOOKINGS WHERE BookingId = ?")) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("UserId");
            }
        }
        return -1;
    }

    public static void promoteBooking(int bookingId) throws SQLException {
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE BOOKINGS SET Status = 'Confirmed', WaitlistPosition = NULL WHERE BookingId = ?")) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
    }

    public static void insertNotification(int userId, Integer bookingId, String message) throws SQLException {
        String sql = "INSERT INTO NOTIFICATIONS (UserId, BookingId, Message) VALUES (?, ?, ?)";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (bookingId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, bookingId);
            ps.setString(3, message);
            ps.executeUpdate();
        }
    }

    public static void insertAudit(Integer userId, String action, String entityType,
                                   Integer entityId, String newValue) throws SQLException {
        String sql = "INSERT INTO AUDIT_LOG (UserId, Action, EntityType, EntityId, NewValue) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, entityType);
            if (entityId == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, entityId);
            ps.setString(5, newValue);
            ps.executeUpdate();
        }
    }

    private static String normalizeTime(String t) {
        if (t == null) return null;
        t = t.trim();
        if (t.matches("\\d{2}:\\d{2}")) return t + ":00";
        return t;
    }
}