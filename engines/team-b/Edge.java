public class Edge {

    private String edgeId;

    private Node source;
    private Node destination;

    private int distance;
    private int latencyMs;

    // Capacity is String because
    // some node types have blank capacity
    private String capacity;

    private boolean availability;

    // ===================== CONSTRUCTOR =====================

    public Edge(
            String edgeId,
            Node source,
            Node destination,
            int distance,
            int latencyMs,
            String capacity,
            boolean availability) {

        this.edgeId = edgeId;
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.latencyMs = latencyMs;
        this.capacity = capacity;
        this.availability = availability;
    }

    // ===================== GETTERS =====================

    public String getEdgeId() {
        return edgeId;
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }

    public int getDistance() {
        return distance;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public String getCapacity() {
        return capacity;
    }

    public boolean isAvailable() {
        return availability;
    }

    // ===================== SETTERS =====================

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setLatencyMs(int latencyMs) {
        this.latencyMs = latencyMs;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    // ===================== DISPLAY =====================

    @Override
    public String toString() {

        return edgeId
                + " | "
                + source.getNodeId()
                + " -> "
                + destination.getNodeId()
                + " | Distance = "
                + distance
                + " | Latency = "
                + latencyMs
                + " ms"
                + " | Capacity = "
                + capacity
                + " | Available = "
                + availability;
    }
}