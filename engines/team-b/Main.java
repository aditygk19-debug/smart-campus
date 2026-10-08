import java.util.Scanner;

public class Main {

        public static void main(String[] args) {

                // =========================================================
                // SMART CAMPUS ROUTING
                // =========================================================

                System.out.println("==============================================");
                System.out.println("          SMART CAMPUS ROUTING");
                System.out.println("==============================================");
                System.out.println("System Status : ONLINE");
                System.out.println("Graph Status  : LOADED");
                System.out.println();

                // =========================================================
                // CREATE GRAPH
                // =========================================================

                Graph graph = new Graph();

                // =========================================================
                // CSE NODES
                // =========================================================

                graph.addNode(new Node("C1", "Classroom", "CSE Classroom 1", "ACTIVE"));
                graph.addNode(new Node("C2", "Classroom", "CSE Classroom 2", "ACTIVE"));
                graph.addNode(new Node("C3", "Classroom", "CSE Classroom 3", "ACTIVE"));

                graph.addNode(new Node("L1", "Lab", "CSE Lab 1", "ACTIVE"));
                graph.addNode(new Node("L2", "Lab", "CSE Lab 2", "ACTIVE"));
                graph.addNode(new Node("L3", "Lab", "CSE Lab 3", "ACTIVE"));
                graph.addNode(new Node("L4", "Lab", "CSE Lab 4", "ACTIVE"));

                graph.addNode(new Node("N4", "Department", "CSE Department", "ACTIVE"));

                // =========================================================
                // AIML NODES
                // =========================================================

                graph.addNode(new Node("AC1", "Classroom", "AIML Classroom 1", "ACTIVE"));
                graph.addNode(new Node("AC2", "Classroom", "AIML Classroom 2", "ACTIVE"));
                graph.addNode(new Node("AC3", "Classroom", "AIML Classroom 3", "ACTIVE"));

                graph.addNode(new Node("AL1", "Lab", "AIML Lab 1", "ACTIVE"));
                graph.addNode(new Node("AL2", "Lab", "AIML Lab 2", "ACTIVE"));
                graph.addNode(new Node("AL3", "Lab", "AIML Lab 3", "ACTIVE"));

                graph.addNode(new Node("N7", "Department", "AIML Building", "ACTIVE"));

                // =========================================================
                // ELECTRICAL NODES
                // =========================================================

                graph.addNode(new Node("EC1", "Classroom", "Electrical Classroom 1", "ACTIVE"));
                graph.addNode(new Node("EC2", "Classroom", "Electrical Classroom 2", "ACTIVE"));
                graph.addNode(new Node("EC3", "Classroom", "Electrical Classroom 3", "ACTIVE"));

                graph.addNode(new Node("EL1", "Lab", "Electrical Lab 1", "ACTIVE"));
                graph.addNode(new Node("EL2", "Lab", "Electrical Lab 2", "ACTIVE"));

                graph.addNode(new Node("N6", "Department", "Electrical Department", "ACTIVE"));

                // =========================================================
                // MECHANICAL NODES
                // =========================================================

                graph.addNode(new Node("MC1", "Classroom", "Mechanical Classroom 1", "ACTIVE"));
                graph.addNode(new Node("MC2", "Classroom", "Mechanical Classroom 2", "ACTIVE"));

                graph.addNode(new Node("ML1", "Lab", "Mechanical Lab 1", "ACTIVE"));
                graph.addNode(new Node("ML2", "Lab", "Mechanical Lab 2", "ACTIVE"));

                graph.addNode(new Node("N8", "Department", "Mechanical Department", "ACTIVE"));

                // =========================================================
                // MAIN CAMPUS NODES
                // =========================================================

                graph.addNode(new Node("N1", "Gate", "Main Gate", "ACTIVE"));
                graph.addNode(new Node("N2", "Gate", "Gate 1", "ACTIVE"));
                graph.addNode(new Node("N3", "Library", "Library", "ACTIVE"));
                graph.addNode(new Node("N5", "Office", "Office", "ACTIVE"));
                graph.addNode(new Node("CH1", "Conference Hall", "Conference Hall", "ACTIVE"));

                // =========================================================
                // MAIN CAMPUS CONNECTIONS
                // =========================================================

                graph.addBidirectionalEdge(
                                "E00", "E00R",
                                "N1", "N4",
                                55, 10, true);

                graph.addBidirectionalEdge(
                                "E00A", "E00AR",
                                "N1", "N5",
                                70, 15, true);

                graph.addBidirectionalEdge(
                                "E00B", "E00BR",
                                "N2", "N5",
                                80, 18, true);

                graph.addBidirectionalEdge(
                                "E00C", "E00CR",
                                "N4", "N6",
                                10, 5, true);

                graph.addBidirectionalEdge(
                                "E00D", "E00DR",
                                "N6", "N7",
                                30, 8, true);

                graph.addBidirectionalEdge(
                                "E00E", "E00ER",
                                "N7", "N8",
                                30, 8, true);

                graph.addBidirectionalEdge(
                                "E00F", "E00FR",
                                "N8", "N2",
                                50, 12, true);

                // =========================================================
                // CSE CONNECTIONS
                // =========================================================

                graph.addBidirectionalEdge(
                                "E01", "E01R",
                                "N4", "L1",
                                10, 3, true);

                graph.addBidirectionalEdge(
                                "E03", "E03R",
                                "N4", "L2",
                                15, 4, true);

                graph.addBidirectionalEdge(
                                "E05", "E05R",
                                "N4", "L3",
                                20, 5, true);

                graph.addBidirectionalEdge(
                                "E07", "E07R",
                                "N4", "L4",
                                22, 5, true);

                graph.addBidirectionalEdge(
                                "E09", "E09R",
                                "N4", "C1",
                                15, 4, true);

                graph.addBidirectionalEdge(
                                "E11", "E11R",
                                "N4", "C2",
                                24, 5, true);

                graph.addBidirectionalEdge(
                                "E13", "E13R",
                                "N4", "C3",
                                25, 6, true);

                graph.addBidirectionalEdge(
                                "E15", "E15R",
                                "N4", "CH1",
                                22, 5, true);

                // =========================================================
                // ELECTRICAL CONNECTIONS
                // =========================================================

                graph.addBidirectionalEdge(
                                "E17", "E17R",
                                "N4", "EL1",
                                70, 12, true);

                graph.addBidirectionalEdge(
                                "E19", "E19R",
                                "N4", "EL2",
                                80, 14, true);

                graph.addBidirectionalEdge(
                                "E21", "E21R",
                                "N4", "EC1",
                                75, 13, true);

                graph.addBidirectionalEdge(
                                "E23", "E23R",
                                "N4", "EC2",
                                83, 15, true);

                graph.addBidirectionalEdge(
                                "E25", "E25R",
                                "N4", "EC3",
                                85, 16, true);

                // =========================================================
                // AIML CONNECTIONS
                // =========================================================

                graph.addBidirectionalEdge(
                                "E27", "E27R",
                                "N6", "AC1",
                                70, 12, true);

                graph.addBidirectionalEdge(
                                "E29", "E29R",
                                "N6", "AC2",
                                75, 13, true);

                graph.addBidirectionalEdge(
                                "E31", "E31R",
                                "N6", "AC3",
                                80, 14, true);

                graph.addBidirectionalEdge(
                                "E33", "E33R",
                                "N6", "AL1",
                                90, 15, true);

                graph.addBidirectionalEdge(
                                "E35", "E35R",
                                "N6", "AL2",
                                92, 16, true);

                graph.addBidirectionalEdge(
                                "E37", "E37R",
                                "N6", "AL3",
                                94, 17, true);

                // =========================================================
                // MECHANICAL CONNECTIONS
                // =========================================================

                graph.addBidirectionalEdge(
                                "E39", "E39R",
                                "N7", "MC1",
                                70, 12, true);

                graph.addBidirectionalEdge(
                                "E41", "E41R",
                                "N7", "MC2",
                                75, 13, true);

                graph.addBidirectionalEdge(
                                "E43", "E43R",
                                "N7", "ML1",
                                82, 15, true);

                graph.addBidirectionalEdge(
                                "E45", "E45R",
                                "N7", "ML2",
                                84, 16, true);

                // =========================================================
                // DISPLAY CAMPUS INFORMATION
                // =========================================================

                graph.displayNodes();

                System.out.println();

                graph.displayGraph();

                System.out.println();
                System.out.println("Graph Version : " + graph.getGraphVersion());

                // =========================================================
                // B4 CONNECTIVITY AND PERFORMANCE ANALYSIS
                // =========================================================

                ConnectivityAnalysis connectivityAnalysis = new ConnectivityAnalysis();

                connectivityAnalysis.analyze(graph, "N1");

                PerformanceAnalysis performanceAnalysis = new PerformanceAnalysis();

                performanceAnalysis.benchmark(graph, "N1", "ML2", 100);

                TraversalBenchmark traversalBenchmark = new TraversalBenchmark();

                traversalBenchmark.benchmark(graph, "N1", 100);

                // =========================================================
                // B3 ROUTING CACHE BENCHMARK
                // =========================================================

                RoutingEngine routingEngine = new RoutingEngine();

                RouteResult cacheRoute = routingEngine.findRoute(
                                graph,
                                "N1",
                                "L1",
                                "DIJKSTRA");

                RoutingCacheBenchmark routingCacheBenchmark = new RoutingCacheBenchmark();

                routingCacheBenchmark.benchmark(
                                graph,
                                cacheRoute,
                                1000);

                // =========================================================
                // ROUTING ENGINE
                // =========================================================

                // =========================================================
                // SCANNER
                // =========================================================

                Scanner scanner = new Scanner(System.in);

                // =========================================================
                // ROUTING LOOP
                // =========================================================

                boolean continueRouting = true;

                while (continueRouting) {

                        System.out.println();
                        System.out.println("==============================================");
                        System.out.println("              ROUTE FINDER");
                        System.out.println("==============================================");

                        // =====================================================
                        // SOURCE INPUT
                        // =====================================================

                        System.out.print("Enter source node ID: ");

                        String sourceId = scanner.nextLine().trim();

                        // =====================================================
                        // DESTINATION INPUT
                        // =====================================================

                        System.out.print("Enter destination node ID: ");

                        String destinationId = scanner.nextLine().trim();

                        // =====================================================
                        // VALIDATE SOURCE
                        // =====================================================

                        if (graph.getNode(sourceId) == null) {

                                System.out.println(
                                                "Invalid source node: " + sourceId);

                                continue;
                        }

                        // =====================================================
                        // VALIDATE DESTINATION
                        // =====================================================

                        if (graph.getNode(destinationId) == null) {

                                System.out.println(
                                                "Invalid destination node: " + destinationId);

                                continue;
                        }

                        // =====================================================
                        // AUTOMATIC ALGORITHM SELECTION
                        // =====================================================

                        System.out.println();
                        System.out.println(
                                        "Routing algorithm selected automatically: Dijkstra");

                        // =====================================================
                        // FIND ROUTE USING ROUTING ENGINE
                        // =====================================================

                        RouteResult result = routingEngine.findRoute(
                                        graph,
                                        sourceId,
                                        destinationId);

                        // =====================================================
                        // DISPLAY RESULT
                        // =====================================================

                        System.out.println(result);

                        // =====================================================
                        // ASK WHETHER TO CONTINUE
                        // =====================================================

                        System.out.println();

                        System.out.print(
                                        "Do you want to find another route? (yes/no): ");

                        String again = scanner.nextLine().trim();

                        if (!again.equalsIgnoreCase("yes")) {

                                continueRouting = false;
                        }
                }

                // =========================================================
                // END SYSTEM
                // =========================================================

                scanner.close();

                System.out.println();
                System.out.println("==============================================");
                System.out.println("       SMART CAMPUS ROUTING CLOSED");
                System.out.println("==============================================");
        }
}
