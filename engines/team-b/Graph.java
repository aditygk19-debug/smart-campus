import java.util.ArrayList;
import java.util.HashMap;

public class Graph {

        // =========================================================
        // DATA STRUCTURES
        // =========================================================

        private HashMap<String, Node> nodes;
        private HashMap<String, ArrayList<Edge>> graph;
        private long topologyVersion;

        // =========================================================
        // CONSTRUCTOR
        // =========================================================

        public Graph() {
                nodes = new HashMap<>();
                graph = new HashMap<>();
                topologyVersion = 0;
        }

        // =========================================================
        // ADD NODE
        // =========================================================

        public void addNode(Node node) {

                if (node == null) {
                        return;
                }

                nodes.put(
                                node.getNodeId(),
                                node);

                graph.put(
                                node.getNodeId(),
                                new ArrayList<Edge>());

                topologyVersion++;
        }

        // =========================================================
        // ADD EDGE
        // =========================================================

        public void addEdge(
                        String edgeId,
                        String from,
                        String to,
                        int distance,
                        int latencyMs,
                        boolean availability) {

                Node source = nodes.get(from);
                Node destination = nodes.get(to);

                // -----------------------------------------------------
                // CHECK NODES
                // -----------------------------------------------------

                if (source == null || destination == null) {

                        System.out.println(
                                        "Invalid edge: " + edgeId);

                        return;
                }

                // -----------------------------------------------------
                // CHECK DISTANCE
                // -----------------------------------------------------

                if (distance < 0) {

                        System.out.println(
                                        "Invalid distance for edge: "
                                                        + edgeId);

                        return;
                }

                // -----------------------------------------------------
                // CHECK LATENCY
                // -----------------------------------------------------

                if (latencyMs < 0) {

                        System.out.println(
                                        "Invalid latency for edge: "
                                                        + edgeId);

                        return;
                }

                // -----------------------------------------------------
                // AUTOMATIC CAPACITY
                // -----------------------------------------------------

                String capacity = "";

                if (destination.getNodeType()
                                .equalsIgnoreCase("Lab")) {

                        capacity = "20";

                } else if (destination.getNodeType()
                                .equalsIgnoreCase("Classroom")) {

                        capacity = "60";

                } else if (destination.getNodeType()
                                .equalsIgnoreCase("Conference Hall")) {

                        capacity = "150";
                }

                // -----------------------------------------------------
                // CREATE EDGE
                // -----------------------------------------------------

                Edge edge = new Edge(
                                edgeId,
                                source,
                                destination,
                                distance,
                                latencyMs,
                                capacity,
                                availability);

                // -----------------------------------------------------
                // ADD TO ADJACENCY LIST
                // -----------------------------------------------------

                if (!graph.containsKey(from)) {

                        graph.put(
                                        from,
                                        new ArrayList<Edge>());
                }

                graph.get(from).add(edge);

                topologyVersion++;
        }

        // =========================================================
        // GET GRAPH VERSION
        // =========================================================

        public String getGraphVersion() {
                return "G-" + topologyVersion;
        }

        public long getTopologyVersionNumber() {
                return topologyVersion;
        }

        // =========================================================
        // ADD BIDIRECTIONAL EDGE
        // =========================================================

        public void addBidirectionalEdge(
                        String edgeId1,
                        String edgeId2,
                        String from,
                        String to,
                        int distance,
                        int latencyMs,
                        boolean availability) {

                // Forward direction
                addEdge(
                                edgeId1,
                                from,
                                to,
                                distance,
                                latencyMs,
                                availability);

                // Reverse direction
                addEdge(
                                edgeId2,
                                to,
                                from,
                                distance,
                                latencyMs,
                                availability);
        }

        // =========================================================
        // GET NODE
        // =========================================================

        public Node getNode(String nodeId) {

                return nodes.get(nodeId);
        }

        // =========================================================
        // GET NEIGHBOURS
        // =========================================================
        //
        // Deterministic neighbour-order rule:
        //
        // Neighbours are processed in edge insertion order.
        // The adjacency list uses ArrayList, which preserves
        // insertion order.
        //
        // Therefore BFS and DFS use the same deterministic
        // neighbour order for a fixed campus topology.
        //
        // =========================================================

        public ArrayList<Edge> getNeighbors(String nodeId) {

                ArrayList<Edge> neighbors = new ArrayList<Edge>();

                ArrayList<Edge> outgoing = graph.get(nodeId);

                if (outgoing != null) {

                        neighbors.addAll(outgoing);
                }

                return neighbors;
        }

