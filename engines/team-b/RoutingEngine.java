public class RoutingEngine {

    private DijkstraAlgorithm dijkstra;
    private BellmanFordAlgorithm bellmanFord;
    private BFSAlgorithm bfs;
    private DFSAlgorithm dfs;

    public RoutingEngine() {
        dijkstra = new DijkstraAlgorithm();
        bellmanFord = new BellmanFordAlgorithm();
        bfs = new BFSAlgorithm();
        dfs = new DFSAlgorithm();
    }

    // Default route computation uses Dijkstra.
    public RouteResult findRoute(
            Graph graph,
            String sourceId,
            String destinationId) {

        return findRoute(
                graph,
                sourceId,
                destinationId,
                "DIJKSTRA");
    }

    // Route computation with explicit algorithm selection.
    public RouteResult findRoute(
            Graph graph,
            String sourceId,
            String destinationId,
            String algorithm) {

        if (graph == null) {
            return new RouteResult(
                    new java.util.ArrayList<String>(),
                    new java.util.ArrayList<String>(),
                    0,
                    0,
                    0,
                    "NONE",
                    false);
        }

        if (sourceId == null || destinationId == null
                || graph.getNode(sourceId) == null
                || graph.getNode(destinationId) == null) {

            return new RouteResult(
                    new java.util.ArrayList<String>(),
                    new java.util.ArrayList<String>(),
                    0,
                    0,
                    0,
                    algorithm == null ? "NONE" : algorithm.toUpperCase(),
                    false);
        }

        if (algorithm == null || algorithm.trim().isEmpty()) {
            algorithm = "DIJKSTRA";
        }

        RouteResult result;

        switch (algorithm.trim().toUpperCase()) {

            case "DIJKSTRA":
                result = dijkstra.findShortestPath(
                        graph,
                        sourceId,
                        destinationId);
                break;

            case "BELLMAN_FORD":
            case "BELLMAN-FORD":
                result = bellmanFord.findShortestPath(
                        graph,
                        sourceId,
                        destinationId);
                break;

            case "BFS":
                result = bfs.findShortestPath(
                        graph,
                        sourceId,
                        destinationId);
                break;

            case "DFS":
                result = dfs.findRoute(
                        graph,
                        sourceId,
                        destinationId);
                break;

            default:
                System.out.println("Invalid algorithm: " + algorithm);
                System.out.println(
                        "Supported algorithms: DIJKSTRA, BELLMAN_FORD, BFS, DFS");

                result = new RouteResult(
                        new java.util.ArrayList<String>(),
                        new java.util.ArrayList<String>(),
                        0,
                        0,
                        0,
                        algorithm.toUpperCase(),
                        false);
                break;
        }

        result.setGraphVersion(graph.getGraphVersion());

        return result;
    }
}
