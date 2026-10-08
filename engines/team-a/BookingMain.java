import java.time.LocalDateTime;

public class BookingMain {

    public static void main(String[] args)
            throws Exception {

        /*
         * Change this password to your MySQL
         * root password.
         */

        String mysqlUser = "root";
        String mysqlPassword = "Sup@123*";

        BookingManager manager =
                new BookingManager(
                        mysqlUser,
                        mysqlPassword
                );

        // =====================================================
        // REGISTER CAMPUS RESOURCES
        // =====================================================

        manager.addResource(
                "L1",
                BookingResourceType.LAB,
                "CSE Lab 1",
                30
        );

        manager.addResource(
                "L2",
                BookingResourceType.LAB,
                "CSE Lab 2",
                30
        );

        manager.addResource(
                "C1",
                BookingResourceType.CLASSROOM,
                "CSE Classroom 1",
                60
        );

        manager.addResource(
                "C2",
                BookingResourceType.CLASSROOM,
                "CSE Classroom 2",
                60
        );

        manager.addResource(
                "H1",
                BookingResourceType.CONFERENCE_HALL,
                "Main Conference Hall",
                150
        );

        // =====================================================
        // CREATE USER BOOKING REQUESTS
        // =====================================================

        LocalDateTime start =
                LocalDateTime.now()
                        .plusHours(1);

        LocalDateTime end =
                start.plusHours(2);

        // Student 1 requests CSE Lab 1

        manager.submitBooking(
                "STUDENT01",
                BookingResourceType.LAB,
                "L1",
                start,
                end,
                3
        );

        // Student 2 requests same lab
        // with higher priority.

        manager.submitBooking(
                "STUDENT02",
                BookingResourceType.LAB,
                "L1",
                start,
                end,
                1
        );

        // Student 3 requests classroom

        manager.submitBooking(
                "STUDENT03",
                BookingResourceType.CLASSROOM,
                "C1",
                start,
                end,
                2
        );

        // Student 4 requests conference hall

        manager.submitBooking(
                "STUDENT04",
                BookingResourceType.CONFERENCE_HALL,
                "H1",
                start,
                end,
                1
        );

        // =====================================================
        // PRIORITY SCHEDULING
        // =====================================================

        manager.scheduleBookings(
                SchedulingPolicy.PRIORITY
        );

        // =====================================================
        // DISPLAY RESULT
        // =====================================================

        manager.displayBookings();

        System.out.println(
                "\n=========================================="
        );

        System.out.println(
                "BOOKING SYSTEM FINISHED"
        );

        System.out.println(
                "=========================================="
        );
    }
}