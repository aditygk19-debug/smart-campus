import java.util.ArrayList;

public class RouteResult {

    private ArrayList<String> orderedNodes;
    private ArrayList<String> orderedEdges;

    private int totalCost;
    private int estimatedLatency;
    private int exploredNodeCount;

    private String algorithm;
    private boolean routeFound;
    private String graphVersion;

    public RouteResult(
            ArrayList<String> orderedNodes,
            ArrayList<String> orderedEdges,
            int totalCost,
            int estimatedLatency,
            int exploredNodeCount,
            String algorithm,
            boolean routeFound) {

        this.orderedNodes = orderedNodes;
        this.orderedEdges = orderedEdges;
        this.totalCost = totalCost;
        this.estimatedLatency = estimatedLatency;
        this.exploredNodeCount = exploredNodeCount;
        this.algorithm = algorithm;
        this.routeFound = routeFound;
        this.graphVersion = "UNKNOWN";
    }

    public ArrayList<String> getOrderedNodes() {
        return orderedNodes;
    }

    public ArrayList<String> getOrderedEdges() {
        return orderedEdges;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public int getEstimatedLatency() {
        return estimatedLatency;
    }

    public int getExploredNodeCount() {
        return exploredNodeCount;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public boolean isRouteFound() {
        return routeFound;
    }

    public String getGraphVersion() {
        return graphVersion;
    }

    public void setGraphVersion(String graphVersion) {
        this.graphVersion = graphVersion;
    }

    @Override
    public String toString() {

        return "\n========== ROUTE RESULT ==========\n"
                + "Algorithm       : " + algorithm + "\n"
                + "Route Found     : " + routeFound + "\n"
                + "Graph Version   : " + graphVersion + "\n"
                + "Path            : " + String.join("-> ", orderedNodes) + "\n"
                + "Edges           : " + orderedEdges + "\n"
                + "Total Distance  : " + totalCost + "\n"
                + "Total Latency   : " + estimatedLatency + " ms\n"
                + "Nodes Explored  : " + exploredNodeCount + "\n"
                + "==================================";
    }
}
