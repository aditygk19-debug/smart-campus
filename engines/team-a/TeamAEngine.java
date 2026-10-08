import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class TeamAEngine {

    private ResourceManager resourceManager =
            new ResourceManager();

    private LockManager lockManager =
            new LockManager();

    private Scheduler scheduler =
            new Scheduler();

    private Metrics metrics =
            new Metrics();

    private Map<String, SchedulingRequest> requests =
            new HashMap<>();

    private Map<String, Queue<SchedulingRequest>> labQueues =
            new HashMap<>();

    private SchedulingPolicy currentPolicy =
            SchedulingPolicy.FCFS;

    private int requestCounter = 100;

    // ---------------------------------------------------------
    // ADD LAB
    // ---------------------------------------------------------

    public void addLab(
            String id,
            String department,
            String name,
            int computers
    ) {

        resourceManager.addLab(
                id,
                department,
                name,
                computers
        );

        labQueues.put(
                id,
                new LinkedList<>()
        );
    }

    // ---------------------------------------------------------
    // SUBMIT REQUEST
    // ---------------------------------------------------------

    public SchedulingRequest submitRequest(
            String userId,
            String labId,
            int computers,
            int duration,
            int priority
    ) {

        if (!resourceManager.labExists(labId)) {

            System.out.println(
                    "ERROR: Lab " +
                    labId +
                    " does not exist."
            );

            return null;
        }

        String requestId =
                "REQ" + (++requestCounter);

        SchedulingRequest request =
                new SchedulingRequest(
                        requestId,
                        userId,
                        labId,
                        computers,
                        duration,
                        priority
                );

        request.setState(
                ProcessState.QUEUED
        );

        requests.put(
                requestId,
                request
        );

        labQueues
                .get(labId)
                .offer(request);

        request.setQueuePosition(
                labQueues
                        .get(labId)
                        .size()
        );

        System.out.println(
                "\nRequest accepted!"
        );

        System.out.println(request);

        return request;
    }

    // ---------------------------------------------------------
    // RUN SCHEDULER
    // ---------------------------------------------------------

    public void runScheduler(
            String labId,
            SchedulingPolicy policy
    ) {

        if (!labQueues.containsKey(labId)) {

            System.out.println(
                    "Lab not found."
            );

            return;
        }

        currentPolicy = policy;

        Queue<SchedulingRequest> queue =
                labQueues.get(labId);

        if (queue.isEmpty()) {

            System.out.println(
                    "No requests in queue for "
                    + labId
            );

            return;
        }

        List<SchedulingRequest> requestList =
                new ArrayList<>(queue);

        List<SchedulingRequest> ordered =
                scheduler.schedule(
                        requestList,
                        policy
                );

        queue.clear();

        queue.addAll(ordered);

        System.out.println(
                "\n============================================"
        );

        System.out.println(
                "SCHEDULING LAB: " + labId
        );

        System.out.println(
                "POLICY: " + policy
        );

        System.out.println(
                "============================================"
        );

        int position = 1;

        for (SchedulingRequest request : queue) {

            request.setQueuePosition(
                    position++
            );

            request.setState(
                    ProcessState.READY
            );

            System.out.println(
                    request.getQueuePosition()
                    + ". "
                    + request.getRequestId()
                    + " | Duration="
                    + request.getDurationMinutes()
                    + " | Priority="
                    + request.getPriority()
            );
        }
    }

    // ---------------------------------------------------------
    // EXECUTE NEXT PROCESS
    // ---------------------------------------------------------

    public void executeNext(String labId) {

        Queue<SchedulingRequest> queue =
                labQueues.get(labId);

        if (queue == null || queue.isEmpty()) {

            System.out.println(
                    "Queue is empty."
            );

            return;
        }

        SchedulingRequest request =
                queue.peek();

        Lab lab =
                resourceManager.getLab(labId);

        System.out.println(
                "\nTrying to execute: "
                + request.getRequestId()
        );

        // -----------------------------------------------------
        // RESOURCE CHECK
        // -----------------------------------------------------

        if (!lab.canAllocate(
                request.getRequiredComputers()
        )) {

            request.setState(
                    ProcessState.WAITING
            );

            System.out.println(
                    "Not enough resources."
            );

            System.out.println(
                    request.getRequestId()
                    + " -> WAITING"
            );

            return;
        }

        // -----------------------------------------------------
        // RESOURCE ALLOCATION
        // -----------------------------------------------------

        List<String> resources =
                resourceManager.allocateResources(
                        labId,
                        request.getRequiredComputers(),
                        request.getRequestId()
                );

        if (resources.isEmpty()) {

            request.setState(
                    ProcessState.WAITING
            );

            return;
        }

        // -----------------------------------------------------
        // LOCK
        // -----------------------------------------------------

        boolean lockGranted =
                lockManager.acquire(
                        request.getRequestId(),
                        resources
                );

        if (!lockGranted) {

            request.setState(
                    ProcessState.WAITING
            );

            resourceManager.releaseResources(
                    labId,
                    resources
            );

            System.out.println(
                    "Lock denied."
            );

            return;
        }

        // -----------------------------------------------------
        // RUNNING
        // -----------------------------------------------------

        queue.poll();

        request.setState(
                ProcessState.RUNNING
        );

        request.setScheduledStart(
                LocalDateTime.now()
        );

        long waitingTime =
                Duration.between(
                        request.getSubmittedAt(),
                        request.getScheduledStart()
                ).toMinutes();

        request.setWaitingTime(
                waitingTime
        );

        request.setAssignedResources(
                resources
        );

        System.out.println(
                "\nPROCESS STARTED"
        );

        System.out.println(
                "Request   : "
                + request.getRequestId()
        );

        System.out.println(
                "Lab       : "
                + labId
        );

        System.out.println(
                "Policy    : "
                + currentPolicy
        );

        System.out.println(
                "Resources : "
                + resources
        );

        System.out.println(
                "State     : RUNNING"
        );

        // -----------------------------------------------------
        // SIMULATE EXECUTION
        // -----------------------------------------------------

        try {

            Thread.sleep(
                    Math.min(
                            request.getDurationMinutes()
                                    * 100L,
                            3000L
                    )
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        // -----------------------------------------------------
        // COMPLETED
        // -----------------------------------------------------

        request.setState(
                ProcessState.COMPLETED
        );

        request.setCompletionTime(
                LocalDateTime.now()
        );

        long turnaroundTime =
                Duration.between(
                        request.getSubmittedAt(),
                        request.getCompletionTime()
                ).toMinutes();

        request.setTurnaroundTime(
                turnaroundTime
        );

        // -----------------------------------------------------
        // RELEASE LOCK
        // -----------------------------------------------------

        lockManager.release(
                request.getRequestId(),
                resources
        );

        // -----------------------------------------------------
        // RELEASE COMPUTERS
        // -----------------------------------------------------

        resourceManager.releaseResources(
                labId,
                resources
        );

        metrics.record(request);

        System.out.println(
                "\nPROCESS COMPLETED"
        );

        System.out.println(
                "Request        : "
                + request.getRequestId()
        );

        System.out.println(
                "Waiting Time   : "
                + request.getWaitingTime()
                + " minutes"
        );

        System.out.println(
                "Turnaround Time: "
                + request.getTurnaroundTime()
                + " minutes"
        );

        System.out.println(
                "Resources      : "
                + resources
        );

        System.out.println(
                "State          : COMPLETED"
        );
    }

    // ---------------------------------------------------------
    // DEADLOCK ANALYSIS
    // ---------------------------------------------------------

    public void analyseDeadlock() {

        boolean deadlock =
                lockManager.detectDeadlock();

        System.out.println(
                "\n========== DEADLOCK ANALYSIS =========="
        );

        if (deadlock) {

            System.out.println(
                    "DEADLOCK DETECTED!"
            );

        } else {

            System.out.println(
                    "No deadlock detected."
            );
        }
    }

    // ---------------------------------------------------------
    // DISPLAY QUEUE
    // ---------------------------------------------------------

    public void displayQueue(String labId) {

        Queue<SchedulingRequest> queue =
                labQueues.get(labId);

        System.out.println(
                "\n========== QUEUE: "
                + labId
                + " =========="
        );

        if (queue == null || queue.isEmpty()) {

            System.out.println(
                    "Queue empty."
            );

            return;
        }

        int position = 1;

        for (SchedulingRequest request :
                queue) {

            System.out.println(
                    position++
                    + ". "
                    + request
            );
        }
    }

    // ---------------------------------------------------------
    // DISPLAY ALL REQUESTS
    // ---------------------------------------------------------

    public void displayAllRequests() {

        System.out.println(
                "\n========== ALL REQUESTS =========="
        );

        for (SchedulingRequest request :
                requests.values()) {

            System.out.println(request);
        }
    }

    // ---------------------------------------------------------
    // GET RESOURCE MANAGER
    // ---------------------------------------------------------

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    // ---------------------------------------------------------
    // GET METRICS
    // ---------------------------------------------------------

    public Metrics getMetrics() {
        return metrics;
    }

    // ---------------------------------------------------------
    // GET LOCK MANAGER
    // ---------------------------------------------------------

    public LockManager getLockManager() {
        return lockManager;
    }
}