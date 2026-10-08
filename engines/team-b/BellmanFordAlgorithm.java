import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BellmanFordAlgorithm {

    // =========================================================
    // FIND SHORTEST PATH USING BELLMAN-FORD
    // =========================================================

    public RouteResult findShortestPath(
            Graph graph,
            String sourceId,
            String destinationId) {

        // -----------------------------------------------------
        // Validate source and destination
        // -----------------------------------------------------

        Node source = graph.getNode(sourceId);
        Node destination = graph.getNode(destinationId);

        if (source == null || destination == null) {

            return new RouteResult(
                    new ArrayList<String>(),
                    new ArrayList<String>(),
                    0,
                    0,
                    0,
                    "BELLMAN_FORD",
                    false);
        }

        // -----------------------------------------------------
        // Get all nodes and edges
        // -----------------------------------------------------

        ArrayList<Node> nodes = graph.getAllNodes();
        ArrayList<Edge> edges = graph.getAllEdges();

        // -----------------------------------------------------
        // Distance from source
        // -----------------------------------------------------

        Map<String, Integer> distance = new HashMap<String, Integer>();

        // -----------------------------------------------------
        // Previous node
        // -----------------------------------------------------

        Map<String, String> previousNode = new HashMap<String, String>();

        // -----------------------------------------------------
        // Previous edge
        // -----------------------------------------------------

        Map<String, String> previousEdge = new HashMap<String, String>();

        // -----------------------------------------------------
        // Initialize
        // -----------------------------------------------------

        for (Node node : nodes) {

            distance.put(
                    node.getNodeId(),
                    Integer.MAX_VALUE);

            previousNode.put(
                    node.getNodeId(),
                    null);

            previousEdge.put(
                    node.getNodeId(),
                    null);
        }

        distance.put(sourceId, 0);

        int exploredNodeCount = 0;

        // =====================================================
        // BELLMAN-FORD
        // =====================================================

        for (int i = 0; i < nodes.size() - 1; i++) {

            boolean changed = false;

            // -------------------------------------------------
            // Relax every edge
            // -------------------------------------------------

            for (Edge edge : edges) {

                // Ignore unavailable edges
                if (!edge.isAvailable()) {
                    continue;
                }

                String from = edge.getSource().getNodeId();

                String to = edge.getDestination().getNodeId();

                int weight = edge.getDistance();

                // -------------------------------------------------
                // Negative weight check
                // -------------------------------------------------

                if (weight < 0) {

                    System.out.println();
                    System.out.println(
                            "ERROR: Negative edge weight detected.");
                    System.out.println(
                            "Edge ID : " + edge.getEdgeId());
                    System.out.println(
                            "Weight  : " + weight);

                    return new RouteResult(
                            new ArrayList<String>(),
                            new ArrayList<String>(),
                            0,
                            0,
                            exploredNodeCount,
                            "BELLMAN_FORD",
                            false);
                }

                // -------------------------------------------------
                // If source node has not been reached
                // -------------------------------------------------

                if (distance.get(from) == Integer.MAX_VALUE) {

                    continue;
                }

                exploredNodeCount++;

                // -------------------------------------------------
                // Relaxation
                // -------------------------------------------------

                int newDistance = distance.get(from) + weight;

                if (newDistance < distance.get(to)) {

                    distance.put(
                            to,
                            newDistance);

                    previousNode.put(
                            to,
                            from);

                    previousEdge.put(
                            to,
                            edge.getEdgeId());

                    changed = true;
                }
            }

            // -------------------------------------------------
            // Stop early if no changes
            // -------------------------------------------------

            if (!changed) {
                break;
            }
        }

        // =====================================================
        // CHECK DESTINATION
        // =====================================================

        if (distance.get(destinationId) == Integer.MAX_VALUE) {

            return new RouteResult(
                    new ArrayList<String>(),
                    new ArrayList<String>(),
                    0,
                    0,
                    exploredNodeCount,
                    "BELLMAN_FORD",
                    false);
        }

        // =====================================================
        // BUILD PATH
        // =====================================================

        ArrayList<String> orderedNodes = new ArrayList<String>();

        ArrayList<String> orderedEdges = new ArrayList<String>();

        String current = destinationId;

        while (current != null) {

            orderedNodes.add(
                    0,
                    current);

            String edgeId = previousEdge.get(current);

            if (edgeId != null) {

                orderedEdges.add(
                        0,
                        edgeId);
            }

            current = previousNode.get(current);
        }

        // =====================================================
        // CALCULATE TOTAL LATENCY
        // =====================================================

        int totalLatency = 0;

        for (String edgeId : orderedEdges) {

            for (Edge edge : edges) {

                if (edge.getEdgeId()
                        .equals(edgeId)) {

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
                distance.get(destinationId),
                totalLatency,
                exploredNodeCount,
                "BELLMAN_FORD",
                true);
    }
}
