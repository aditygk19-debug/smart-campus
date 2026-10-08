import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TraversalBenchmark {

    public void benchmark(Graph graph, String startNodeId, int repetitions) {

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                 BFS / DFS BENCHMARK");
        System.out.println("==============================================================");

        if (graph.getNode(startNodeId) == null) {
            System.out.println("Invalid start node: " + startNodeId);
            return;
        }

        if (repetitions <= 0) {
            System.out.println("Invalid number of repetitions.");
            return;
        }

        long totalBfsTime = 0;
        long totalDfsTime = 0;

        int bfsVisitedCount = 0;
        int dfsVisitedCount = 0;

        ArrayList<String> bfsOrder = new ArrayList<String>();
        ArrayList<String> dfsOrder = new ArrayList<String>();

        // Warm-up
        traverseBFS(graph, startNodeId);
        traverseDFS(graph, startNodeId);

        for (int i = 0; i < repetitions; i++) {

            long bfsStart = System.nanoTime();
            ArrayList<String> currentBfsOrder =
                    traverseBFS(graph, startNodeId);
            long bfsEnd = System.nanoTime();

            totalBfsTime += (bfsEnd - bfsStart);

            if (i == 0) {
                bfsOrder = currentBfsOrder;
            }

            bfsVisitedCount = currentBfsOrder.size();

            long dfsStart = System.nanoTime();
            ArrayList<String> currentDfsOrder =
                    traverseDFS(graph, startNodeId);
            long dfsEnd = System.nanoTime();

            totalDfsTime += (dfsEnd - dfsStart);

            if (i == 0) {
                dfsOrder = currentDfsOrder;
            }

            dfsVisitedCount = currentDfsOrder.size();
        }

        double averageBfsTime =
                (totalBfsTime / (double) repetitions) / 1000.0;

        double averageDfsTime =
                (totalDfsTime / (double) repetitions) / 1000.0;

        System.out.println();
        System.out.println("Start Node          : " + startNodeId);
        System.out.println("Repetitions         : " + repetitions);
        System.out.println("BFS Visited Nodes   : " + bfsVisitedCount);
        System.out.println("DFS Visited Nodes   : " + dfsVisitedCount);

        System.out.printf(
                "Average BFS Time    : %.3f microseconds%n",
                averageBfsTime);

        System.out.printf(
                "Average DFS Time    : %.3f microseconds%n",
                averageDfsTime);

        System.out.println();
        System.out.println("BFS Traversal Order : " + bfsOrder);
        System.out.println("DFS Traversal Order : " + dfsOrder);

        System.out.println();
        System.out.println("BFS Time Complexity : O(V + E)");
        System.out.println("BFS Space Complexity: O(V)");

        System.out.println("DFS Time Complexity : O(V + E)");
        System.out.println("DFS Space Complexity: O(V)");

        System.out.println("==============================================================");
    }

    private ArrayList<String> traverseBFS(
            Graph graph,
            String startNodeId) {

        ArrayList<String> order = new ArrayList<String>();

        Set<String> visited = new HashSet<String>();

        Queue<String> queue = new LinkedList<String>();

        queue.add(startNodeId);
        visited.add(startNodeId);

        while (!queue.isEmpty()) {

            String currentId = queue.poll();

            order.add(currentId);

            for (Edge edge : graph.getNeighbors(currentId)) {

                if (!edge.isAvailable()) {
                    continue;
                }

                String nextId = edge.getDestination().getNodeId();

                if (!visited.contains(nextId)) {
                    visited.add(nextId);
                    queue.add(nextId);
                }
            }
        }

        return order;
    }

    private ArrayList<String> traverseDFS(
            Graph graph,
            String startNodeId) {

        ArrayList<String> order = new ArrayList<String>();

        Set<String> visited = new HashSet<String>();

        dfs(
                graph,
                startNodeId,
                visited,
                order);

        return order;
    }

    private void dfs(
            Graph graph,
            String currentId,
            Set<String> visited,
            ArrayList<String> order) {

        visited.add(currentId);
        order.add(currentId);

        for (Edge edge : graph.getNeighbors(currentId)) {

            if (!edge.isAvailable()) {
                continue;
            }

            String nextId = edge.getDestination().getNodeId();

            if (!visited.contains(nextId)) {

                dfs(
                        graph,
                        nextId,
                        visited,
                        order);
            }
        }
    }
}