        // =========================================================
        // GET ALL NODES
        // =========================================================
        //
        // Required by Bellman-Ford.
        //
        // =========================================================

        public ArrayList<Node> getAllNodes() {

                return new ArrayList<Node>(
                                nodes.values());
        }

        // =========================================================
        // GET ALL EDGES
        // =========================================================
        //
        // Required by Bellman-Ford.
        //
        // =========================================================

        public ArrayList<Edge> getAllEdges() {

                ArrayList<Edge> allEdges = new ArrayList<Edge>();

                for (ArrayList<Edge> edgeList : graph.values()) {

                        if (edgeList != null) {

                                allEdges.addAll(edgeList);
                        }
                }

                return allEdges;
        }

        // =========================================================
        // DISPLAY NODES
        // =========================================================

        public void displayNodes() {

                System.out.println();

                System.out.println(
                                "==============================================================");

                System.out.println(
                                "                     CAMPUS NODE DETAILS");

                System.out.println(
                                "==============================================================");

                System.out.printf(
                                "%-8s %-20s %-30s %-10s%n",
                                "ID",
                                "TYPE",
                                "LABEL",
                                "STATUS");

                System.out.println(
                                "--------------------------------------------------------------");

                /*
                 * HashMap does not guarantee display order.
                 *
                 * To maintain the same order as your original output,
                 * use the campus node ID list below.
                 */

                String[] nodeOrder = {

                                "C1",
                                "C2",
                                "C3",

                                "AC1",
                                "AC2",
                                "AC3",

                                "EC1",
                                "EC2",
                                "EC3",

                                "MC1",
                                "MC2",

                                "N1",
                                "N2",
                                "N3",
                                "N4",
                                "N5",
                                "N6",
                                "N7",
                                "N8",

                                "L1",
                                "L2",
                                "L3",
                                "L4",

                                "AL1",
                                "AL2",
                                "AL3",

                                "EL1",
                                "EL2",

                                "ML1",
                                "ML2",

                                "CH1"
                };

                for (String nodeId : nodeOrder) {

                        Node node = nodes.get(nodeId);

                        if (node != null) {

                                System.out.printf(
                                                "%-8s %-20s %-30s %-10s%n",
                                                node.getNodeId(),
                                                node.getNodeType(),
                                                node.getLabel(),
                                                node.getStatus());
                        }
                }

                System.out.println(
                                "==============================================================");
        }

        // =========================================================
        // DISPLAY GRAPH
        // =========================================================

        public void displayGraph() {

                System.out.println();

                System.out.println(
                                "==========================================================================");

                System.out.println(
                                "                         CAMPUS CONNECTION DETAILS");

                System.out.println(
                                "==========================================================================");

                System.out.printf(
                                "%-6s %-8s %-8s %-12s %-12s %-12s %-12s%n",
                                "EDGE",
                                "FROM",
                                "TO",
                                "DISTANCE",
                                "LATENCY",
                                "CAPACITY",
                                "STATUS");

                System.out.println(
                                "--------------------------------------------------------------------------");

                /*
                 * Display edges in the order they were added to each
                 * node's adjacency list.
                 */

                String[] nodeOrder = {

                                "C1",
                                "C2",
                                "C3",

                                "AC1",
                                "AC2",
                                "AC3",

                                "EC1",
                                "EC2",
                                "EC3",

                                "MC1",
                                "MC2",

                                "N1",
                                "N2",
                                "N3",
                                "N4",
                                "N5",
                                "N6",
                                "N7",
                                "N8",

                                "L1",
                                "L2",
                                "L3",
                                "L4",

                                "AL1",
                                "AL2",
                                "AL3",

                                "EL1",
                                "EL2",

                                "ML1",
                                "ML2",

                                "CH1"
                };

                for (String nodeId : nodeOrder) {

                        ArrayList<Edge> edges = graph.get(nodeId);

                        if (edges != null &&
                                        !edges.isEmpty()) {

                                for (Edge edge : edges) {

                                        System.out.printf(
                                                        "%-6s %-8s %-8s %-12d %-12s %-12s %-12s%n",

                                                        edge.getEdgeId(),

                                                        edge.getSource()
                                                                        .getNodeId(),

                                                        edge.getDestination()
                                                                        .getNodeId(),

                                                        edge.getDistance(),

                                                        edge.getLatencyMs()
                                                                        + " ms",

                                                        edge.getCapacity(),

                                                        edge.isAvailable()
                                                                        ? "Available"
                                                                        : "Unavailable");
                                }
                        }
                }

                System.out.println(
                                "==========================================================================");
        }
}