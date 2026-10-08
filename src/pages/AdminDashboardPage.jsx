import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import {
  LogOut,
  AlertCircle,
  RefreshCw,
  Loader2,
  Building2,
  Boxes,
  Users,
  CalendarClock,
  ChevronDown,
  ChevronRight,
  ShieldCheck,
  Activity,
  CheckCircle2,
  XCircle,
} from "lucide-react";

/* ------------------------------------------------------------------ */
/* Constants                                                           */
/* ------------------------------------------------------------------ */

const REFRESH_MS = 30000;

/* ------------------------------------------------------------------ */
/* Helpers                                                             */
/* ------------------------------------------------------------------ */

function formatTime(hhmm) {
  const [h, m] = String(hhmm).split(":").map(Number);
  if (Number.isNaN(h) || Number.isNaN(m)) return hhmm;
  const period = h >= 12 ? "PM" : "AM";
  const hour12 = h % 12 === 0 ? 12 : h % 12;
  return `${hour12}:${String(m).padStart(2, "0")} ${period}`;
}

// "15 Oct · 10:00 AM – 11:00 AM"
function formatDateTime(dateString, startTime, endTime) {
  const [y, m, d] = String(dateString).split("-").map(Number);
  const dt = new Date(y, m - 1, d);
  const datePart = Number.isNaN(dt.getTime())
    ? dateString
    : dt.toLocaleDateString("en-IN", { day: "numeric", month: "short" });
  return `${datePart} \u00B7 ${formatTime(startTime)} \u2013 ${formatTime(endTime)}`;
}

function getStatusBadge(status) {
  switch (String(status).toLowerCase()) {
    case "available":
      return { className: "bg-green-100 text-green-700", label: "Available" };
    case "confirmed":
      return { className: "bg-green-100 text-green-700", label: "Confirmed" };
    case "occupied":
      return { className: "bg-red-100 text-red-700", label: "Occupied" };
    case "pending":
      return { className: "bg-amber-100 text-amber-700", label: "Pending" };
    case "cancelled":
      return { className: "bg-slate-100 text-slate-700", label: "Cancelled" };
    default:
      return { className: "bg-slate-100 text-slate-700", label: status || "Unknown" };
  }
}

// Returns { [departmentId]: Resource[] }. Resources whose department isn't in
// the departments list are grouped under their departmentName instead.
function groupResourcesByDepartment(resources, departments) {
  const idByName = {};
  const groups = {};
  departments.forEach((d) => {
    idByName[d.departmentName] = d.departmentId;
    groups[d.departmentId] = [];
  });
  resources.forEach((r) => {
    const key = idByName[r.departmentName] ?? r.departmentName ?? "Unassigned";
    if (!groups[key]) groups[key] = [];
    groups[key].push(r);
  });
  return groups;
}

async function request(url, signal, isValid) {
  try {
    const res = await fetch(url, { signal });
    if (!res.ok) throw new Error(`Request failed: ${res.status}`);
    const data = await res.json();
    if (!isValid(data)) throw new Error("Unexpected response");
    return { ok: true, data };
  } catch (err) {
    return { ok: false, err };
  }
}

const isObject = (v) => v !== null && typeof v === "object" && !Array.isArray(v);
const fmtNum = (n) => (typeof n === "number" ? n.toLocaleString("en-IN") : "\u2013");

/* ------------------------------------------------------------------ */
/* Small presentational pieces                                         */
/* ------------------------------------------------------------------ */

function Badge({ status, small = false }) {
  const { className, label } = getStatusBadge(status);
  return (
    <span
      className={`shrink-0 rounded-full font-medium ${
        small ? "px-2.5 py-0.5 text-xs" : "px-3 py-1 text-xs"
      } ${className}`}
    >
      {label}
    </span>
  );
}

