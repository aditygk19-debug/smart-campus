import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class DijkstraAlgorithm {

    // =====================================================
    // PRIORITY QUEUE NODE
    // =====================================================

    private static class QueueNode
            implements Comparable<QueueNode> {

        String nodeId;
        int distance;

        QueueNode(String nodeId, int distance) {

            this.nodeId = nodeId;
            this.distance = distance;
        }

        @Override
        public int compareTo(QueueNode other) {

            return Integer.compare(
                    this.distance,
                    other.distance);
        }
    }

    // =====================================================
    // FIND SHORTEST PATH
    // =====================================================

    public RouteResult findShortestPath(
            Graph graph,
            String sourceId,
            String destinationId) {

        // =================================================
        // CHECK SOURCE
        // =================================================

        if (graph.getNode(sourceId) == null) {

            System.out.println(
                    "Invalid source node: "
                            + sourceId);

            return new RouteResult(
                    new ArrayList<String>(),
                    new ArrayList<String>(),
                    0,
                    0,
                    0,
                    "DIJKSTRA",
                    false);
        }

        // =================================================
        // CHECK DESTINATION
        // =================================================

        if (graph.getNode(destinationId) == null) {

            System.out.println(
                    "Invalid destination node: "
                            + destinationId);

            return new RouteResult(
                    new ArrayList<String>(),
                    new ArrayList<String>(),
                    0,
                    0,
                    0,
                    "DIJKSTRA",
                    false);
        }

        // =================================================
        // SOURCE = DESTINATION
        // =================================================

        if (sourceId.equals(destinationId)) {

            ArrayList<String> path = new ArrayList<String>();

            path.add(sourceId);

            return new RouteResult(
                    path,
                    new ArrayList<String>(),
                    0,
                    0,
                    1,
                    "DIJKSTRA",
                    true);
        }

        // =================================================
        // DATA STRUCTURES
        // =================================================

        HashMap<String, Integer> distance = new HashMap<String, Integer>();

        HashMap<String, Integer> latency = new HashMap<String, Integer>();

        HashMap<String, String> previousNode = new HashMap<String, String>();

        HashMap<String, Edge> previousEdge = new HashMap<String, Edge>();

        Set<String> visited = new HashSet<String>();

        PriorityQueue<QueueNode> queue = new PriorityQueue<QueueNode>();

        // =================================================
        // INITIALIZE SOURCE
        // =================================================

        distance.put(sourceId, 0);

        latency.put(sourceId, 0);

        queue.add(
                new QueueNode(
                        sourceId,
                        0));

        int nodesExplored = 0;

        // =================================================
        // DIJKSTRA
        // =================================================

        while (!queue.isEmpty()) {

            QueueNode current = queue.poll();

            String currentId = current.nodeId;

            // Skip already visited nodes

            if (visited.contains(currentId)) {

                continue;
            }

            visited.add(currentId);

            nodesExplored++;

            // =================================================
            // DESTINATION REACHED
            // =================================================

            if (currentId.equals(destinationId)) {

                break;
            }

            // =================================================
            // GET NEIGHBOURS
            // =================================================

            ArrayList<Edge> neighbors = graph.getNeighbors(currentId);

            if (neighbors == null) {

                continue;
            }

            // =================================================
            // PROCESS EACH EDGE
            // =================================================

            for (Edge edge : neighbors) {

                // Ignore unavailable connections

                if (!edge.isAvailable()) {

                    continue;
                }

                String nextNodeId;

                // =================================================
                // NORMAL DIRECTION
                // =================================================

                if (edge.getSource()
                        .getNodeId()
                        .equals(currentId)) {

                    nextNodeId = edge.getDestination()
                            .getNodeId();
                }

                // =================================================
                // REVERSE DIRECTION
                // =================================================

                else if (edge.getDestination()
                        .getNodeId()
                        .equals(currentId)) {

                    nextNodeId = edge.getSource()
                            .getNodeId();
                }

                else {

                    continue;
                }

                // =================================================
                // CALCULATE NEW DISTANCE
                // =================================================

                int newDistance = distance.get(currentId)
                        + edge.getDistance();

                int newLatency = latency.get(currentId)
                        + edge.getLatencyMs();

                // =================================================
                // CHECK BETTER PATH
                // =================================================

                if (!distance.containsKey(nextNodeId)
                        || newDistance < distance.get(nextNodeId)) {

                    distance.put(
                            nextNodeId,
                            newDistance);

                    latency.put(
                            nextNodeId,
                            newLatency);

                    previousNode.put(
                            nextNodeId,
                            currentId);

                    previousEdge.put(
                            nextNodeId,
                            edge);

                    queue.add(
                            new QueueNode(
                                    nextNodeId,
                                    newDistance));
                }
            }
        }

        // =================================================
        // NO ROUTE FOUND
        // =================================================

        if (!distance.containsKey(destinationId)) {

            System.out.println(
                    "No route found from "
                            + sourceId
                            + " to "
                            + destinationId);

            return new RouteResult(
                    new ArrayList<String>(),
                    new ArrayList<String>(),
                    0,
                    0,
                    nodesExplored,
                    "DIJKSTRA",
                    false);
        }

        // =================================================
        // BUILD PATH
        // =================================================

        ArrayList<String> orderedNodes = new ArrayList<String>();

        ArrayList<String> orderedEdges = new ArrayList<String>();

        String current = destinationId;

        while (current != null) {

            orderedNodes.add(current);

            if (previousEdge.containsKey(current)) {

                orderedEdges.add(
                        previousEdge
                                .get(current)
                                .getEdgeId());
            }

            current = previousNode.get(current);
        }

        // =================================================
        // REVERSE PATH
        // =================================================

        Collections.reverse(
                orderedNodes);

        Collections.reverse(
                orderedEdges);

        // =================================================
        // RETURN RESULT
        // =================================================

        return new RouteResult(
                orderedNodes,
                orderedEdges,
                distance.get(destinationId),
                latency.get(destinationId),
                nodesExplored,
                "DIJKSTRA",
                true);
    }
}