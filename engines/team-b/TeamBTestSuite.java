import java.util.ArrayList;

/** Repeatable correctness checks for Team B's graph and lookup structures. */
public class TeamBTestSuite {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        run("negative edge is rejected", TeamBTestSuite::testNegativeEdge);
        run("Dijkstra finds reference shortest path", TeamBTestSuite::testShortestPath);
        run("Bellman-Ford matches reference shortest path", TeamBTestSuite::testBellmanFord);
        run("BFS and DFS find reachable destinations", TeamBTestSuite::testTraversals);
        run("cyclic graph is handled", TeamBTestSuite::testCycle);
        run("disconnected destination returns no route", TeamBTestSuite::testNoRoute);
        run("source equals destination", TeamBTestSuite::testSameNode);
        run("adjacency matrix mirrors graph edges", TeamBTestSuite::testAdjacencyMatrix);
        run("red-black tree insert, update, search and delete", TeamBTestSuite::testRedBlackTree);

        System.out.println("\n====================================");
        System.out.println("TEAM B TEST SUMMARY");
        System.out.println("Passed : " + passed);
        System.out.println("Failed : " + failed);
        System.out.println("====================================");
        if (failed > 0) {
            throw new AssertionError("Team B tests failed: " + failed);
        }
    }

    private static void run(String name, TestCase test) {
        try {
            test.execute();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable error) {
            failed++;
            System.out.println("[FAIL] " + name + " - " + error.getMessage());
        }
    }

    private static void testNegativeEdge() {
        Graph graph = new Graph();
        graph.addNode(node("A"));
        graph.addNode(node("B"));
        long before = graph.getTopologyVersionNumber();
        graph.addEdge("NEG", "A", "B", -1, 1, true);
        check(graph.getAllEdges().isEmpty(), "negative edge was stored");
        check(graph.getTopologyVersionNumber() == before, "version changed after rejected edge");
    }

    private static void testShortestPath() {
        Graph graph = new Graph();
        graph.addNode(node("A"));
        graph.addNode(node("B"));
        graph.addNode(node("C"));
        graph.addEdge("AB", "A", "B", 10, 2, true);
        graph.addEdge("AC", "A", "C", 50, 8, true);
        graph.addEdge("BC", "B", "C", 20, 3, true);
        RouteResult result = new RoutingEngine().findRoute(graph, "A", "C", "DIJKSTRA");
        check(result.isRouteFound(), "expected a route");
        check(result.getTotalCost() == 30, "expected cost 30, got " + result.getTotalCost());
        check(result.getOrderedNodes().equals(list("A", "B", "C")), "unexpected path " + result.getOrderedNodes());
    }

    private static void testBellmanFord() {
        Graph graph = new Graph();
        graph.addNode(node("A"));
        graph.addNode(node("B"));
        graph.addNode(node("C"));
        graph.addEdge("AB", "A", "B", 10, 2, true);
        graph.addEdge("AC", "A", "C", 50, 8, true);
        graph.addEdge("BC", "B", "C", 20, 3, true);
        RouteResult result = new RoutingEngine().findRoute(graph, "A", "C", "BELLMAN_FORD");
        check(result.isRouteFound(), "expected a Bellman-Ford route");
        check(result.getTotalCost() == 30, "expected cost 30, got " + result.getTotalCost());
        check(result.getOrderedNodes().equals(list("A", "B", "C")), "unexpected path " + result.getOrderedNodes());
    }

    private static void testTraversals() {
        Graph graph = new Graph();
        graph.addNode(node("A"));
        graph.addNode(node("B"));
        graph.addNode(node("C"));
        graph.addEdge("AB", "A", "B", 1, 1, true);
        graph.addEdge("BC", "B", "C", 1, 1, true);
        RouteResult bfs = new RoutingEngine().findRoute(graph, "A", "C", "BFS");
        RouteResult dfs = new RoutingEngine().findRoute(graph, "A", "C", "DFS");
        check(bfs.isRouteFound(), "BFS did not find reachable destination");
        check(dfs.isRouteFound(), "DFS did not find reachable destination");
    }

    private static void testCycle() {
        Graph graph = new Graph();
        graph.addNode(node("A"));
        graph.addNode(node("B"));
        graph.addNode(node("C"));
        graph.addBidirectionalEdge("AB", "BA", "A", "B", 4, 1, true);
        graph.addBidirectionalEdge("BC", "CB", "B", "C", 5, 1, true);
        graph.addBidirectionalEdge("CA", "AC", "C", "A", 6, 1, true);
        RouteResult result = new RoutingEngine().findRoute(graph, "A", "C", "DIJKSTRA");
        check(result.isRouteFound(), "cycle graph should remain routable");
        check(result.getTotalCost() == 6, "expected direct edge cost 6");
    }

    private static void testNoRoute() {
        Graph graph = new Graph();
        graph.addNode(node("A"));
        graph.addNode(node("B"));
        graph.addNode(node("C"));
        graph.addEdge("AB", "A", "B", 1, 1, true);
        RouteResult result = new RoutingEngine().findRoute(graph, "A", "C", "DIJKSTRA");
        check(!result.isRouteFound(), "disconnected destination should not have a route");
    }

    private static void testSameNode() {
        Graph graph = new Graph();
        graph.addNode(node("A"));
        RouteResult result = new RoutingEngine().findRoute(graph, "A", "A", "DIJKSTRA");
        check(result.isRouteFound(), "same-node route should be found");
        check(result.getTotalCost() == 0, "same-node cost should be zero");
        check(result.getOrderedNodes().equals(list("A")), "same-node path should contain A only");
    }

    private static void testAdjacencyMatrix() {
        Graph graph = new Graph();
        graph.addNode(node("A"));
        graph.addNode(node("B"));
        graph.addNode(node("C"));
        graph.addEdge("AB", "A", "B", 12, 4, true);
        graph.addEdge("BC", "B", "C", 7, 2, false);

        AdjacencyMatrix matrix = new AdjacencyMatrix(graph);
        check(matrix.size() == 3, "matrix should have three nodes");
        check(matrix.getDistance("A", "B") == 12, "A->B weight mismatch");
        check(matrix.getLatency("A", "B") == 4, "A->B latency mismatch");
        check("AB".equals(matrix.getEdgeId("A", "B")), "A->B edge ID mismatch");
        check(matrix.getDistance("B", "C") == AdjacencyMatrix.NO_EDGE,
                "unavailable B->C should be treated as no usable edge");
        check(matrix.getDistance("A", "C") == AdjacencyMatrix.NO_EDGE,
                "missing A->C should be no edge");
    }

    private static void testRedBlackTree() {
        RedBlackTree tree = new RedBlackTree();
        long expiry = System.currentTimeMillis() + 60_000;
        for (int i = 0; i < 100; i++) {
            String key = String.format("K%03d", i);
            tree.insert(new RoutingEntry(key, "NEXT", "A->B", expiry, "G-1"));
        }
        check(tree.size() == 100, "expected 100 entries");
        for (int i = 0; i < 100; i++) {
            String key = String.format("K%03d", i);
            check(tree.search(key) != null, "missing inserted key " + key);
        }
        tree.insert(new RoutingEntry("K050", "UPDATED", "A->C", expiry, "G-1"));
        check("UPDATED".equals(tree.search("K050").getNextHop()), "duplicate key did not update");
        check(tree.search("K050", "G-1") != null, "valid version lookup failed");
        check(tree.search("K050", "G-2") == null, "stale version was accepted");
        for (int i = 0; i < 100; i += 2) {
            tree.remove(String.format("K%03d", i));
        }
        check(tree.size() == 50, "expected 50 entries after deletion, got " + tree.size());
        for (int i = 0; i < 100; i++) {
            String key = String.format("K%03d", i);
            check((tree.search(key) != null) == (i % 2 == 1), "incorrect result after deletion for " + key);
        }
    }

    private static Node node(String id) {
        return new Node(id, "Test", id, "ACTIVE");
    }

    private static ArrayList<String> list(String... values) {
        ArrayList<String> result = new ArrayList<>();
        for (String value : values) result.add(value);
        return result;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private interface TestCase {
        void execute();
    }
}
