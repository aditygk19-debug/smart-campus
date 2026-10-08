# Smart Campus — Lab & Resource Optimizer

A web-based dashboard for managing and monitoring campus lab, classroom, and conference hall resources. This is **Engine 4 (Control & Monitor Dashboard)** of the Smart Campus mini-project.

Built by **Team D** (Batch C2) as part of the Hybrid SDLC Mini-Project framework.

---

## 👥 Team D Members

| Name | Roll No |
|---|---|
| Prasenjeet Patil | 2403163 |
| Aditya Patil | 2403171 |
| Pranav Jagdale | 2403173 |
| Aditya Gaikwad | 2403177 |
| Sarthak Patil | 2403180 |

**Mentor:** Prof. Priyanka N. Jadhav

---

## 🎯 Project Overview

The Smart Campus system is decomposed into **4 engines**, each tied to a core CS course:

| Engine | Team | Course | Responsibility |
|---|---|---|---|
| **Engine 1 — Memory & Process Scheduler** | Team A | OS | Scheduling (FCFS/Priority), locks, deadlock |
| **Engine 2 — Faster Routing Engine** | Team B | DSA | Graph, Dijkstra, BFS/DFS, routing cache |
| **Engine 3 — Transaction & Inventory Manager** | Team C | DBMS | MySQL, ACID, bookings, audit |
| **Engine 4 — Control & Monitor Dashboard** | **Team D** | **Web Tech** | **UI, role-based views, SSE, integration** |

**This repository contains Engine 4 — the frontend.**

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Framework | React 18 |
| Build | Vite 5 |
| Styling | Tailwind CSS 3 |
| Routing | React Router v6 |
| State | Context API + useReducer |
| HTTP | Fetch API |
| Icons | lucide-react |
| Dev Tools | ESLint, Prettier |

**Planned (backend integration):**
- Node.js + Express (BFF — Backend for Frontend)
- MySQL (Team C's database)
- SSE (Server-Sent Events) for live updates
- JWT for authentication

---

## 📄 Pages & Routes

| # | Page | Route | Role |
|---|---|---|---|
| 1 | Login | `/login` | Everyone |
| 2 | Dashboard | `/dashboard` | Student, Faculty |
| 3 | Department Resources | `/dashboard/department/:deptId` | Student, Faculty |
| 4 | Resource List + Date/Time | `/dashboard/department/:deptId/:typeId` | Student, Faculty |
| 5 | Booking Form | `/dashboard/book/:resourceId` | Student, Faculty |
| 6 | My Bookings | `/dashboard/bookings` | Student, Faculty |
| 7 | Admin Dashboard | `/admin` | Admin |
| 8 | Admin Reports | `/admin/reports` | Admin |

---

## 🚀 Getting Started

### Prerequisites
- Node.js 18 or higher
- npm 9 or higher

### Install & Run

```bash
# Clone the repository
git clone https://github.com/aditygk19-debug/smart-campus.git
cd smart-campus

# Install dependencies
npm install

# Start the dev server
npm run dev