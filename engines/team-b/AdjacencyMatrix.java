import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Matrix representation of the existing campus Graph.
 * This is a comparison adapter; it does not modify Graph or its topology.
 */
public class AdjacencyMatrix {

    public static final int NO_EDGE = Integer.MAX_VALUE;

    private final ArrayList<String> nodeIds;
    private final Map<String, Integer> indexByNodeId;
    private final int[][] distanceMatrix;
    private final int[][] latencyMatrix;
    private final boolean[][] availableMatrix;
    private final String[][] edgeIdMatrix;
    private final String graphVersion;

    public AdjacencyMatrix(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null.");
        }

        nodeIds = new ArrayList<>();
        for (Node node : graph.getAllNodes()) {
            nodeIds.add(node.getNodeId());
        }
        Collections.sort(nodeIds);

        indexByNodeId = new HashMap<>();
        for (int i = 0; i < nodeIds.size(); i++) {
            indexByNodeId.put(nodeIds.get(i), i);
        }

        int size = nodeIds.size();
        distanceMatrix = new int[size][size];
        latencyMatrix = new int[size][size];
        availableMatrix = new boolean[size][size];
        edgeIdMatrix = new String[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                distanceMatrix[i][j] = (i == j) ? 0 : NO_EDGE;
                latencyMatrix[i][j] = (i == j) ? 0 : NO_EDGE;
            }
        }

        for (Edge edge : graph.getAllEdges()) {
            Integer from = indexByNodeId.get(edge.getSource().getNodeId());
            Integer to = indexByNodeId.get(edge.getDestination().getNodeId());
            if (from == null || to == null) {
                continue;
            }

            distanceMatrix[from][to] = edge.getDistance();
            latencyMatrix[from][to] = edge.getLatencyMs();
            availableMatrix[from][to] = edge.isAvailable();
            edgeIdMatrix[from][to] = edge.getEdgeId();
        }

        graphVersion = graph.getGraphVersion();
    }

    public int size() {
        return nodeIds.size();
    }

    public List<String> getNodeIds() {
        return Collections.unmodifiableList(nodeIds);
    }

    public String getGraphVersion() {
        return graphVersion;
    }

    public boolean containsNode(String nodeId) {
        return indexByNodeId.containsKey(nodeId);
    }

    /** Returns NO_EDGE for a missing or unavailable connection. */
    public int getDistance(String fromNodeId, String toNodeId) {
        int[] indexes = getIndexes(fromNodeId, toNodeId);
        if (indexes == null || !availableMatrix[indexes[0]][indexes[1]]) {
            return NO_EDGE;
        }
        return distanceMatrix[indexes[0]][indexes[1]];
    }

    /** Returns NO_EDGE for a missing or unavailable connection. */
    public int getLatency(String fromNodeId, String toNodeId) {
        int[] indexes = getIndexes(fromNodeId, toNodeId);
        if (indexes == null || !availableMatrix[indexes[0]][indexes[1]]) {
            return NO_EDGE;
        }
        return latencyMatrix[indexes[0]][indexes[1]];
    }

    public boolean isAvailable(String fromNodeId, String toNodeId) {
        int[] indexes = getIndexes(fromNodeId, toNodeId);
        return indexes != null && availableMatrix[indexes[0]][indexes[1]];
    }

    public String getEdgeId(String fromNodeId, String toNodeId) {
        int[] indexes = getIndexes(fromNodeId, toNodeId);
        return indexes == null ? null : edgeIdMatrix[indexes[0]][indexes[1]];
    }

    private int[] getIndexes(String fromNodeId, String toNodeId) {
        Integer from = indexByNodeId.get(fromNodeId);
        Integer to = indexByNodeId.get(toNodeId);
        if (from == null || to == null) {
            return null;
        }
        return new int[] { from, to };
    }

    public void displayDistanceMatrix() {
        System.out.println("\nADJACENCY MATRIX (DISTANCE; - = NO AVAILABLE EDGE)");
        System.out.printf("%-8s", "NODE");
        for (String nodeId : nodeIds) {
            System.out.printf("%8s", nodeId);
        }
        System.out.println();

        for (int i = 0; i < nodeIds.size(); i++) {
            System.out.printf("%-8s", nodeIds.get(i));
            for (int j = 0; j < nodeIds.size(); j++) {
                int value = (i == j) ? 0
                        : (availableMatrix[i][j] ? distanceMatrix[i][j] : NO_EDGE);
                if (value == NO_EDGE) {
                    System.out.printf("%8s", "-");
                } else {
                    System.out.printf("%8d", value);
                }
            }
            System.out.println();
        }
    }
}
