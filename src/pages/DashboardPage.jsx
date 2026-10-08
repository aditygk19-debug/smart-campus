import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Building2,
  ArrowRight,
  LogOut,
  AlertCircle,
  GraduationCap,
  BookOpen,
} from "lucide-react";

export default function DashboardPage({ user, onLogout }) {
  const navigate = useNavigate();

  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const loadDepartments = useCallback(async (signal) => {
    setLoading(true);
    setError(false);
    try {
      const res = await fetch("/api/departments", { signal });
      if (!res.ok) throw new Error(`Request failed: ${res.status}`);
      const data = await res.json();
      setDepartments(Array.isArray(data) ? data : []);
    } catch (err) {
      if (err.name === "AbortError") return;
      setError(true);
    } finally {
      if (!signal || !signal.aborted) setLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    loadDepartments(controller.signal);
    return () => controller.abort();
  }, [loadDepartments]);

  const isActive = user?.status === "Active";
  const roleInitial = (user?.role || "?").charAt(0).toUpperCase();
  const RoleIcon = user?.role === "Faculty" ? BookOpen : GraduationCap;

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 flex flex-col">
      {/* TOP BAR */}
      <header className="sticky top-0 z-10 bg-white border-b border-slate-200">
        <div className="mx-auto max-w-6xl px-4 sm:px-6 h-16 flex items-center justify-between gap-3">
          <Link
            to="/dashboard"
            className="flex items-center gap-2.5 rounded-lg focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2"
          >
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-600 text-xs font-bold text-white">
              SC
            </span>
            <span className="font-semibold">Smart Campus</span>
          </Link>

          <div className="flex items-center gap-2 sm:gap-3">
            <div className="flex items-center gap-2 rounded-lg border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-sm">
              <span className="text-slate-600">ID: {user?.userId}</span>
              <span className="hidden sm:inline-flex items-center gap-1 rounded-md bg-indigo-50 px-2 py-0.5 text-xs font-medium text-indigo-700">
                <RoleIcon className="h-3 w-3" aria-hidden="true" />
                {user?.role}
              </span>
            </div>
            <button
              type="button"
              onClick={onLogout}
              className="inline-flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2"
            >
              <LogOut className="h-4 w-4" aria-hidden="true" />
              <span className="hidden sm:inline">Logout</span>
              <span className="sr-only sm:hidden">Logout</span>
            </button>
          </div>
        </div>
      </header>

      <main className="flex-1 mx-auto w-full max-w-6xl px-4 sm:px-6 py-6 sm:py-8">
        <h1 className="sr-only">Dashboard</h1>

        {/* PROFILE CARD */}
        <section
          aria-label="Profile"
          className="rounded-xl border border-slate-200 bg-white p-6 flex flex-col gap-6 md:flex-row md:items-center"
        >
          <div className="flex items-center gap-5 md:flex-1">
            <div
              className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-indigo-100 text-2xl font-semibold text-indigo-700"
              aria-hidden="true"
            >
              {roleInitial}
            </div>

            <dl className="space-y-1 text-sm">
              <div className="flex gap-2">
                <dt className="w-16 text-slate-500">User ID</dt>
                <dd className="font-medium">{user?.userId}</dd>
              </div>
              <div className="flex gap-2">
                <dt className="w-16 text-slate-500">Role</dt>
                <dd className="font-medium">{user?.role}</dd>
              </div>
              <div className="flex gap-2">
                <dt className="w-16 text-slate-500">Status</dt>
                <dd className="flex items-center gap-2 font-medium">
                  <span
                    className={`h-2 w-2 rounded-full ${
                      isActive ? "bg-green-500" : "bg-slate-400"
                    }`}
                    aria-hidden="true"
                  />
                  {user?.status}
                </dd>
              </div>
            </dl>
          </div>

          <div className="md:text-right border-t border-slate-200 pt-4 md:border-t-0 md:pt-0">
            <h2 className="text-2xl font-semibold">Welcome back</h2>
            <p className="mt-1 text-sm text-slate-500">
              Select a department to continue
            </p>
          </div>
        </section>

        {/* SECTION HEADING */}
        <div className="mt-10">
          <h2 className="text-xl font-semibold">Departments</h2>
          <p className="mt-1 text-sm text-slate-500">
            Choose a department to browse classrooms, labs, and conference halls.
          </p>
        </div>

        {/* ERROR STATE */}
        {error && (
          <div
            role="alert"
            className="mt-4 flex flex-col gap-3 rounded-xl border border-red-200 bg-red-50 p-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="flex items-center gap-2 text-sm text-red-700">
              <AlertCircle className="h-4 w-4 shrink-0" aria-hidden="true" />
              Couldn&apos;t load departments. Please try again.
            </div>
            <button
              type="button"
              onClick={() => loadDepartments()}
              className="self-start rounded-lg border border-indigo-600 bg-white px-3 py-1.5 text-sm font-medium text-indigo-600 transition-colors hover:bg-indigo-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 sm:self-auto"
            >
              Retry
            </button>
          </div>
        )}

        {/* DEPARTMENT GRID */}
        <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {loading &&
            [0, 1, 2, 3].map((i) => (
              <div
                key={i}
                className="h-40 animate-pulse rounded-xl border border-slate-200 bg-slate-200"
                aria-hidden="true"
              />
            ))}

          {!loading &&
            departments.map((dept) => (
              <button
                key={dept.departmentId}
                type="button"
                onClick={() =>
                  navigate(`/dashboard/department/${dept.departmentId}`)
                }
                aria-label={`Open ${dept.departmentName} department`}
                className="group flex min-h-[10rem] cursor-pointer flex-col rounded-xl border border-slate-200 bg-white p-6 text-left transition-all hover:scale-[1.02] hover:border-indigo-300 hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2"
              >
                <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-50">
                  <Building2 className="h-5 w-5 text-indigo-600" aria-hidden="true" />
                </span>

                <span className="mt-4 text-lg font-semibold">
                  {dept.departmentName}
                </span>
                <span className="mt-1 text-xs text-slate-500">
                  Department ID: {dept.departmentId}
                </span>

                <span className="mt-auto flex items-end justify-between pt-4">
                  {/* TODO: backend should include "resourceCount" in GET /api/departments.
                      Until then, "—" is shown. */}
                  <span className="text-sm text-slate-600">
                    {dept.resourceCount != null
                      ? `${dept.resourceCount} resources`
                      : "— resources"}
                  </span>
                  <ArrowRight
                    className="h-5 w-5 text-slate-400 transition-colors group-hover:text-indigo-600"
                    aria-hidden="true"
                  />
                </span>
              </button>
            ))}
        </div>

        {!loading && !error && departments.length === 0 && (
          <p className="mt-6 text-sm text-slate-500">
            No departments are available yet.
          </p>
        )}
      </main>

   
    </div>
  );
}