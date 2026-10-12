# Smart Campus Lab & Resource Optimizer

A web-based system where students, faculty, and admins can search, book, and manage campus resources — labs, classrooms, and conference halls. Four independent engines, each built around a core CS subject, are integrated into a single working application.

**Team D (Engine 4) — Control & Monitor Dashboard**

---

## Architecture
┌─────────────────────────────────────────────────────────┐
│ Frontend (React + Vite + Tailwind) :5173 │
└──────────────────────┬──────────────────────────────────┘
│ HTTP/JSON + SSE
▼
┌─────────────────────────────────────────────────────────┐
│ BFF (Node.js + Express) :8080 │
│ Auth · Search · Bookings · Notifications · Admin │
└──────┬────────────────┬──────────────────┬──────────────┘
│ │ │
▼ ▼ ▼
┌────────────┐ ┌────────────┐ ┌──────────────────────┐
│ Team A OS │ │ Team B DSA │ │ Team C DBMS │
│ :8090 │ │ :8091 │ │ MySQL 8 :3306 │
│ Scheduler │ │ Routing │ │ smartcampus DB │
└────────────┘ └────────────┘ └──────────────────────┘

text

| Engine | Team | Course | Responsibility | Tech |
|---|---|---|---|---|
| E1 | A | OS | Scheduling (FCFS + Priority), waitlist, deadlock | Java |
| E2 | B | DSA | Campus routing (Dijkstra/BFS/DFS/Bellman-Ford) | Java |
| E3 | C | DBMS | MySQL persistence, ACID transactions, stored procs | SQL |
| E4 | D | Web Tech | React dashboard, BFF orchestration, SSE, RBAC | React + Node |

---

## Repository layout
smart-campus/
├── src/ React frontend
│ ├── pages/ Login, Dashboard, Department, ResourceList,
│ │ BookingForm, BookingStatus, SearchResources,
│ │ SearchResults, AdminDashboard, AdminUsers,
│ │ AdminReports, AdminAuditLogs
│ ├── components/ NotificationBell
│ └── hooks/ useSSE
├── bff/ Node.js Express Backend-for-Frontend
│ ├── server.js
│ ├── db.js MySQL pool
│ ├── engines.js HTTP clients for Team A + Team B
│ ├── sse.js Server-Sent Events stream
│ ├── routes/ auth, departments, resources, campus-nodes,
│ │ bookings, search, notifications, admin,
│ │ admin-stats
│ └── utils/nodeMap.js ResourceId → campus NodeId lookup
├── engines/
│ ├── team-a/ Team A full Java source + TeamAHttpServer
│ │ └── lib/ mysql-connector-j
│ └── team-b/ Team B full Java source + TeamBHttpServer
│ └── lib/ mysql-connector-j
└── db/
├── smartcampus_dump.sql Full schema + data
└── phase1_migration.sql Idempotent procedures + constraints

text

---

## Features

### Student / Faculty
- Search resources by date, time slot, type, capacity, source location
- Results sorted by walking distance (Team B Dijkstra)
- Book a resource — the request goes to Team A's OS scheduler
- If the resource is free → **Confirmed**
- If occupied → **Waitlisted** with a priority-based position
- Cancel a booking — the highest-priority waiter is promoted automatically
- Live notifications via SSE when promoted
- Personal booking history with filters (All / Pending / Confirmed / Waitlisted / Cancelled)

### Admin
- Live dashboard: total departments, resources, users, bookings
- Resource status by department (expanded/collapsed)
- User management: add, edit role/status, delete, reset password
- Audit log with every system event (bookings, waitlist, users)
- Reports & analytics: utilization, peak hour, booking trend, department-wise stats
- Range selector (7d / 30d / 90d), CSV export, print view

---

## Booking flow
Student submits a booking
→ BFF POST /api/bookings
→ Team A /schedule

FCFS if free

Priority if contended (1 = highest)

Waitlist if occupied
→ Team A writes to MySQL
→ Response returned with bookingId + status

Student cancels
→ BFF POST /api/bookings/:id/cancel
→ MySQL CANCEL_BOOKING_TRANSACTION
→ BFF calls Team A /promote

Highest-priority waiter → Confirmed

NOTIFICATIONS row inserted
→ SSE pushes notification to the winner

Search Resources
→ BFF POST /api/search
→ Team B /search

Queries MySQL for available resources

Computes distance from source via Dijkstra

Returns list sorted by distance

text

---

## Tech stack

### Frontend
- React 19, Vite 8, Tailwind CSS 3
- React Router 7
- lucide-react icons
- Context API + hooks (useSSE for live updates)

