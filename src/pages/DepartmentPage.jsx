import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  Building2,
  ArrowRight,
  LogOut,
  AlertCircle,
  ChevronRight,
  FlaskConical,
  GraduationCap,
  Users,
  BookOpen,
} from "lucide-react";

// Temporary lookup until the backend provides department names.
const DEPT_NAMES = {
  A1: "CSE",
  B1: "Electrical",
  C1: "AIML",
  D1: "Mechanical",
};

const TYPE_ICONS = {
  Lab: FlaskConical,
  Classroom: GraduationCap,
  "Conference Hall": Users,
};

function TopBar({ user, onLogout }) {
  const RoleIcon = user?.role === "Faculty" ? BookOpen : GraduationCap;

  return (
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
  );
}

export default function DepartmentPage({ user, onLogout }) {
  const { deptId } = useParams();
  const navigate = useNavigate();

  const deptName = DEPT_NAMES[deptId] || "Department";

  const [types, setTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const loadTypes = useCallback(
    async (signal) => {
      setLoading(true);
      setError(false);
      try {
        const res = await fetch(
          `/api/departments/${encodeURIComponent(deptId)}/resource-types`,
          { signal }
        );
        if (!res.ok) throw new Error(`Request failed: ${res.status}`);
        const data = await res.json();
        setTypes(Array.isArray(data) ? data : []);
      } catch (err) {
        if (err.name === "AbortError") return;
        setError(true);
      } finally {
        if (!signal || !signal.aborted) setLoading(false);
      }
    },
    [deptId]
  );

  useEffect(() => {
    const controller = new AbortController();
    loadTypes(controller.signal);
    return () => controller.abort();
  }, [loadTypes]);

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 flex flex-col">
      <TopBar user={user} onLogout={onLogout} />

      <main className="flex-1 mx-auto w-full max-w-6xl px-4 sm:px-6 py-6 sm:py-8">
        {/* BREADCRUMB */}
        <nav aria-label="Breadcrumb" className="text-sm">
          <ol className="flex items-center gap-1.5">
            <li>
              <Link
                to="/dashboard"
                className="rounded text-slate-500 transition-colors hover:text-indigo-600 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2"
              >
                Dashboard
              </Link>
            </li>
            <li aria-hidden="true">
              <ChevronRight className="h-4 w-4 text-slate-400" />
            </li>
            <li className="font-medium text-slate-800" aria-current="page">
              {deptName}
            </li>
          </ol>
        </nav>

        {/* PAGE HEADING */}
        <div className="mt-6">
          <p className="text-xs uppercase tracking-wide text-slate-500">
            Department
          </p>
          <h1 className="mt-1 text-3xl font-bold">{deptName}</h1>
          <p className="mt-1 text-sm text-slate-500">
            Browse resources by category
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
              Couldn&apos;t load categories. Please try again.
            </div>
            <button
              type="button"
              onClick={() => loadTypes()}
              className="self-start rounded-lg border border-indigo-600 bg-white px-3 py-1.5 text-sm font-medium text-indigo-600 transition-colors hover:bg-indigo-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 sm:self-auto"
            >
              Retry
            </button>
          </div>
        )}

        {/* CATEGORY GRID */}
        <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {loading &&
            [0, 1, 2].map((i) => (
              <div
                key={i}
                className="h-44 animate-pulse rounded-xl border border-slate-200 bg-slate-200"
                aria-hidden="true"
              />
            ))}

          {!loading &&
            types.map((type) => {
              const Icon = TYPE_ICONS[type.typeName] || Building2;
              return (
                <button
                  key={type.typeId}
                  type="button"
                  onClick={() =>
                    navigate(`/dashboard/department/${deptId}/${type.typeId}`)
                  }
                  aria-label={`Open ${type.typeName} category`}
                  className="group flex min-h-[11rem] cursor-pointer flex-col rounded-xl border border-slate-200 bg-white p-6 text-left transition-all hover:scale-[1.02] hover:border-indigo-300 hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2"
                >
                  <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-50">
                    <Icon className="h-5 w-5 text-indigo-600" aria-hidden="true" />
                  </span>

                  <span className="mt-4 text-lg font-semibold">
                    {type.typeName}
                  </span>
                  <span className="mt-1 text-sm text-slate-500">
                    {type.description}
                  </span>

                  <span className="mt-auto flex items-center justify-between pt-4">
                    <span className="text-sm text-slate-600">
                      {type.count != null ? `${type.count} resources` : "—"}
                    </span>
                    <ArrowRight
                      className="h-5 w-5 text-slate-400 transition-colors group-hover:text-indigo-600"
                      aria-hidden="true"
                    />
                  </span>
                </button>
              );
            })}
        </div>

        {/* EMPTY STATE */}
        {!loading && !error && types.length === 0 && (
          <p className="mt-6 text-sm text-slate-500">
            No categories available for this department.
          </p>
        )}
      </main>
    </div>
  );
}