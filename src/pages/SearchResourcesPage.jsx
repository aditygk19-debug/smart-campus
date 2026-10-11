import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import {
  LogOut,
  ChevronRight,
  Search,
  AlertCircle,
  Loader2,
  MapPin,
} from "lucide-react";

const RESOURCE_TYPES = [
  { value: "", label: "Any type" },
  { value: "Lab", label: "Lab" },
  { value: "Classroom", label: "Classroom" },
  { value: "Conference Hall", label: "Conference Hall" },
];

const TIME_SLOTS = [
  { start: "09:00", end: "10:00", label: "9:00 AM \u2013 10:00 AM" },
  { start: "10:00", end: "11:00", label: "10:00 AM \u2013 11:00 AM" },
  { start: "11:00", end: "12:00", label: "11:00 AM \u2013 12:00 PM" },
  { start: "12:00", end: "13:00", label: "12:00 PM \u2013 1:00 PM" },
  { start: "13:00", end: "14:00", label: "1:00 PM \u2013 2:00 PM" },
  { start: "14:00", end: "15:00", label: "2:00 PM \u2013 3:00 PM" },
  { start: "15:00", end: "16:00", label: "3:00 PM \u2013 4:00 PM" },
  { start: "16:00", end: "17:00", label: "4:00 PM \u2013 5:00 PM" },
];

const FALLBACK_NODES = [
  { nodeId: "N1", nodeName: "Main Gate" },
  { nodeId: "N3", nodeName: "Library" },
  { nodeId: "N5", nodeName: "Office" },
  { nodeId: "N4", nodeName: "CSE Department" },
];

function pad(n) { return String(n).padStart(2, "0"); }
function toDateString(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
function getToday() { return toDateString(new Date()); }
function getMaxDate() {
  const d = new Date();
  d.setDate(d.getDate() + 30);
  return toDateString(d);
}

export default function SearchResourcesPage({ user, onLogout }) {
  const navigate = useNavigate();

  const [date, setDate] = useState(getToday());
  const [slotIdx, setSlotIdx] = useState(0);
  const [resourceType, setResourceType] = useState("");
  const [minCapacity, setMinCapacity] = useState(0);
  const [sourceNodeId, setSourceNodeId] = useState("N1");
  const [nodes, setNodes] = useState(FALLBACK_NODES);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();
    async function load() {
      try {
        const res = await fetch("/api/campus-nodes", { signal: controller.signal });
        if (!res.ok) throw new Error();
        const data = await res.json();
        if (Array.isArray(data) && data.length > 0) {
          const places = data.filter((n) =>
            ["Gate", "Library", "Office", "Department"].includes(n.nodeType)
          );
          if (places.length > 0) setNodes(places);
        }
      } catch {
        setNodes(FALLBACK_NODES);
      }
    }
    load();
    return () => controller.abort();
  }, []);

  const slot = TIME_SLOTS[slotIdx];

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const res = await fetch("/api/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          date,
          startTime: slot.start,
          endTime: slot.end,
          resourceType: resourceType || null,
          minCapacity: Number(minCapacity) || 0,
          sourceNodeId,
        }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || "Search failed");

      sessionStorage.setItem(
        "sc_search_request",
        JSON.stringify({ date, startTime: slot.start, endTime: slot.end, resourceType, minCapacity, sourceNodeId })
      );
      sessionStorage.setItem("sc_search_results", JSON.stringify(data));
      navigate("/dashboard/search-results");
    } catch (err) {
      setError(err.message || "Search failed");
    } finally {
      setSubmitting(false);
    }
  }

  const fieldClass =
    "w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-800 " +
    "focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600";

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

      <main className="mx-auto max-w-3xl px-4 py-6 sm:px-6">
        <nav aria-label="Breadcrumb" className="mb-5">
          <ol className="flex flex-wrap items-center gap-1 text-sm text-slate-500">
            <li><Link to="/dashboard" className="rounded hover:text-indigo-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600">Dashboard</Link></li>
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
            <li aria-current="page" className="font-medium text-slate-800">Search Resources</li>
          </ol>
        </nav>

        <div className="mb-6">
          <p className="text-xs uppercase text-slate-500">Find a resource</p>
          <h1 className="text-3xl font-bold">Search available resources</h1>
          <p className="text-sm text-slate-500">
            Pick a time, type, minimum capacity, and where you are now. Results are sorted by walking distance.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 rounded-xl border border-slate-200 bg-white p-5 sm:p-6">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-sm font-medium">Date</label>
              <input
                type="date"
                value={date}
                min={getToday()}
                max={getMaxDate()}
                onChange={(e) => setDate(e.target.value)}
                className={fieldClass}
                required
              />
            </div>

            <div>
              <label className="mb-1 block text-sm font-medium">Time slot</label>
              <select value={slotIdx} onChange={(e) => setSlotIdx(Number(e.target.value))} className={fieldClass}>
                {TIME_SLOTS.map((s, i) => (
                  <option key={s.start} value={i}>{s.label}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-1 block text-sm font-medium">Resource type</label>
              <select value={resourceType} onChange={(e) => setResourceType(e.target.value)} className={fieldClass}>
                {RESOURCE_TYPES.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-1 block text-sm font-medium">Minimum capacity</label>
              <input
                type="number"
                min={0}
                value={minCapacity}
                onChange={(e) => setMinCapacity(e.target.value)}
                className={fieldClass}
              />
            </div>

            <div className="sm:col-span-2">
              <label className="mb-1 flex items-center gap-1.5 text-sm font-medium">
                <MapPin className="h-4 w-4 text-slate-500" aria-hidden="true" />
                Your location
              </label>
              <select value={sourceNodeId} onChange={(e) => setSourceNodeId(e.target.value)} className={fieldClass}>
                {nodes.map((n) => (
                  <option key={n.nodeId} value={n.nodeId}>{n.nodeName}</option>
                ))}
              </select>
            </div>
          </div>

          {error && (
            <div role="alert" className="flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-70"
          >
            {submitting ? (
              <><Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />Searching…</>
            ) : (
              <><Search className="h-4 w-4" aria-hidden="true" />Search</>
            )}
          </button>
        </form>
      </main>
    </div>
  );
}