### BFF
- Node.js 20, Express 5
- mysql2, jsonwebtoken, bcryptjs
- cors, dotenv
- Server-Sent Events for live notifications

### Team A (OS)
- Java 21
- `com.sun.net.httpserver`
- mysql-connector-j 8.4.0 (JDBC)

### Team B (DSA)
- Java 21
- `com.sun.net.httpserver`
- mysql-connector-j 8.4.0 (JDBC)

### Team C (DBMS)
- MySQL 8.0
- 12 tables (users, bookings, resources, time_slot, campus_node, campus_edge, route_history, department, location, resource_type, notifications, audit_log)
- 8 stored procedures

---

## Local setup

### Prerequisites
- Node.js 20+
- JDK 21
- MySQL 8.0
- Git

### 1. Clone

```powershell
git clone https://github.com/aditygk19-debug/smart-campus.git
cd smart-campus
2. Set up the database
powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "DROP DATABASE IF EXISTS smartcampus;"
cmd /c ""C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < db\smartcampus_dump.sql"
cmd /c ""C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < db\phase1_migration.sql"
3. Install dependencies
powershell
npm install
cd bff
npm install
cd ..
4. Configure the BFF
Create bff/.env:

env
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
5. Start everything — 5 windows
Window 1 — MySQL service (admin PowerShell, one-time)

powershell
Start-Service MySQL80
Window 2 — Team A

powershell
cd engines\team-a
javac -cp ".;lib/*" *.java
java -cp ".;lib/*" TeamAHttpServer
Window 3 — Team B

powershell
cd engines\team-b
javac -cp ".;lib/*" *.java
java -cp ".;lib/*" TeamBHttpServer
Window 4 — BFF

powershell
cd bff
npm run dev
Window 5 — Frontend

powershell
npm run dev
Open http://localhost:5173

6. Verify
powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8090/health
Invoke-RestMethod http://localhost:8091/health
All three should return status: ok.

Demo credentials
Role	User ID	Password
Student	2403163	any (auth is simplified for demo)
Student	2403173	any
Faculty	10001	any
Admin	99999	admin123
API reference (BFF)
Method	Path	Purpose
POST	/api/auth/login	Issue JWT
GET	/api/departments	List departments
GET	/api/departments/:deptId/resource-types	Categories per dept
GET	/api/resources	List resources with filters
GET	/api/resources/:id	Single resource
GET	/api/campus-nodes	All 31 campus nodes
POST	/api/bookings	Create booking (Team A)
GET	/api/bookings?userId=	List user bookings
POST	/api/bookings/:id/cancel	Cancel + auto-promote
POST	/api/search	Distance-sorted search (Team B)
GET	/api/notifications?userId=	List notifications
GET	/api/notifications/unread-count?userId=	Unread count
POST	/api/notifications/:id/read	Mark one read
POST	/api/notifications/read-all	Mark all read
GET	/api/stream?userId=	SSE live stream
GET	/api/admin/stats	Dashboard counts
GET	/api/admin/resources/status	All resources
GET	/api/admin/reports?range=	Reports data
GET	/api/admin/users	List users
POST	/api/admin/users	Add user
PUT	/api/admin/users/:id	Edit user
DELETE	/api/admin/users/:id	Delete user
POST	/api/admin/users/:id/reset-password	Reset password
GET	/api/admin/audit-logs	Audit history
Notifications (SSE)
The BFF exposes /api/stream?userId=X. Every 3 seconds it polls NOTIFICATIONS for rows newer than the last delivered ID and pushes them as:

text
event: notification
data: [{"NotificationId":N,"BookingId":B,"Message":"...","CreatedAt":"..."}]
The frontend subscribes via useSSE hook and updates the notification bell.

Team D deliverables
Deliverable	Status
S0 Problem definition	✅
S1 Functional requirements	✅
S2 System architecture + diagrams	✅
S3 Data model + API design	✅
S4 Algorithm + business logic	✅
S5 Implementation (React + BFF)	✅
S6 Testing + validation	✅
S7 RCA + improvement	✅
Gate 2 (Architecture review)	✅
Gate 3 (Implementation review)	✅
Gate 4 (Validation review)	✅
System Gate (Integration)	✅
Team
Team D — Batch C2

Prasenjeet Patil

Aditya Patil

Pranav Jagdale

Aditya Gaikwad

Sarthak Patil

Mentor: Prof. Priyanka N. Jadhav

License
Academic project — Smart Campus Lab & Resource Optimizer, Batch C2.

text

Save.

---

## Step 2 — Commit and push

```powershell
cd C:\Users\adigk\Projects\smart-campus-frontend
git add README.md
git commit -m "docs: add comprehensive README with architecture, setup, and API reference"
git push
