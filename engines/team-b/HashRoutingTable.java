import java.util.HashMap;

public class HashRoutingTable {

    private HashMap<String, RoutingEntry> table;

    public HashRoutingTable() {

        table = new HashMap<String, RoutingEntry>();
    }

    // =====================================================
    // INSERT / UPDATE
    // =====================================================

    public void insert(RoutingEntry entry) {

        table.put(
                entry.getKey(),
                entry);
    }

    // =====================================================
    // NORMAL LOOKUP
    // =====================================================

    public RoutingEntry search(String key) {

        return table.get(key);
    }

    // =====================================================
    // VERSION-AWARE SEARCH
    // =====================================================

    public RoutingEntry search(
            String key,
            String currentTopologyVersion) {

        RoutingEntry entry = search(key);

        if (entry == null) {

            return null;
        }

        // Check expiry
        if (entry.isExpired()) {

            System.out.println(
                    "Cache entry expired for key: "
                            + key);

            return null;
        }

        // Check topology version
        if (!entry.getTopologyVersion()
                .equals(currentTopologyVersion)) {

            System.out.println(
                    "Topology version mismatch for key: "
                            + key);

            System.out.println(
                    "Cached Version: "
                            + entry.getTopologyVersion());

            System.out.println(
                    "Current Version: "
                            + currentTopologyVersion);

            return null;
        }

        return entry;
    }

    // =====================================================
    // REMOVE / INVALIDATE
    // =====================================================

    public void remove(String key) {
        table.remove(key);
    }

    // =====================================================
    // SIZE
    // =====================================================

    public int size() {

        return table.size();
    }
}