public class Main {

    public static void main(String[] args) {

        TeamAEngine engine =
                new TeamAEngine();

        // =====================================================
        // CSE LABS
        // =====================================================

        engine.addLab(
                "L1",
                "CSE",
                "CSE Lab 1",
                30
        );

        engine.addLab(
                "L2",
                "CSE",
                "CSE Lab 2",
                30
        );

        engine.addLab(
                "L3",
                "CSE",
                "CSE Lab 3",
                30
        );

        engine.addLab(
                "L4",
                "CSE",
                "CSE Lab 4",
                30
        );

        // =====================================================
        // ELECTRICAL LABS
        // =====================================================

        engine.addLab(
                "EL1",
                "ELECTRICAL",
                "Electrical Lab 1",
                30
        );

        engine.addLab(
                "EL2",
                "ELECTRICAL",
                "Electrical Lab 2",
                30
        );

        // =====================================================
        // AIML LABS
        // =====================================================

        engine.addLab(
                "AL1",
                "AIML",
                "AIML Lab 1",
                30
        );

        engine.addLab(
                "AL2",
                "AIML",
                "AIML Lab 2",
                30
        );

        engine.addLab(
                "AL3",
                "AIML",
                "AIML Lab 3",
                30
        );

        // =====================================================
        // MECHANICAL LABS
        // =====================================================

        engine.addLab(
                "ML1",
                "MECHANICAL",
                "Mechanical Lab 1",
                30
        );

        engine.addLab(
                "ML2",
                "MECHANICAL",
                "Mechanical Lab 2",
                30
        );
                // =====================================================
        // CLASSROOMS + CONFERENCE HALL
        // (added by Team D for integration — original set was 11 labs)
        // =====================================================

        // CSE Classrooms
        engine.addLab("C1",  "CSE",        "CSE Classroom 1", 60);
        engine.addLab("C2",  "CSE",        "CSE Classroom 2", 60);
        engine.addLab("C3",  "CSE",        "CSE Classroom 3", 60);
        engine.addLab("CH1", "CSE",        "Conference Hall", 100);

        // Electrical Classrooms
        engine.addLab("EC1", "ELECTRICAL", "Electrical Classroom 1", 60);
        engine.addLab("EC2", "ELECTRICAL", "Electrical Classroom 2", 60);
        engine.addLab("EC3", "ELECTRICAL", "Electrical Classroom 3", 60);

        // AIML Classrooms
        engine.addLab("AC1", "AIML",       "AIML Classroom 1", 60);
        engine.addLab("AC2", "AIML",       "AIML Classroom 2", 60);
        engine.addLab("AC3", "AIML",       "AIML Classroom 3", 60);

        // Mechanical Classrooms
        engine.addLab("MC1", "MECHANICAL", "Mechanical Classroom 1", 60);
        engine.addLab("MC2", "MECHANICAL", "Mechanical Classroom 2", 60);

        // =====================================================
        // SHOW LAB STATUS
        // =====================================================

        engine.getResourceManager()
                .displayLabs();

        // =====================================================
        // CREATE BOOKING / PROCESS REQUESTS
        // =====================================================

        engine.submitRequest(
                "STUDENT01",
                "L1",
                5,
                60,
                2
        );

        engine.submitRequest(
                "STUDENT02",
                "L1",
                10,
                30,
                1
        );

        engine.submitRequest(
                "STUDENT03",
                "L1",
                4,
                20,
                3
        );

        engine.submitRequest(
                "STUDENT04",
                "L1",
                8,
                45,
                2
        );

        // =====================================================
        // DISPLAY QUEUE BEFORE SCHEDULING
        // =====================================================

        engine.displayQueue("L1");

        // =====================================================
        // RUN PRIORITY SCHEDULER
        // =====================================================

        engine.runScheduler(
                "L1",
                SchedulingPolicy.PRIORITY
        );

        // =====================================================
        // DISPLAY QUEUE AFTER SCHEDULING
        // =====================================================

        engine.displayQueue("L1");

        // =====================================================
        // EXECUTE PROCESSES
        // =====================================================

        engine.executeNext("L1");

        engine.executeNext("L1");

        engine.executeNext("L1");

        engine.executeNext("L1");

        // =====================================================
        // DEADLOCK ANALYSIS
        // =====================================================

        engine.analyseDeadlock();

        // =====================================================
        // DISPLAY RESOURCE STATUS
        // =====================================================

        engine.getResourceManager()
                .displayLabs();

        engine.getResourceManager()
                .displayComputers("L1");

        // =====================================================
        // DISPLAY METRICS
        // =====================================================

        engine.getMetrics()
                .display();

        // =====================================================
        // DISPLAY REQUESTS
        // =====================================================

        engine.displayAllRequests();

        System.out.println(
                "\n=============================================="
        );

        System.out.println(
                "TEAM A OS SCHEDULER FINISHED"
        );

        System.out.println(
                "=============================================="
        );
    }
}