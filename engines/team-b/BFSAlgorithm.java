import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class BFSAlgorithm {

    // =========================================================
    // FIND PATH USING BREADTH-FIRST SEARCH
    // =========================================================

    public RouteResult findShortestPath(
            Graph graph,
            String sourceId,
            String destinationId) {

        // -----------------------------------------------------
        // GET SOURCE AND DESTINATION
        // -----------------------------------------------------

        Node source = graph.getNode(sourceId);

        Node destination = graph.getNode(destinationId);

        // -----------------------------------------------------
        // INVALID NODES
        // -----------------------------------------------------

        if (source == null ||
                destination == null) {

            return new RouteResult(
                    new ArrayList<String>(),
                    new ArrayList<String>(),
                    0,
                    0,
                    0,
                    "BFS",
                    false);
        }

        // -----------------------------------------------------
        // SOURCE = DESTINATION
        // -----------------------------------------------------

        if (sourceId.equals(destinationId)) {

            ArrayList<String> path = new ArrayList<String>();

            path.add(sourceId);

            return new RouteResult(
                    path,
                    new ArrayList<String>(),
                    0,
                    0,
                    1,
                    "BFS",
                    true);
        }

        // =====================================================
        // BFS DATA STRUCTURES
        // =====================================================

        Queue<Node> queue = new LinkedList<Node>();

        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();

        HashMap<String, Node> parent = new HashMap<String, Node>();

        HashMap<String, Edge> parentEdge = new HashMap<String, Edge>();

        // =====================================================
        // START BFS
        // =====================================================

        queue.add(source);

        visited.put(
                sourceId,
                true);

        int exploredNodeCount = 0;

        boolean routeFound = false;

        // =====================================================
        // BFS LOOP
        // =====================================================

        while (!queue.isEmpty()) {

            Node current = queue.poll();

            exploredNodeCount++;

            // -------------------------------------------------
            // CHECK DESTINATION
            // -------------------------------------------------

            if (current.getNodeId()
                    .equals(destinationId)) {

                routeFound = true;

                break;
            }

            // -------------------------------------------------
            // GET NEIGHBOURS
            // -------------------------------------------------

            ArrayList<Edge> neighbors = graph.getNeighbors(
                    current.getNodeId());

            // -------------------------------------------------
            // VISIT NEIGHBOURS
            // -------------------------------------------------

            for (Edge edge : neighbors) {

                // -------------------------------------------------
                // IGNORE UNAVAILABLE EDGES
                // -------------------------------------------------

                if (!edge.isAvailable()) {

                    continue;
                }

                Node next = edge.getDestination();

                String nextId = next.getNodeId();

                // -------------------------------------------------
                // IF NOT VISITED
                // -------------------------------------------------

                if (!visited.containsKey(nextId)
                        || !visited.get(nextId)) {

                    visited.put(
                            nextId,
                            true);

                    parent.put(
                            nextId,
                            current);

                    parentEdge.put(
                            nextId,
                            edge);

                    queue.add(next);
                }
            }
        }

        // =====================================================
        // NO ROUTE FOUND
        // =====================================================

        if (!routeFound) {

            return new RouteResult(
                    new ArrayList<String>(),
                    new ArrayList<String>(),
                    0,
                    0,
                    exploredNodeCount,
                    "BFS",
                    false);
        }

        // =====================================================
        // RECONSTRUCT PATH
        // =====================================================

        ArrayList<String> reversedPath = new ArrayList<String>();

        ArrayList<String> reversedEdges = new ArrayList<String>();

        Node current = destination;

        while (current != null) {

            String currentId = current.getNodeId();

            reversedPath.add(
                    currentId);

            if (currentId.equals(sourceId)) {

                break;
            }

            Edge edge = parentEdge.get(currentId);

            if (edge != null) {

                reversedEdges.add(
                        edge.getEdgeId());
            }

            current = parent.get(currentId);
        }

        // =====================================================
        // REVERSE PATH
        // =====================================================

        ArrayList<String> orderedPath = new ArrayList<String>();

        for (int i = reversedPath.size() - 1; i >= 0; i--) {

            orderedPath.add(
                    reversedPath.get(i));
        }

        // =====================================================
        // REVERSE EDGES
        // =====================================================

        ArrayList<String> orderedEdges = new ArrayList<String>();

        for (int i = reversedEdges.size() - 1; i >= 0; i--) {

            orderedEdges.add(
                    reversedEdges.get(i));
        }

        // =====================================================
        // CALCULATE TOTAL DISTANCE AND LATENCY
        // =====================================================

        int totalDistance = 0;

        int totalLatency = 0;

        for (String edgeId : orderedEdges) {

            Edge selectedEdge = null;

            // Find the edge in the graph
            for (String nodeId : getPathNodeIds(orderedPath)) {

                ArrayList<Edge> edges = graph.getNeighbors(nodeId);

                for (Edge edge : edges) {

                    if (edge.getEdgeId()
                            .equals(edgeId)) {

                        selectedEdge = edge;

                        break;
                    }
                }

                if (selectedEdge != null) {

                    break;
                }
            }

            if (selectedEdge != null) {

                totalDistance += selectedEdge.getDistance();

                totalLatency += selectedEdge.getLatencyMs();
            }
        }

        // =====================================================
        // RETURN RESULT
        // =====================================================

        return new RouteResult(
                orderedPath,
                orderedEdges,
                totalDistance,
                totalLatency,
                exploredNodeCount,
                "BFS",
                true);
    }

    // =========================================================
    // HELPER METHOD
    // =========================================================

    private ArrayList<String> getPathNodeIds(
            ArrayList<String> path) {

        return path;
    }
}