public class RoutingEntry {

    private String key;
    private String nextHop;
    private String cachedRoute;
    private long expiry;
    private String topologyVersion;

    public RoutingEntry(
            String key,
            String nextHop,
            String cachedRoute,
            long expiry,
            String topologyVersion) {

        this.key = key;
        this.nextHop = nextHop;
        this.cachedRoute = cachedRoute;
        this.expiry = expiry;
        this.topologyVersion = topologyVersion;
    }

    public String getKey() {
        return key;
    }

    public String getNextHop() {
        return nextHop;
    }

    public String getCachedRoute() {
        return cachedRoute;
    }

    public long getExpiry() {
        return expiry;
    }

    public String getTopologyVersion() {
        return topologyVersion;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiry;
    }

    @Override
    public String toString() {

        return "Key = " + key
                + " | Next Hop = " + nextHop
                + " | Cached Route = " + cachedRoute
                + " | Expiry = " + expiry
                + " | Topology Version = " + topologyVersion;
    }
}