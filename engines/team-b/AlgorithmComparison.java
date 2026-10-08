public class AlgorithmComparison {

    public static void compare(
            Graph graph,
            String sourceId,
            String destinationId) {

        // =========================================
        // CREATE ALGORITHM OBJECTS
        // =========================================

        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm();

        BellmanFordAlgorithm bellmanFord = new BellmanFordAlgorithm();

        BFSAlgorithm bfs = new BFSAlgorithm();

        DFSAlgorithm dfs = new DFSAlgorithm();

        // =========================================
        // RUN DIJKSTRA
        // =========================================

        long startDijkstra = System.nanoTime();

        RouteResult dijkstraResult = dijkstra.findShortestPath(
                graph,
                sourceId,
                destinationId);

        long endDijkstra = System.nanoTime();

        double dijkstraTime = (endDijkstra - startDijkstra)
                / 1_000_000.0;

        // =========================================
        // RUN BELLMAN-FORD
        // =========================================

        long startBellmanFord = System.nanoTime();

        RouteResult bellmanFordResult = bellmanFord.findShortestPath(
                graph,
                sourceId,
                destinationId);

        long endBellmanFord = System.nanoTime();

        double bellmanFordTime = (endBellmanFord - startBellmanFord)
                / 1_000_000.0;

        // =========================================
        // RUN BFS
        // =========================================

        long startBFS = System.nanoTime();

        RouteResult bfsResult = bfs.findShortestPath(
                graph,
                sourceId,
                destinationId);

        long endBFS = System.nanoTime();

        double bfsTime = (endBFS - startBFS)
                / 1_000_000.0;

        // =========================================
        // RUN DFS
        // =========================================

        long startDFS = System.nanoTime();

        RouteResult dfsResult = dfs.findRoute(
                graph,
                sourceId,
                destinationId);

        long endDFS = System.nanoTime();

        double dfsTime = (endDFS - startDFS)
                / 1_000_000.0;

        // =========================================
        // DISPLAY COMPARISON
        // =========================================

        System.out.println();
        System.out.println(
                "==============================================================");

        System.out.println(
                "                 ALGORITHM COMPARISON");

        System.out.println(
                "==============================================================");

        System.out.println(
                "Source      : " + sourceId);

        System.out.println(
                "Destination : " + destinationId);

        System.out.println(
                "--------------------------------------------------------------");

        System.out.printf(
                "%-18s %-12s %-12s %-15s %-15s%n",
                "Algorithm",
                "Distance",
                "Latency",
                "Nodes Explored",
                "Time (ms)");

        System.out.println(
                "--------------------------------------------------------------");

        System.out.printf(
                "%-18s %-12d %-12d %-15d %-15.4f%n",
                "Dijkstra",
                dijkstraResult.getTotalCost(),
                dijkstraResult.getEstimatedLatency(),
                dijkstraResult.getExploredNodeCount(),
                dijkstraTime);

        System.out.printf(
                "%-18s %-12d %-12d %-15d %-15.4f%n",
                "Bellman-Ford",
                bellmanFordResult.getTotalCost(),
                bellmanFordResult.getEstimatedLatency(),
                bellmanFordResult.getExploredNodeCount(),
                bellmanFordTime);

        System.out.printf(
                "%-18s %-12d %-12d %-15d %-15.4f%n",
                "BFS",
                bfsResult.getTotalCost(),
                bfsResult.getEstimatedLatency(),
                bfsResult.getExploredNodeCount(),
                bfsTime);

        System.out.printf(
                "%-18s %-12d %-12d %-15d %-15.4f%n",
                "DFS",
                dfsResult.getTotalCost(),
                dfsResult.getEstimatedLatency(),
                dfsResult.getExploredNodeCount(),
                dfsTime);

        System.out.println(
                "--------------------------------------------------------------");

        System.out.println();
        System.out.println("ROUTES:");

        System.out.println(
                "Dijkstra     : "
                        + String.join(
                                " -> ",
                                dijkstraResult.getOrderedNodes()));

        System.out.println(
                "Bellman-Ford : "
                        + String.join(
                                " -> ",
                                bellmanFordResult.getOrderedNodes()));

        System.out.println(
                "BFS          : "
                        + String.join(
                                " -> ",
                                bfsResult.getOrderedNodes()));

        System.out.println(
                "DFS          : "
                        + String.join(
                                " -> ",
                                dfsResult.getOrderedNodes()));

        System.out.println(
                "==============================================================");
    }
}