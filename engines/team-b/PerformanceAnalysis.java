import java.util.ArrayList;
import java.util.Collections;

public class PerformanceAnalysis {

    public void benchmark(
            Graph graph,
            String startNodeId,
            int repetitions) {

        benchmark(
                graph,
                startNodeId,
                "ML2",
                repetitions);
    }

    public void benchmark(
            Graph graph,
            String startNodeId,
            String destinationNodeId,
            int repetitions) {

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                 PERFORMANCE ANALYSIS");
        System.out.println("==============================================================");

        if (graph.getNode(startNodeId) == null) {
            System.out.println("Invalid start node: " + startNodeId);
            return;
        }

        if (graph.getNode(destinationNodeId) == null) {
            System.out.println(
                    "Invalid destination node: " + destinationNodeId);
            return;
        }

        if (repetitions <= 0) {
            System.out.println("Invalid number of repetitions.");
            return;
        }

        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm();

        // Warm-up to reduce JVM startup/JIT effects.
        for (int i = 0; i < 10; i++) {
            dijkstra.findShortestPath(
                    graph,
                    startNodeId,
                    destinationNodeId);
        }

        ArrayList<Long> times = new ArrayList<Long>();

        RouteResult lastResult = null;

        for (int i = 0; i < repetitions; i++) {

            long startTime = System.nanoTime();

            lastResult = dijkstra.findShortestPath(
                    graph,
                    startNodeId,
                    destinationNodeId);

            long endTime = System.nanoTime();

            times.add(endTime - startTime);
        }

        Collections.sort(times);

        long totalTime = 0;

        for (Long time : times) {
            totalTime += time;
        }

        double averageTimeMicros =
                (totalTime / (double) repetitions) / 1000.0;

        int p95Index = (int) Math.ceil(
                repetitions * 0.95) - 1;

        if (p95Index < 0) {
            p95Index = 0;
        }

        if (p95Index >= times.size()) {
            p95Index = times.size() - 1;
        }

        double p95TimeMicros =
                times.get(p95Index) / 1000.0;

        System.out.println();
        System.out.println("Algorithm           : DIJKSTRA");
        System.out.println("Source Node         : " + startNodeId);
        System.out.println("Destination Node    : " + destinationNodeId);
        System.out.println("Repetitions         : " + repetitions);

        if (lastResult != null) {
            System.out.println(
                    "Route Found         : "
                            + lastResult.isRouteFound());

            System.out.println(
                    "Graph Version       : "
                            + graph.getGraphVersion());
        }

        System.out.printf(
                "Average Time        : %.3f microseconds%n",
                averageTimeMicros);

        System.out.printf(
                "P95 Time            : %.3f microseconds%n",
                p95TimeMicros);

        System.out.println();
        System.out.println("Time Complexity     : O((V + E) log V)");
        System.out.println("Space Complexity    : O(V)");

        System.out.println("==============================================================");
    }
}
