import java.util.*;

public class Scheduler {

    public List<SchedulingRequest> schedule(
            List<SchedulingRequest> requests,
            SchedulingPolicy policy
    ) {

        List<SchedulingRequest> result =
                new ArrayList<>(requests);

        if (policy == SchedulingPolicy.FCFS) {

            // FCFS:
            // Keep requests in the order
            // in which they were submitted.

            result.sort(
                    Comparator.comparing(
                            SchedulingRequest::getSubmittedAt
                    )
            );

        } else if (policy == SchedulingPolicy.PRIORITY) {

            // Priority:
            // Smaller priority number = higher priority.
            // If priority is same, earlier submission
            // time gets preference.

            result.sort(
                    Comparator
                            .comparingInt(
                                    SchedulingRequest::getPriority
                            )
                            .thenComparing(
                                    SchedulingRequest::getSubmittedAt
                            )
            );
        }

        return result;
    }
}