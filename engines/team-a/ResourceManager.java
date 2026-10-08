import java.util.*;

public class ResourceManager {

    private Map<String, Lab> labs = new LinkedHashMap<>();

    private Map<String, List<Computer>> labComputers =
            new HashMap<>();

    // ---------------------------------------------------------
    // ADD LAB
    // ---------------------------------------------------------

    public void addLab(
            String id,
            String department,
            String name,
            int computerCount
    ) {

        Lab lab = new Lab(
                id,
                department,
                name,
                computerCount
        );

        labs.put(id, lab);

        List<Computer> computers =
                new ArrayList<>();

        for (int i = 1; i <= computerCount; i++) {

            String computerId =
                    id + "-PC" +
                    String.format("%02d", i);

            computers.add(
                    new Computer(
                            computerId,
                            id
                    )
            );
        }

        labComputers.put(id, computers);
    }

    // ---------------------------------------------------------
    // CHECK LAB
    // ---------------------------------------------------------

    public boolean labExists(String labId) {
        return labs.containsKey(labId);
    }

    // ---------------------------------------------------------
    // GET LAB
    // ---------------------------------------------------------

    public Lab getLab(String labId) {
        return labs.get(labId);
    }

    // ---------------------------------------------------------
    // ALLOCATE RESOURCES
    // ---------------------------------------------------------

    public List<String> allocateResources(
            String labId,
            int required,
            String requestId
    ) {

        Lab lab = labs.get(labId);

        if (lab == null) {
            return Collections.emptyList();
        }

        if (!lab.canAllocate(required)) {
            return Collections.emptyList();
        }

        List<String> allocated =
                new ArrayList<>();

        for (Computer computer :
                labComputers.get(labId)) {

            if (computer.isAvailable()) {

                computer.allocate(requestId);

                allocated.add(computer.getId());

                if (allocated.size() == required) {
                    break;
                }
            }
        }

        if (allocated.size() == required) {

            lab.allocate(required);

            return allocated;
        }

        // Rollback if allocation fails
        for (String id : allocated) {

            for (Computer computer :
                    labComputers.get(labId)) {

                if (computer.getId().equals(id)) {
                    computer.release();
                }
            }
        }

        return Collections.emptyList();
    }

    // ---------------------------------------------------------
    // RELEASE RESOURCES
    // ---------------------------------------------------------

    public void releaseResources(
            String labId,
            List<String> resourceIds
    ) {

        if (resourceIds == null) {
            return;
        }

        int released = 0;

        for (Computer computer :
                labComputers.get(labId)) {

            if (resourceIds.contains(
                    computer.getId())
                    && !computer.isAvailable()) {

                computer.release();
                released++;
            }
        }

        Lab lab = labs.get(labId);

        if (lab != null) {
            lab.release(released);
        }
    }

    // ---------------------------------------------------------
    // DISPLAY LABS
    // ---------------------------------------------------------

    public void displayLabs() {

        System.out.println(
                "\n================ LAB STATUS ================\n"
        );

        for (Lab lab : labs.values()) {
            System.out.println(lab);
        }
    }

    // ---------------------------------------------------------
    // DISPLAY COMPUTERS
    // ---------------------------------------------------------

    public void displayComputers(String labId) {

        if (!labComputers.containsKey(labId)) {

            System.out.println(
                    "Lab " + labId +
                    " does not exist."
            );

            return;
        }

        System.out.println(
                "\nComputers in " + labId + ":"
        );

        for (Computer computer :
                labComputers.get(labId)) {

            System.out.println(
                    "  " + computer
            );
        }
    }
}