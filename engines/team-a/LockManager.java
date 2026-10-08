import java.util.*;

public class LockManager {

    private Map<String, String> resourceOwner =
            new HashMap<>();

    private Map<String, Set<String>> waitingFor =
            new HashMap<>();

    // ---------------------------------------------------------
    // ACQUIRE LOCK
    // ---------------------------------------------------------

    public boolean acquire(
            String requestId,
            List<String> resources
    ) {

        for (String resource : resources) {

            if (resourceOwner.containsKey(resource)) {

                String owner =
                        resourceOwner.get(resource);

                waitingFor
                        .computeIfAbsent(
                                requestId,
                                k -> new HashSet<>()
                        )
                        .add(owner);

                return false;
            }
        }

        for (String resource : resources) {

            resourceOwner.put(
                    resource,
                    requestId
            );
        }

        return true;
    }

    // ---------------------------------------------------------
    // RELEASE LOCK
    // ---------------------------------------------------------

    public void release(
            String requestId,
            List<String> resources
    ) {

        for (String resource : resources) {

            if (requestId.equals(
                    resourceOwner.get(resource))) {

                resourceOwner.remove(resource);
            }
        }

        waitingFor.remove(requestId);
    }

    // ---------------------------------------------------------
    // DEADLOCK DETECTION
    // ---------------------------------------------------------

    public boolean detectDeadlock() {

        Set<String> visited =
                new HashSet<>();

        Set<String> recursionStack =
                new HashSet<>();

        for (String process :
                waitingFor.keySet()) {

            if (detectCycle(
                    process,
                    visited,
                    recursionStack
            )) {

                return true;
            }
        }

        return false;
    }

    private boolean detectCycle(
            String process,
            Set<String> visited,
            Set<String> recursionStack
    ) {

        if (recursionStack.contains(process)) {
            return true;
        }

        if (visited.contains(process)) {
            return false;
        }

        visited.add(process);
        recursionStack.add(process);

        Set<String> dependencies =
                waitingFor.get(process);

        if (dependencies != null) {

            for (String next :
                    dependencies) {

                if (detectCycle(
                        next,
                        visited,
                        recursionStack
                )) {

                    return true;
                }
            }
        }

        recursionStack.remove(process);

        return false;
    }

    // ---------------------------------------------------------
    // DISPLAY LOCKS
    // ---------------------------------------------------------

    public void displayLocks() {

        System.out.println(
                "\n========== ACTIVE RESOURCE LOCKS =========="
        );

        if (resourceOwner.isEmpty()) {

            System.out.println(
                    "No active locks."
            );

            return;
        }

        for (Map.Entry<String, String> entry :
                resourceOwner.entrySet()) {

            System.out.println(
                    entry.getKey() +
                    " --> " +
                    entry.getValue()
            );
        }
    }
}