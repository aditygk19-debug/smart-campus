import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DFSAlgorithm {

    // =========================================================
    // FIND ROUTE USING DEPTH-FIRST SEARCH
    // =========================================================

    public RouteResult findRoute(
            Graph graph,
            String sourceId,
            String destinationId) {

        ArrayList<String> orderedNodes = new ArrayList<String>();

        ArrayList<String> orderedEdges = new ArrayList<String>();

        Set<String> visited = new HashSet<String>();

        boolean found = dfs(
                graph,
                sourceId,
                destinationId,
                visited,
                orderedNodes,
                orderedEdges);

        // =====================================================
        // NO ROUTE FOUND
        // =====================================================

        if (!found) {

            return new RouteResult(
                    orderedNodes,
                    orderedEdges,
                    0,
                    0,
                    visited.size(),
                    "DFS",
                    false);
        }

        // =====================================================
        // CALCULATE DISTANCE AND LATENCY
        // =====================================================

        int totalDistance = 0;

        int totalLatency = 0;

        for (int i = 0; i < orderedEdges.size(); i++) {

            String edgeId = orderedEdges.get(i);

            String currentNode = orderedNodes.get(i);

            ArrayList<Edge> neighbors = graph.getNeighbors(currentNode);

            for (Edge edge : neighbors) {

                if (edge.getEdgeId()
                        .equals(edgeId)) {

                    totalDistance += edge.getDistance();

                    totalLatency += edge.getLatencyMs();

                    break;
                }
            }
        }

        // =====================================================
        // RETURN RESULT
        // =====================================================

        return new RouteResult(
                orderedNodes,
                orderedEdges,
                totalDistance,
                totalLatency,
                visited.size(),
                "DFS",
                true);
    }

    // =========================================================
    // DEPTH-FIRST SEARCH
    // =========================================================

    private boolean dfs(
            Graph graph,
            String currentId,
            String destinationId,
            Set<String> visited,
            ArrayList<String> orderedNodes,
            ArrayList<String> orderedEdges) {

        // -----------------------------------------------------
        // MARK CURRENT NODE AS VISITED
        // -----------------------------------------------------

        visited.add(currentId);

        // -----------------------------------------------------
        // ADD CURRENT NODE TO CURRENT PATH
        // -----------------------------------------------------

        orderedNodes.add(currentId);

        // -----------------------------------------------------
        // DESTINATION REACHED
        // -----------------------------------------------------

        if (currentId.equals(destinationId)) {

            return true;
        }

        // -----------------------------------------------------
        // GET NEIGHBOURS
        // -----------------------------------------------------

        ArrayList<Edge> neighbors = graph.getNeighbors(currentId);

        // -----------------------------------------------------
        // PROCESS NEIGHBOURS IN INSERTION ORDER
        // -----------------------------------------------------

        for (Edge edge : neighbors) {

            // -------------------------------------------------
            // IGNORE UNAVAILABLE EDGES
            // -------------------------------------------------

            if (!edge.isAvailable()) {

                continue;
            }

            String nextNode = edge.getDestination()
                    .getNodeId();

            // -------------------------------------------------
            // IGNORE ALREADY VISITED NODES
            // -------------------------------------------------

            if (visited.contains(nextNode)) {

                continue;
            }

            // -------------------------------------------------
            // ADD EDGE TO CURRENT PATH
            // -------------------------------------------------

            orderedEdges.add(
                    edge.getEdgeId());

            // -------------------------------------------------
            // RECURSIVELY SEARCH
            // -------------------------------------------------

            boolean found = dfs(
                    graph,
                    nextNode,
                    destinationId,
                    visited,
                    orderedNodes,
                    orderedEdges);

            if (found) {

                return true;
            }

            // -------------------------------------------------
            // BACKTRACK
            // -------------------------------------------------

            orderedEdges.remove(
                    orderedEdges.size() - 1);

            orderedNodes.remove(
                    orderedNodes.size() - 1);
        }

        return false;
    }
}