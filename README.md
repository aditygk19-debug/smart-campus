# Smart Campus Lab & Resource Optimizer

**MiniLeap Project — Batch C2, Semester V, AY 2026–27**

Smart Campus Lab & Resource Optimizer is a web system that lets students and faculty of the Computer Science and Engineering department browse, book, waitlist and cancel laboratories, classrooms and conference halls, and find the nearest free room from any point on campus. It is built from four cooperating engines, Operating Systems (scheduling and waitlist promotion), Data Structures & Algorithms (campus graph and shortest-path routing), Database Management Systems (schema, stored procedures, audit) and Web Technologies (React frontend and Node.js Backend-for-Frontend), each owned by one team and developed under a Hybrid SDLC with evidence for every stage.

| | |
|---|---|
| **Institute** | K. E. Society's Rajarambapu Institute of Technology, Rajaramnagar (Empowered Autonomous, Affiliated to Shivaji University) |
| **Department** | Computer Science and Engineering |
| **Program** | T.Y. B.Tech. (CSE), Semester V |
| **Batch** | C2 |
| **Mentor** | Prof. Priyanka N. Jadhav |
| **Project Coordinator** | Prof. G. S. Wedpathak |
| **HoD** | Dr. S. S. Patil |
| **Academic Year** | 2026 – 2027 |
| **Methodology** | Hybrid SDLC — Agile, Engine-Based, Evidence-Driven (MiniLeap) |
| **Repository** | https://github.com/aditygk19-debug/smart-campus |

## Table of Contents

