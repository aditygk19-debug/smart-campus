public class RoutingCacheBenchmark {

    public void benchmark(Graph graph, RouteResult routeResult, int repetitions) {
        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                 ROUTING CACHE BENCHMARK");
        System.out.println("==============================================================");

        if (graph == null || routeResult == null || !routeResult.isRouteFound() || repetitions <= 0) {
            System.out.println("Invalid graph, route result, or repetition count.");
            return;
        }

        String key = routeResult.getOrderedNodes().get(0) + "->"
                + routeResult.getOrderedNodes().get(routeResult.getOrderedNodes().size() - 1);
        String nextHop = routeResult.getOrderedNodes().size() > 1
                ? routeResult.getOrderedNodes().get(1)
                : routeResult.getOrderedNodes().get(0);
        String cachedRoute = String.join(" -> ", routeResult.getOrderedNodes());
        long expiry = System.currentTimeMillis() + 60_000;
        RoutingEntry entry = new RoutingEntry(key, nextHop, cachedRoute, expiry, graph.getGraphVersion());

        AVLTree avlTree = new AVLTree();
        RedBlackTree redBlackTree = new RedBlackTree();
        HashRoutingTable hashTable = new HashRoutingTable();
        avlTree.insert(entry);
        redBlackTree.insert(entry);
        hashTable.insert(entry);

        int avlHits = 0, redBlackHits = 0, hashHits = 0;
        long avlTotal = 0, redBlackTotal = 0, hashTotal = 0;

        // Warm up each lookup implementation before collecting timings.
        for (int i = 0; i < Math.min(100, repetitions); i++) {
            avlTree.search(key, graph.getGraphVersion());
            redBlackTree.search(key, graph.getGraphVersion());
            hashTable.search(key, graph.getGraphVersion());
        }

        for (int i = 0; i < repetitions; i++) {
            long start = System.nanoTime();
            RoutingEntry avlResult = avlTree.search(key, graph.getGraphVersion());
            avlTotal += System.nanoTime() - start;
            if (avlResult != null) avlHits++;

            start = System.nanoTime();
            RoutingEntry rbResult = redBlackTree.search(key, graph.getGraphVersion());
            redBlackTotal += System.nanoTime() - start;
            if (rbResult != null) redBlackHits++;

            start = System.nanoTime();
            RoutingEntry hashResult = hashTable.search(key, graph.getGraphVersion());
            hashTotal += System.nanoTime() - start;
            if (hashResult != null) hashHits++;
        }

        System.out.println("Cache Key           : " + key);
        System.out.println("Topology Version    : " + graph.getGraphVersion());
        System.out.println("Repetitions         : " + repetitions);
        printResult("AVL", avlHits, avlTotal, repetitions);
        printResult("Red-Black", redBlackHits, redBlackTotal, repetitions);
        printResult("Hash", hashHits, hashTotal, repetitions);

        // Verify version rejection and expiry handling for the new tree.
        RedBlackTree staleTree = new RedBlackTree();
        staleTree.insert(new RoutingEntry(key, nextHop, cachedRoute, expiry, "OLD-VERSION"));
        System.out.println("RB Version Mismatch : "
                + (staleTree.search(key, graph.getGraphVersion()) == null ? "REJECTED" : "ACCEPTED"));

        RedBlackTree expiredTree = new RedBlackTree();
        expiredTree.insert(new RoutingEntry(key, nextHop, cachedRoute,
                System.currentTimeMillis() - 1, graph.getGraphVersion()));
        System.out.println("RB Expired Entry    : "
                + (expiredTree.search(key, graph.getGraphVersion()) == null ? "REJECTED" : "ACCEPTED"));

        avlTree.remove(key);
        redBlackTree.remove(key);
        hashTable.remove(key);
        System.out.println("AVL Invalidation    : " + (avlTree.search(key) == null ? "SUCCESS" : "FAILED"));
        System.out.println("Red-Black Invalidate: " + (redBlackTree.search(key) == null ? "SUCCESS" : "FAILED"));
        System.out.println("Hash Invalidation   : " + (hashTable.search(key) == null ? "SUCCESS" : "FAILED"));

        System.out.println();
        System.out.println("AVL Lookup Complexity       : O(log n)");
        System.out.println("Red-Black Lookup Complexity : O(log n)");
        System.out.println("Hash Lookup Complexity      : O(1) average");
        System.out.println("Entries in each structure   : 1 (same workload)");
        System.out.println("Memory note                 : timings are measured; exact bytes are JVM-dependent.");
        System.out.println("==============================================================");
    }

    private void printResult(String label, int hits, long totalNanos, int repetitions) {
        double hitRate = hits * 100.0 / repetitions;
        double averageMicros = (totalNanos / (double) repetitions) / 1000.0;
        System.out.printf("%-20s : hit rate %.2f%% | average %.3f microseconds%n",
                label, hitRate, averageMicros);
    }
}
