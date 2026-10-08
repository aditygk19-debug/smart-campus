public class Computer {

    private String id;
    private String labId;

    private ResourceState state;

    private String allocatedTo;

    public Computer(String id, String labId) {

        this.id = id;
        this.labId = labId;

        this.state = ResourceState.AVAILABLE;
        this.allocatedTo = null;
    }

    public boolean isAvailable() {
        return state == ResourceState.AVAILABLE;
    }

    public void allocate(String requestId) {

        state = ResourceState.ALLOCATED;
        allocatedTo = requestId;
    }

    public void release() {

        state = ResourceState.AVAILABLE;
        allocatedTo = null;
    }

    public String getId() {
        return id;
    }

    public String getLabId() {
        return labId;
    }

    public ResourceState getState() {
        return state;
    }

    public String getAllocatedTo() {
        return allocatedTo;
    }

    @Override
    public String toString() {

        return id +
                " [" +
                state +
                "]";
    }
}