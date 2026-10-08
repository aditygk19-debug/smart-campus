public class Lab {

    private String id;
    private String department;
    private String name;

    private int totalComputers;
    private int availableComputers;

    public Lab(
            String id,
            String department,
            String name,
            int totalComputers
    ) {
        this.id = id;
        this.department = department;
        this.name = name;

        this.totalComputers = totalComputers;
        this.availableComputers = totalComputers;
    }

    public boolean canAllocate(int requiredComputers) {
        return availableComputers >= requiredComputers;
    }

    public void allocate(int count) {

        if (count > availableComputers) {
            throw new IllegalStateException(
                    "Not enough computers available in " + id
            );
        }

        availableComputers -= count;
    }

    public void release(int count) {

        availableComputers += count;

        if (availableComputers > totalComputers) {
            availableComputers = totalComputers;
        }
    }

    public double utilization() {

        if (totalComputers == 0) {
            return 0;
        }

        return ((double)
                (totalComputers - availableComputers)
                / totalComputers) * 100;
    }

    public String getId() {
        return id;
    }

    public String getDepartment() {
        return department;
    }

    public String getName() {
        return name;
    }

    public int getTotalComputers() {
        return totalComputers;
    }

    public int getAvailableComputers() {
        return availableComputers;
    }

    @Override
    public String toString() {

        return id +
                " | " +
                department +
                " | " +
                name +
                " | Computers: " +
                availableComputers +
                "/" +
                totalComputers +
                " available";
    }
}