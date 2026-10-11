import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import {
  LogOut,
  ChevronRight,
  ArrowRight,
  AlertCircle,
  FlaskConical,
  GraduationCap,
  Users,
  MapPin,
  Navigation,
  Clock,
} from "lucide-react";

const TYPE_ICON = {
  Lab: FlaskConical,
  Classroom: GraduationCap,
  "Conference Hall": Users,
};

function loadRequest() {
  try { return JSON.parse(sessionStorage.getItem("sc_search_request") || "null"); } catch { return null; }
}
function loadResults() {
  try {
    const raw = sessionStorage.getItem("sc_search_results");
    const parsed = raw ? JSON.parse(raw) : null;
    return Array.isArray(parsed) ? parsed : [];
  } catch { return []; }
}

function formatDate(dateString) {
  if (!dateString) return "";
  const [y, m, d] = dateString.split("-").map(Number);
  const dt = new Date(y, m - 1, d);
  return dt.toLocaleDateString("en-IN", { weekday: "short", day: "numeric", month: "short", year: "numeric" });
}

function formatTime(hhmm) {
  if (!hhmm) return "";
  const [h, m] = hhmm.split(":").map(Number);
  const period = h >= 12 ? "PM" : "AM";
  const hour12 = h % 12 === 0 ? 12 : h % 12;
  return `${hour12}:${String(m).padStart(2, "0")} ${period}`;
}

export default function SearchResultsPage({ user, onLogout }) {
  const navigate = useNavigate();
  const [request, setRequest] = useState(null);
  const [results, setResults] = useState([]);

  useEffect(() => {
    setRequest(loadRequest());
    setResults(loadResults());
  }, []);

  function handlePick(r) {
    const params = new URLSearchParams({
      date: request.date,
      startTime: request.startTime,
      endTime: request.endTime,
      sourceNodeId: request.sourceNodeId || "",
      fromSearch: "1",
    });
    navigate(`/dashboard/book/${r.resourceId}?${params.toString()}`);
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3 sm:px-6">
          <Link to="/dashboard" className="flex items-center gap-3 rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600">
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-600 text-sm font-bold text-white">SC</span>
            <span className="hidden text-sm font-semibold sm:block">Smart Campus Lab &amp; Resource Optimizer</span>
          </Link>
          <div className="flex items-center gap-3">
            <div className="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs">
              <span className="font-medium">{user?.userId}</span>
              {user?.role && <span className="text-slate-500"> &middot; {user.role}</span>}
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

      <main className="mx-auto max-w-5xl px-4 py-6 sm:px-6">
        <nav aria-label="Breadcrumb" className="mb-5">
          <ol className="flex flex-wrap items-center gap-1 text-sm text-slate-500">
            <li><Link to="/dashboard" className="rounded hover:text-indigo-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600">Dashboard</Link></li>
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
            <li><Link to="/dashboard/search" className="rounded hover:text-indigo-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600">Search</Link></li>
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
            <li aria-current="page" className="font-medium text-slate-800">Results</li>
          </ol>
        </nav>

        <div className="mb-6">
          <p className="text-xs uppercase text-slate-500">Search Results</p>
          <h1 className="text-3xl font-bold">
            {results.length} resource{results.length === 1 ? "" : "s"} available
          </h1>
          {request && (
            <p className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-sm text-slate-500">
              <span className="inline-flex items-center gap-1.5">
                <Clock className="h-4 w-4" aria-hidden="true" />
                {formatDate(request.date)} · {formatTime(request.startTime)} – {formatTime(request.endTime)}
              </span>
              <span className="inline-flex items-center gap-1.5">
                <MapPin className="h-4 w-4" aria-hidden="true" />
                From {request.sourceNodeId}
              </span>
            </p>
          )}
        </div>

        {results.length === 0 ? (
          <div className="flex flex-col items-center rounded-xl border border-slate-200 bg-white px-4 py-12 text-center">
            <AlertCircle className="mb-3 h-8 w-8 text-slate-400" aria-hidden="true" />
            <p className="text-base font-semibold">No resources matched your search</p>
            <p className="mt-1 mb-5 text-sm text-slate-500">
              Try a wider time slot, lower minimum capacity, or a different type.
            </p>
            <button
              type="button"
              onClick={() => navigate("/dashboard/search")}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2"
            >
              Modify search
            </button>
          </div>
        ) : (
          <ul className="space-y-3">
            {results.map((r) => {
              const Icon = TYPE_ICON[r.typeName] || FlaskConical;
              return (
                <li key={r.resourceId}>
                  <button
                    type="button"
                    onClick={() => handlePick(r)}
                    aria-label={`Book ${r.resourceName}`}
                    className="group flex w-full items-center gap-4 rounded-xl border border-slate-200 bg-white p-4 text-left transition-all hover:scale-[1.01] hover:border-indigo-300 hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2"
                  >
                    <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-indigo-50">
                      <Icon className="h-6 w-6 text-indigo-600" aria-hidden="true" />
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className="block truncate text-lg font-semibold">{r.resourceName}</span>
                      <span className="mt-0.5 block text-xs text-slate-500">
                        {r.departmentName} · {r.typeName} · Capacity {r.capacity}
                      </span>
                      <span className="mt-1 inline-flex items-center gap-1.5 text-xs text-slate-600">
                        <Navigation className="h-3.5 w-3.5 text-indigo-600" aria-hidden="true" />
                        {r.distance} m · {r.latency} ms · via {(r.path || []).join(" → ")}
                      </span>
                    </span>
                    <ArrowRight className="h-5 w-5 shrink-0 text-slate-400 transition-colors group-hover:text-indigo-600" aria-hidden="true" />
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </main>
    </div>
  );
}