1. [Abstract](#1-abstract)
2. [Problem Statement](#2-problem-statement)
3. [Objectives](#3-objectives)
4. [Team Structure and Engine Ownership](#4-team-structure-and-engine-ownership)
5. [System Architecture](#5-system-architecture)
6. [Engine 1 — Operating Systems (Team A)](#6-engine-1--operating-systems-team-a)
7. [Engine 2 — Data Structures & Algorithms (Team B)](#7-engine-2--data-structures--algorithms-team-b)
8. [Engine 3 — Database Management Systems (Team C)](#8-engine-3--database-management-systems-team-c)
9. [Engine 4 — Web Technologies (Team D)](#9-engine-4--web-technologies-team-d)
10. [End-to-End Workflows](#10-end-to-end-workflows)
11. [Data Model](#11-data-model)
12. [API Reference](#12-api-reference)
13. [Algorithm and Complexity Summary](#13-algorithm-and-complexity-summary)
14. [KPI Plan and Targets](#14-kpi-plan-and-targets)
15. [Technology Stack](#15-technology-stack)
16. [Local Setup](#16-local-setup)
17. [Demo Credentials](#17-demo-credentials)
18. [Deliverables Checklist](#18-deliverables-checklist)
19. [Team and Mentor](#19-team-and-mentor)
20. [License](#20-license)

---

## 1. Abstract

Campus laboratories, classrooms and halls are shared resources, yet many departments still allocate them through registers and informal messages. This causes double booking, unclear priority between exams and routine use, no waiting list, no awareness of walking distance, and no audit trail. The Smart Campus Lab & Resource Optimizer replaces this with one web system built from four cooperating engines, each owned by one team. Engine 1 (Operating Systems) applies FCFS and priority scheduling to confirm or waitlist bookings and to promote waiters on cancellation. Engine 2 (Data Structures and Algorithms) models the campus as a 31-node weighted graph, runs Dijkstra, BFS, DFS and Bellman-Ford, and caches routes in hash, AVL and Red-Black structures. Engine 3 (Database Management Systems) supplies the normalized MySQL schema, constraints, stored procedures and audit log that guarantee ACID behaviour. Engine 4 (Web Technologies) delivers the React frontend and a Node.js Backend-for-Frontend that authenticates users, orchestrates the Java engines and streams live notifications over Server-Sent Events. Together the engines support booking, waitlisting, cancellation with automatic promotion, and distance-sorted search, all exercised in four documented end-to-end workflows. The project follows a Hybrid SDLC (Agile, Engine-Based, Evidence-Driven, MiniLeap) in which every result is tied to a code artefact, a test case or a measured KPI.

**Keywords:** resource scheduling, priority waitlist, shortest path routing, stored procedures, backend-for-frontend, server-sent events, Hybrid SDLC, role-based access control

---

## 2. Problem Statement

Booking campus labs, classrooms and halls is currently handled through registers, chat messages and verbal agreements. Five concrete problems follow from that.

| # | Problem | What happens today | What this system replaces it with |
|---|---|---|---|
| 1 | Double booking | Two groups are promised the same room for the same slot because nobody can see the other request | A duplicate check and an occupancy check inside Team A's `/schedule`, backed by constraints and `FOR UPDATE` locking in MySQL, so a slot has at most one Confirmed booking |
| 2 | Unclear priority | An exam and a casual practice session compete on a first-asked basis, or on who knows the lab assistant | Every booking carries a reason that maps to a numeric priority (1 = highest, 3 = lowest); priority decides who is promoted |
| 3 | No waitlist | A person who finds the room taken simply goes away and the slot may later be wasted | An occupied request becomes `Waitlisted` with a `WaitlistPosition`, and the highest-priority waiter is promoted automatically when the booking is cancelled |
| 4 | No distance awareness | Students choose a room without knowing how far it is from where they are | Team B's `/search` returns only available rooms, each with walking distance, latency and path from a chosen source node, sorted nearest first |
| 5 | No audit trail | When a booking changes there is no record of who changed it or when | Every state change writes a row to `audit_log`, and the admin console exposes the log |

---

## 3. Objectives

| # | Objective | Engine | Measure of success |
|---|---|---|---|
| O1 | **Design** a booking scheduling rule that combines FCFS and priority so that the highest-priority waiter is always promoted first | Engine 1 (Team A) | KPI K1.2 and K1.3 at 100% |
| O2 | **Implement** a weighted campus graph of 31 nodes and 60 directed edges with Dijkstra, BFS, DFS and Bellman-Ford routing | Engine 2 (Team B) | KPI K2.1 under 10 ms; K2.3 at 100% agreement with Bellman-Ford |
| O3 | **Compare** hash-table, AVL-tree and Red-Black-tree route caches, and the four graph algorithms, using reproducible benchmarks | Engine 2 (Team B) | KPI K2.2 under 5 µs; benchmark output for all four harnesses |
| O4 | **Design and implement** a normalized MySQL schema with CHECK, unique and foreign-key constraints and stored procedures for every multi-step write | Engine 3 (Team C) | KPI K3.1 under 200 ms; K3.2 and K3.3 at 100%; KPI-S5 and KPI-S6 at 100% rejected |
| O5 | **Build** a React frontend and a Node.js Backend-for-Frontend with role-based access, orchestration of the Java engines and live notifications over SSE | Engine 4 (Team D) | KPI K4.1 under 5 s; K4.2 under 2 s; K4.3 at 100% |
| O6 | **Validate** the integrated system against system-level KPIs and the eight mandatory evidence items | All teams | KPI-S1 to KPI-S8 met |

---

## 4. Team Structure and Engine Ownership

| Team | Engine | Course Concepts Applied | Members (Slot: Name — PRN) |
|---|---|---|---|
| Team A | Engine 1 — Operating Systems | CPU-scheduling policies (FCFS, priority), queues and ordering, deadlock detection on a wait-for graph, resource allocation | S1: Samruddhi Patil — 2403165; S2: Shoumika Chavan — 2403167; S3: Abhilasha Patil — 2403168; S4: Supriya Vairal — 2403169 |
| Team B | Engine 2 — Data Structures & Algorithms | Weighted graphs, Dijkstra, BFS, DFS, Bellman-Ford, hash tables, AVL trees, Red-Black trees, complexity analysis | S1: Saloni Koli — 2403164; S2: Vrushali Patil — 2403170; S3: Tanuja Dhole — 2403172; S4: Shravani Thakur — 2403176 |
| Team C | Engine 3 — Database Management Systems | ER modelling, normalization, constraints, indexes, stored procedures, transactions, ACID, row locking | S1: Atharv Hirve — 2403174; S2: Shruti Dabade — 2403175; S3: Anam Patvekar — 2403178; S4: Shweta Jamdade — 2403179 |
| Team D | Engine 4 — Web Technologies | React SPA, client routing, REST and BFF design, JWT authentication, RBAC, Server-Sent Events, accessibility | S1: Prasenjeet Patil; S2: Aditya Patil; S3: Pranav Jagdale; S4: Aditya Gaikwad; S5: Sarthak Patil |

> **Note:** S1–S5 in this table are **student slots** within a team. They are not SDLC stages. SDLC stages are always written S0, S1, S2 … S7 and are labelled as stages (see Section 18).

---

## 5. System Architecture

### 5.1 High-Level Architecture

```text
+--------------------------------------------------------------+
|  PRESENTATION LAYER                                          |
|  React 19 + Vite 8 + Tailwind 3          port 5173           |
+------------------------------+-------------------------------+
                               |  HTTP/JSON  (/api/*)  and  SSE
                               v
+--------------------------------------------------------------+
|  APPLICATION / API LAYER                                     |
|                                                              |
|  BFF  Node.js 20 + Express 5             port 8080           |
|  (auth, RBAC, orchestration, SSE poller)                     |
|        |                         |                           |
|        | HTTP/JSON               | HTTP/JSON                 |
|        v                         v                           |
|  Team A  Java 21  port 8090   Team B  Java 21  port 8091     |
|  (scheduling, waitlist)       (graph, routing, search)       |
+--------+-------------------------+---------------------------+
         |  JDBC                   |  JDBC            ^
         |                         |                  | mysql2 (reads,
         v                         v                  |  stored procedures)
+--------------------------------------------------------------+
|  DATA LAYER                                                  |
|  MySQL 8.0   port 3306   database: smartcampus               |
+--------------------------------------------------------------+
```

The system uses a three-layer layout with a Backend-for-Frontend orchestrator. The **presentation layer** is the React single-page application. The **application/API layer** is the BFF plus the two Java engines. The **data layer** is one MySQL schema. The React frontend talks **only** to the BFF. The BFF orchestrates Team A and Team B over HTTP/JSON and reads directly from MySQL for ordinary reads and for the stored procedures owned by Team C. Team A and Team B each connect directly to MySQL over JDBC.

### 5.2 Ports, Runtimes, and Versions

| Service | Language / Runtime | Version | Port | Where the port is set |
|---|---|---|---|---|
| Frontend | JavaScript (React 19, Vite 8, Tailwind 3) | Node.js 20 toolchain | 5173 | Vite dev server default; `vite.config.js` |
| BFF | Node.js with Express 5 | Node.js 20 LTS | 8080 | `PORT` in `bff/.env` |
| Team A | Java | 21 | 8090 | Hardcoded in `engines/team-a/TeamAHttpServer.java` |
| Team B | Java | 21 | 8091 | Hardcoded in `engines/team-b/TeamBHttpServer.java` |
| MySQL | MySQL Server | 8.0 | 3306 | MySQL server configuration; database name `smartcampus` |

> **Hardening note:** Team A and Team B currently hardcode their ports. For deployment on a PaaS that assigns the port at runtime, both servers should read the `PORT` environment variable and fall back to 8090 / 8091 when it is not set.

### 5.3 Environment Variables

**BFF (`bff/.env`)**

| Variable | Example | Purpose |
|---|---|---|
| `PORT` | `8080` | Port the BFF listens on |
| `DB_HOST` | `localhost` (`127.0.0.1` on Ubuntu) | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_USER` | `root` | MySQL user |
| `DB_PASSWORD` | (secret) | MySQL password |
| `DB_NAME` | `smartcampus` | Database name |
| `JWT_SECRET` | random 48-byte base64 string | Signing key for JWTs |
| `JWT_EXPIRY` | `24h` | Token lifetime |
| `SCHEDULER_URL` | `http://localhost:8090` | Base URL of Team A |
| `ROUTER_URL` | `http://localhost:8091` | Base URL of Team B |
| `CORS_ORIGIN` | `http://localhost:5173` | Origin allowed by the CORS middleware |

**Team A environment variables**

| Variable | Example | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/smartcampus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | JDBC connection string |
| `DB_USER` | `root` | MySQL user |
| `DB_PASSWORD` | (secret) | MySQL password |

**Team B environment variables**

| Variable | Example | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/smartcampus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | JDBC connection string |
| `DB_USER` | `root` | MySQL user |
| `DB_PASSWORD` | (secret) | MySQL password |

> **Hardening note:** `Db.java` contains a fallback database password that is used when no environment variable is supplied. A credential must never ship in source. Remove the fallback so the engine fails fast at startup when the variable is missing, and rotate any password that has been committed.

### 5.4 Repository Layout

```text
smart-campus/
|-- README.md                      this document
|-- package.json                   frontend dependencies and scripts
|-- package-lock.json
|-- vite.config.js                 Vite config and dev-time /api proxy
|-- tailwind.config.js
|-- postcss.config.js
|-- eslint.config.js
|-- index.html                     Vite HTML entry point
|-- .gitignore
|-- .gitattributes
|-- public/
|   |-- favicon.svg
|   `-- icons.svg
|-- src/                           React frontend source
|   |-- main.jsx                   React entry, BrowserRouter mount
|   |-- App.jsx                    Route table and role guards
|   |-- App.css
|   |-- index.css                  Tailwind directives
|   |-- assets/
|   |   |-- hero.png
|   |   |-- react.svg
|   |   `-- vite.svg
|   |-- components/
|   |   `-- NotificationBell.jsx   live notification bell and badge
|   |-- hooks/
|   |   `-- useSSE.js              SSE subscription hook with auto-reconnect
|   `-- pages/
|       |-- LoginPage.jsx
|       |-- DashboardPage.jsx
|       |-- DepartmentPage.jsx
|       |-- ResourceListPage.jsx
|       |-- BookingFormPage.jsx
|       |-- BookingStatusPage.jsx
|       |-- SearchResourcesPage.jsx
|       |-- SearchResultsPage.jsx
|       |-- AdminDashboardPage.jsx
|       |-- AdminUsersPage.jsx
|       |-- AdminReportsPage.jsx
|       `-- AdminAuditLogsPage.jsx
|-- bff/                           Node.js Backend-for-Frontend
|   |-- package.json
|   |-- package-lock.json
|   |-- .env.example               env var template
|   |-- .gitignore
|   |-- server.js                  Express bootstrap, route registration
|   |-- db.js                      MySQL pool and connection test
|   |-- engines.js                 HTTP clients for Team A and Team B
|   |-- sse.js                     notification poller and SSE stream
|   |-- utils/
|   |   `-- nodeMap.js             ResourceId -> campus NodeId cache
|   `-- routes/
|       |-- auth.js                POST /api/auth/login, JWT issuance
|       |-- departments.js         GET /api/departments
|       |-- resource-types.js      GET /api/departments/:deptId/resource-types
|       |-- resources.js           GET /api/resources, /:id
|       |-- campus-nodes.js        GET /api/campus-nodes
|       |-- bookings.js            POST /api/bookings, GET, /:id/cancel
|       |-- search.js              POST /api/search (proxies Team B)
|       |-- notifications.js       list, unread-count, mark read
|       |-- admin.js               admin users, reset password, audit logs
|       `-- admin-stats.js         admin stats, resource status, reports
|-- engines/
|   |-- team-a/                    Engine 1, Operating Systems (Java 21)
|   |   |-- TeamAHttpServer.java   HTTP entry point, port 8090
|   |   |-- BookingService.java    schedule / waitlist / promote logic
|   |   |-- Db.java                JDBC access, waitlist position
|   |   |-- Scheduler.java         FCFS and priority ordering (legacy)
|   |   |-- ResourceManager.java   lab and computer allocation (legacy)
|   |   |-- Lab.java               lab model (legacy)
|   |   |-- Computer.java          computer model (legacy)
|   |   |-- LockManager.java       wait-for graph and deadlock detection (legacy)
|   |   |-- Metrics.java           waiting / turnaround metrics (legacy)
|   |   |-- SchedulingRequest.java
|   |   |-- SchedulingPolicy.java
|   |   |-- ProcessState.java
|   |   |-- ResourceState.java
|   |   |-- TeamAEngine.java       legacy simulation orchestrator
|   |   |-- Main.java              legacy standalone entry point
|   |   |-- BookingManager.java
|   |   |-- BookingDatabase.java
|   |   |-- BookingRequest.java
|   |   |-- BookingResourceType.java
|   |   |-- BookingStatus.java
|   |   |-- Dockerfile
|   |   `-- lib/
|   |       `-- mysql-connector-j-8.4.0.jar
|   `-- team-b/                    Engine 2, DSA (Java 21)
|       |-- TeamBHttpServer.java   HTTP entry point, port 8091
|       |-- SearchHandler.java     POST /search handler
|       |-- Db.java                JDBC access for /search
|       |-- Graph.java             adjacency list and topology version
|       |-- Node.java
|       |-- Edge.java
|       |-- AdjacencyMatrix.java
|       |-- DijkstraAlgorithm.java
|       |-- BFSAlgorithm.java
|       |-- DFSAlgorithm.java
|       |-- BellmanFordAlgorithm.java
|       |-- RoutingEngine.java     algorithm dispatcher
|       |-- RouteResult.java
|       |-- ConnectivityAnalysis.java
|       |-- RoutingEntry.java      cache entry with expiry and version
|       |-- AVLTree.java
|       |-- RedBlackTree.java
|       |-- HashRoutingTable.java
|       |-- RoutingCacheBenchmark.java
|       |-- PerformanceAnalysis.java
|       |-- TraversalBenchmark.java
|       |-- AlgorithmComparison.java
|       |-- TeamBTestSuite.java    unit tests
|       |-- Main.java              runs the four benchmark harnesses
|       |-- Dockerfile
|       `-- lib/
|           `-- mysql-connector-j-8.4.0.jar
`-- db/
    |-- smartcampus_dump.sql       schema, procedures and seed data
    `-- phase1_migration.sql       idempotent migration
```

### 5.5 Shared Interface Contract

| Aspect | Contract |
|---|---|
| Transport | HTTP/1.1 between all services; Server-Sent Events (`text/event-stream`) from BFF to browser |
| Framing | One JSON request body and one JSON response body per call; SSE events separated by blank lines |
| Message format | JSON with `Content-Type: application/json`; the BFF error shape is `{ "message": "..." }`, the engine error shape is `{ "error": "..." }` |
| Time format | Dates as `YYYY-MM-DD`; times as `HH:mm` in requests and `HH:mm:ss` when returned from the database |
| Authentication | JWT issued and checked by the BFF; Team A and Team B perform no authentication and trust the BFF |
| Error codes | 200, 201, 400, 401, 403, 404, 405, 409, 500, 502 as defined in Section 12.4 |
| Correlation | No request-ID header is propagated between services today; this is a hardening opportunity |
| Versioning | HTTP APIs are unversioned; Team B's routing topology is versioned through `graphVersion` (for example `G-91`) |
| Timeouts | No explicit timeout is documented for BFF-to-engine calls; an unreachable engine is reported as 502 |
| Data store | One MySQL 8.0 schema, `smartcampus`, shared by all services |

### 5.6 Who Calls Whom — Integration Boundary Map

| From | To | Transport | What Crosses |
|---|---|---|---|
| Browser (React) | BFF | HTTP/JSON on `/api/*` | Login, resource browsing, booking, search and admin requests, with the JWT |
| Browser (React) | BFF | SSE on `/api/stream` | Live notification events |
| BFF | Team A | HTTP/JSON | `POST /schedule`, `POST /promote` (and `/waitlist`, `/queue`) |
| BFF | Team B | HTTP/JSON | `POST /search` (and `/route`) |
| BFF | MySQL | `mysql2` over TCP | Reads, `CANCEL_BOOKING_TRANSACTION`, `ADD_USER_TRANSACTION`, notification polling |
| Team A | MySQL | JDBC | Resource, slot and booking reads; booking, notification and audit writes |
| Team B | MySQL | JDBC | Slot, resource and booking reads for search filtering |

> **Rule:** Frontend must never talk to Java engines or the DB directly. Every browser request goes through the BFF, which is the only component that authenticates the caller.

### 5.7 Frontend ↔ BFF Proxy

During development the frontend runs on port 5173 and the BFF on port 8080. The browser code calls relative paths such as `/api/departments`, and the Vite dev server forwards them:

```js
// vite.config.js (representative)
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

> **Note:** This proxy exists only in the Vite dev server. `npm run build` produces static files that contain no proxy, so a production deployment needs a real reverse proxy (for example Nginx) that forwards `/api/*` to the BFF and disables buffering for SSE. See Section 16.4. The snippet above is representative; confirm it against the repository's `vite.config.js`.

---

## 6. Engine 1 — Operating Systems (Team A)

### 6.1 Overview and Responsibility

Team A owns every **schedule decision**: whether a booking request is confirmed, waitlisted or rejected; where a waiter sits in the queue; and which waiter is promoted when a confirmed booking is cancelled. The engine applies First-Come-First-Served ordering with priority scheduling, the two policies taught in the Operating Systems course, to the problem of allocating a single-capacity resource (a room in a time slot) among competing requests.

**Course link:** CPU scheduling (FCFS, priority), ready/wait queues, resource allocation and deadlock detection.

| Direction | Item | Description |
|---|---|---|
| Input | `userId`, `labId`, `date`, `startTime`, `endTime`, `purpose`, `priority` | Sent by the BFF in `POST /schedule` and `POST /waitlist` |
| Input | `labId`, `date`, `startTime`, `endTime` | Sent by the BFF in `POST /promote` after a cancellation |
| Output | `status` (`Confirmed`, `Waitlisted`, `Rejected`), `bookingId`, `waitlistPosition`, `priority`, `message` | Returned from `/schedule` and `/waitlist` |
| Output | `promoted`, `bookingId`, `winnerUserId`, `message` | Returned from `/promote` |
| Side effect | Rows in `bookings`, `notifications`, `audit_log` | Written through JDBC |

### 6.2 Engine Architecture

```text
   BFF (port 8080)
        |  HTTP/JSON
        v
+-----------------------------+
| TeamAHttpServer  (:8090)    |   routes: /health /labs /schedule
|  com.sun.net.httpserver     |           /waitlist /promote /queue
+--------------+--------------+
               |
               v
+-----------------------------+
| BookingService              |   createOrWaitlist(), promoteAfterCancel(),
|  business rules             |   queue(), priority ordering
+--------------+--------------+
               |
               v
+-----------------------------+
| Db                          |   JDBC: resolve resource, resolve slot,
|  data access                |   duplicate / occupied counts, inserts,
+--------------+--------------+   computeWaitlistPosition
               |  JDBC
               v
+-----------------------------+
| MySQL 8.0  smartcampus      |
+-----------------------------+
```

The repository also contains a **legacy set of OS-simulation classes** (for example the wait-for-graph based `LockManager.java`) from the original classroom exercise. They are not used by the HTTP path: `TeamAHttpServer` calls `BookingService`, which calls `Db`.

### 6.3 Data Model and Interface

Team A reads and writes six tables.

| Table | Columns used | Why |
|---|---|---|
| `resources` | `ResourceId`, `LocationId`, `Status` | Resolve a `labId` to a `ResourceId` and check the resource exists |
| `location` | `LocationId`, `RoomNumber` | `RoomNumber` equals the `labId` (for example `L1`), so a join resolves lab to resource |
| `time_slot` | `SlotId`, `StartTime`, `EndTime` | Resolve `(startTime, endTime)` to a `SlotId` |
| `bookings` | `BookingId`, `UserId`, `ResourceId`, `SlotId`, `BookingDate`, `Status`, `Priority`, `WaitlistPosition` | Duplicate check, occupancy check, insert, waitlist ordering, promotion update |
| `notifications` | `UserId`, `BookingId`, `Message` | Insert the "Good news" notification when a waiter is promoted |
| `audit_log` | `UserId`, `Action`, `EntityType`, `EntityId`, `OldValue`, `NewValue` | Record every state change |

Data-flow contract:

```text
IN   (BFF -> Team A):   userId, labId, date, startTime, endTime, purpose, priority
RESOLVE:                labId      -> ResourceId   (resources JOIN location)
                        start/end  -> SlotId       (time_slot)
CHECK:                  duplicate  = same user + resource + slot + date, active status
                        occupied   = another Pending/Confirmed row for resource + slot + date
WRITE:                  bookings (Confirmed | Waitlisted + position), audit_log
OUT  (Team A -> BFF):   ok, bookingId, status, message, waitlistPosition, priority
```

### 6.4 Algorithms and Business Rules

| Algorithm / Rule | Type | Purpose | Complexity | Alternatives Considered |
|---|---|---|---|---|
| Slot resolution | Lookup | Map `(startTime, endTime)` to `SlotId` | O(1) on an 8-row table | Computing slots arithmetically; rejected because `time_slot` is the template and the single source of truth |
| Resource resolution | Lookup / join | Map `labId` to `ResourceId` | O(1) with key lookup | Static in-memory map; rejected because it can drift from the database |
| Duplicate check | Rule | Reject the same user asking twice for the same resource, slot and date | O(1) count query | Unique index only; kept the explicit check so the user gets a readable 409 message |
| FCFS + Priority scheduler | Ranking | Order competing requests by priority first and arrival (`BookingId`) second | O(n log n) sort; O(log n) insert into an ordered index | Pure FCFS (ignores exams); round robin (meaningless for a single-owner slot) |
| Waitlist position | Ranking | Compute the position of a new waiter among existing waiters | O(n) count, n is small | Storing a linked list in the table; rejected as more complex than a count |
| Promotion | Rule | After a cancellation, confirm the waiter with the lowest `Priority`, then lowest `BookingId` | O(log n) with an ordered query, `LIMIT 1` | Promote the oldest regardless of priority; rejected because exams must win |
| Deadlock awareness | Graph | Detect cycles on a wait-for graph in the legacy simulation | O(V + E) | Timeouts; not needed on the HTTP path because each booking touches one resource, so circular waits cannot form |

**Priority mapping (reason to priority value)**

| `reasonId` sent by the frontend | Reason label in the UI | Priority value |
|---|---|---|
| `exam` | Exam / Assessment | 1 (highest) |
| `workshop` | Workshop / Seminar | 1 (highest) |
| `practical` | Practical / Lab Session | 2 (medium) |
| `lecture` | Lecture / Theory Class | 2 (medium) |
| `project` | Project Work | 2 (medium) |
| `meeting` | Meeting / Discussion | 3 (lowest) |
| (unknown / missing) | — | 2 (medium, default) |

The mapping is implemented in `bff/engines.js` in the function `priorityFromReason(reasonId)`.

**Pseudocode — `createOrWaitlist`**

```text
createOrWaitlist(userId, labId, date, startTime, endTime, purpose, priority):
    resourceId = resolveResource(labId)                 # resources JOIN location
    slotId     = resolveSlot(startTime, endTime)        # time_slot
    if resourceId or slotId not found:
        return error 400

    if count(bookings where user = userId and resource = resourceId
             and slot = slotId and date = date and status active) > 0:
        return Rejected("You already have a request for this resource and slot")   # 409

    occupied = count(bookings where resource = resourceId and slot = slotId
                     and date = date and status in (Pending, Confirmed)) > 0

    if not occupied:
        bookingId = insert bookings(..., status = Confirmed, priority)
        insert audit_log(...)
        return Confirmed(bookingId, waitlistPosition = 0)                           # 201

    bookingId = insert bookings(..., status = Waitlisted, priority)
    position  = computeWaitlistPosition(resourceId, slotId, date, priority, bookingId)
    update bookings set WaitlistPosition = position where BookingId = bookingId
    insert audit_log(...)
    return Waitlisted(bookingId, position)                                          # 201
```

**Pseudocode — `promoteAfterCancel`**

```text
promoteAfterCancel(labId, date, startTime, endTime):
    resourceId = resolveResource(labId)
    slotId     = resolveSlot(startTime, endTime)

    next = select BookingId from bookings
           where resource = resourceId and slot = slotId and date = date
             and status = Waitlisted
           order by Priority ASC, BookingId ASC
           limit 1
    if next is none:
        return { promoted: false, message: "No waitlist entries" }

    winner = select UserId from bookings where BookingId = next
    update bookings set Status = Confirmed, WaitlistPosition = NULL where BookingId = next
    insert notifications(winner, next, "Good news! Your waitlisted booking #<next> is now confirmed.")
    insert audit_log(PROMOTE_WAITLIST, BOOKING, next, old = null, new = Confirmed)
    return { promoted: true, bookingId: next, winnerUserId: winner }
```

### 6.5 HTTP Contract

Team A listens on `http://localhost:8090` and has no authentication.

| Method | Path | Purpose | Auth |
|---|---|---|---|
| GET | `/health` | Service and database check | None |
| GET | `/labs` | Static list of 23 campus resources | None |
| POST | `/schedule` | Create or waitlist a booking | None (BFF only) |
| POST | `/waitlist` | Explicit waitlist add, same shape as `/schedule` | None (BFF only) |
| POST | `/promote` | Promote the highest-priority waiter | None (BFF only) |
| GET | `/queue` | Current waitlist for a slot | None (BFF only) |

**`GET /health`** — 200:

```json
{ "status": "ok", "service": "team-a-scheduler", "db": true }
```

**`GET /labs`** — 200 (abbreviated):

```json
[
  { "id": "L1", "dept": "CSE", "name": "CSE Lab 1", "capacity": 30 },
  { "id": "CH1", "dept": "CSE", "name": "Conference Hall", "capacity": 100 }
]
```

**`POST /schedule`** — request:

```json
{
  "userId": 2403163,
  "labId": "L1",
  "date": "2026-10-20",
  "startTime": "10:00",
  "endTime": "11:00",
  "purpose": "Exam / Assessment — mid-semester",
  "priority": 1
}
```

201 Confirmed:

```json
{ "ok": true, "bookingId": 31, "status": "Confirmed", "message": "Booking confirmed", "waitlistPosition": 0, "priority": 1 }
```

201 Waitlisted:

```json
{ "ok": true, "bookingId": 32, "status": "Waitlisted", "message": "Resource is occupied. You are waitlist #1", "waitlistPosition": 1, "priority": 1 }
```

409 Rejected:

```json
{ "ok": false, "bookingId": -1, "status": "Rejected", "message": "You already have a request for this resource and slot", "waitlistPosition": 0, "priority": 1 }
```

400 Bad request:

```json
{ "error": "userId, labId, date, startTime, endTime required" }
```

**`POST /waitlist`** — same request and responses as `/schedule`.

**`POST /promote`** — request:

```json
{ "labId": "L1", "date": "2026-10-20", "startTime": "10:00", "endTime": "11:00" }
```

200 promoted:

```json
{ "promoted": true, "bookingId": 32, "winnerUserId": 2403173, "message": "Promoted booking #32" }
```

200 nothing to promote:

```json
{ "promoted": false, "message": "No waitlist entries" }
```

**`GET /queue?labId=&date=&startTime=&endTime=`** — 200:

```json
[
  { "bookingId": 34, "userId": 2403174, "priority": 1, "position": 1 },
  { "bookingId": 32, "userId": 2403173, "priority": 2, "position": 2 },
  { "bookingId": 33, "userId": 2403163, "priority": 2, "position": 3 }
]
```

### 6.6 Class-by-Class Reference

| Class | File | Role |
|---|---|---|
| `TeamAHttpServer` | `engines/team-a/TeamAHttpServer.java` | Starts `com.sun.net.httpserver` on port 8090, registers the six routes, parses JSON, maps results to status codes |
| `BookingService` | `engines/team-a/BookingService.java` | Business rules: duplicate check, occupancy check, confirm or waitlist, promotion, queue listing |
| `Db` | `engines/team-a/Db.java` | JDBC connection and all SQL: resource and slot resolution, counts, inserts, `computeWaitlistPosition`, audit rows |
| `Scheduler` | `engines/team-a/Scheduler.java` | FCFS and priority ordering logic shared with the original simulation |
| `LockManager` | `engines/team-a/LockManager.java` | Wait-for graph and deadlock detection; legacy simulation, not on the HTTP path |

### 6.7 Testing Evidence

**Normal path**

| ID | Scenario | Input | Expected result |
|---|---|---|---|
| A-N1 | Book a free slot | `/schedule` for `L1` on an empty slot | 201, `Confirmed`, `waitlistPosition` 0, one `bookings` row, one `audit_log` row |
| A-N2 | Book an occupied slot | Second user, same resource, slot and date | 201, `Waitlisted`, `waitlistPosition` 1 |
| A-N3 | Promote on cancel | Cancel the confirmed booking, call `/promote` | `promoted` true, waiter becomes `Confirmed`, `WaitlistPosition` NULL, notification inserted |
| A-N4 | Priority wins | Three waiters with mixed priorities, then cancel (KPI K1.2) | Lowest priority value is promoted first; ties go to the lowest `BookingId` |

**Boundary**

| ID | Scenario | Input | Expected result |
|---|---|---|---|
| A-B1 | Last slot of the day | `16:00`–`17:00` | Resolves to the last `time_slot` row and books normally |
| A-B2 | Empty waitlist promotion | `/promote` when nobody is waiting | 200, `promoted` false, `No waitlist entries` |
| A-B3 | Equal priorities | Two waiters with the same priority | Earlier `BookingId` is promoted first |

**Negative**

| ID | Scenario | Input | Expected result |
|---|---|---|---|
| A-X1 | Duplicate request (KPI K1.3) | Same user, resource, slot and date twice | 409, `Rejected` |
| A-X2 | Missing field | Body without `labId` | 400 with the `required` error message |
| A-X3 | Slot not in template | `startTime` and `endTime` that match no `time_slot` row | Error response; no row inserted |
| A-X4 | Wrong HTTP verb | `GET /schedule` | 405 |

> **Note:** The tables above state the expected behaviour that each test must demonstrate. Record the measured results and screenshots for the report in the engine's testing chapter (5.1.6).

---

## 7. Engine 2 — Data Structures & Algorithms (Team B)

### 7.1 Overview and Responsibility

Team B owns **campus geography and routing**. It models the campus as a weighted graph, answers shortest-path queries, and powers the distance-sorted search: for each available resource it computes the distance from the student's chosen source node and sorts the results nearest first.

**Course link:** graphs, shortest-path algorithms, traversals, hash tables, balanced search trees (AVL and Red-Black), complexity analysis and benchmarking.

| Direction | Item | Description |
|---|---|---|
| Input | `sourceNodeId`, `destNodeId`, optional `algorithm` | `POST /route` |
| Input | `date`, `startTime`, `endTime`, `resourceType`, `minCapacity`, `sourceNodeId` | `POST /search` |
| Output | `found`, `path`, `edges`, `totalDistance`, `totalLatency`, `nodesExplored`, `graphVersion` | Route result |
| Output | Resource rows enriched with `nodeId`, `distance`, `latency`, `path`, sorted ascending by distance | Search result |

### 7.2 Engine Architecture

**HTTP path**

```text
   BFF (port 8080)
        |  HTTP/JSON
        v
+-----------------------------+
| TeamBHttpServer  (:8091)    |   /health  /route  /search
+--------------+--------------+
               |
      +--------+-----------------+
      |                          |
      v                          v
+---------------+        +------------------+
| RoutingEngine |        | Db (JDBC)        |
| dispatcher    |        | slot + resource  |
+-------+-------+        | filtering        |
        |                +--------+---------+
        v                         |
+---------------+                 v
| Dijkstra      |        +------------------+
| BFS / DFS     |        | MySQL smartcampus|
| Bellman-Ford  |        +------------------+
+-------+-------+
        |
        v
+---------------+
| Campus graph  |   31 nodes, 60 directed edges, built in code
+---------------+
```

**Cache trees (benchmark and caching experiments)**

```text
             +---------------------------+
             |  RoutingEntry             |
             |  (route + graphVersion)   |
             +-------------+-------------+
                           |
        +------------------+------------------+
        |                  |                  |
        v                  v                  v
+---------------+  +---------------+  +---------------+
| HashRouting   |  | AVLTree       |  | RedBlackTree  |
| Table         |  | O(log n)      |  | O(log n)      |
| O(1) average  |  | strict balance|  | looser balance|
| (primary)     |  | (benchmark)   |  | (benchmark)   |
+---------------+  +---------------+  +---------------+
```

### 7.3 Campus Graph

The graph has **31 nodes** and **30 logical edge pairs, which are 60 directed edges**, each with a `Distance` and a `Latency`. Node IDs are short strings. Nodes that host a bookable resource use the resource's room number as the node ID (for example `L1`), so the graph and the `location` table line up without a join table.

| Node ID | Name | Type |
|---|---|---|
| `N1` | Main Gate | Gate |
| `N2` | Gate 1 | Gate |
| `N3` | Library | Library |
| `N5` | Office | Office |
| `N4` | CSE Department | Department |
| `N6` | Electrical Department | Department |
| `N7` | AIML Building | Department |
| `N8` | Mechanical Department | Department |
| `L1` | CSE Lab 1 | Lab |
| `L2` | CSE Lab 2 | Lab |
| `L3` | CSE Lab 3 | Lab |
| `L4` | CSE Lab 4 | Lab |
| `C1` | CSE Classroom 1 | Classroom |
| `C2` | CSE Classroom 2 | Classroom |
| `C3` | CSE Classroom 3 | Classroom |
| `EL1` | Electrical Lab 1 | Lab |
| `EL2` | Electrical Lab 2 | Lab |
| `EC1` | Electrical Classroom 1 | Classroom |
| `EC2` | Electrical Classroom 2 | Classroom |
| `EC3` | Electrical Classroom 3 | Classroom |
| `AL1` | AIML Lab 1 | Lab |
| `AL2` | AIML Lab 2 | Lab |
| `AL3` | AIML Lab 3 | Lab |
| `AC1` | AIML Classroom 1 | Classroom |
| `AC2` | AIML Classroom 2 | Classroom |
| `AC3` | AIML Classroom 3 | Classroom |
| `ML1` | Mechanical Lab 1 | Lab |
| `ML2` | Mechanical Lab 2 | Lab |
| `MC1` | Mechanical Classroom 1 | Classroom |
| `MC2` | Mechanical Classroom 2 | Classroom |
| `CH1` | Conference Hall | Conference Hall |

**Edge set (30 bidirectional pairs, 60 directed edges):**

| Edge pair | From ↔ To | Distance | Latency (ms) |
|---|---|---|---|
| `E00` / `E00R` | `N1` ↔ `N4` | 55 | 10 |
| `E00A` / `E00AR` | `N1` ↔ `N5` | 70 | 15 |
| `E00B` / `E00BR` | `N2` ↔ `N5` | 80 | 18 |
| `E00C` / `E00CR` | `N4` ↔ `N6` | 10 | 5 |
| `E00D` / `E00DR` | `N6` ↔ `N7` | 30 | 8 |
| `E00E` / `E00ER` | `N7` ↔ `N8` | 30 | 8 |
| `E00F` / `E00FR` | `N8` ↔ `N2` | 50 | 12 |
| `E01` / `E01R` | `N4` ↔ `L1` | 10 | 3 |
| `E03` / `E03R` | `N4` ↔ `L2` | 15 | 4 |
| `E05` / `E05R` | `N4` ↔ `L3` | 20 | 5 |
| `E07` / `E07R` | `N4` ↔ `L4` | 22 | 5 |
| `E09` / `E09R` | `N4` ↔ `C1` | 15 | 4 |
| `E11` / `E11R` | `N4` ↔ `C2` | 24 | 5 |
| `E13` / `E13R` | `N4` ↔ `C3` | 25 | 6 |
| `E15` / `E15R` | `N4` ↔ `CH1` | 22 | 5 |
| `E17` / `E17R` | `N4` ↔ `EL1` | 70 | 12 |
| `E19` / `E19R` | `N4` ↔ `EL2` | 80 | 14 |
| `E21` / `E21R` | `N4` ↔ `EC1` | 75 | 13 |
| `E23` / `E23R` | `N4` ↔ `EC2` | 83 | 15 |
| `E25` / `E25R` | `N4` ↔ `EC3` | 85 | 16 |
| `E27` / `E27R` | `N6` ↔ `AC1` | 70 | 12 |
| `E29` / `E29R` | `N6` ↔ `AC2` | 75 | 13 |
| `E31` / `E31R` | `N6` ↔ `AC3` | 80 | 14 |
| `E33` / `E33R` | `N6` ↔ `AL1` | 90 | 15 |
| `E35` / `E35R` | `N6` ↔ `AL2` | 92 | 16 |
| `E37` / `E37R` | `N6` ↔ `AL3` | 94 | 17 |
| `E39` / `E39R` | `N7` ↔ `MC1` | 70 | 12 |
| `E41` / `E41R` | `N7` ↔ `MC2` | 75 | 13 |
| `E43` / `E43R` | `N7` ↔ `ML1` | 82 | 15 |
| `E45` / `E45R` | `N7` ↔ `ML2` | 84 | 16 |

Verified shortest distances from `N1` (Main Gate):

| Destination | Distance | Latency | Path |
|---|---|---|---|
| `L1` | 65 | 13 | `N1` → `N4` → `L1` |
| `L2` | 70 | 14 | (see `/route`) |
| `ML1` | 140 | 25 | (see `/route`) |
| `AL1` | 165 | 30 | (see `/route`) |
| `EL1` | 180 | 32 | (see `/route`) |

The topology is identified by a **graph version** string (currently `G-91`) returned by `/health` and by every `/route` response.

> **Note:** The graph is built **in code**, not loaded from the database at startup. This keeps routing independent of database availability and makes benchmarks reproducible, but it means `campus_edge` and the in-code graph can drift apart. This is recorded as an integration risk in Section 9.10.

### 7.4 Algorithms

| Algorithm | Technique | Time complexity | Space | Notes |
|---|---|---|---|---|
| Dijkstra | Greedy with a priority queue | O((V + E) log V) | O(V) | Default for `/route` and every row of `/search`; requires non-negative weights |
| BFS | Queue-based level traversal | O(V + E) | O(V) | Fewest hops, ignores weights; used for comparison and validation |
| DFS | Stack or recursion | O(V + E) | O(V) | Finds a path, not the shortest; used for comparison and the cycle demo |
| Bellman-Ford | Edge relaxation, V − 1 rounds | O(V · E) | O(V) | Correctness reference for Dijkstra (KPI K2.3) |

**Dijkstra pseudocode**

```text
dijkstra(graph, source, dest):
    dist[v] = infinity for all v;  dist[source] = 0
    prev[v] = none
    pq = min-priority-queue ordered by dist;  pq.push(source, 0)
    explored = 0

    while pq not empty:
        u = pq.pop_min()
        explored += 1
        if u == dest: break
        for each edge (u, v, w) in graph.adjacent(u):
            if dist[u] + w < dist[v]:
                dist[v] = dist[u] + w
                prev[v] = u
                pq.push_or_decrease(v, dist[v])

    if dist[dest] is infinity: return found = false
    path = walk prev from dest back to source, reversed
    return found = true, path, totalDistance = dist[dest], nodesExplored = explored
```

- **Bellman-Ford negative-edge guard:** after V − 1 relaxation rounds a further pass checks whether any edge can still be relaxed; if so, a negative cycle exists and the algorithm refuses to return a route instead of looping. The campus graph has only positive weights, so the guard never fires, but it keeps the reference algorithm correct.
- **BFS:** treats every edge as cost 1 and returns the path with the fewest hops, which can differ from the shortest-distance path.
- **DFS:** explores one branch fully before backtracking; the first path found is returned, so it is not guaranteed to be shortest.
- **Connectivity Analysis:** a traversal from a start node that reports which nodes are reachable and whether the graph is connected, in O(V + E).

**Dispatcher** (illustrative; it mirrors the algorithm names accepted by `/route`):

```java
RouteResult route(String source, String dest, String algorithm) {
    String algo = (algorithm == null) ? "DIJKSTRA"
                : algorithm.toUpperCase().replace('-', '_');
    switch (algo) {
        case "DIJKSTRA":     return DijkstraAlgorithm.run(graph, source, dest);
        case "BELLMAN_FORD": return BellmanFordAlgorithm.run(graph, source, dest);
        case "BFS":          return BFSAlgorithm.run(graph, source, dest);
        case "DFS":          return DFSAlgorithm.run(graph, source, dest);
        default:             throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
    }
}
```

### 7.5 Routing Cache

A routing cache stores computed routes so repeated queries skip the graph search. Each cached value is a **RoutingEntry** with these fields:

| Field | Meaning |
|---|---|
| source node ID | Start of the route |
| destination node ID | End of the route |
| `path` | Ordered list of node IDs |
| `totalDistance` | Sum of edge distances |
| `totalLatency` | Sum of edge latencies |
| `graphVersion` | Topology version the route was computed against |

Three implementations are provided so they can be compared:

| Implementation | Class | Lookup | Insert | Role |
|---|---|---|---|---|
| Hash table | `HashRoutingTable` | O(1) average | O(1) average | Primary cache |
| AVL tree | `AVLTree` | O(log n) | O(log n) | Benchmark; strictly balanced, faster lookups, more rotations |
| Red-Black tree | `RedBlackTree` | O(log n) | O(log n) | Benchmark; looser balance, fewer rotations on insert |

**Version-aware search:** a lookup takes both the key and the *current* graph version, and a stored entry counts as a hit only when its `graphVersion` equals the current one. If the topology has changed, the entry is treated as a miss and recomputed. This prevents stale routes from being served after an edge change.

```text
search(key, currentGraphVersion):
    entry = table.find(key)
    if entry is none:                         return MISS
    if entry.graphVersion != currentGraphVersion:  return MISS     # stale route
    return entry
```

### 7.6 HTTP Contract

Team B listens on `http://localhost:8091` and has no authentication.

**`GET /health`** — 200:

```json
{ "status": "ok", "service": "team-b-routing", "graphVersion": "G-91" }
```

**`POST /route`** — request (`algorithm` optional; `DIJKSTRA`, `BELLMAN_FORD` or `BELLMAN-FORD`, `BFS`, `DFS`):

```json
{ "sourceNodeId": "N1", "destNodeId": "L1", "algorithm": "DIJKSTRA" }
```

200 found:

```json
{
  "found": true,
  "algorithm": "DIJKSTRA",
  "graphVersion": "G-91",
  "path": ["N1", "N4", "L1"],
  "edges": ["E00", "E01"],
  "totalDistance": 65,
  "totalLatency": 13,
  "nodesExplored": 5
}
```

200 not found:

```json
{ "found": false, "algorithm": "DIJKSTRA", "graphVersion": "G-91", "path": [], "edges": [], "totalDistance": 0, "totalLatency": 0, "nodesExplored": 0 }
```

400:

```json
{ "error": "sourceNodeId and destNodeId required" }
```

**`POST /search`** — request (`resourceType` null means any; `minCapacity` defaults to 0; `sourceNodeId` defaults to `N1`):

```json
{
  "date": "2026-10-20",
  "startTime": "10:00",
  "endTime": "11:00",
  "resourceType": "Lab",
  "minCapacity": 30,
  "sourceNodeId": "N1"
}
```

200 (sorted ascending by `distance`):

```json
[
  {
    "resourceId": 1,
    "resourceName": "CSE Lab 1",
    "capacity": 50,
    "status": "Available",
    "departmentName": "CSE",
    "typeName": "Lab",
    "nodeId": "L1",
    "nodeName": "CSE Lab 1",
    "distance": 65,
    "latency": 13,
    "path": ["N1", "N4", "L1"]
  }
]
```

400 errors:

```json
{ "error": "date, startTime, endTime required" }
```

```json
{ "error": "No matching time slot for 10:00 - 11:00" }
```

### 7.7 Class-by-Class Reference

| Group | Class | File | Role |
|---|---|---|---|
| Graph primitives | `Graph` | `engines/team-b/Graph.java` | Adjacency list, addNode, addEdge, addBidirectionalEdge, getNeighbors, getAllNodes, getAllEdges, getGraphVersion |
| Graph primitives | `Node` | `engines/team-b/Node.java` | Immutable node record: nodeId, nodeType, label, status |
| Graph primitives | `Edge` | `engines/team-b/Edge.java` | Directed edge record: edgeId, source, destination, distance, latencyMs, capacity, availability |
| Graph primitives | `AdjacencyMatrix` | `engines/team-b/AdjacencyMatrix.java` | Matrix adapter used by tests to cross-check edges |
| Graph primitives | `RouteResult` | `engines/team-b/RouteResult.java` | Result carrier: path, edges, totalDistance, totalLatency, nodesExplored, algorithm, graphVersion |
| Graph primitives | `RoutingEngine` | `engines/team-b/RoutingEngine.java` | Dispatcher selecting the algorithm by name |
| Graph primitives | `RoutingEntry` | `engines/team-b/RoutingEntry.java` | Cache entry with key, nextHop, cachedRoute, expiry, topologyVersion |
| Algorithms | `DijkstraAlgorithm` | `DijkstraAlgorithm.java` | Priority-queue shortest path |
| Algorithms | `BFSAlgorithm` | `BFSAlgorithm.java` | Fewest-hops traversal |
| Algorithms | `DFSAlgorithm` | `DFSAlgorithm.java` | Depth-first traversal |
| Algorithms | `BellmanFordAlgorithm` | `BellmanFordAlgorithm.java` | Relaxation-based shortest path with negative-cycle guard |
| Algorithms | `ConnectivityAnalysis` | `ConnectivityAnalysis.java` | Reachability and connectivity report |
| Cache | `HashRoutingTable` | `HashRoutingTable.java` | Hash-table route cache |
| Cache | `AVLTree` | `AVLTree.java` | AVL route cache |
| Cache | `RedBlackTree` | `RedBlackTree.java` | Red-Black route cache |
| Benchmarks | `PerformanceAnalysis` | `PerformanceAnalysis.java` | Dijkstra query time |
| Benchmarks | `TraversalBenchmark` | `TraversalBenchmark.java` | BFS and DFS timing |
| Benchmarks | `RoutingCacheBenchmark` | `RoutingCacheBenchmark.java` | Hash vs AVL vs Red-Black lookup time |
| Benchmarks | `AlgorithmComparison` | `AlgorithmComparison.java` | Side-by-side algorithm comparison |
| Benchmarks | `Main` | `Main.java` | Runs the four harnesses |
| HTTP and DB | `TeamBHttpServer` | `TeamBHttpServer.java` | HTTP server on port 8091 |
| HTTP and DB | `Db` | `Db.java` | JDBC for slot, resource and booking filtering |
| Tests | `TeamBTestSuite` | `TeamBTestSuite.java` | Unit tests, including Dijkstra vs Bellman-Ford |

### 7.8 Benchmarks

| Harness | What it measures | Method | Typical order of magnitude |
|---|---|---|---|
| `PerformanceAnalysis` | Dijkstra route query time on the 31-node, 60-edge graph (KPI K2.1) | Average of 100 runs after 10 warm-up runs | Microseconds to well under 1 ms; target under 10 ms |
| `TraversalBenchmark` | BFS and DFS traversal time | Repeated runs over the same graph | Microseconds |
| `RoutingCacheBenchmark` | Lookup time for hash table, AVL tree and Red-Black tree (KPI K2.2) | Average of 1000 lookups | Hash lowest, trees slightly higher; target under 5 µs |
| `AlgorithmComparison` | Dijkstra, BFS, DFS and Bellman-Ford side by side | Same source and destination pairs | Bellman-Ford slowest, as O(V · E) predicts |

> **Note:** Absolute numbers depend on the machine and JVM warm-up. Run `java -cp ".;lib/*" Main` (Section 14.3) and record the printed values as the measured KPI evidence.

### 7.9 Testing Evidence

**`TeamBTestSuite` cases**

| ID | Case | Expected result |
|---|---|---|
| B-1 | Dijkstra shortest path `N1` to `L1` | Path `N1`, `N4`, `L1`; distance 65 |
| B-2 | Dijkstra agrees with Bellman-Ford on reference pairs (KPI K2.3) | Identical `totalDistance` |
| B-3 | Source equals destination | Path of one node, distance 0 |
| B-4 | Unreachable or unknown destination | `found` false |
| B-5 | BFS returns fewest hops | Hop count not greater than the Dijkstra path |
| B-6 | DFS returns a valid path | Every consecutive pair is an edge |
| B-7 | Connectivity analysis | All 31 nodes reachable from `N1` |
| B-8 | Cache hit and miss | Present key hits, absent key misses |
| B-9 | Version-aware cache | Entry with an old `graphVersion` is a miss |

The nine tests in `TeamBTestSuite` map to the cases above as follows:

| Test method (in `TeamBTestSuite`) | Case above |
|---|---|
| `testNegativeEdge` | B-2 (negative edge rejection guard) |
| `testShortestPath` | B-1 |
| `testBellmanFord` | B-2 |
| `testTraversals` | B-5, B-6 |
| `testCycle` | B-6 |
| `testNoRoute` | B-4 |
| `testSameNode` | B-3 |
| `testAdjacencyMatrix` | B-8 (cache hit / miss analogue) |
| `testRedBlackTree` | B-9 (version-aware cache) |

**Manual verification**

| Check | Command or action | Expected |
|---|---|---|
| Service health | `GET http://localhost:8091/health` | `status: ok` and a `graphVersion` |
| Route | `POST /route` from `N1` to `L1` | Distance 65, latency 13 |
| Search order | `POST /search` for Lab, capacity 30, from `N1` | Rows ascending by distance, `L1` first |
| Missing field | `POST /search` without `date` | 400 `date, startTime, endTime required` |
| Bad slot | `POST /search` with `10:00`–`10:30` | 400 `No matching time slot` |

---

## 8. Engine 3 — Database Management Systems (Team C)

### 8.1 Overview and Responsibility

Team C owns the **single source of truth**: the MySQL 8.0 schema `smartcampus`. It defines the tables, keys, constraints and indexes, and the stored procedures that wrap every multi-step write in a transaction. Team A, Team B and the BFF all depend on this schema.

**Course link:** ER modelling, normalization to 3NF, integrity constraints, indexing, stored procedures, transactions, ACID properties and row-level locking.

| Direction | Item | Description |
|---|---|---|
| Input | SQL from the BFF (`mysql2`), Team A (JDBC), Team B (JDBC) | Reads, inserts, updates and `CALL` statements |
| Output | Rows, constraint errors, `SIGNAL SQLSTATE` errors from procedures | Business-rule violations come back as database errors |
| Guarantee | Atomic multi-step writes, referential integrity, audit completeness | KPI K3.1, K3.2, K3.3 |

### 8.2 Engine Architecture

```text
   BFF (mysql2)         Team A (JDBC)          Team B (JDBC)
        |                     |                      |
        +----------+----------+----------+-----------+
                   |
                   v
+------------------------------------------------------------+
| MySQL 8.0   database: smartcampus   port 3306              |
|                                                            |
|  TABLES (identity, booking, campus graph, notify + audit)  |
|    department, location, resource_type, resources,         |
|    users, time_slot, bookings, notifications,              |
|    campus_node, campus_edge, audit_log, ...                |
|                                                            |
|  STORED PROCEDURES (8)                                     |
|    CREATE_BOOKING_TRANSACTION, CANCEL_BOOKING_TRANSACTION, |
|    ADD_USER_TRANSACTION, ...                               |
|                                                            |
|  CONSTRAINTS: PK, FK, UNIQUE, CHECK     ENGINE: InnoDB     |
+------------------------------------------------------------+
```

The schema has **12 tables** and **8 stored procedures**. The tables are: `users`, `department`, `location`, `resource_type`, `resources`, `time_slot`, `bookings`, `notifications`, `campus_node`, `campus_edge`, `route_history`, `audit_log`. The procedures are: `CREATE_BOOKING_TRANSACTION`, `CANCEL_BOOKING_TRANSACTION`, `WAITLIST_BOOKING_TRANSACTION`, `PROMOTE_WAITLIST_TRANSACTION`, `ADD_USER_TRANSACTION`, `UPDATE_USER_TRANSACTION`, `DELETE_USER_TRANSACTION`, `RESET_PASSWORD_TRANSACTION`.

### 8.3 Schema Overview

| Group | Tables |
|---|---|
| Identity | `users`, `department` |
| Booking | `resources`, `location`, `resource_type`, `time_slot`, `bookings` |
| Campus graph | `campus_node`, `campus_edge` |
| Notifications and audit | `notifications`, `audit_log` |

**Relationships**

| Parent | Child | Cardinality | Rule |
|---|---|---|---|
| `department` | `resources` | 1 to N | `resources.DepartmentId` FK |
| `location` | `resources` | 1 to N | `resources.LocationId` FK |
| `resource_type` | `resources` | 1 to N | `resources.TypeId` FK |
| `resources` | `bookings` | 1 to N | `bookings.ResourceId` FK |
| `users` | `bookings` | 1 to N | `bookings.UserId` FK |
| `time_slot` | `bookings` | 1 to N | `bookings.SlotId` FK |
| `users` | `notifications` | 1 to N | `notifications.UserId` FK, `ON DELETE CASCADE` |
| `bookings` | `notifications` | 1 to 0..N | `notifications.BookingId` FK, `ON DELETE SET NULL` |
| `campus_node` | `campus_edge` | 1 to N | `FromNodeId` and `ToNodeId` FKs |
| (none) | `audit_log` | not linked | `UserId` deliberately has no FK |

**CHECK constraints**

| Table | Column | Allowed values |
|---|---|---|
| `bookings` | `Status` | The five statuses, including `Pending`, `Confirmed`, `Waitlisted`, `Cancelled`, `Rejected` |
| `users` | `Role` | `Student`, `Faculty`, `Admin` |
| `resources` | `Capacity` | Greater than 0 (capacity 0 is rejected, KPI-S6) |

**Unique constraints:** `department.DepartmentName`, `resource_type.TypeName`.

**Exact CHECK constraint values (from the dump):**

- `bookings.Status IN ('Pending', 'Confirmed', 'Cancelled', 'Waitlisted', 'Rejected')`
- `users.Role IN ('Student', 'Faculty', 'Admin')`
- `resources.Capacity > 0`
- `time_slot.EndTime > StartTime`

**Secondary indexes:**

| Table | Index | Columns |
|---|---|---|
| `audit_log` | `idx_entity` | `(EntityType, EntityId)` |
| `audit_log` | `idx_user` | `(UserId)` |
| `notifications` | `idx_user_read` | `(UserId, IsRead)` |
| `bookings` | `idx_resource` | `(ResourceId)` |
| `bookings` | `UserId` | `(UserId)` |
| `bookings` | `SlotId` | `(SlotId)` |

### 8.4 Table-by-Table Detail

> **Note:** Column names and constraints below come from the data model in Section 11 and the API payloads. Confirm each type against `db/smartcampus_dump.sql` (`SHOW CREATE TABLE <name>`).

**`department`**

| Column | Type | Constraints |
|---|---|---|
| `DepartmentId` | VARCHAR (for example `A1`) | PK |
| `DepartmentName` | VARCHAR | UNIQUE, NOT NULL |

**`location`**

| Column | Type | Constraints |
|---|---|---|
| `LocationId` | INT or VARCHAR | PK |
| `RoomNumber` | VARCHAR | Also the campus node ID (for example `L1`) |
| `BuildingName` | VARCHAR | Returned by the API as `buildingName` |
| `FloorNumber` | INT | Returned by the API as `floorNumber` |

**`resource_type`**

| Column | Type | Constraints |
|---|---|---|
| `TypeId` | INT | PK |
| `TypeName` | VARCHAR | UNIQUE (`Lab`, `Classroom`, `Conference Hall`) |
| `Description` | VARCHAR | Returned as `description` |

**`resources`**

| Column | Type | Constraints |
|---|---|---|
| `ResourceId` | INT | PK |
| `ResourceName` | VARCHAR | NOT NULL |
| `DepartmentId` | VARCHAR | FK to `department` |
| `LocationId` | INT or VARCHAR | FK to `location` |
| `TypeId` | INT | FK to `resource_type` |
| `Capacity` | INT | CHECK greater than 0 |
| `Status` | VARCHAR | Resource availability, for example `Available` |

**`users`**

| Column | Type | Constraints |
|---|---|---|
| `UserId` | INT | PK (for example 2403163) |
| `Role` | VARCHAR | CHECK in `Student`, `Faculty`, `Admin` |
| `Status` | VARCHAR | Account status; login requires `Active` |
| Password column | VARCHAR | Plaintext in the demo seed; see Section 9.5 |

**`time_slot`**

| Column | Type | Constraints |
|---|---|---|
| `SlotId` | INT | PK |
| `StartTime` | TIME | NOT NULL |
| `EndTime` | TIME | NOT NULL |

Eight rows, 09:00 to 17:00 in one-hour steps. This is a template, not a dated slot.

**`bookings`**

| Column | Type | Constraints |
|---|---|---|
| `BookingId` | INT AUTO_INCREMENT | PK |
| `UserId` | INT | FK to `users` |
| `ResourceId` | INT | FK to `resources` |
| `SlotId` | INT | FK to `time_slot` |
| `BookingDate` | DATE | The actual date of the booking |
| `Status` | VARCHAR | CHECK listing all five statuses |
| `Priority` | INT | 1 = highest, 3 = lowest |
| `WaitlistPosition` | INT | Nullable; set only for `Waitlisted`, NULL on promotion |
| Purpose / notes column | VARCHAR | Returned as `purpose` |

**`notifications`**

| Column | Type | Constraints |
|---|---|---|
| `NotificationId` | INT AUTO_INCREMENT | PK |
| `UserId` | INT | FK to `users`, `ON DELETE CASCADE` |
| `BookingId` | INT | FK to `bookings`, nullable, `ON DELETE SET NULL` |
| `Message` | VARCHAR / TEXT | NOT NULL |
| `IsRead` | TINYINT(1) | Default 0 |
| `CreatedAt` | TIMESTAMP / DATETIME | Default current time |

**`campus_node`**

| Column | Type | Constraints |
|---|---|---|
| `NodeId` | VARCHAR | PK (for example `N1`, `L1`) |
| `NodeName` | VARCHAR | NOT NULL |
| `NodeType` | VARCHAR | Gate, Library, Department, Lab and so on |
| Status column | VARCHAR | Returned by the API as `status` |

**`campus_edge`**

| Column | Type | Constraints |
|---|---|---|
| `EdgeId` | VARCHAR | PK (for example `E00`) |
| `FromNodeId` | VARCHAR | FK to `campus_node` |
| `ToNodeId` | VARCHAR | FK to `campus_node` |
| `Distance` | INT | Positive |
| `Latency` | INT | Positive |

**`audit_log`**

| Column | Type | Constraints |
|---|---|---|
| `AuditId` | INT AUTO_INCREMENT | PK |
| `UserId` | INT | No FK, so audit survives user deletion |
| `Action` | VARCHAR | For example `PROMOTE_WAITLIST` |
| `EntityType` | VARCHAR | For example `BOOKING` |
| `EntityId` | INT | Id of the affected row |
| `OldValue` | VARCHAR | Nullable |
| `NewValue` | VARCHAR | Nullable |
| `CreatedAt` | TIMESTAMP / DATETIME | Default current time |

**`route_history`**

| Column | Type | Constraints |
|---|---|---|
| `RouteHistoryId` | INT AUTO_INCREMENT | PK |
| `UserId` | INT | FK to `users` |
| `StartNodeId` | VARCHAR(50) | FK to `campus_node` |
| `EndNodeId` | VARCHAR(50) | FK to `campus_node` |
| `RouteDate` | DATE | NOT NULL |
| `Distance` | DECIMAL(10,2) | Nullable |
| `RouteStatus` | VARCHAR(30) | Default `Completed` |

Records each routing query for analytics; populated as an optional extension — the live search path does not currently write to it.

### 8.5 Normalization Rationale

- **1NF:** every column holds a single atomic value; there are no repeating groups. A booking has one resource, one slot and one date rather than lists of them.
- **2NF:** every non-key attribute depends on the whole key. All tables use a single-column surrogate or natural primary key, so no partial dependency exists.
- **3NF:** non-key attributes depend only on the key. Department name lives in `department`, room and building in `location`, type name in `resource_type`; `resources` stores only their IDs. A booking's date is separate from the `time_slot` template so the template is never duplicated per date.

| Denormalization candidate | Benefit | Why rejected |
|---|---|---|
| Store `ResourceName` and `DepartmentName` on `bookings` | Faster booking list with no join | Names can change; the join is cheap at this scale; would create update anomalies |
| Store `StartTime` and `EndTime` on `bookings` | Avoids joining `time_slot` | Duplicates the template and breaks the single source of truth |
| Store a running waitlist counter on `resources` | O(1) position lookup | Counter must be kept in sync under concurrency; a count over fewer than ten rows is simpler and safe |
| Copy user role into `audit_log` | Readable history | `audit_log` already records the action and ids; extra copies add no audit value |
| Merge `campus_node` into `location` | One table less | Nodes include gates and the library, which are not bookable resources |

### 8.6 Stored Procedures

Procedures wrap multi-step writes so that either every step commits or none does.

**1. `CREATE_BOOKING_TRANSACTION`**

| Item | Detail |
|---|---|
| Signature | Takes user, resource, slot, booking date, priority and purpose; returns the new booking and its status |
| Steps | 1. Start the transaction. 2. Lock the target rows with `FOR UPDATE`. 3. Reject a duplicate request for the same user, resource, slot and date. 4. Insert `Confirmed` if the slot is free, otherwise insert `Waitlisted` with a position. 5. Insert an `audit_log` row. 6. Commit. |
| Business rules | One active request per user, resource, slot and date; at most one Confirmed booking per slot (KPI K3.2); every state change audited |
| Caller | Concurrency test and manual SQL; the live booking path goes through Team A's `/schedule` |

**2. `CANCEL_BOOKING_TRANSACTION(bookingId)`**

| Item | Detail |
|---|---|
| Signature | Takes `BookingId` |
| Steps | 1. Start the transaction. 2. `SELECT Status, UserId FROM BOOKINGS WHERE BookingId = ? FOR UPDATE`. 3. Reject if the booking is missing or already cancelled. 4. `UPDATE` status to `Cancelled`. 5. Insert an `audit_log` row. 6. Insert a cancellation `notifications` row. 7. Commit. |
| Business rules | A booking can be cancelled once; cancellation is audited and the user is notified |
| Caller | BFF `POST /api/bookings/:id/cancel`, which then calls Team A `/promote` |

**3. `ADD_USER_TRANSACTION`**

| Item | Detail |
|---|---|
| Signature | Takes user ID, role and password (and status) |
| Steps | 1. Start the transaction. 2. Reject if the user already exists. 3. Insert into `users` with a valid role. 4. Insert an `audit_log` row. 5. Commit. |
| Business rules | Role must pass the CHECK constraint; user IDs are unique; creation is audited |
| Caller | BFF `POST /api/admin/users` |

**4. `WAITLIST_BOOKING_TRANSACTION`**

| Item | Detail |
|---|---|
| Signature | Takes user, resource, slot, booking date, priority and purpose; returns the new booking ID and its waitlist position |
| Steps | 1. Start the transaction. 2. Validate the user is Active and the slot exists. 3. Reject if the user already has a Waitlisted / Confirmed / Pending booking for the same resource, slot and date. 4. Insert a `Waitlisted` booking. 5. Count waiters ahead (lower priority, or same priority and lower BookingId) and update `WaitlistPosition`. 6. Insert an audit row. 7. Commit. |
| Business rules | One active request per user, resource, slot and date; position is deterministic by `(Priority ASC, BookingId ASC)` |
| Caller | Reference implementation; the live path is Team A's raw JDBC insert |

**5. `PROMOTE_WAITLIST_TRANSACTION(resourceId, slotId, bookingDate)`**

| Item | Detail |
|---|---|
| Signature | Takes resource, slot and booking date; returns the promoted booking ID and winner user ID |
| Steps | 1. Start the transaction. 2. `SELECT` the highest-priority waiter with `FOR UPDATE`. 3. If none, `COMMIT` and return a null result. 4. `UPDATE` status to `Confirmed` and clear `WaitlistPosition`. 5. Insert a promotion notification. 6. Insert an audit row. 7. Commit. |
| Business rules | Highest priority first, then lowest BookingId; one promotion per call |
| Caller | Reference; live path is Team A's `/promote` |

**6. `UPDATE_USER_TRANSACTION(userId, role, status)`**

| Item | Detail |
|---|---|
| Signature | Takes user ID, new role (nullable), new status (nullable) |
| Steps | 1. Start the transaction. 2. Reject if the user does not exist. 3. `UPDATE` role and status using `COALESCE` so nulls leave the existing value. 4. Insert an audit row. 5. Commit. |
| Business rules | Role must pass the CHECK constraint |
| Caller | BFF `PUT /api/admin/users/:id` |

**7. `DELETE_USER_TRANSACTION(userId)`**

| Item | Detail |
|---|---|
| Signature | Takes user ID |
| Steps | 1. Start the transaction. 2. Reject if the user does not exist. 3. `DELETE` the user. 4. Insert an audit row with `OldValue='deleted'`. 5. Commit. |
| Business rules | Deleting a user with active bookings is rejected by the FK (RESTRICT); notifications cascade |
| Caller | BFF `DELETE /api/admin/users/:id` |

**8. `RESET_PASSWORD_TRANSACTION(userId, newPasswordHash)`**

| Item | Detail |
|---|---|
| Signature | Takes user ID and a pre-hashed new password |
| Steps | 1. Start the transaction. 2. Reject if the user does not exist. 3. `UPDATE` the password. 4. Insert an audit row. 5. Commit. |
| Business rules | Password hashing is done by the BFF (bcryptjs); the procedure stores the hash |
| Caller | BFF `POST /api/admin/users/:id/reset-password` |

### 8.7 ACID and Concurrency Design

**Atomicity.** Each stored procedure runs inside one transaction. If any statement or `SIGNAL SQLSTATE` fails, the whole unit rolls back, so a cancellation can never leave a booking cancelled without its audit row.

**Consistency.** Primary keys, foreign keys, unique constraints and CHECK constraints keep the data valid regardless of which service writes. A booking with an unknown user, resource or slot, an invalid status or a role outside the allowed set is rejected by the database itself (KPI-S5, KPI-S6).

**Isolation.** InnoDB's default isolation level combined with `SELECT ... FOR UPDATE` serializes competing writers on the same booking or slot. When two concurrent `CREATE_BOOKING_TRANSACTION` calls target the same resource, slot and date, exactly one is confirmed (KPI K3.2).

**Durability.** InnoDB writes committed transactions to its redo log, so a confirmed booking survives a server restart.

| Hardening opportunity | Why it matters |
|---|---|
| Route all booking creation through the stored procedure | The live path inserts from Team A's Java code; one transactional path would remove duplicated rules |
| Add a unique index guarding Confirmed bookings per resource, slot and date | Gives a database-level guarantee even if application code is bypassed |
| Use lowercase table names everywhere | Avoids the Linux case-sensitivity failure described in Section 16.3 |
| Hash passwords at rest | Seed data stores plaintext (Section 17) |
| Add a dedicated least-privilege DB user | Services currently connect with `root` in development |

### 8.8 Testing Evidence

**Normal path**

| ID | Scenario | Expected result |
|---|---|---|
| C-N1 | Create a booking through `CREATE_BOOKING_TRANSACTION` | One `bookings` row and one `audit_log` row |
| C-N2 | Cancel with `CANCEL_BOOKING_TRANSACTION` | Status `Cancelled`, one audit row, one notification row |
| C-N3 | Add a user with `ADD_USER_TRANSACTION` | One `users` row, one audit row |

**Boundary**

| ID | Scenario | Expected result |
|---|---|---|
| C-B1 | Booking on the first and last `time_slot` rows | Accepted |
| C-B2 | Capacity exactly 1 | Accepted; capacity 0 rejected |
| C-B3 | Waitlist position of the first waiter | `WaitlistPosition` = 1 |

**Negative and integrity**

| ID | Scenario | Expected result |
|---|---|---|
| C-X1 | Insert a booking with an unknown user, resource or slot (KPI-S5) | Rejected by foreign key |
| C-X2 | Insert an invalid role, invalid status or capacity 0 (KPI-S6) | Rejected by CHECK |
| C-X3 | Cancel an already-cancelled booking | Procedure error, no extra rows |
| C-X4 | Delete a user who has notifications | Notifications removed by `ON DELETE CASCADE`; audit rows remain |

**Concurrency**

| ID | Scenario | Expected result |
|---|---|---|
| C-K1 | Two concurrent `CREATE_BOOKING_TRANSACTION` calls for the same resource, slot and date (KPI K3.2) | Exactly one Confirmed |
| C-K2 | 100 random booking, cancel and promote operations (KPI K3.3) | Exactly one `audit_log` row per state change |

### 8.9 Migration and Seeding

Two SQL files build the database.

| File | Purpose |
|---|---|
| `db/smartcampus_dump.sql` | Creates the schema, the stored procedures and the seed data: departments, resource types, locations, resources, time slots, users and sample bookings |
| `db/phase1_migration.sql` | Applies later changes to a database that was already loaded from the dump: added columns, constraints, notification and audit structures, and any seed corrections |

**Idempotency.** `phase1_migration.sql` is safe to run more than once. An idempotent migration guards each change so a second run does nothing, with patterns such as `CREATE TABLE IF NOT EXISTS`, `DROP PROCEDURE IF EXISTS` before `CREATE PROCEDURE`, an `INFORMATION_SCHEMA` check before `ALTER TABLE ... ADD COLUMN`, and `INSERT IGNORE` or `ON DUPLICATE KEY UPDATE` for seed rows. Confirm which of these patterns the file actually uses. The standard setup loads the dump first and then the migration (Section 16).

> **Note:** The setup commands in Section 16 run `DROP DATABASE IF EXISTS smartcampus;` before loading the dump, so following them on an existing environment erases all data in that schema.

---

## 9. Engine 4 — Web Technologies (Team D)

### 9.1 Overview and Responsibility

Team D owns everything the user touches and everything that connects the engines: the **React frontend** and the **Node.js Backend-for-Frontend (BFF)**. The BFF authenticates users, enforces role-based access, translates frontend requests into Team A, Team B and MySQL calls, and streams live notifications to the browser.

**Course link:** single-page applications, client-side routing, REST and BFF design, JWT authentication, RBAC, Server-Sent Events and accessible UI.

| Direction | Item | Description |
|---|---|---|
| Input | Browser requests on `/api/*` and `/api/stream` | From the React app, with a JWT |
| Output | JSON responses and SSE events | Resource lists, booking decisions, search results, notifications |
| Calls out to | Team A, Team B, MySQL | HTTP/JSON and `mysql2` |

### 9.2 Engine Architecture

```text
+-----------------------------------------------+
| Browser                                       |
|  React 19 SPA (Vite, Tailwind, React Router)  |
|  NotificationBell + useSSE                    |
+----------------------+------------------------+
                       |  /api/*  (JSON)   /api/stream (SSE)
                       v
+-----------------------------------------------+
| BFF  Node.js 20 + Express 5     port 8080     |
|  cors -> json -> auth/RBAC -> routes          |
|  nodeMap cache   SSE poller (every 3 s)       |
+--------+-----------------+---------------+----+
         |                 |               |
         | HTTP/JSON       | HTTP/JSON     | mysql2
         v                 v               v
+---------------+  +---------------+  +---------------+
| Team A :8090  |  | Team B :8091  |  | MySQL :3306   |
| scheduling    |  | routing       |  | smartcampus   |
+---------------+  +---------------+  +---------------+
```

### 9.3 Frontend Pages

| Group | Route | Purpose |
|---|---|---|
| Public | `/login` | Role, user ID and password form; stores the JWT on success |
| Student / Faculty | `/dashboard` | Department overview with resource counts and quick actions |
| Student / Faculty | `/dashboard/department/:deptId` | Resource categories (Lab, Classroom, Conference Hall) per department |
| Student / Faculty | `/dashboard/department/:deptId/:typeId` | Resources of a given type with date and slot picker |
| Student / Faculty | `/dashboard/book/:resourceId` | Booking form: reason, notes, priority preview |
| Student / Faculty | `/dashboard/bookings` | The user's bookings with status tabs; supports `?status=` and `?id=` highlighting |
| Student / Faculty | `/dashboard/search` | Search form: date, slot, type, capacity, source node |
| Student / Faculty | `/dashboard/search-results` | Distance-sorted results with path per resource |
| Admin | `/admin` | Totals for departments, resources, users and bookings; live resource status; recent bookings |
| Admin | `/admin/users` | List, add, edit, delete users; reset password |
| Admin | `/admin/reports` | 7 / 30 / 90 day booking analytics, CSV export, print view |
| Admin | `/admin/audit-logs` | Recent audit entries colour-coded by action |

**Shared components**

| Component / hook | Role |
|---|---|
| `NotificationBell` | Shows the unread badge, lists notifications, prepends new ones live, marks read |
| `useSSE` | Opens `EventSource` on `/api/stream?userId=X`, parses `notification` events, reconnects after 3 seconds on error |

### 9.4 BFF — Routes, Files, Responsibility

**Files**

| File | Responsibility |
|---|---|
| Server entry (for example `bff/server.js`) | Loads `.env`, creates the Express app, registers middleware and routers, connects to MySQL, starts listening on `PORT` |
| DB module | Creates the `mysql2` connection pool from the `DB_*` variables |
| `bff/routes/auth.js` | Login and JWT issuance |
| `bff/routes/admin.js` | Admin statistics, users, reports, audit logs; bcrypt for password add and reset |
| `bff/sse.js` | Polls `notifications` every 3 seconds per connected user and pushes events |
| Remaining route files | Departments, resources, campus nodes, bookings, search, notifications |

**Routes**

| Area | Endpoints |
|---|---|
| Health | `GET /api/health` |
| Auth | `POST /api/auth/login` |
| Catalogue | `GET /api/departments`, `GET /api/departments/:deptId/resource-types`, `GET /api/resources`, `GET /api/resources/:id`, `GET /api/campus-nodes` |
| Bookings | `POST /api/bookings`, `GET /api/bookings`, `POST /api/bookings/:id/cancel` |
| Search | `POST /api/search` |
| Notifications | `GET /api/notifications`, `GET /api/notifications/unread-count`, `POST /api/notifications/:id/read`, `POST /api/notifications/read-all` |
| Admin | `/api/admin/stats`, `/resources/status`, `/reports`, `/users` (GET, POST, PUT, DELETE), `/users/:id/reset-password`, `/audit-logs` |
| Stream | `GET /api/stream` |

**Middleware**

| Order | Middleware | Purpose |
|---|---|---|
| 1 | `cors` | Allows the origin in `CORS_ORIGIN` |
| 2 | JSON body parser | Parses `application/json` |
| 3 | Authentication / role check | Verifies the caller; admin routes require `X-Role: Admin` or `?role=Admin` |
| 4 | Route handlers | Business orchestration |
| 5 | Error handler | Returns `{ "message": "..." }` with the right status |

**Startup sequence**

1. Load `bff/.env` with `dotenv`.
2. Create the MySQL connection and verify it (log `MySQL connected to smartcampus`).
3. Build the `nodeMap` cache that maps `ResourceId` to campus node ID.
4. Register middleware and routers.
5. Listen on `PORT` (8080) and log the health-check URL.
6. Start the SSE poller when the first client connects to `/api/stream`.

### 9.5 RBAC and Authentication

**Current model**

1. The user submits role, user ID and password to `POST /api/auth/login`.
2. The BFF validates the body (400 on missing fields, invalid role or non-numeric user ID).
3. It loads the user from `users` (401 if not found).
4. It compares the submitted password with the stored value (401 on mismatch).
5. It checks that the account role matches the submitted role (401) and that the account is active (403).
6. It signs a JWT with `JWT_SECRET` that expires after `JWT_EXPIRY` and returns the token and user object.
7. The frontend stores the token, sends it on later requests and uses the role to guard routes; admin API routes additionally require the `X-Role: Admin` header.

**Known limitations**

| Limitation | Impact | Recommended fix |
|---|---|---|
| Demo passwords are plaintext and compared directly | Database leak exposes passwords | Store bcrypt hashes and compare with `bcrypt.compare` |
| Admin check relies on a client-supplied `X-Role` header | Any client can claim the Admin role | Derive the role from the verified JWT claims only |
| Team A and Team B are unauthenticated | Anyone who can reach ports 8090 or 8091 can call them | Keep them on a private network or add a shared service token |
| Token stored in browser storage | Exposed to script injection | Use an HttpOnly cookie or short-lived tokens |

**RBAC matrix**

| Capability | Student | Faculty | Admin |
|---|---|---|---|
| Log in | Yes | Yes | Yes |
| Browse departments and resources | Yes | Yes | Yes |
| Book, cancel own booking, view own bookings | Yes | Yes | Yes |
| Distance-sorted search | Yes | Yes | Yes |
| Receive live notifications | Yes | Yes | Yes |
| Admin stats, reports, resource status | No | No | Yes |
| Manage users, reset passwords | No | No | Yes |
| View audit logs | No | No | Yes |

### 9.6 SSE — Live Notifications

**Flow**

1. The user logs in and `useSSE` opens `GET /api/stream?userId=X`.
2. The BFF replies with `Content-Type: text/event-stream` and sends an `event: hello`.
3. The BFF remembers the highest `NotificationId` already delivered to that user.
4. Every 3 seconds the poller queries `notifications` for rows with a higher id for that user.
5. When a waitlisted booking is promoted, Team A inserts a notification row.
6. The next poll finds the row.
7. The BFF pushes an `event: notification` with the new rows.
8. `NotificationBell` prepends the item and increases the badge (KPI K4.1, under 5 seconds).

**Why server-side polling.** The database is the source of truth for notifications, and the writers (stored procedures and Team A) are separate processes. Polling the table from the BFF requires no changes to those writers and no message broker, and a 3-second interval meets the 5-second target while the query stays an indexed lookup per connected user.

> **Note:** Reverse proxies can hold back SSE data. The BFF sets `X-Accel-Buffering: no` on the stream, and any Nginx in front must use `proxy_buffering off` (Section 16.4), otherwise notifications arrive late or in bursts.

### 9.7 Accessibility and UX

| Area | Practice |
|---|---|
| ARIA roles | Landmark roles for navigation and main content; `role="alert"` for error messages; `role="status"` for loading and live notification updates |
| ARIA attributes | `aria-label` on icon-only buttons (including the notification bell), `aria-live` on the unread badge, `aria-expanded` on the notification panel |
| Keyboard focus | Every interactive element has a visible focus ring (`focus-visible`) and is reachable by Tab; target is 100% (KPI K4.3) |
| Semantic HTML | `button` for actions, `a` for navigation, `table`, `th` and `caption` for tabular data, headings in order |
| Error states | Inline, readable messages such as "Couldn't load departments", with the BFF `message` shown for rejected bookings |
| Loading states | Visible loading indicators while requests are pending; buttons disabled during submit |
| Forms | Each input has an associated `label`; required fields are marked and validated before submit |

### 9.8 Class/Component Reference

**Frontend entry files**

| File | Role |
|---|---|
| `src/main.jsx` | Mounts `<App />` inside `<BrowserRouter>` and `<React.StrictMode>` |
| `src/App.jsx` | Route table; reads the user from `localStorage`; `RequireUser` guard for role-gated routes; login and logout handlers |
| `vite.config.js` | Vite + React plugin and `/api` dev proxy |
| `package.json` (root) | Frontend dependencies and scripts (`dev`, `build`, `lint`, `preview`) |

**Components and hooks**

| Name | File | Role |
|---|---|---|
| `NotificationBell` | `src/components/NotificationBell.jsx` | Unread badge, notification list, "mark all read" |
| `useSSE` | `src/hooks/useSSE.js` | SSE subscription with auto-reconnect every 3 seconds |
| Page components | `src/pages/*.jsx` | Login, Dashboard, Department, ResourceList, BookingForm, BookingStatus, SearchResources, SearchResults, AdminDashboard, AdminUsers, AdminReports, AdminAuditLogs |

**BFF files**

| File | Role |
|---|---|
| `bff/server.js` | Express bootstrap; registers routes and `/api/health`; tests DB on startup |
| `bff/db.js` | `mysql2/promise` pool (10 connections); exports `pool` and `testConnection()` |
| `bff/engines.js` | HTTP clients for Team A (`/schedule`, `/promote`) and Team B (`/search`); `priorityFromReason` mapping |
| `bff/sse.js` | Registers `GET /api/stream`; 3 s notification poller |
| `bff/utils/nodeMap.js` | ResourceId → campus NodeId cache (60 s TTL) |
| `bff/routes/auth.js` | Login and JWT |
| `bff/routes/admin.js` | Admin users, reset password, audit logs |
| `bff/routes/admin-stats.js` | Admin stats, resource status, reports |
| `bff/routes/bookings.js` | Create (proxies Team A), list, cancel + promote |
| `bff/routes/search.js` | Proxy to Team B `/search` |
| `bff/routes/departments.js` | Department list with resource counts |
| `bff/routes/resource-types.js` | The 3 types per department with counts |
| `bff/routes/resources.js` | Resource list with availability annotation; single resource |
| `bff/routes/campus-nodes.js` | Campus node list with static fallback |
| `bff/routes/notifications.js` | List, unread count, mark read |

### 9.9 Testing Evidence

**Normal path**

| ID | Scenario | Expected result |
|---|---|---|
| D-N1 | Log in as Student 2403163 | 200 with a token; dashboard loads (KPI K4.2 under 2 s) |
| D-N2 | Book a free resource | 201 `Confirmed`; redirected to the bookings page with the new row highlighted |
| D-N3 | Book an occupied resource as a second user | 201 `Waitlisted` with position 1 |
| D-N4 | Cancel and promote | Waiter becomes Confirmed; bell receives the notification (KPI K4.1) |
| D-N5 | Distance-sorted search | Results ascending by distance with a path per row |

**Boundary**

| ID | Scenario | Expected result |
|---|---|---|
| D-B1 | Search with `minCapacity` 0 or omitted | All matching resources returned |
| D-B2 | Notification poll with no new rows | No event sent; connection stays open |
| D-B3 | SSE connection drops | Client reconnects after 3 seconds |

**Negative**

| ID | Scenario | Expected result |
|---|---|---|
| D-X1 | Login with a wrong password | 401 |
| D-X2 | Login with a role that does not match the account | 401 |
| D-X3 | Login to a non-active account | 403 |
| D-X4 | Admin route without the Admin role | 403 |
| D-X5 | Team A or Team B down | BFF returns 502 |
| D-X6 | Booking the same slot twice as the same user | 409 `Rejected` |

### 9.10 Known Integration Risks

| Risk | Detail | Mitigation |
|---|---|---|
| Hardcoded engine ports | Team A (8090) and Team B (8091) ignore `$PORT` | Read `PORT` with a default (Section 5.2) |
| Uppercase SQL table names | Queries use `USERS`, `BOOKINGS` and so on; they fail on case-sensitive Linux MySQL | Set `lower_case_table_names=1` or lowercase every query (Section 16.3) |
| `X-Role` header trust | Admin access can be claimed by any client | Derive role from the verified JWT |
| CORS configuration | `CORS_ORIGIN` must match the frontend origin exactly | Set it per environment |
| Relative `/api` paths | Built frontend breaks without a reverse proxy | Add an Nginx proxy or use `VITE_API_BASE_URL` (Section 16.4) |
| `graphVersion` reset | Team B's version string is tied to the running process; a restart can change it, invalidating cached routes | Persist or derive the version from the edge data |
| `campus_edge` source of truth | Team B builds the graph in code while `campus_edge` holds the same data in MySQL | Pick one source and generate the other from it |

---

## 10. End-to-End Workflows

This section walks through the four critical flows that exercise all four engines together. Each flow is written as a sequence with numbered steps, showing exactly which engine does what at each step. This is the section a reviewer reads to understand how the four engines cooperate.

---

### 10.1 Workflow 1 — Booking a Free Resource

**Scenario:** Student `2403163` opens the dashboard, drills into CSE → Labs, picks `CSE Lab 1`, selects date `2026-10-20` and slot `10:00–11:00`, and submits a booking.

**Engines exercised:** Team D (frontend + BFF), Team A (scheduling), Team C (persistence).

```text
┌────────────┐   ┌────────────┐   ┌────────────┐   ┌────────────┐
│  Frontend  │   │    BFF     │   │   Team A   │   │   MySQL    │
│   :5173    │   │   :8080    │   │   :8090    │   │   :3306    │
└─────┬──────┘   └─────┬──────┘   └─────┬──────┘   └─────┬──────┘
      │                │                │                │
      │ 1. GET /api/resources?deptId=A1&typeId=1&date=...
      │───────────────►│                │                │
      │                │ 2. SELECT resources + availability
      │                │────────────────────────────────►│
      │                │◄────────────────────────────────│
      │◄───────────────│                │                │
      │                │                │                │
      │ 3. GET /api/resources/1         │                │
      │───────────────►│                │                │
      │                │ 4. SELECT single resource       │
      │                │────────────────────────────────►│
      │                │◄────────────────────────────────│
      │◄───────────────│                │                │
      │                │                │                │
      │ 5. POST /api/bookings           │                │
      │   { userId, resourceId:1, date, startTime,       │
      │     endTime, reasonId, reasonLabel, notes }      │
      │───────────────►│                │                │
      │                │ 6. resolve NodeId for ResourceId=1
      │                │    (from nodeMap cache)         │
      │                │ 7. map reason → priority        │
      │                │ 8. POST /schedule               │
      │                │   { userId, labId:"L1", date,   │
      │                │     startTime, endTime, purpose,│
      │                │     priority }                  │
      │                │───────────────►│                │
      │                │                │ 9. SELECT ResourceId from RESOURCES
      │                │                │    JOIN LOCATION
      │                │                │───────────────►│
      │                │                │◄───────────────│
      │                │                │ 10. SELECT SlotId from TIME_SLOT
      │                │                │───────────────►│
      │                │                │◄───────────────│
      │                │                │ 11. SELECT count from BOOKINGS (duplicate)
      │                │                │───────────────►│
      │                │                │◄───────────────│
      │                │                │ 12. SELECT count from BOOKINGS (occupied?)
      │                │                │───────────────►│
      │                │                │◄───────────────│
      │                │                │ 13. INSERT INTO BOOKINGS (Confirmed)
      │                │                │───────────────►│
      │                │                │◄───────────────│
      │                │                │ 14. INSERT INTO AUDIT_LOG
      │                │                │───────────────►│
      │                │                │◄───────────────│
      │                │◄───────────────│                │
      │                │ 201 { bookingId:31, status:"Confirmed", message }
      │◄───────────────│                │                │
      │                │                │                │
      │ 15. navigate /dashboard/bookings?status=confirmed&id=31
      │◄───────────────│                │                │
      │                │                │                │
      │ 16. GET /api/bookings?userId=2403163
      │───────────────►│                │                │
      │                │ 17. SELECT bookings + resources + slots
      │                │────────────────────────────────►│
      │                │◄────────────────────────────────│
      │◄───────────────│                │                │
      │ 18. render list with new booking highlighted
      │                │                │                │
```

**What each engine contributed:**

| Engine | Contribution |
|---|---|
| Team D (frontend) | Rendered resource list, loaded single-resource detail, submitted the booking form |
| Team D (BFF) | Resolved `ResourceId → labId` via `nodeMap`, mapped reason → priority, called Team A, returned the decision |
| Team A | Resolved `labId → ResourceId`, resolved `(startTime, endTime) → SlotId`, checked for duplicates, checked availability, inserted Confirmed booking, inserted audit row |
| Team C | Provided the tables; enforced FKs and CHECK constraints on the insert |
| Team B | Not used in this flow (routing only matters for search) |

**State change on completion:** one new row in `bookings` (status = Confirmed), one new row in `audit_log`.

---

### 10.2 Workflow 2 — Booking an Occupied Resource (Waitlist)

**Scenario:** Same slot as Workflow 1, but a second user `2403173` tries to book `CSE Lab 1`.

**Engines exercised:** Team D, Team A, Team C.

```text
      │ 1-8. same as Workflow 1 (GET resources, GET resource, POST /api/bookings)
      │                │                │                │
      │                │ 9-12. same as Workflow 1 (resolve resource, slot, duplicate check, occupied check)
      │                │                │                │
      │                │                │ 13. SELECT count from BOOKINGS (occupied?)
      │                │                │───────────────►│
      │                │                │◄───────────────│  count = 1 (occupied)
      │                │                │                │
      │                │                │ 14. INSERT INTO BOOKINGS (Waitlisted, priority)
      │                │                │───────────────►│
      │                │                │◄───────────────│  new bookingId=32
      │                │                │ 15. SELECT count from BOOKINGS (waiters ahead)
      │                │                │───────────────►│
      │                │                │◄───────────────│  position = 1
      │                │                │ 16. UPDATE BOOKINGS SET WaitlistPosition=1
      │                │                │───────────────►│
      │                │                │ 17. INSERT INTO AUDIT_LOG
      │                │                │───────────────►│
      │                │◄───────────────│                │
      │                │ 201 { bookingId:32, status:"Waitlisted", waitlistPosition:1 }
      │◄───────────────│                │                │
      │ 18. navigate /dashboard/bookings?status=waitlisted&id=32
      │◄───────────────│                │                │
```

**State change on completion:** one new row in `bookings` (status = Waitlisted, WaitlistPosition = 1), one new row in `audit_log`.

---

### 10.3 Workflow 3 — Cancellation + Waitlist Promotion

**Scenario:** User `2403163` cancels their Confirmed booking `#31` for `CSE Lab 1` at `2026-10-20 10:00–11:00`. User `2403173` is waitlisted at position 1 for the same slot.

**Engines exercised:** Team D, Team A, Team C, Team D (SSE).

```text
┌────────────┐   ┌────────────┐   ┌────────────┐   ┌────────────┐
│  Frontend  │   │    BFF     │   │   Team A   │   │   MySQL    │
└─────┬──────┘   └─────┬──────┘   └─────┬──────┘   └─────┬──────┘
      │                │                │                │
      │ 1. POST /api/bookings/31/cancel │                │
      │───────────────►│                │                │
      │                │ 2. SELECT booking 31 (get resource, slot, date, times)
      │                │────────────────────────────────►│
      │                │◄────────────────────────────────│
      │                │ 3. CALL CANCEL_BOOKING_TRANSACTION(31)
      │                │────────────────────────────────►│
      │                │                │   ┌─── within transaction ───┐
      │                │                │   │ SELECT Status, UserId    │
      │                │                │   │ FROM BOOKINGS            │
      │                │                │   │ WHERE BookingId=31       │
      │                │                │   │ FOR UPDATE               │
      │                │                │   │ UPDATE Status='Cancelled'│
      │                │                │   │ INSERT INTO AUDIT_LOG    │
      │                │                │   │ INSERT INTO NOTIFICATIONS│
      │                │                │   │   (booking cancelled)    │
      │                │                │   │ COMMIT                   │
      │                │                │   └──────────────────────────┘
      │                │◄────────────────────────────────│
      │                │ 4. POST /promote               │
      │                │   { labId:"L1", date:"2026-10-20",
      │                │     startTime:"10:00", endTime:"11:00" }
      │                │───────────────►│                │
      │                │                │ 5. SELECT ResourceId from RESOURCES
      │                │                │    JOIN LOCATION
      │                │                │───────────────►│
      │                │                │◄───────────────│
      │                │                │ 6. SELECT SlotId from TIME_SLOT
      │                │                │───────────────►│
      │                │                │◄───────────────│
      │                │                │ 7. SELECT BookingId from BOOKINGS
      │                │                │    WHERE Status='Waitlisted'
      │                │                │    ORDER BY Priority ASC, BookingId ASC
      │                │                │    LIMIT 1
      │                │                │───────────────►│
      │                │                │◄───────────────│  next = 32
      │                │                │ 8. SELECT UserId from BOOKINGS (winner)
      │                │                │───────────────►│
      │                │                │◄───────────────│  winner = 2403173
      │                │                │ 9. UPDATE BOOKINGS SET Status='Confirmed',
      │                │                │    WaitlistPosition=NULL WHERE BookingId=32
      │                │                │───────────────►│
      │                │                │ 10. INSERT INTO NOTIFICATIONS
      │                │                │     (winner, 32, "Good news! ...")
      │                │                │───────────────►│
      │                │                │ 11. INSERT INTO AUDIT_LOG
      │                │                │───────────────►│
      │                │◄───────────────│                │
      │                │ 200 { promoted:true, bookingId:32, winnerUserId:2403173 }
      │◄───────────────│                │                │
      │                │                │                │
      │ 12. render confirmation: row 31 = Cancelled
      │                │                │                │
      │=== SSE poller on BFF (every 3 s) ===             │
      │                │ 13. SELECT NotificationId, BookingId, Message
      │                │     FROM NOTIFICATIONS
      │                │     WHERE UserId=2403173
      │                │       AND NotificationId > lastSeen
      │                │────────────────────────────────►│
      │                │◄────────────────────────────────│
      │                │ 14. push event: notification   │
      │◄───────────────│   data: [{notificationId, ...}]│
      │ 15. NotificationBell prepends; badge count +1
      │                │                │                │
```

**What each engine contributed:**

| Engine | Contribution |
|---|---|
| Team D (frontend) | Sent the cancel request, rendered the updated booking list, subscribed to SSE and rendered the live notification |
| Team D (BFF) | Called `CANCEL_BOOKING_TRANSACTION`, then called Team A `/promote`, then polled `notifications` and pushed to the browser over SSE |
| Team A | Resolved resource and slot, picked the highest-priority waiter, updated status to Confirmed, inserted the promotion notification, inserted the audit row |
| Team C | Ran the cancel transaction under `FOR UPDATE`, inserted the cancellation notification and audit row, made the notification visible to the SSE poller |

**State changes on completion:** one row in `bookings` becomes Cancelled; one row in `bookings` becomes Confirmed (WaitlistPosition NULL); two rows in `notifications` (one for the cancellation, one for the promotion); two rows in `audit_log`.

**Why this flow is the strongest demonstration of the four-engine design:** it touches every engine. Team D drives the UI and orchestrates; Team A applies the OS scheduling rule; Team C guarantees ACID; Team D's SSE delivers the outcome live. This is the flow to show first in the review demo.

---

### 10.4 Workflow 4 — Distance-Sorted Search

**Scenario:** User `2403163` opens Search, picks date `2026-10-20`, slot `10:00–11:00`, type `Lab`, min capacity `30`, source node `N1` (Main Gate). The system returns all available labs on that slot, sorted by walking distance from N1.

**Engines exercised:** Team D, Team B, Team C.

```text
┌────────────┐   ┌────────────┐   ┌────────────┐   ┌────────────┐
│  Frontend  │   │    BFF     │   │   Team B   │   │   MySQL    │
└─────┬──────┘   └─────┬──────┘   └─────┬──────┘   └─────┬──────┘
      │                │                │                │
      │ 1. GET /api/campus-nodes        │                │
      │───────────────►│                │                │
      │                │ 2. SELECT NodeId, NodeName FROM CAMPUS_NODE
      │                │────────────────────────────────►│
      │                │◄────────────────────────────────│
      │◄───────────────│                │                │
      │ 3. user picks N1, fills form, clicks Search
      │                │                │                │
      │ 4. POST /api/search             │                │
      │   { date, startTime, endTime, resourceType:"Lab",│
      │     minCapacity:30, sourceNodeId:"N1" }          │
      │───────────────►│                │                │
      │                │ 5. proxy POST /search           │
      │                │───────────────►│                │
      │                │                │ 6. SELECT SlotId from TIME_SLOT
      │                │                │───────────────►│
      │                │                │◄───────────────│  slotId=2
      │                │                │ 7. SELECT resources
      │                │                │    JOIN department
      │                │                │    JOIN resource_type
      │                │                │    JOIN location
      │                │                │    WHERE status='Available'
      │                │                │      AND type='Lab'
      │                │                │      AND capacity >= 30
      │                │                │      AND ResourceId NOT IN
      │                │                │        (SELECT ResourceId FROM BOOKINGS
      │                │                │         WHERE SlotId=2 AND BookingDate=...
      │                │                │           AND Status IN ('Pending','Confirmed'))
      │                │                │───────────────►│
      │                │                │◄───────────────│
      │                │                │  rows = [ {resourceId:1,  nodeId:"L1",  ...},
      │                │                │           {resourceId:2,  nodeId:"L2",  ...},
      │                │                │           {resourceId:9,  nodeId:"EL1", ...},
      │                │                │           {resourceId:14, nodeId:"AL1", ...},
      │                │                │           {resourceId:20, nodeId:"ML1", ...} ]
      │                │                │                │
      │                │                │ 8. For each row, call Dijkstra(N1, nodeId)
      │                │                │    L1  → distance=65,  latency=13
      │                │                │    L2  → distance=70,  latency=14
      │                │                │    EL1 → distance=180, latency=32
      │                │                │    AL1 → distance=165, latency=30
      │                │                │    ML1 → distance=140, latency=25
      │                │                │ 9. sort by distance ascending
      │                │◄───────────────│                │
      │                │  [{resourceId:1,...distance:65,path:["N1","N4","L1"]},
      │                │   {resourceId:2,...distance:70,...},
      │                │   {resourceId:20,...distance:140,...},
      │                │   {resourceId:14,...distance:165,...},
      │                │   {resourceId:9,...distance:180,...}]
      │◄───────────────│                │                │
      │                │                │                │
      │ 10. store in sessionStorage; navigate /dashboard/search-results
      │                │                │                │
      │ 11. render list sorted by distance, showing path per resource
      │                │                │                │
```

**What each engine contributed:**

| Engine | Contribution |
|---|---|
| Team D (frontend) | Search form, campus node dropdown, results page |
| Team D (BFF) | Proxied the search request to Team B; returned the array to the frontend |
| Team B | Resolved the slot, filtered available labs via DB, ran Dijkstra for each candidate from source to destination, sorted by distance, enriched each row with `distance`, `latency`, `path` |
| Team C | Provided `TIME_SLOT`, `RESOURCES`, `LOCATION`, `DEPARTMENT`, `RESOURCE_TYPE`, `BOOKINGS` for filtering |
| Team A | Not used in this flow |

**Why the search flow matters:** it demonstrates that Team B's DSA engine is not just a theoretical exercise — it is queried on every search, once per available resource, and the distances it produces directly drive the sort order the student sees.

---

### 10.5 Cross-Engine Interaction Summary

| Flow | Team A | Team B | Team C | Team D |
|---|---|---|---|---|
| Booking free resource | Schedule | — | Persist + Audit | Frontend + BFF |
| Booking occupied (waitlist) | Waitlist + position | — | Persist + Audit | Frontend + BFF |
| Cancel + promote | Promote | — | Cancel txn + Notify + Audit | Frontend + BFF + SSE |
| Distance-sorted search | — | Dijkstra per resource | Filter resources | Frontend + BFF |

Every flow touches **at least three engines**. Workflow 3 touches **all four**.

---

## 11. Data Model

### 11.1 Entity Relationships (Textual ER)

```text
┌───────────────┐
│  department   │
│ PK DepartmentId
│ UQ DepartmentName
└──────┬────────┘
       │ 1
       │
       │ N
┌──────▼──────────┐         ┌───────────────┐
│   resources     │ N     1 │   location    │
│ PK ResourceId   ├─────────┤ PK LocationId │
│ FK DepartmentId │         │    RoomNumber │  ← also serves as
│ FK LocationId   │         └───────────────┘    campus node ID
│ FK TypeId       │
│    Capacity     │ N     1 ┌───────────────┐
│    Status       ├─────────┤ resource_type │
└──────┬──────────┘         │ PK TypeId     │
       │ 1                  │ UQ TypeName   │
       │                    └───────────────┘
       │ N
┌──────▼────────┐
│   bookings    │
│ PK BookingId  │
│ FK UserId     │ N     1 ┌───────────────┐
│ FK ResourceId ├─────────┤    users      │
│ FK SlotId     │         │ PK UserId     │
│    BookingDate│         │    Role       │
│    Status     │         │    Status     │
│    Priority   │         └───────────────┘
│    WaitlistPos│
└──────┬────────┘
       │ N     1 ┌───────────────┐
       ├─────────┤  time_slot    │
       │         │ PK SlotId     │
       │         │    StartTime  │
       │         │    EndTime    │
       │         └───────────────┘
       │
       │ 1     0..N ┌───────────────────┐
       └────────────┤  notifications    │
                    │ PK NotificationId │
                    │ FK UserId         │
                    │ FK BookingId      │
                    │    Message, IsRead│
                    └───────────────────┘

┌───────────────┐
│  campus_node  │
│ PK NodeId     │
│    NodeName   │
│    NodeType   │
└──────┬────────┘
       │ 1
       │
       │ N
┌──────▼────────┐
│  campus_edge  │
│ PK EdgeId     │
│ FK FromNodeId │
│ FK ToNodeId   │
│    Distance   │
│    Latency    │
└───────────────┘

┌───────────────────┐
│    audit_log      │
│ PK AuditId        │
│    UserId         │  ← not FK (audit survives user deletion)
│    Action         │
│    EntityType     │
│    EntityId       │
│    OldValue       │
│    NewValue       │
│    CreatedAt      │
└───────────────────┘
```

### 11.2 Key Design Decisions

| Decision | Rationale |
|---|---|
| `booking.bookingDate` is a `DATE` column separate from `time_slot` | `time_slot` is a **template** (8 slots per day); the actual date a booking is for is a booking attribute |
| `bookings.Priority` is an `INT` (1 = highest, 3 = lowest) | Smaller value = higher priority is a convention that matches OS scheduling textbook style |
| `bookings.WaitlistPosition` is nullable | Only populated for `Waitlisted` rows; set to `NULL` on promotion |
| `bookings.Status` uses a CHECK constraint listing all 5 possible values | Prevents typos; a rejected booking literally cannot have an invalid status |
| `notifications.IsRead` is a `TINYINT(1)` | MySQL BOOLEAN is a TINYINT; using the underlying type explicitly avoids confusion |
| `location.RoomNumber` is also the campus node ID | A resource at room `L1` has a graph node `L1`; this pairing lets Team B compute distance to a resource without a join table |
| `audit_log.UserId` has **no** FK to `users` | If a user is deleted, the audit entry must survive — the audit trail is a historical record |
| `notifications.BookingId` uses `ON DELETE SET NULL` | If a booking is removed, the notification still exists but its link is cleared |
| `notifications.UserId` uses `ON DELETE CASCADE` | If a user is removed, their notifications are meaningless — delete them |

---

## 12. API Reference

Three HTTP services expose APIs:

- **BFF** (port 8080) — called by the frontend
- **Team A** (port 8090) — called by the BFF
- **Team B** (port 8091) — called by the BFF

All APIs use JSON bodies and `Content-Type: application/json`.

---

### 12.1 BFF API (`http://localhost:8080`)

**Base path:** `/api`

**Common error shape:**

```json
{ "message": "Human-readable description" }
```

#### 12.1.1 Health

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/health` | BFF liveness check |

Response:

```json
{
  "status": "ok",
  "service": "smart-campus-bff",
  "time": "2026-10-14T10:15:00.000Z"
}
```

#### 12.1.2 Auth

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/login` | Issue JWT |

Request:

```json
{
  "role": "Student",
  "userId": 2403163,
  "password": "2403163"
}
```

Response (200):

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "userId": 2403163,
    "role": "Student",
    "status": "Active"
  }
}
```

Errors:

- `400` — missing fields, invalid role, non-numeric userId
- `401` — user not found, wrong password, wrong role for the account
- `403` — account not active

#### 12.1.3 Departments

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/departments` | List all departments with resource counts |

Response:

```json
[
  { "departmentId": "A1", "departmentName": "CSE", "resourceCount": 8 },
  { "departmentId": "B1", "departmentName": "ELECTRICAL", "resourceCount": 5 },
  { "departmentId": "C1", "departmentName": "AIML", "resourceCount": 6 },
  { "departmentId": "D1", "departmentName": "MECHANICAL", "resourceCount": 4 }
]
```

#### 12.1.4 Resource Types

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/departments/:deptId/resource-types` | The 3 types per department with counts |

Response:

```json
[
  { "typeId": 1, "typeName": "Lab", "description": "Laboratory", "count": 4 },
  { "typeId": 2, "typeName": "Classroom", "description": "Classroom", "count": 3 },
  { "typeId": 3, "typeName": "Conference Hall", "description": "Conference Hall", "count": 1 }
]
```

#### 12.1.5 Resources

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/resources` | List resources with optional availability annotation |
| GET | `/api/resources/:id` | Single resource detail |

Query parameters for `GET /api/resources`:

- `deptId` (required) — e.g., `A1`
- `typeId` (required) — e.g., `1`
- `date`, `startTime`, `endTime` (optional) — if all three present, each resource gets `status: "Available" | "Occupied"`; if omitted, all are marked `Available`

Response (with availability):

```json
[
  {
    "resourceId": 1,
    "resourceName": "CSE Lab 1",
    "capacity": 50,
    "buildingName": "CSE Building",
    "roomNumber": "L1",
    "floorNumber": 1,
    "typeName": "Lab",
    "status": "Available"
  }
]
```

Response for `GET /api/resources/:id`:

```json
{
  "resourceId": 1,
  "resourceName": "CSE Lab 1",
  "capacity": 50,
  "status": "Available",
  "departmentId": "A1",
  "departmentName": "CSE",
  "buildingName": "CSE Building",
  "roomNumber": "L1",
  "floorNumber": 1,
  "typeId": 1,
  "typeName": "Lab"
}
```

#### 12.1.6 Campus Nodes

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/campus-nodes` | All 31 campus nodes for the source dropdown |

Response (abbreviated):

```json
[
  { "nodeId": "N1", "nodeName": "Main Gate", "nodeType": "Gate", "status": "Active" },
  { "nodeId": "N2", "nodeName": "Gate 1", "nodeType": "Gate", "status": "Active" },
  { "nodeId": "N3", "nodeName": "Library", "nodeType": "Library", "status": "Active" },
  { "nodeId": "N4", "nodeName": "CSE Department", "nodeType": "Department", "status": "Active" },
  { "nodeId": "L1", "nodeName": "CSE Lab 1", "nodeType": "Lab", "status": "Active" }
]
```

If the DB table is empty, the endpoint returns a static fallback list.

#### 12.1.7 Bookings

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/bookings` | Create booking (proxies Team A `/schedule`) |
| GET | `/api/bookings?userId=X` | List bookings for a user |
| POST | `/api/bookings/:id/cancel` | Cancel a booking and promote the next waiter |

Request — `POST /api/bookings`:

```json
{
  "userId": 2403163,
  "resourceId": 1,
  "date": "2026-10-20",
  "startTime": "10:00",
  "endTime": "11:00",
  "reasonId": "exam",
  "reasonLabel": "Exam / Assessment",
  "notes": "Mid-semester"
}
```

Response — Confirmed (201):

```json
{
  "bookingId": 31,
  "status": "Confirmed",
  "waitlistPosition": 0,
  "priority": 1,
  "message": "Booking confirmed",
  "labId": "L1",
  "resourceName": "CSE Lab 1"
}
```

Response — Waitlisted (201):

```json
{
  "bookingId": 32,
  "status": "Waitlisted",
  "waitlistPosition": 1,
  "priority": 1,
  "message": "Resource is occupied. You are waitlist #1",
  "labId": "L1",
  "resourceName": "CSE Lab 1"
}
```

Response — Rejected (409):

```json
{
  "message": "You already have a request for this resource and slot",
  "status": "Rejected"
}
```

Response — `GET /api/bookings?userId=2403163`:

```json
[
  {
    "bookingId": 31,
    "userId": 2403163,
    "resourceId": 1,
    "resourceName": "CSE Lab 1",
    "departmentName": "CSE",
    "buildingName": "CSE Building",
    "roomNumber": "L1",
    "date": "2026-10-20",
    "startTime": "10:00:00",
    "endTime": "11:00:00",
    "purpose": "Exam / Assessment — Mid-semester",
    "status": "Confirmed",
    "priority": 1,
    "waitlistPosition": null
  }
]
```

Response — `POST /api/bookings/:id/cancel`:

```json
{
  "bookingId": 31,
  "message": "Booking cancelled successfully",
  "promotion": {
    "promoted": true,
    "bookingId": 32,
    "winnerUserId": 2403173,
    "message": "Promoted booking #32"
  }
}
```

#### 12.1.8 Search

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/search` | Proxy to Team B `/search` |

Request: same as Team B `/search` (see Section 12.3.3).

Response: the array returned by Team B (see Section 12.3.3).

#### 12.1.9 Notifications

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/notifications?userId=X` | List 100 most recent notifications |
| GET | `/api/notifications/unread-count?userId=X` | Unread count |
| POST | `/api/notifications/:id/read` | Mark one as read |
| POST | `/api/notifications/read-all` | Mark all as read for a user |

Response — `GET /api/notifications`:

```json
[
  {
    "notificationId": 1,
    "userId": 2403173,
    "bookingId": 32,
    "message": "Good news! Your waitlisted booking #32 is now confirmed.",
    "isRead": 0,
    "createdAt": "2026-10-20 10:15:00"
  }
]
```

Response — `GET /api/notifications/unread-count`:

```json
{ "count": 1 }
```

Request — `POST /api/notifications/read-all`:

```json
{ "userId": 2403173 }
```

#### 12.1.10 Admin

All admin routes require the `X-Role: Admin` header (or `?role=Admin` query).

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/admin/stats` | Total counts of departments / resources / users / bookings |
| GET | `/api/admin/resources/status` | Every resource with its current status |
| GET | `/api/admin/reports?range=7d\|30d\|90d` | Reports data |
| GET | `/api/admin/users` | List users |
| POST | `/api/admin/users` | Add a user (via `ADD_USER_TRANSACTION`) |
| PUT | `/api/admin/users/:id` | Edit role / status |
| DELETE | `/api/admin/users/:id` | Delete a user |
| POST | `/api/admin/users/:id/reset-password` | Reset password |
| GET | `/api/admin/audit-logs?limit=100` | Audit log entries |

Response — `GET /api/admin/stats`:

```json
{
  "totalDepartments": 4,
  "totalResources": 23,
  "totalUsers": 50,
  "totalBookings": 19
}
```

Response — `GET /api/admin/resources/status`:

```json
[
  {
    "resourceId": 1,
    "resourceName": "CSE Lab 1",
    "status": "Available",
    "departmentName": "CSE",
    "typeName": "Lab"
  }
]
```

Response — `GET /api/admin/reports?range=30d`:

```json
{
  "range": "30d",
  "days": 30,
  "totalBookings": 19,
  "confirmedBookings": 12,
  "cancelledBookings": 5,
  "utilizationRate": 63,
  "avgDuration": 60,
  "peakHour": 10,
  "trend": [ { "date": "2026-10-08", "count": 2 } ],
  "hourly": [ { "hour": 10, "count": 5 } ],
  "byResourceType": [ { "name": "Lab", "count": 8 } ],
  "byDepartment": [ { "name": "CSE", "count": 10 } ]
}
```

Response — `POST /api/admin/users`:

```json
{ "ok": true, "message": "User added" }
```

Response — `GET /api/admin/audit-logs`:

```json
[
  {
    "auditId": 4,
    "userId": 2403173,
    "action": "PROMOTE_WAITLIST",
    "entityType": "BOOKING",
    "entityId": 29,
    "oldValue": null,
    "newValue": "Confirmed",
    "createdAt": "2026-10-11 19:11:37"
  }
]
```

#### 12.1.11 SSE Stream

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/stream?userId=X` | Server-Sent Events stream of new notifications |

Event types:

```text
event: hello
data: {"userId":2403163,"ts":1728900000000}
```

```text
event: notification
data: [{"NotificationId":2,"BookingId":32,"Message":"Good news! Your waitlisted booking #32 is now confirmed.","CreatedAt":"2026-10-20 10:15:00"}]
```

The client reconnects on error after 3 seconds.

---

### 12.2 Team A API (`http://localhost:8090`)

**Base path:** `/`

> **Note:** Team A does not enforce authentication. It is only ever called by the BFF over a trusted network (localhost in dev, private service network in deployment). This is intentional — auth is a Team D concern, and Team A trusts the BFF to have already authenticated the request.

#### 12.2.1 Health

| Method | Path | Purpose |
|---|---|---|
| GET | `/health` | Service + DB check |

Response:

```json
{ "status": "ok", "service": "team-a-scheduler", "db": true }
```

#### 12.2.2 Labs

| Method | Path | Purpose |
|---|---|---|
| GET | `/labs` | Static list of 23 campus resources |

Response (abbreviated):

```json
[
  { "id": "L1", "dept": "CSE", "name": "CSE Lab 1", "capacity": 30 },
  { "id": "EL1", "dept": "ELECTRICAL", "name": "Electrical Lab 1", "capacity": 30 },
  { "id": "AL1", "dept": "AIML", "name": "AIML Lab 1", "capacity": 30 },
  { "id": "ML1", "dept": "MECHANICAL", "name": "Mechanical Lab 1", "capacity": 30 },
  { "id": "C1", "dept": "CSE", "name": "CSE Classroom 1", "capacity": 60 },
  { "id": "CH1", "dept": "CSE", "name": "Conference Hall", "capacity": 100 }
]
```

#### 12.2.3 Schedule

| Method | Path | Purpose |
|---|---|---|
| POST | `/schedule` | Create or waitlist a booking |

Request:

```json
{
  "userId": 2403163,
  "labId": "L1",
  "date": "2026-10-20",
  "startTime": "10:00",
  "endTime": "11:00",
  "purpose": "Exam / Assessment — mid-semester",
  "priority": 1
}
```

Response — Confirmed (201):

```json
{
  "ok": true,
  "bookingId": 31,
  "status": "Confirmed",
  "message": "Booking confirmed",
  "waitlistPosition": 0,
  "priority": 1
}
```

Response — Waitlisted (201):

```json
{
  "ok": true,
  "bookingId": 32,
  "status": "Waitlisted",
  "message": "Resource is occupied. You are waitlist #1",
  "waitlistPosition": 1,
  "priority": 1
}
```

Response — Rejected (409):

```json
{
  "ok": false,
  "bookingId": -1,
  "status": "Rejected",
  "message": "You already have a request for this resource and slot",
  "waitlistPosition": 0,
  "priority": 1
}
```

Response — Bad request (400):

```json
{ "error": "userId, labId, date, startTime, endTime required" }
```

#### 12.2.4 Waitlist

| Method | Path | Purpose |
|---|---|---|
| POST | `/waitlist` | Explicit waitlist add |

Same request and response shape as `/schedule`.

#### 12.2.5 Promote

| Method | Path | Purpose |
|---|---|---|
| POST | `/promote` | Promote highest-priority waiter |

Request:

```json
{
  "labId": "L1",
  "date": "2026-10-20",
  "startTime": "10:00",
  "endTime": "11:00"
}
```

Response (200):

```json
{
  "promoted": true,
  "bookingId": 32,
  "winnerUserId": 2403173,
  "message": "Promoted booking #32"
}
```

Response — No waitlist entries:

```json
{ "promoted": false, "message": "No waitlist entries" }
```

#### 12.2.6 Queue

| Method | Path | Purpose |
|---|---|---|
| GET | `/queue?labId=&date=&startTime=&endTime=` | Current waitlist for a slot |

Response:

```json
[
  { "bookingId": 34, "userId": 2403174, "priority": 1, "position": 1 },
  { "bookingId": 32, "userId": 2403173, "priority": 2, "position": 2 },
  { "bookingId": 33, "userId": 2403163, "priority": 2, "position": 3 }
]
```

---

### 12.3 Team B API (`http://localhost:8091`)

**Base path:** `/`

> **Note:** Like Team A, Team B does not enforce authentication. It is only called by the BFF over a trusted network.

#### 12.3.1 Health

| Method | Path | Purpose |
|---|---|---|
| GET | `/health` | Service + graph version |

Response:

```json
{ "status": "ok", "service": "team-b-routing", "graphVersion": "G-91" }
```

#### 12.3.2 Route

| Method | Path | Purpose |
|---|---|---|
| POST | `/route` | Shortest path between two nodes |

Request:

```json
{
  "sourceNodeId": "N1",
  "destNodeId": "L1",
  "algorithm": "DIJKSTRA"
}
```

`algorithm` is optional. Supported: `DIJKSTRA`, `BELLMAN_FORD` (or `BELLMAN-FORD`), `BFS`, `DFS`.

Response:

```json
{
  "found": true,
  "algorithm": "DIJKSTRA",
  "graphVersion": "G-91",
  "path": ["N1", "N4", "L1"],
  "edges": ["E00", "E01"],
  "totalDistance": 65,
  "totalLatency": 13,
  "nodesExplored": 5
}
```

Response — No route:

```json
{
  "found": false,
  "algorithm": "DIJKSTRA",
  "graphVersion": "G-91",
  "path": [],
  "edges": [],
  "totalDistance": 0,
  "totalLatency": 0,
  "nodesExplored": 0
}
```

Response — Bad request (400):

```json
{ "error": "sourceNodeId and destNodeId required" }
```

#### 12.3.3 Search

| Method | Path | Purpose |
|---|---|---|
| POST | `/search` | Available resources sorted by distance |

Request:

```json
{
  "date": "2026-10-20",
  "startTime": "10:00",
  "endTime": "11:00",
  "resourceType": "Lab",
  "minCapacity": 30,
  "sourceNodeId": "N1"
}
```

- `resourceType` — `null` = any type; otherwise a TypeName like `Lab`, `Classroom`, `Conference Hall`
- `minCapacity` — defaults to `0`
- `sourceNodeId` — defaults to `N1`

Response (sorted ascending by `distance`):

```json
[
  {
    "resourceId": 1,
    "resourceName": "CSE Lab 1",
    "capacity": 50,
    "status": "Available",
    "departmentName": "CSE",
    "typeName": "Lab",
    "nodeId": "L1",
    "nodeName": "CSE Lab 1",
    "distance": 65,
    "latency": 13,
    "path": ["N1", "N4", "L1"]
  }
]
```

Response — Bad request (400):

```json
{ "error": "date, startTime, endTime required" }
```

Response — No matching slot (400):

```json
{ "error": "No matching time slot for 10:00 - 11:00" }
```

---

### 12.4 Error Code Reference

| Code | Meaning | Where it happens |
|---|---|---|
| 200 | Success | Reads, updates, successful promotes |
| 201 | Created | Successful booking insert |
| 400 | Bad request — missing or malformed parameters | All services |
| 401 | Unauthorized — wrong credentials or role mismatch | BFF login |
| 403 | Forbidden — role not allowed for this route | BFF admin routes |
| 404 | Not found — resource does not exist | BFF single-resource |
| 405 | Method not allowed — wrong HTTP verb | Team A, Team B |
| 409 | Conflict — business rule violation | BFF booking create, cancel |
| 500 | Server error — unexpected failure | All services |
| 502 | Bad gateway — upstream engine unreachable | BFF → Team A / Team B |

---

## 13. Algorithm and Complexity Summary

Every non-trivial algorithm in the system, its complexity, and where it is used.

| Algorithm | Type | File | Complexity | Where Used |
|---|---|---|---|---|
| **FCFS scheduling** | Ranking | `engines/team-a/Scheduler.java` | O(n log n) sort | Legacy OS simulation; also the FCFS half of the production scheduler |
| **Priority scheduling** | Ranking | `engines/team-a/Scheduler.java`, `BookingService.java` | O(n log n) sort; O(log n) insert into ordered index | Production booking waitlist |
| **Waitlist position computation** | Ranking | `engines/team-a/Db.java` → `computeWaitlistPosition` | O(n) count, small n | Waitlist insert |
| **Deadlock detection** | Graph | `engines/team-a/LockManager.java` | O(V + E) on the wait-for graph | Legacy OS simulation |
| **Dijkstra** | Shortest path | `engines/team-b/DijkstraAlgorithm.java` | O((V + E) log V) | Every `/route` and every row in `/search` |
| **BFS** | Traversal | `engines/team-b/BFSAlgorithm.java` | O(V + E) | Comparison / validation |
| **DFS** | Traversal | `engines/team-b/DFSAlgorithm.java` | O(V + E) | Comparison / cycle demo |
| **Bellman-Ford** | Shortest path | `engines/team-b/BellmanFordAlgorithm.java` | O(V · E) | Correctness reference |
| **AVL tree lookup** | Cache | `engines/team-b/AVLTree.java` | O(log n) | Route cache (benchmark) |
| **Red-Black tree lookup** | Cache | `engines/team-b/RedBlackTree.java` | O(log n) | Route cache (benchmark) |
| **Hash table lookup** | Cache | `engines/team-b/HashRoutingTable.java` | O(1) avg | Route cache (primary) |
| **Connectivity analysis** | Graph | `engines/team-b/ConnectivityAnalysis.java` | O(V + E) | Reachability reporting |
| **JWT issuance** | Crypto | `bff/routes/auth.js` | O(1) | Login |
| **bcrypt hash** | Crypto | `bff/routes/admin.js` | O(2^cost) | Password add / reset |
| **SSE polling** | Polling | `bff/sse.js` | O(1) query every 3 s per connected user | Live notifications |

**Graph size context:** V = 31 nodes, E = 60 directed edges. At this scale, all graph algorithms complete in microseconds; the benchmark suite exists to demonstrate *why* one structure is faster than another, not to optimise a large graph.

**Schedule size context:** the `time_slot` table has 8 rows; a single waitlist is expected to hold fewer than 10 entries at a time; the FCFS / Priority sort is therefore always on a tiny list. The complexity is stated for correctness, not because the system stresses it.

---

## 14. KPI Plan and Targets

The KPIs below were defined at S1 of the Hybrid SDLC and are measured at S6. They are listed here with their **targets** and the **method** by which they are measured. Actual measured values depend on the machine the system is run on; a reproducible benchmark script for each KPI is included.

### 14.1 Engine-Level KPIs

| KPI ID | Engine | KPI | Linked NFR | Measurement Method | Target |
|---|---|---|---|---|---|
| K1.1 | Team A | Booking decision latency (BFF → Team A `/schedule` → DB write → response) | NFR-Performance | `curl -w "%{time_total}\n"` average over 100 calls on localhost | < 500 ms |
| K1.2 | Team A | Waitlist promotion correctness | NFR-Correctness | Test case: insert 3 waiters with mixed priorities, cancel the confirmed booking, verify highest-priority waiter is promoted | 100% |
| K1.3 | Team A | Duplicate booking rejection | NFR-Correctness | Test case: same user + resource + slot + date twice → second rejected | 100% |
| K2.1 | Team B | Dijkstra route query time on the 31-node / 60-edge graph | NFR-Performance | `PerformanceAnalysis.java`, average of 100 runs with 10 warm-up | < 10 ms |
| K2.2 | Team B | Routing cache lookup time | NFR-Performance | `RoutingCacheBenchmark.java`, average of 1000 runs | < 5 µs |
| K2.3 | Team B | Route correctness against Bellman-Ford | NFR-Correctness | `TeamBTestSuite.java` — Dijkstra and Bellman-Ford return identical distance for a reference graph | 100% |
| K3.1 | Team C | Stored-procedure transaction completion time | NFR-Performance | Time each of the 8 procedures, average over 100 calls | < 200 ms |
| K3.2 | Team C | Concurrent booking conflict rejection | NFR-Correctness | Run 2 concurrent `CREATE_BOOKING_TRANSACTION` calls on the same resource + slot + date → exactly one succeeds | 100% |
| K3.3 | Team C | Audit completeness | NFR-Auditability | After 100 random booking / cancel / promote operations, `audit_log` has exactly one row per state change | 100% |
| K4.1 | Team D | SSE notification delivery latency (waitlist promotion → visible in browser) | NFR-Performance | Measure from promotion commit time to notification appearing in the bell | < 5 s |
| K4.2 | Team D | Dashboard initial load time (localhost) | NFR-Performance | Browser dev-tools Performance tab, cold reload | < 2 s |
| K4.3 | Team D | Accessibility pass on interactive elements | NFR-Accessibility | Manual audit of every button / input / link | 100% have focus-visible ring + label |

### 14.2 System-Level KPIs (System Gate 4)

| KPI ID | Scope | KPI | Measurement Method | Target |
|---|---|---|---|---|
| KPI-S1 | End-to-end | Full booking flow success rate | 50 sequential booking attempts; count successes | ≥ 95% |
| KPI-S2 | End-to-end | Full cancel + promote flow success rate | 20 cancel + promote attempts on pre-seeded waitlists; count correct promotions | 100% |
| KPI-S3 | End-to-end | Full search flow correctness | 20 searches with known source / filters; verify results are sorted by distance and contain only available resources | 100% |
| KPI-S4 | End-to-end | Notification delivery to the correct user | Trigger 10 promotions; verify each notification reaches the intended `userId` only | 100% |
| KPI-S5 | Data integrity | Referential integrity maintained | Attempt to insert a booking with an invalid user / resource / slot; verify FK rejects | 100% rejected |
| KPI-S6 | Data integrity | CHECK constraints enforced | Attempt invalid role, invalid status, capacity 0; verify CHECK rejects | 100% rejected |
| KPI-S7 | Documentation | Every mandatory evidence item present in report | Cross-check the 8 evidence items in Appendix B of the MiniLeap report | 8 of 8 present |
| KPI-S8 | Release | Reproducible build from a fresh clone | Clone to a new folder, follow Section 16, run the system, book a resource | Works on Windows and Ubuntu |

### 14.3 How to Measure Locally

**K1.1 — Booking decision latency** (run in PowerShell while BFF, Team A, and MySQL are running):

```powershell
1..100 | ForEach-Object {
  $t = Measure-Command {
    Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" `
      -Method Post `
      -ContentType "application/json" `
      -Body '{"userId":2403163,"resourceId":2,"date":"2026-11-01","startTime":"10:00","endTime":"11:00","reasonId":"exam","reasonLabel":"Exam"}'
  }
  $t.TotalMilliseconds
} | Measure-Object -Average -Maximum -Minimum
```

**K2.1, K2.2 — Team B benchmarks** (run in a terminal with JDK 21 and mysql-connector-j on the classpath):

```powershell
cd engines\team-b
javac -cp ".;lib/*" *.java
java -cp ".;lib/*" Main
```

This runs `PerformanceAnalysis`, `TraversalBenchmark`, `RoutingCacheBenchmark`, and `AlgorithmComparison`, printing the numbers directly.

**K3.1 — Stored procedure timing** (in MySQL Workbench or the mysql shell):

```sql
SELECT BENCHMARK(100,
  (SELECT 1 FROM DUAL WHERE EXISTS (
    SELECT 1 FROM bookings WHERE BookingId = 1
  ))
);
```

For a full procedure timing loop, wrap `CALL CREATE_BOOKING_TRANSACTION(...)` in a script and measure wall-clock time.

**K4.1 — SSE notification delivery** — open two browser windows: one as the canceling user, one as the waitlisted user. Cancel a booking in the first; time how long the notification takes to appear in the second's bell.

**KPI-S1 through KPI-S4 — End-to-end** — write a PowerShell or Postman collection that runs the flows in sequence and prints pass / fail per flow.

---

## 15. Technology Stack

### 15.1 By Layer

| Layer | Technology | Version | Justification |
|---|---|---|---|
| Frontend framework | React | 19.2 | Component model, hooks, large ecosystem, route-driven SPA |
| Frontend build | Vite | 8.3 | Fast dev server, native ESM, HMR, simple config |
| Frontend styling | Tailwind CSS | 3.4 | Utility-first, no CSS-in-JS runtime, small bundle |
| Frontend routing | React Router | 7.18 | Standard SPA router, role-based guards |
| Frontend icons | lucide-react | 1.52 | Tree-shakeable SVG icons, consistent style |
| BFF runtime | Node.js | 20 LTS | Same language as the build toolchain, async I/O for many small reads |
| BFF framework | Express | 5.2 | Minimal, well-understood, middleware model |
| BFF DB driver | mysql2 | 3.24 | Promise API, prepared statements |
| BFF auth | jsonwebtoken | 9.0 | Standard JWT issuance |
| BFF password hashing | bcryptjs | 3.0 | Pure-JS bcrypt, no native build step |
| BFF CORS | cors | 2.8 | Standard CORS middleware |
| BFF env loading | dotenv | 18.0 | Reads `bff/.env` |
| OS engine language | Java | 21 (LTS) | Strong typing, JDBC, `com.sun.net.httpserver` ships with the JDK |
| OS engine HTTP | `com.sun.net.httpserver` | JDK built-in | No extra framework needed |
| DSA engine language | Java | 21 | Same as Team A; graph data structures are first-class |
| DSA engine HTTP | `com.sun.net.httpserver` | JDK built-in | Consistent with Team A |
| Java DB driver | mysql-connector-j | 8.4.0 | Official MySQL JDBC driver |
| Database | MySQL | 8.0 | ACID InnoDB, stored procedures, CHECK constraints, `FOR UPDATE` |
| IDE / editor | VS Code | latest | Built-in PowerShell + Java + Node tooling |

### 15.2 Justification of Key Decisions

**Why microservices-with-a-BFF instead of a monolith?** The project's grading rubric requires that each team's engine be independently demonstrable. Splitting into four processes gives each team a clear ownership boundary and lets each team show their engine in isolation. The BFF is a thin orchestration layer that keeps the frontend simple.

**Why Java for the engines?** Both the OS and DSA curricula use Java. Using Java for both engines keeps the language consistent across the two algorithmic engines and lets Team A / Team B share code patterns (JDBC access, HTTP server setup).

**Why Node.js for the BFF?** The BFF is I/O-bound — it mostly proxies requests and reads from the DB. Node's async model handles concurrent SSE connections naturally. It also shares the same language as the frontend build toolchain, which simplifies developer setup.

**Why MySQL?** The DBMS course in this curriculum teaches MySQL. Using MySQL lets Team C demonstrate the exact concepts they learn (stored procedures, `FOR UPDATE`, `SIGNAL SQLSTATE`, CHECK constraints).

**Why Server-Sent Events instead of WebSocket?** Notifications flow one-way (server → client) and are infrequent. SSE is simpler: it works over plain HTTP, requires no handshake protocol, and reconnects automatically. WebSocket would be overkill for this use case.

**Why a single MySQL schema shared by all services?** The project's scope is a campus lab booking system, not a distributed system. A single schema with clear ownership per table (Team C owns all tables; Team A / Team B / Team D read and write according to their responsibility) is simpler and matches the course expectations.

### 15.3 Development Tools

| Tool | Purpose |
|---|---|
| Git | Version control |
| GitHub | Remote repository hosting |
| VS Code | Editor |
| MySQL Workbench | DB inspection and querying |
| Postman / curl | API testing |
| PowerShell (Windows) | Running services, timing KPIs |
| Bash (Linux / Ubuntu) | Running services on the college server |
| Chrome DevTools | Frontend debugging, network inspection |

---

## 16. Local Setup

### 16.1 Prerequisites

Install all four before proceeding:

| Tool | Minimum Version | Where to get it |
|---|---|---|
| Node.js | 20 LTS | https://nodejs.org |
| JDK | 21 | https://adoptium.net |
| MySQL | 8.0 | https://dev.mysql.com/downloads/mysql/ |
| Git | latest | https://git-scm.com |

Verify each installs correctly:

```powershell
node --version      # should print v20.x or v22.x
npm --version       # should print 10.x or higher
java --version      # should print "openjdk 21.x"
git --version       # should print "git version 2.x"
```

### 16.2 First-Time Setup — Windows

**Step 1 — Clone the repository**

```powershell
git clone https://github.com/aditygk19-debug/smart-campus.git
cd smart-campus
```

**Step 2 — Start MySQL**

Open an administrator PowerShell and start the MySQL service:

```powershell
Start-Service MySQL80
```

**Step 3 — Create the database**

Load the schema, then run the idempotent migration:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "DROP DATABASE IF EXISTS smartcampus;"
cmd /c ""C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < db\smartcampus_dump.sql"
cmd /c ""C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < db\phase1_migration.sql"
```

You will be prompted for your MySQL root password each time.

**Step 4 — Install Node dependencies**

```powershell
npm install
cd bff
npm install
cd ..
```

**Step 5 — Create the BFF `.env`**

Create `bff/.env` with the following contents (replace `<your MySQL password>` and generate a random JWT secret):

```text
PORT=8080
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=<your MySQL password>
DB_NAME=smartcampus
JWT_SECRET=<random 48-byte base64 string>
JWT_EXPIRY=24h
SCHEDULER_URL=http://localhost:8090
ROUTER_URL=http://localhost:8091
CORS_ORIGIN=http://localhost:5173
```

Generate a JWT secret:

```powershell
[Convert]::ToBase64String((1..48 | ForEach-Object { [byte](Get-Random -Maximum 256) }))
```

**Step 6 — Start the five services**

Open five PowerShell windows, one per service. Each is independent.

*Window 1 — MySQL* (already running from step 2; keep it running)

*Window 2 — Team A (OS Scheduler)*

```powershell
cd engines\team-a
javac -cp ".;lib/*" *.java
java -cp ".;lib/*" TeamAHttpServer
```

Expected output: `Team A HTTP Server running on http://localhost:8090`

*Window 3 — Team B (DSA Router)*

```powershell
cd engines\team-b
javac -cp ".;lib/*" *.java
java -cp ".;lib/*" TeamBHttpServer
```

Expected output: `Team B HTTP Server running on http://localhost:8091`

*Window 4 — BFF*

```powershell
cd bff
npm run dev
```

Expected output:

```text
MySQL connected to smartcampus
BFF running at http://localhost:8080
   Health check: http://localhost:8080/api/health
```

*Window 5 — Frontend*

```powershell
npm run dev
```

Expected output: `VITE ready in <n> ms` with `Local: http://localhost:5173/`

**Step 7 — Verify everything is up**

```powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8090/health
Invoke-RestMethod http://localhost:8091/health
```

Each should return a JSON object with `status: ok`.

**Step 8 — Open the app**

Browse to http://localhost:5173 and log in with one of the demo credentials in Section 17.

### 16.3 First-Time Setup — Linux / Ubuntu

The college server runs Ubuntu. The steps are the same but adapted.

**Step 1 — Install prerequisites**

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk nodejs npm mysql-server git
```

Verify:

```bash
node --version
java --version
mysql --version
```

**Step 2 — Clone the repository**

```bash
git clone https://github.com/aditygk19-debug/smart-campus.git
cd smart-campus
```

**Step 3 — Start MySQL**

```bash
sudo systemctl start mysql
sudo systemctl enable mysql
```

**Step 4 — Load the schema**

```bash
sudo mysql -u root -p -e "DROP DATABASE IF EXISTS smartcampus;"
sudo mysql -u root -p < db/smartcampus_dump.sql
sudo mysql -u root -p < db/phase1_migration.sql
```

**Step 5 — Install Node dependencies**

```bash
npm install
cd bff && npm install && cd ..
```

**Step 6 — Create the BFF `.env`**

Create `bff/.env` with the same keys as Section 16.2, step 5. On Ubuntu use:

```text
DB_HOST=127.0.0.1
```

(`localhost` also works, but `127.0.0.1` avoids any socket-vs-TCP ambiguity.)

**Step 7 — Start the services**

Java uses `:` instead of `;` in the classpath on Linux:

*Terminal 1 — Team A*

```bash
cd engines/team-a
javac -cp ".:lib/*" *.java
java -cp ".:lib/*" TeamAHttpServer
```

*Terminal 2 — Team B*

```bash
cd engines/team-b
javac -cp ".:lib/*" *.java
java -cp ".:lib/*" TeamBHttpServer
```

*Terminal 3 — BFF*

```bash
cd bff
npm run dev
```

*Terminal 4 — Frontend*

```bash
npm run dev
```

**Step 8 — Verify**

```bash
curl http://localhost:8080/api/health
curl http://localhost:8090/health
curl http://localhost:8091/health
```

**Step 9 — Important: Linux MySQL is case-sensitive for table names**

The current code queries tables with uppercase names (`USERS`, `BOOKINGS`, etc.). Windows MySQL is case-insensitive by default, so this works on a Windows dev laptop. On Linux, MySQL is case-sensitive for table names by default, so those queries will fail with `Table 'smartcampus.USERS' doesn't exist`.

Two ways to fix on the college server:

**Option A — Set `lower_case_table_names=1` (server-wide, recommended)**

Edit `/etc/mysql/mysql.conf.d/mysqld.cnf` and add:

```text
[mysqld]
lower_case_table_names=1
```

Then restart MySQL:

```bash
sudo systemctl restart mysql
```

Re-load the schema afterwards:

```bash
sudo mysql -u root -p < db/smartcampus_dump.sql
sudo mysql -u root -p < db/phase1_migration.sql
```

**Option B — Change every SQL query to use lowercase table names**

This is the cleaner long-term fix. In the BFF, replace `FROM USERS` with `FROM users`, `INTO BOOKINGS` with `INTO bookings`, etc., in every route file and in `sse.js`. Similarly, in Team A's `Db.java` and Team B's `Db.java`, lowercase every table name.

Discuss with your mentor which approach fits the college server policy.

### 16.4 Production Build Notes

`npm run dev` runs Vite's dev server. For a production build:

```bash
npm run build
```

This produces `dist/` — static HTML, CSS, and JS. To serve it:

- Deploy `dist/` behind any static host (Nginx, Apache, Vercel, Netlify, S3).
- The built frontend still calls `/api/...` relatively, so the host must proxy `/api/*` to the BFF. On Nginx:

```nginx
location /api/ {
    proxy_pass http://localhost:8080/api/;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_buffering off;      # important for SSE
}
```

The `proxy_buffering off` line is required for SSE notifications to reach the browser without delay.

Alternatively, use a `VITE_API_BASE_URL` environment variable and replace relative `/api/...` calls with `${import.meta.env.VITE_API_BASE_URL}/...`. This requires editing every fetch call in the frontend.

### 16.5 Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| BFF logs `MySQL connection failed` | Wrong `DB_PASSWORD` in `bff/.env`, or MySQL not running | Verify password, `Start-Service MySQL80` |
| `Table 'smartcampus.USERS' doesn't exist` | Running on Linux MySQL (case-sensitive) | See Section 16.3, step 9 |
| Team A / Team B crash at startup with `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | `mysql-connector-j` not on classpath | Compile and run with `-cp ".:lib/*"` (Linux) or `.;lib/*` (Windows) |
| Frontend shows "Couldn't load departments" | BFF not running, or `/api` proxy misconfigured | Verify `http://localhost:8080/api/departments` returns data |
| SSE notifications never arrive | Proxy buffering enabled | Add `proxy_buffering off` in Nginx; `X-Accel-Buffering: no` is already set in `sse.js` |
| Port already in use | Another instance is running | Kill the old process or change the port |
| Booking fails with "No matching time slot" | Frontend sent a slot not in `time_slot` | Check that `startTime` / `endTime` match the 8 rows in `time_slot` (09:00–17:00 in 1-hour increments) |
| `Access denied for user 'root'@'localhost'` | Wrong password | Reset MySQL root password or use a different DB user |

---

## 17. Demo Credentials

The seed data includes the following accounts. Passwords are stored as plaintext equal to the UserId for demo purposes; the login flow compares directly.

| Role | User ID | Password |
|---|---|---|
| Admin | 99999 | admin123 |
| Student | 2403163 | 2403163 |
| Student | 2403173 | 2403173 |
| Student | 2403174 | 2403174 |
| Student | 2403164 | 2403164 |
| Faculty | 10001 | 10001 |
| Faculty | 10002 | 10002 |

**Note:** The login flow in `routes/auth.js` compares the submitted password directly to the stored value. For all seeded accounts the stored value equals the UserId. So for the demo, enter the UserId as both fields.

**Recommended demo flow:**

1. Log in as Student `2403163` — book a resource.
2. Log out, log in as Student `2403173` — book the same resource, same slot, and watch it go to **Waitlisted**.
3. Log in as Student `2403163` again — cancel your booking.
4. Back in Student `2403173`'s browser tab — watch the notification bell receive the promotion in real time.

This sequence demonstrates all four engines in under two minutes.

---

## 18. Deliverables Checklist

The Hybrid SDLC defines seven stages (S0–S7) plus a Release stage. The table below maps each stage to its chapter in the MiniLeap report, its gate, its core evidence, and where that evidence lives in this project.

| Stage | Report Chapter | Gate | Core Evidence | Where in This Project |
|---|---|---|---|---|
| S0 — Problem Definition | Ch. 2 | Gate 0 | Problem statement, stakeholders, AS-IS, scope, feasibility, risks | Sections 1–3 of this README |
| S1 — Requirements | Ch. 3 | Gate 1 | FR / NFR, use cases, engine identification, KPI plan, RTM | Sections 3, 4, 12, 14 of this README |
| S2 — Architecture | Ch. 4, 5.n.2 | Gate 2 | Architecture diagrams, shared contract, mapping | Section 5 of this README |
| S3 — Data Model & API | Ch. 5.n.3 | Gate 2 | Schema / data structures, API specification | Sections 8, 11, 12 of this README |
| S4 — Algorithm & Logic | Ch. 5.n.4 | Gate 3 | Algorithms, complexity, business rules | Sections 6.4, 7.4, 8.6, 13 of this README |
| S5 — Implementation | Ch. 5.n.5 | Gate 3 | Working code, Git history, unit tests | The whole `smart-campus/` repository |
| S6 — Testing & Validation | Ch. 5.n.6, 6, 7 | Gate 4 / System Gate 4 | Test cases, benchmarks, security, KPI results | Sections 6.7, 7.9, 8.8, 9.9, 14 of this README |
| S7 — RCA & Improvement | Ch. 5.n.7, 7 | Gate 5 / System Gate 5 | Defect log, RCA, fixes, regression | Documented in the report body |
| Release | Ch. 8 | Final Review | Deployment note, rollback plan, documentation, demo | Section 16 of this README + this file |

### 18.1 Mandatory Evidence Set (8 items)

Every MiniLeap project must produce eight evidence items. All eight are present in this repository.

| # | Evidence | Where in This Repository |
|---|---|---|
| 1 | Requirements document | Section 3 (Objectives), Section 12 (API Reference) |
| 2 | System architecture diagram | Section 5.1 of this README |
| 3 | Database schema or data model | Section 8 (Team C), Section 11 (Data Model) |
| 4 | API specification | Section 12 (API Reference) |
| 5 | Algorithm or business logic | Sections 6.4, 7.4, 8.6, 13 |
| 6 | Test cases with results | Sections 6.7, 7.9, 8.8, 9.9 |
| 7 | Debugging log | Maintained in the report body (Chapter 5.n.7 for each engine) |
| 8 | Git history | The Git log of this repository (frequent, meaningful commits) |

### 18.2 Gate Status

| Gate | Scope | Status |
|---|---|---|
| Gate 0 (Problem) | Common | ✅ Complete |
| Gate 1 (Requirements) | Common | ✅ Complete |
| Gate 2 (Design) | Common + per engine | ✅ Complete |
| Gate 3 (Build) | Per engine | ✅ Complete |
| Gate 4 (Validation) | Per engine | ✅ Complete |
| System Gate 4 (System validation) | Whole system | ✅ Complete |
| Gate 5 (Improvement) | Per engine | ✅ Complete |
| System Gate 5 (System hardening) | Whole system | ✅ Complete |
| Final Review | Whole system | ⏳ In progress (Review V) |

---

## 19. Team and Mentor

**Team D — Engine 4 (Web Technologies)**

| Slot | Name | Role |
|---|---|---|
| S1 | Prasenjeet Patil | Frontend architecture, routing, auth integration |
| S2 | Aditya Patil | BFF orchestration, SSE, admin modules |
| S3 | Pranav Jagdale | Booking flow, search flow, UX |
| S4 | Aditya Gaikwad | Admin dashboard, reports, audit logs |
| S5 | Sarthak Patil | Integration testing, deployment configuration, documentation |

**Team A — Engine 1 (Operating Systems)**

| Slot | Name | PRN |
|---|---|---|
| S1 | Samruddhi Patil | 2403165 |
| S2 | Shoumika Chavan | 2403167 |
| S3 | Abhilasha Patil | 2403168 |
| S4 | Supriya Vairal | 2403169 |

**Team B — Engine 2 (Data Structures & Algorithms)**

| Slot | Name | PRN |
|---|---|---|
| S1 | Saloni Koli | 2403164 |
| S2 | Vrushali Patil | 2403170 |
| S3 | Tanuja Dhole | 2403172 |
| S4 | Shravani Thakur | 2403176 |

**Team C — Engine 3 (Database Management Systems)**

| Slot | Name | PRN |
|---|---|---|
| S1 | Atharv Hirve | 2403174 |
| S2 | Shruti Dabade | 2403175 |
| S3 | Anam Patvekar | 2403178 |
| S4 | Shweta Jamdade | 2403179 |

- **Mentor:** Prof. Priyanka N. Jadhav
- **Project Coordinator:** Prof. G. S. Wedpathak
- **Head of Department:** Dr. S. S. Patil

**Batch:** C2
**Academic Year:** 2026 – 2027
**Semester:** V
**Program:** T.Y. B.Tech. (Computer Science and Engineering)

---

## 20. License

This project was developed as part of the academic requirements of T.Y. B.Tech. (Computer Science and Engineering) at K. E. Society's Rajarambapu Institute of Technology, Rajaramnagar, during the academic year 2026–2027.

**Academic use only. Not licensed for commercial use.**
