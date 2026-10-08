import java.sql.*;
import java.time.LocalDateTime;

public class BookingDatabase {

    private final String url;
    private final String user;
    private final String password;

    public BookingDatabase(
            String user,
            String password
    ) {

        this.user = user;
        this.password = password;

        this.url =
                "jdbc:mysql://localhost:3306/smart_campus" +
                "?useSSL=false" +
                "&serverTimezone=Asia/Kolkata";
    }

    public Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                url,
                user,
                password
        );
    }

    // ---------------------------------------------------------
    // INITIALIZE DATABASE
    // ---------------------------------------------------------

    public void initialize()
            throws SQLException {

        String serverUrl =
                "jdbc:mysql://localhost:3306/" +
                "?useSSL=false" +
                "&serverTimezone=Asia/Kolkata";

        try (
                Connection con =
                        DriverManager.getConnection(
                                serverUrl,
                                user,
                                password
                        );

                Statement statement =
                        con.createStatement()
        ) {

            statement.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS smart_campus"
            );
        }

        try (
                Connection con =
                        getConnection();

                Statement statement =
                        con.createStatement()
        ) {

            // Campus resources

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS campus_resources (" +
                    "resource_id VARCHAR(50) PRIMARY KEY," +
                    "resource_type VARCHAR(30) NOT NULL," +
                    "resource_name VARCHAR(150) NOT NULL," +
                    "capacity INT DEFAULT 0" +
                    ")"
            );

            // User bookings

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS campus_bookings (" +
                    "booking_id VARCHAR(50) PRIMARY KEY," +
                    "user_id VARCHAR(100) NOT NULL," +
                    "resource_id VARCHAR(50) NOT NULL," +
                    "resource_type VARCHAR(30) NOT NULL," +
                    "start_time DATETIME NOT NULL," +
                    "end_time DATETIME NOT NULL," +
                    "priority INT NOT NULL," +
                    "submitted_at DATETIME NOT NULL," +
                    "status VARCHAR(30) NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(resource_id) " +
                    "REFERENCES campus_resources(resource_id)" +
                    ")"
            );
        }
    }

    // ---------------------------------------------------------
    // ADD RESOURCE
    // ---------------------------------------------------------

    public void addResource(
            String resourceId,
            BookingResourceType type,
            String name,
            int capacity
    ) throws SQLException {

        String sql =
                "INSERT INTO campus_resources " +
                "(resource_id, resource_type, resource_name, capacity) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "resource_type = VALUES(resource_type), " +
                "resource_name = VALUES(resource_name), " +
                "capacity = VALUES(capacity)";

        try (
                Connection con =
                        getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    resourceId
            );

            ps.setString(
                    2,
                    type.name()
            );

            ps.setString(
                    3,
                    name
            );

            ps.setInt(
                    4,
                    capacity
            );

            ps.executeUpdate();
        }
    }

    // ---------------------------------------------------------
    // CHECK RESOURCE
    // ---------------------------------------------------------

    public boolean resourceExists(
            String resourceId
    ) throws SQLException {

        String sql =
                "SELECT resource_id " +
                "FROM campus_resources " +
                "WHERE resource_id = ?";

        try (
                Connection con =
                        getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    resourceId
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next();
            }
        }
    }

    // ---------------------------------------------------------
    // CHECK AVAILABILITY + CONFIRM
    // ---------------------------------------------------------

    public boolean confirmBooking(
            BookingRequest request
    ) throws SQLException {

        try (
                Connection con =
                        getConnection()
        ) {

            con.setAutoCommit(false);

            try {

                // Lock resource row.

                try (
                        PreparedStatement lock =
                                con.prepareStatement(
                                        "SELECT resource_id " +
                                        "FROM campus_resources " +
                                        "WHERE resource_id = ? " +
                                        "FOR UPDATE"
                                )
                ) {

                    lock.setString(
                            1,
                            request.getResourceId()
                    );

                    try (
                            ResultSet rs =
                                    lock.executeQuery()
                    ) {

                        if (!rs.next()) {

                            con.rollback();

                            return false;
                        }
                    }
                }

                // Check overlapping confirmed bookings.

                String conflictSql =
                        "SELECT booking_id " +
                        "FROM campus_bookings " +
                        "WHERE resource_id = ? " +
                        "AND status = 'CONFIRMED' " +
                        "AND start_time < ? " +
                        "AND end_time > ? " +
                        "LIMIT 1";

                try (
                        PreparedStatement ps =
                                con.prepareStatement(
                                        conflictSql
                                )
                ) {

                    ps.setString(
                            1,
                            request.getResourceId()
                    );

                    ps.setTimestamp(
                            2,
                            Timestamp.valueOf(
                                    request.getEndTime()
                            )
                    );

                    ps.setTimestamp(
                            3,
                            Timestamp.valueOf(
                                    request.getStartTime()
                            )
                    );

                    try (
                            ResultSet rs =
                                    ps.executeQuery()
                    ) {

                        if (rs.next()) {

                            con.rollback();

                            return false;
                        }
                    }
                }

                // Save confirmed booking.

                String insertSql =
                        "INSERT INTO campus_bookings " +
                        "(booking_id,user_id,resource_id," +
                        "resource_type,start_time,end_time," +
                        "priority,submitted_at,status) " +
                        "VALUES (?,?,?,?,?,?,?,?,?)";

                try (
                        PreparedStatement ps =
                                con.prepareStatement(
                                        insertSql
                                )
                ) {

                    ps.setString(
                            1,
                            request.getBookingId()
                    );

                    ps.setString(
                            2,
                            request.getUserId()
                    );

                    ps.setString(
                            3,
                            request.getResourceId()
                    );

                    ps.setString(
                            4,
                            request.getResourceType()
                                    .name()
                    );

                    ps.setTimestamp(
                            5,
                            Timestamp.valueOf(
                                    request.getStartTime()
                            )
                    );

                    ps.setTimestamp(
                            6,
                            Timestamp.valueOf(
                                    request.getEndTime()
                            )
                    );

                    ps.setInt(
                            7,
                            request.getPriority()
                    );

                    ps.setTimestamp(
                            8,
                            Timestamp.valueOf(
                                    request.getSubmittedAt()
                            )
                    );

                    ps.setString(
                            9,
                            BookingStatus.CONFIRMED.name()
                    );

                    ps.executeUpdate();
                }

                con.commit();

                return true;

            } catch (SQLException e) {

                con.rollback();

                throw e;

            } finally {

                con.setAutoCommit(true);
            }
        }
    }

    // ---------------------------------------------------------
    // SAVE WAITING REQUEST
    // ---------------------------------------------------------

    public void saveWaitingRequest(
            BookingRequest request
    ) throws SQLException {

        String sql =
                "INSERT INTO campus_bookings " +
                "(booking_id,user_id,resource_id," +
                "resource_type,start_time,end_time," +
                "priority,submitted_at,status) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";

        try (
                Connection con =
                        getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    request.getBookingId()
            );

            ps.setString(
                    2,
                    request.getUserId()
            );

            ps.setString(
                    3,
                    request.getResourceId()
            );

            ps.setString(
                    4,
                    request.getResourceType().name()
            );

            ps.setTimestamp(
                    5,
                    Timestamp.valueOf(
                            request.getStartTime()
                    )
            );

            ps.setTimestamp(
                    6,
                    Timestamp.valueOf(
                            request.getEndTime()
                    )
            );

            ps.setInt(
                    7,
                    request.getPriority()
            );

            ps.setTimestamp(
                    8,
                    Timestamp.valueOf(
                            request.getSubmittedAt()
                    )
            );

            ps.setString(
                    9,
                    BookingStatus.WAITING.name()
            );

            ps.executeUpdate();
        }
    }

    // ---------------------------------------------------------
    // UPDATE WAITING -> CONFIRMED
    // ---------------------------------------------------------

    public boolean tryConfirmExisting(
            BookingRequest request
    ) throws SQLException {

        try (
                Connection con =
                        getConnection()
        ) {

            con.setAutoCommit(false);

            try {

                String conflictSql =
                        "SELECT booking_id " +
                        "FROM campus_bookings " +
                        "WHERE resource_id = ? " +
                        "AND status = 'CONFIRMED' " +
                        "AND booking_id <> ? " +
                        "AND start_time < ? " +
                        "AND end_time > ? " +
                        "LIMIT 1";

                try (
                        PreparedStatement ps =
                                con.prepareStatement(
                                        conflictSql
                                )
                ) {

                    ps.setString(
                            1,
                            request.getResourceId()
                    );

                    ps.setString(
                            2,
                            request.getBookingId()
                    );

                    ps.setTimestamp(
                            3,
                            Timestamp.valueOf(
                                    request.getEndTime()
                            )
                    );

                    ps.setTimestamp(
                            4,
                            Timestamp.valueOf(
                                    request.getStartTime()
                            )
                    );

                    try (
                            ResultSet rs =
                                    ps.executeQuery()
                    ) {

                        if (rs.next()) {

                            con.rollback();

                            return false;
                        }
                    }
                }

                try (
                        PreparedStatement ps =
                                con.prepareStatement(
                                        "UPDATE campus_bookings " +
                                        "SET status='CONFIRMED' " +
                                        "WHERE booking_id=?"
                                )
                ) {

                    ps.setString(
                            1,
                            request.getBookingId()
                    );

                    ps.executeUpdate();
                }

                con.commit();

                return true;

            } catch (SQLException e) {

                con.rollback();

                throw e;

            } finally {

                con.setAutoCommit(true);
            }
        }
    }
}