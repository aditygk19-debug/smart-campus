public class Metrics {

    private int completedProcesses = 0;

    private long totalWaitingTime = 0;

    private long totalTurnaroundTime = 0;

    public void record(
            SchedulingRequest request
    ) {

        completedProcesses++;

        totalWaitingTime +=
                request.getWaitingTime();

        totalTurnaroundTime +=
                request.getTurnaroundTime();
    }

    public void display() {

        System.out.println(
                "\n============== SCHEDULING METRICS =============="
        );

        if (completedProcesses == 0) {

            System.out.println(
                    "No completed processes."
            );

            return;
        }

        double averageWaiting =
                (double) totalWaitingTime
                / completedProcesses;

        double averageTurnaround =
                (double) totalTurnaroundTime
                / completedProcesses;

        System.out.println(
                "Completed Processes : "
                + completedProcesses
        );

        System.out.println(
                "Average Waiting Time: "
                + averageWaiting
                + " minutes"
        );

        System.out.println(
                "Average Turnaround  : "
                + averageTurnaround
                + " minutes"
        );

        System.out.println(
                "Throughput          : "
                + completedProcesses
                + " processes"
        );
    }
}