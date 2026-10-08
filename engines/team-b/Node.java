public class Node {

    private String nodeId;
    private String nodeType;
    private String label;
    private String status;

    public Node(String nodeId, String nodeType, String label, String status) {
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.label = label;
        this.status = status;
    }

    // ===================== GETTERS =====================

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getLabel() {
        return label;
    }

    public String getStatus() {
        return status;
    }

    // ===================== SETTER =====================

    public void setStatus(String status) {
        this.status = status;
    }

    // ===================== DISPLAY =====================

    @Override
    public String toString() {

        return nodeId + " | "
                + nodeType + " | "
                + label + " | "
                + status;
    }
}