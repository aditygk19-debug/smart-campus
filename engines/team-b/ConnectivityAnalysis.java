import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ConnectivityAnalysis {

    // =========================================================
    // ANALYZE CONNECTIVITY
    // =========================================================

    public void analyze(Graph graph, String startNodeId) {

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                CONNECTIVITY ANALYSIS");
        System.out.println("==============================================================");

        // ---------------------------------------------------------
        // CHECK START NODE
        // ---------------------------------------------------------

        if (graph.getNode(startNodeId) == null) {

            System.out.println(
                    "Invalid start node: " + startNodeId);

            return;
        }

        // ---------------------------------------------------------
        // FIND REACHABLE NODES
        // ---------------------------------------------------------

        ArrayList<String> traversalOrder = new ArrayList<String>();

        Set<String> visited = new HashSet<String>();

        Queue<String> queue = new LinkedList<String>();

        queue.add(startNodeId);
        visited.add(startNodeId);

        while (!queue.isEmpty()) {

            String currentId = queue.poll();

            traversalOrder.add(currentId);

            ArrayList<Edge> neighbors = graph.getNeighbors(currentId);

            for (Edge edge : neighbors) {

                // Ignore unavailable connections
                if (!edge.isAvailable()) {
                    continue;
                }

                String nextNodeId = edge.getDestination().getNodeId();

                if (!visited.contains(nextNodeId)) {

                    visited.add(nextNodeId);

                    queue.add(nextNodeId);
                }
            }
        }

        // ---------------------------------------------------------
        // DISPLAY REACHABLE NODES
        // ---------------------------------------------------------

        System.out.println();
        System.out.println("Start Node       : " + startNodeId);

        System.out.println(
                "Reachable Nodes  : " + visited.size());

        System.out.println(
                "Traversal Order  : "
                        + String.join(" -> ", traversalOrder));

        // ---------------------------------------------------------
        // FIND DISCONNECTED COMPONENTS
        // ---------------------------------------------------------

        Set<String> allVisited = new HashSet<String>();

        int componentCount = 0;

        ArrayList<ArrayList<String>> components = new ArrayList<ArrayList<String>>();

        for (Node node : graph.getAllNodes()) {

            String nodeId = node.getNodeId();

            if (allVisited.contains(nodeId)) {
                continue;
            }

            componentCount++;

            ArrayList<String> component = new ArrayList<String>();

            Queue<String> componentQueue = new LinkedList<String>();

            componentQueue.add(nodeId);
            allVisited.add(nodeId);

            while (!componentQueue.isEmpty()) {

                String currentId = componentQueue.poll();

                component.add(currentId);

                ArrayList<Edge> neighbors = graph.getNeighbors(currentId);

                for (Edge edge : neighbors) {

                    // Ignore unavailable connections
                    if (!edge.isAvailable()) {
                        continue;
                    }

                    String nextNodeId = edge.getDestination().getNodeId();

                    if (!allVisited.contains(nextNodeId)) {

                        allVisited.add(nextNodeId);

                        componentQueue.add(nextNodeId);
                    }
                }
            }

            components.add(component);
        }

        // ---------------------------------------------------------
        // DISPLAY COMPONENTS
        // ---------------------------------------------------------

        System.out.println();
        System.out.println(
                "Disconnected Components : "
                        + componentCount);

        for (int i = 0; i < components.size(); i++) {

            System.out.println(
                    "Component "
                            + (i + 1)
                            + " : "
                            + String.join(
                                    " -> ",
                                    components.get(i)));
        }

        System.out.println(
                "==============================================================");
    }
}