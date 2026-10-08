import java.time.LocalDateTime;

public class BookingRequest {

    private final String bookingId;
    private final String userId;

    private final BookingResourceType resourceType;
    private final String resourceId;

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    private final int priority;
    private final LocalDateTime submittedAt;

    private BookingStatus status;

    private int queuePosition;

    public BookingRequest(
            String bookingId,
            String userId,
            BookingResourceType resourceType,
            String resourceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int priority
    ) {

        this.bookingId = bookingId;
        this.userId = userId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;

        this.submittedAt = LocalDateTime.now();

        this.status = BookingStatus.WAITING;
        this.queuePosition = 0;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public BookingResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setStatus(
            BookingStatus status
    ) {
        this.status = status;
    }

    public void setQueuePosition(
            int queuePosition
    ) {
        this.queuePosition = queuePosition;
    }

    @Override
    public String toString() {

        return bookingId +
                " | User: " + userId +
                " | Resource: " + resourceId +
                " | Type: " + resourceType +
                " | Start: " + startTime +
                " | End: " + endTime +
                " | Priority: " + priority +
                " | Status: " + status;
    }
}