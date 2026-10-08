import java.time.LocalDateTime;
import java.util.*;

public class BookingManager {

    private final BookingDatabase database;

    private final List<BookingRequest> requests =
            new ArrayList<>();

    private int bookingCounter = 1000;

    private SchedulingPolicy currentPolicy =
            SchedulingPolicy.FCFS;

    public BookingManager(
            String mysqlUser,
            String mysqlPassword
    ) throws Exception {

        database =
                new BookingDatabase(
                        mysqlUser,
                        mysqlPassword
                );

        database.initialize();
    }

    // ---------------------------------------------------------
    // ADD CAMPUS RESOURCE
    // ---------------------------------------------------------

    public void addResource(
            String resourceId,
            BookingResourceType type,
            String name,
            int capacity
    ) throws Exception {

        database.addResource(
                resourceId,
                type,
                name,
                capacity
        );
    }

    // ---------------------------------------------------------
    // SUBMIT BOOKING REQUEST
    // ---------------------------------------------------------

    public BookingRequest submitBooking(
            String userId,
            BookingResourceType type,
            String resourceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int priority
    ) throws Exception {

        if (startTime == null ||
            endTime == null) {

            throw new IllegalArgumentException(
                    "Start and end time are required."
            );
        }

        if (!endTime.isAfter(startTime)) {

            throw new IllegalArgumentException(
                    "End time must be after start time."
            );
        }

        if (!database.resourceExists(
                resourceId
        )) {

            throw new IllegalArgumentException(
                    "Resource does not exist: "
                    + resourceId
            );
        }

        String bookingId =
                "BOOK" +
                (++bookingCounter);

        BookingRequest request =
                new BookingRequest(
                        bookingId,
                        userId,
                        type,
                        resourceId,
                        startTime,
                        endTime,
                        priority
                );

        requests.add(request);

        database.saveWaitingRequest(
                request
        );

        request.setQueuePosition(
                requests.size()
        );

        System.out.println(
                "\nBooking request submitted:"
        );

        System.out.println(
                request
        );

        return request;
    }

    // ---------------------------------------------------------
    // RUN BOOKING SCHEDULER
    // ---------------------------------------------------------

    public void scheduleBookings(
            SchedulingPolicy policy
    ) throws Exception {

        if (policy != SchedulingPolicy.FCFS &&
            policy != SchedulingPolicy.PRIORITY) {

            throw new IllegalArgumentException(
                    "Booking system supports only FCFS and PRIORITY."
            );
        }

        currentPolicy = policy;

        List<BookingRequest> waiting =
                new ArrayList<>();

        for (BookingRequest request :
                requests) {

            if (request.getStatus() ==
                    BookingStatus.WAITING) {

                waiting.add(request);
            }
        }

        if (policy == SchedulingPolicy.FCFS) {

            waiting.sort(
                    Comparator.comparing(
                            BookingRequest::getSubmittedAt
                    )
            );

        } else {

            // Smaller priority number =
            // higher priority.
            // Submitted time breaks ties.

            waiting.sort(
                    Comparator
                            .comparingInt(
                                    BookingRequest::getPriority
                            )
                            .thenComparing(
                                    BookingRequest
                                            ::getSubmittedAt
                            )
            );
        }

        System.out.println(
                "\n=========================================="
        );

        System.out.println(
                "BOOKING SCHEDULER"
        );

        System.out.println(
                "POLICY: " + policy
        );

        System.out.println(
                "=========================================="
        );

        int position = 1;

        for (BookingRequest request :
                waiting) {

            request.setQueuePosition(
                    position
            );

            System.out.println(
                    position +
                    ". " +
                    request.getBookingId() +
                    " | Resource=" +
                    request.getResourceId() +
                    " | Priority=" +
                    request.getPriority()
            );

            position++;
        }

        // -----------------------------------------------------
        // AUTOMATIC CONFIRMATION
        // -----------------------------------------------------

        for (BookingRequest request :
                waiting) {

            boolean confirmed =
                    database.tryConfirmExisting(
                            request
                    );

            if (confirmed) {

                request.setStatus(
                        BookingStatus.CONFIRMED
                );

                System.out.println(
                        "\nCONFIRMED: " +
                        request.getBookingId()
                );

            } else {

                System.out.println(
                        "\nWAITING: " +
                        request.getBookingId() +
                        " -> resource unavailable"
                );
            }
        }
    }

    // ---------------------------------------------------------
    // DISPLAY BOOKINGS
    // ---------------------------------------------------------

    public void displayBookings() {

        System.out.println(
                "\n========== BOOKING REQUESTS =========="
        );

        for (BookingRequest request :
                requests) {

            System.out.println(
                    request
            );
        }
    }

    // ---------------------------------------------------------
    // GET CURRENT POLICY
    // ---------------------------------------------------------

    public SchedulingPolicy getCurrentPolicy() {

        return currentPolicy;
    }
}