function StatCard({ icon: Icon, value, label }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5">
      <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-50">
        <Icon className="h-5 w-5 text-indigo-600" aria-hidden="true" />
      </div>
      <p className="text-3xl font-bold">{fmtNum(value)}</p>
      <p className="text-sm text-slate-500">{label}</p>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/* Component                                                           */
/* ------------------------------------------------------------------ */

export default function AdminDashboardPage({ user, onLogout }) {
  const navigate = useNavigate();

  const [stats, setStats] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [resourceStatus, setResourceStatus] = useState([]);
  const [recentBookings, setRecentBookings] = useState([]);

  const [loading, setLoading] = useState(true);
  const [failed, setFailed] = useState({
    stats: false,
    departments: false,
    resources: false,
    bookings: false,
  });
  const [lastUpdated, setLastUpdated] = useState(null);
  const [refreshing, setRefreshing] = useState(false);
  const [expandedDepts, setExpandedDepts] = useState({}); // { A1: true, B1: false, ... }

  const controllerRef = useRef(null);

  // Fetch all four datasets in parallel (mount + interval + manual refresh).
  // A failed call keeps whatever data that section already had.
  const loadAll = useCallback(async () => {
    if (controllerRef.current) controllerRef.current.abort();
    const controller = new AbortController();
    controllerRef.current = controller;
    const { signal } = controller;

    setRefreshing(true);

    const [s, d, r, b] = await Promise.all([
      request("/api/admin/stats", signal, isObject),
      request("/api/departments", signal, Array.isArray),
      request("/api/admin/resources/status", signal, Array.isArray),
      request("/api/bookings?status=all&limit=10", signal, Array.isArray),
    ]);

    if (signal.aborted) return; // superseded by a newer request or unmounted

    if (s.ok) setStats(s.data);
    if (d.ok) setDepartments(d.data);
    if (r.ok) setResourceStatus(r.data);
    if (b.ok) setRecentBookings(b.data.slice(0, 10));

    setFailed({ stats: !s.ok, departments: !d.ok, resources: !r.ok, bookings: !b.ok });
    if (s.ok || d.ok || r.ok || b.ok) setLastUpdated(new Date());
    setLoading(false);
    setRefreshing(false);
  }, []);

  useEffect(() => {
    loadAll();
    const id = setInterval(loadAll, REFRESH_MS);
    return () => {
      clearInterval(id);
      if (controllerRef.current) controllerRef.current.abort();
    };
  }, [loadAll]);

  // Department cards: first one expanded by default, others collapsed
  const deptCards = useMemo(() => {
    const groups = groupResourcesByDepartment(resourceStatus, departments);
    const known = departments.map((d) => ({
      id: d.departmentId,
      name: d.departmentName,
      count: d.resourceCount ?? (groups[d.departmentId] || []).length,
      resources: groups[d.departmentId] || [],
    }));
    const knownIds = new Set(departments.map((d) => d.departmentId));
    const extra = Object.keys(groups)
      .filter((key) => !knownIds.has(key))
      .map((key) => ({ id: key, name: key, count: groups[key].length, resources: groups[key] }));
    return [...known, ...extra];
  }, [resourceStatus, departments]);

  function isExpanded(deptId, index) {
    return expandedDepts[deptId] ?? index === 0;
  }

  function toggleDept(deptId) {
    const index = deptCards.findIndex((c) => c.id === deptId);
    setExpandedDepts((prev) => ({
      ...prev,
      [deptId]: !(prev[deptId] ?? index === 0),
    }));
  }

  // Resource status strip (falls back to counting the resource list)
  const derivedAvailable = resourceStatus.filter((r) => r.status === "Available").length;
  const derivedOccupied = resourceStatus.filter((r) => r.status === "Occupied").length;
  const availableCount =
    stats?.availableResources ?? (resourceStatus.length ? derivedAvailable : null);
  const occupiedCount =
    stats?.occupiedResources ?? (resourceStatus.length ? derivedOccupied : null);

  const failedCount = Object.values(failed).filter(Boolean).length;
  const allFailed = failedCount === 4;
  const partialFailed = failedCount > 0 && !allFailed;

  const cardClass = "rounded-xl border border-slate-200 bg-white";

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">
      {/* Top bar */}
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6">
          <Link
            to="/admin"
            className="flex items-center gap-3 rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
          >
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-600 text-sm font-bold text-white">
              SC
            </span>
            <span className="text-sm font-semibold">Smart Campus</span>
            <span className="hidden border-l border-slate-200 pl-3 text-xs text-slate-500 sm:block">
              Admin Panel
            </span>
          </Link>

          <div className="flex items-center gap-3">
            <div className="inline-flex items-center gap-1.5 rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700">
              <ShieldCheck className="h-3.5 w-3.5" aria-hidden="true" />
              Admin
              {user?.userId && (
                <span className="hidden font-normal text-indigo-600 sm:inline">
                  &middot; {user.userId}
                </span>
              )}
            </div>
            <button
              type="button"
              onClick={onLogout}
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
            >
              <LogOut className="h-4 w-4" aria-hidden="true" />
              <span className="hidden sm:inline">Logout</span>
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6">
        {/* Error banners */}
        {allFailed && !loading && (
          <div
            role="alert"
            className="mb-5 flex flex-col gap-3 rounded-xl border border-red-200 bg-red-50 p-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="flex items-center gap-2 text-sm text-red-700">
              <AlertCircle className="h-5 w-5 shrink-0" aria-hidden="true" />
              Couldn&apos;t load dashboard data. Please try again.
            </div>
            <button
              type="button"
              onClick={loadAll}
              className="self-start rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 sm:self-auto"
            >
              Retry
            </button>
          </div>
        )}
        {partialFailed && (
          <div
            role="alert"
            className="mb-5 flex flex-col gap-3 rounded-xl border border-amber-200 bg-amber-50 p-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="flex items-center gap-2 text-sm text-amber-800">
              <AlertCircle className="h-5 w-5 shrink-0" aria-hidden="true" />
              Some data couldn&apos;t be loaded. Retry.
            </div>
            <button
              type="button"
              onClick={loadAll}
              className="self-start rounded-lg border border-amber-300 bg-white px-4 py-2 text-sm font-medium text-amber-800 hover:bg-amber-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 sm:self-auto"
            >
              Retry
            </button>
          </div>
        )}

        {/* Page header */}
        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-xs uppercase text-slate-500">System Overview</p>
            <h1 className="text-3xl font-bold">Admin Dashboard</h1>
            <p className="text-sm text-slate-500">
              Live status of all campus resources and bookings.
            </p>
          </div>

          <div className="flex items-center gap-2 text-xs text-slate-500">
            <span aria-live="polite">
              {lastUpdated
                ? `Last updated ${lastUpdated.toLocaleTimeString("en-IN", {
                    hour: "numeric",
                    minute: "2-digit",
                    second: "2-digit",
                  })}`
                : "Not updated yet"}
            </span>
            {refreshing && (
              <Loader2 className="h-3.5 w-3.5 animate-spin" aria-hidden="true" />
            )}
            <button
              type="button"
              onClick={loadAll}
              disabled={refreshing}
              aria-label="Refresh data"
              className="rounded-lg border border-slate-200 bg-white p-2 text-slate-600 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 disabled:cursor-not-allowed disabled:opacity-60"
            >
              <RefreshCw className="h-4 w-4" aria-hidden="true" />
            </button>
          </div>
        </div>

        {/* Stats row */}
        <section aria-label="Campus totals" className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {loading ? (
            [0, 1, 2, 3].map((i) => (
              <div key={i} className="h-36 animate-pulse rounded-xl bg-slate-200" />
            ))
          ) : (
            <>
              <StatCard icon={Building2} value={stats?.totalDepartments} label="Departments" />
              <StatCard icon={Boxes} value={stats?.totalResources} label="Total Resources" />
              <StatCard icon={Users} value={stats?.totalUsers} label="Total Users" />
              <StatCard icon={CalendarClock} value={stats?.totalBookings} label="Total Bookings" />
            </>
          )}
        </section>

        {/* Resource status strip */}
        <section
          aria-label="Resource status summary"
          className={`${cardClass} mt-4 flex flex-col gap-3 p-4 sm:flex-row sm:items-center sm:justify-between`}
        >
          <div>
            <p className="flex items-center gap-2 text-sm font-semibold">
              <Activity className="h-4 w-4 text-indigo-600" aria-hidden="true" />
              Resource Status
            </p>
            <p className="mt-0.5 text-xs text-slate-500">
              Live data &mdash; refreshed every 30 seconds
            </p>
          </div>
          {loading ? (
            <div className="h-5 w-48 animate-pulse rounded bg-slate-200" />
          ) : (
            <div className="flex gap-6 text-sm">
              <span className="inline-flex items-center gap-2">
                <span className="h-2.5 w-2.5 rounded-full bg-green-500" aria-hidden="true" />
                {fmtNum(availableCount)} Available
              </span>
              <span className="inline-flex items-center gap-2">
                <span className="h-2.5 w-2.5 rounded-full bg-red-500" aria-hidden="true" />
                {fmtNum(occupiedCount)} Occupied
              </span>
            </div>
          )}
        </section>

        {/* Two-column section */}
        <div className="mt-6 grid gap-6 lg:grid-cols-5">
          {/* LEFT: resources by department */}
          <section aria-labelledby="dept-heading" className="lg:col-span-3">
            <h2 id="dept-heading" className="mb-3 text-sm font-semibold">
              Resource Status by Department
            </h2>

            {loading ? (
              <div className="space-y-4">
                {[0, 1, 2].map((i) => (
                  <div key={i} className="h-28 animate-pulse rounded-xl bg-slate-200" />
                ))}
              </div>
            ) : deptCards.length === 0 ? (
              <div className={`${cardClass} px-4 py-10 text-center text-sm text-slate-500`}>
                {failed.resources || failed.departments
                  ? "Resource data is unavailable right now."
                  : "No resources configured."}
              </div>
            ) : (
              <div className="space-y-4">
                {deptCards.map((dept, index) => {
                  const open = isExpanded(dept.id, index);
                  const panelId = `dept-panel-${index}`;
                  const available = dept.resources.filter((r) => r.status === "Available").length;
                  const occupied = dept.resources.filter((r) => r.status === "Occupied").length;

                  return (
                    <div key={dept.id} className={cardClass}>
                      <button
                        type="button"
                        onClick={() => toggleDept(dept.id)}
                        aria-expanded={open}
                        aria-controls={panelId}
                        className="flex w-full items-center justify-between gap-3 rounded-xl p-4 text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
                      >
                        <span className="flex min-w-0 items-center gap-2">
                          {open ? (
                            <ChevronDown className="h-4 w-4 shrink-0 text-slate-500" aria-hidden="true" />
                          ) : (
                            <ChevronRight className="h-4 w-4 shrink-0 text-slate-500" aria-hidden="true" />
                          )}
                          <span className="truncate font-semibold">{dept.name}</span>
                          <span className="shrink-0 text-xs text-slate-500">
                            {dept.count} {dept.count === 1 ? "resource" : "resources"}
                          </span>
                        </span>
                        <span className="hidden shrink-0 items-center gap-3 text-xs text-slate-500 sm:flex">
                          <span className="inline-flex items-center gap-1">
                            <CheckCircle2 className="h-3.5 w-3.5 text-green-600" aria-hidden="true" />
                            {available} available
                          </span>
                          <span className="inline-flex items-center gap-1">
                            <XCircle className="h-3.5 w-3.5 text-red-600" aria-hidden="true" />
                            {occupied} occupied
                          </span>
                        </span>
                      </button>

                      <div
                        id={panelId}
                        aria-hidden={!open}
                        className={`grid transition-[grid-template-rows] duration-200 motion-reduce:transition-none ${
                          open ? "grid-rows-[1fr]" : "grid-rows-[0fr]"
                        }`}
                      >
                        <div className="overflow-hidden">
                          {dept.resources.length === 0 ? (
                            <p className="border-t border-slate-200 px-4 py-3 text-sm text-slate-500">
                              No resources configured.
                            </p>
                          ) : (
                            <ul className="border-t border-slate-200">
                              {dept.resources.map((r) => (
                                <li
                                  key={r.resourceId}
                                  className="flex items-center justify-between gap-3 border-b border-slate-200 px-4 py-2.5 last:border-b-0"
                                >
                                  <span className="min-w-0 truncate text-sm">
                                    {r.resourceName}
                                    {r.typeName && (
                                      <span className="ml-2 text-xs text-slate-500">{r.typeName}</span>
                                    )}
                                  </span>
                                  <Badge status={r.status} />
                                </li>
                              ))}
                            </ul>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>

          {/* RIGHT: recent bookings */}
          <section aria-labelledby="recent-heading" className="lg:col-span-2">
            <div className={`${cardClass} p-4`}>
              <h2 id="recent-heading" className="mb-3 text-sm font-semibold">
                Recent Bookings
              </h2>

              {loading ? (
                <div className="space-y-3">
                  {[0, 1, 2, 3, 4].map((i) => (
                    <div key={i} className="space-y-2">
                      <div className="h-4 w-3/4 animate-pulse rounded bg-slate-200" />
                      <div className="h-3 w-1/2 animate-pulse rounded bg-slate-200" />
                    </div>
                  ))}
                </div>
              ) : recentBookings.length === 0 ? (
                <p className="py-6 text-sm text-slate-500">
                  {failed.bookings ? "Bookings are unavailable right now." : "No bookings yet."}
                </p>
              ) : (
                <ul className="max-h-96 divide-y divide-slate-200 overflow-y-auto">
                  {recentBookings.map((b) => (
                    <li key={b.bookingId} className="flex items-start justify-between gap-3 py-3 first:pt-0">
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium">{b.resourceName}</p>
                        <p className="text-xs text-slate-500">{b.userId}</p>
                        <p className="text-xs text-slate-500">
                          {formatDateTime(b.date, b.startTime, b.endTime)}
                        </p>
                      </div>
                      <Badge status={b.status} small />
                    </li>
                  ))}
                </ul>
              )}

              <div className="mt-3 border-t border-slate-200 pt-3">
                <button
                  type="button"
                  onClick={() => navigate("/admin/reports")}
                  className="rounded text-sm font-medium text-indigo-600 hover:text-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
                >
                  View all &rarr;
                </button>
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}