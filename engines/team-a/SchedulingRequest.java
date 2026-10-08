import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SchedulingRequest {

    private String requestId;
    private String userId;
    private String labId;

    private int requiredComputers;
    private int durationMinutes;
    private int priority;

    private LocalDateTime submittedAt;

    private ProcessState state;

    private int queuePosition;

    private LocalDateTime scheduledStart;
    private LocalDateTime completionTime;

    private List<String> assignedResources;

    private long waitingTime;
    private long turnaroundTime;

    public SchedulingRequest(
            String requestId,
            String userId,
            String labId,
            int requiredComputers,
            int durationMinutes,
            int priority
    ) {

        this.requestId = requestId;
        this.userId = userId;
        this.labId = labId;

        this.requiredComputers = requiredComputers;
        this.durationMinutes = durationMinutes;
        this.priority = priority;

        this.submittedAt = LocalDateTime.now();

        this.state = ProcessState.CREATED;

        this.assignedResources = new ArrayList<>();
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUserId() {
        return userId;
    }

    public String getLabId() {
        return labId;
    }

    public int getRequiredComputers() {
        return requiredComputers;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public ProcessState getState() {
        return state;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public LocalDateTime getScheduledStart() {
        return scheduledStart;
    }

    public LocalDateTime getCompletionTime() {
        return completionTime;
    }

    public List<String> getAssignedResources() {
        return assignedResources;
    }

    public long getWaitingTime() {
        return waitingTime;
    }

    public long getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public void setScheduledStart(LocalDateTime scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
    }

    public void setAssignedResources(
            List<String> assignedResources
    ) {
        this.assignedResources = assignedResources;
    }

    public void setWaitingTime(long waitingTime) {
        this.waitingTime = waitingTime;
    }

    public void setTurnaroundTime(long turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    @Override
    public String toString() {

        return requestId +
                " | User: " + userId +
                " | Lab: " + labId +
                " | Computers: " + requiredComputers +
                " | Duration: " + durationMinutes +
                " min | Priority: " + priority +
                " | State: " + state;
    }
}