import { useEffect, useState } from "react";
import { useParams, useSearchParams, useNavigate, Link } from "react-router-dom";
import {
  LogOut,
  AlertCircle,
  ChevronRight,
  Send,
  Loader2,
  MapPin,
  CalendarClock,
  Info,
  CheckCircle2,
  ArrowLeft,
} from "lucide-react";

/* ------------------------------------------------------------------ */
/* Constants                                                           */
/* ------------------------------------------------------------------ */

const REASONS = [
  { id: "practical", label: "Practical / Lab Session" },
  { id: "lecture", label: "Lecture / Theory Class" },
  { id: "project", label: "Project Work" },
  { id: "exam", label: "Exam / Assessment" },
  { id: "workshop", label: "Workshop / Seminar" },
  { id: "meeting", label: "Meeting / Discussion" },
];

const PRIORITY_BY_REASON = {
  exam: "high",
  workshop: "high",
  practical: "medium",
  lecture: "medium",
  project: "medium",
  meeting: "low",
};

const PRIORITY_STYLES = {
  high: { label: "High Priority", className: "bg-red-100 text-red-700" },
  medium: { label: "Medium Priority", className: "bg-amber-100 text-amber-700" },
  low: { label: "Low Priority", className: "bg-slate-100 text-slate-700" },
};

const FALLBACK_NODES = [
  { nodeId: "N1", nodeName: "Main Gate", nodeType: "Gate" },
  { nodeId: "N3", nodeName: "Library", nodeType: "Library" },
  { nodeId: "N5", nodeName: "Office", nodeType: "Office" },
  { nodeId: "N4", nodeName: "CSE Department", nodeType: "Department" },
];

// Used only to build breadcrumb links from the names the API returns
const DEPT_IDS_BY_NAME = { CSE: "A1", Electrical: "B1", AIML: "C1", Mechanical: "D1" };
const TYPE_IDS_BY_NAME = {
  Lab: 1,
  Labs: 1,
  Classroom: 2,
  Classrooms: 2,
  "Conference Hall": 3,
  "Conference Halls": 3,
};

const NOTES_MAX = 200;

/* ------------------------------------------------------------------ */
/* Helpers                                                             */
/* ------------------------------------------------------------------ */

function formatTime(hhmm) {
  const [h, m] = hhmm.split(":").map(Number);
  if (Number.isNaN(h) || Number.isNaN(m)) return hhmm;
  const period = h >= 12 ? "PM" : "AM";
  const hour12 = h % 12 === 0 ? 12 : h % 12;
  return `${hour12}:${String(m).padStart(2, "0")} ${period}`;
}

function formatTimeRange(start, end) {
  return `${formatTime(start)} \u2013 ${formatTime(end)}`;
}

function formatDate(dateString) {
  const [y, m, d] = dateString.split("-").map(Number);
  const dt = new Date(y, m - 1, d);
  if (Number.isNaN(dt.getTime())) return dateString;
  return dt.toLocaleDateString("en-IN", {
    weekday: "short",
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

/* ------------------------------------------------------------------ */
/* Small presentational pieces                                         */
/* ------------------------------------------------------------------ */

function Field({ label, children }) {
  return (
    <div>
      <p className="text-xs text-slate-500">{label}</p>
      <p className="text-sm font-medium">{children}</p>
    </div>
  );
}

function ErrorBanner({ message, onRetry }) {
  return (
    <div
      role="alert"
      className="flex flex-col gap-3 rounded-xl border border-red-200 bg-red-50 p-4 sm:flex-row sm:items-center sm:justify-between"
    >
      <div className="flex items-center gap-2 text-sm text-red-700">
        <AlertCircle className="h-5 w-5 shrink-0" aria-hidden="true" />
        {message}
      </div>
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="self-start rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 sm:self-auto"
        >
          Retry
        </button>
      )}
    </div>
  );
}

/* ------------------------------------------------------------------ */
/* Component                                                           */
/* ------------------------------------------------------------------ */

export default function BookingFormPage({ user, onLogout }) {
  const { resourceId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  // Query params
  const date = searchParams.get("date");
  const startTime = searchParams.get("startTime");
  const endTime = searchParams.get("endTime");
  const missingParams = !date || !startTime || !endTime;

  // State
  const [resource, setResource] = useState(null);
  const [nodes, setNodes] = useState([]);
  const [sourceNodeId, setSourceNodeId] = useState("");
  const [reasonId, setReasonId] = useState("");
  const [notes, setNotes] = useState("");
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [loadError, setLoadError] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  // Fetch resource details
  useEffect(() => {
    if (missingParams) return;
    const controller = new AbortController();

    async function load() {
      setLoading(true);
      setLoadError(false);
      try {
        const res = await fetch(`/api/resources/${encodeURIComponent(resourceId)}`, {
          signal: controller.signal,
        });
        if (!res.ok) throw new Error(`Request failed: ${res.status}`);
        const data = await res.json();
        setResource(data);
        setLoading(false);
      } catch (err) {
        if (err.name === "AbortError") return;
        setResource(null);
        setLoadError(true);
        setLoading(false);
      }
    }

    load();
    return () => controller.abort();
  }, [resourceId, missingParams, reloadKey]);

  // Fetch campus nodes (falls back to a hardcoded list on failure)
  useEffect(() => {
    const controller = new AbortController();

    async function loadNodes() {
      try {
        const res = await fetch("/api/campus-nodes", { signal: controller.signal });
        if (!res.ok) throw new Error(`Request failed: ${res.status}`);
        const data = await res.json();
        if (!Array.isArray(data) || data.length === 0) throw new Error("Empty list");
        setNodes(data);
      } catch (err) {
        if (err.name === "AbortError") return;
        setNodes(FALLBACK_NODES);
      }
    }

    loadNodes();
    return () => controller.abort();
  }, []);

  function clearError(field) {
    setErrors((prev) => {
      if (!prev[field]) return prev;
      const next = { ...prev };
      delete next[field];
      return next;
    });
  }

  function validate() {
    const next = {};
    if (!sourceNodeId) next.source = "Select where you are coming from.";
    if (!reasonId) next.reason = "Select a reason for this booking.";
    return next;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (submitting) return;

    const found = validate();
    setErrors(found);
    setSubmitError("");
    if (Object.keys(found).length > 0) return;

    const reason = REASONS.find((r) => r.id === reasonId);
    setSubmitting(true);
    try {
      const res = await fetch("/api/bookings", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: user?.userId,
          resourceId,
          date,
          startTime,
          endTime,
          sourceNodeId,
          reasonId,
          reasonLabel: reason ? reason.label : "",
          notes: notes.trim(),
        }),
      });

      let data = null;
      try {
        data = await res.json();
      } catch {
        data = null;
      }

      if (!res.ok) {
        throw new Error(
          data?.message || data?.error || "Couldn't submit your request. Please try again."
        );
      }

      const bookingId = data?.bookingId ?? data?.id ?? "";
      const params = new URLSearchParams({ status: "confirmed" });
      if (bookingId !== "") params.set("id", String(bookingId));
      navigate(`/dashboard/bookings?${params.toString()}`);
    } catch (err) {
      setSubmitError(err.message || "Couldn't submit your request. Please try again.");
      setSubmitting(false);
    }
  }

  // Breadcrumb data (derived from the fetched resource)
  const deptName = resource?.departmentName;
  const typeName = resource?.typeName;
  const deptId = deptName ? DEPT_IDS_BY_NAME[deptName] : undefined;
  const typeId = typeName ? TYPE_IDS_BY_NAME[typeName] : undefined;

  const priority = reasonId ? PRIORITY_STYLES[PRIORITY_BY_REASON[reasonId]] : null;

  const linkClass =
    "rounded hover:text-indigo-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600";
  const cardClass = "rounded-xl border border-slate-200 bg-white p-4 sm:p-5";
  const inputClass =
    "w-full rounded-lg border bg-white px-3 py-2 text-sm text-slate-800 " +
    "focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600";
  const borderFor = (hasError) => (hasError ? "border-red-400" : "border-slate-200");

  const destinationText = resource
    ? [resource.resourceName, resource.buildingName, resource.roomNumber && `Room ${resource.roomNumber}`]
        .filter(Boolean)
        .join(", ")
    : "";

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">
      {/* Top bar */}
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6">
          <Link
            to="/dashboard"
            className="flex items-center gap-3 rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
          >
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-600 text-sm font-bold text-white">
              SC
            </span>
            <span className="hidden text-sm font-semibold sm:block">
              Smart Campus Lab &amp; Resource Optimizer
            </span>
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

      <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6">
        {/* Breadcrumb */}
        <nav aria-label="Breadcrumb" className="mb-5">
          <ol className="flex flex-wrap items-center gap-1 text-sm text-slate-500">
            <li>
              <Link to="/dashboard" className={linkClass}>
                Dashboard
              </Link>
            </li>
            {deptName && (
              <>
                <ChevronRight className="h-4 w-4" aria-hidden="true" />
                <li>
                  {deptId ? (
                    <Link to={`/dashboard/department/${deptId}`} className={linkClass}>
                      {deptName}
                    </Link>
                  ) : (
                    deptName
                  )}
                </li>
              </>
            )}
            {typeName && (
              <>
                <ChevronRight className="h-4 w-4" aria-hidden="true" />
                <li>
                  {deptId && typeId ? (
                    <Link to={`/dashboard/department/${deptId}/${typeId}`} className={linkClass}>
                      {typeName}
                    </Link>
                  ) : (
                    typeName
                  )}
                </li>
              </>
            )}
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
            <li aria-current="page" className="font-medium text-slate-800">
              Book
            </li>
          </ol>
        </nav>

        {/* Page header */}
        <div className="mb-6">
          <p className="text-xs uppercase text-slate-500">Booking Request</p>
          <h1 className="text-3xl font-bold">Confirm your booking</h1>
          <p className="text-sm text-slate-500">
            Fill in the details below. Your request will be processed by the scheduling engine.
          </p>
        </div>

        {missingParams ? (
          <div
            role="alert"
            className="flex flex-col gap-3 rounded-xl border border-red-200 bg-red-50 p-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="flex items-center gap-2 text-sm text-red-700">
              <AlertCircle className="h-5 w-5 shrink-0" aria-hidden="true" />
              Missing booking details. Please go back and select a slot again.
            </div>
            <Link
              to="/dashboard"
              className="inline-flex items-center gap-1.5 self-start rounded-lg border border-red-300 bg-white px-4 py-2 text-sm font-medium text-red-700 hover:bg-red-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 sm:self-auto"
            >
              <ArrowLeft className="h-4 w-4" aria-hidden="true" />
              Back to dashboard
            </Link>
          </div>
        ) : loadError ? (
          <ErrorBanner
            message="Couldn't load resource details. Please try again."
            onRetry={() => setReloadKey((k) => k + 1)}
          />
        ) : (
          <div className="grid gap-6 lg:grid-cols-5">
            {/* LEFT: summary + form */}
            <div className="lg:col-span-3">
              {loading || !resource ? (
                <div className="animate-pulse" aria-busy="true" aria-label="Loading booking form">
                  <div className="h-44 rounded-xl bg-slate-200" />
                  <div className="mt-6 space-y-4">
                    <div className="h-16 rounded-xl bg-slate-200" />
                    <div className="h-16 rounded-xl bg-slate-200" />
                    <div className="h-16 rounded-xl bg-slate-200" />
                    <div className="h-24 rounded-xl bg-slate-200" />
                    <div className="flex gap-3">
                      <div className="h-10 w-24 rounded-lg bg-slate-200" />
                      <div className="h-10 w-40 rounded-lg bg-slate-200" />
                    </div>
                  </div>
                </div>
              ) : (
                <>
                  {/* Resource summary */}
                  <section aria-labelledby="summary-heading" className={cardClass}>
                    <h2 id="summary-heading" className="sr-only">
                      Resource summary
                    </h2>
                    <div className="mb-4 flex flex-wrap items-center gap-3">
                      <p className="text-lg font-semibold">{resource.resourceName}</p>
                      <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700">
                        {resource.typeName}
                      </span>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <Field label="Department">{resource.departmentName}</Field>
                      <Field label="Building">{resource.buildingName}</Field>
                      <Field label="Room">{resource.roomNumber}</Field>
                      <Field label="Capacity">{resource.capacity}</Field>
                      <Field label="Date">
                        <span className="inline-flex items-center gap-1.5">
                          <CalendarClock className="h-4 w-4 text-indigo-600" aria-hidden="true" />
                          {formatDate(date)}
                        </span>
                      </Field>
                      <Field label="Time">{formatTimeRange(startTime, endTime)}</Field>
                    </div>
                  </section>

                  {/* Request form */}
                  <form onSubmit={handleSubmit} noValidate className="mt-6">
                    <div className={`${cardClass} space-y-5`}>
                      <h2 className="text-base font-semibold">Request details</h2>

                      {/* Source */}
                      <div>
                        <label htmlFor="source" className="mb-1 flex items-center gap-1.5 text-sm font-medium">
                          <MapPin className="h-4 w-4 text-slate-500" aria-hidden="true" />
                          Source Location <span className="text-red-500">*</span>
                        </label>
                        <p id="source-help" className="mb-2 text-xs text-slate-500">
                          Where are you coming from? Used to calculate the shortest path.
                        </p>
                        <select
                          id="source"
                          value={sourceNodeId}
                          onChange={(e) => {
                            setSourceNodeId(e.target.value);
                            clearError("source");
                          }}
                          aria-required="true"
                          aria-invalid={errors.source ? "true" : "false"}
                          aria-describedby={errors.source ? "source-help source-error" : "source-help"}
                          className={`${inputClass} ${borderFor(errors.source)}`}
                        >
                          <option value="">Select a location</option>
                          {nodes.map((n) => (
                            <option key={n.nodeId} value={n.nodeId}>
                              {n.nodeName}
                            </option>
                          ))}
                        </select>
                        {errors.source && (
                          <p id="source-error" className="mt-1 text-xs text-red-600">
                            {errors.source}
                          </p>
                        )}
                      </div>

                      {/* Destination */}
                      <div>
                        <label htmlFor="destination" className="mb-1 block text-sm font-medium">
                          Destination
                        </label>
                        <p id="destination-help" className="mb-2 text-xs text-slate-500">
                          The resource you are booking. Auto-filled.
                        </p>
                        <input
                          id="destination"
                          type="text"
                          value={destinationText}
                          disabled
                          readOnly
                          aria-describedby="destination-help"
                          className={`${inputClass} cursor-not-allowed border-slate-200 bg-slate-100 text-slate-600`}
                        />
                      </div>

                      {/* Reason */}
                      <div>
                        <label htmlFor="reason" className="mb-1 block text-sm font-medium">
                          Purpose / Reason <span className="text-red-500">*</span>
                        </label>
                        <p id="reason-help" className="mb-2 text-xs text-slate-500">
                          Select the reason for this booking. This determines scheduling priority.
                        </p>
                        <select
                          id="reason"
                          value={reasonId}
                          onChange={(e) => {
                            setReasonId(e.target.value);
                            clearError("reason");
                          }}
                          aria-required="true"
                          aria-invalid={errors.reason ? "true" : "false"}
                          aria-describedby={errors.reason ? "reason-help reason-error" : "reason-help"}
                          className={`${inputClass} ${borderFor(errors.reason)}`}
                        >
                          <option value="">Select a reason</option>
                          {REASONS.map((r) => (
                            <option key={r.id} value={r.id}>
                              {r.label}
                            </option>
                          ))}
                        </select>
                        {errors.reason && (
                          <p id="reason-error" className="mt-1 text-xs text-red-600">
                            {errors.reason}
                          </p>
                        )}
                      </div>

                      {/* Notes */}
                      <div>
                        <label htmlFor="notes" className="mb-1 block text-sm font-medium">
                          Additional Notes
                        </label>
                        <p id="notes-help" className="mb-2 text-xs text-slate-500">
                          Optional. Any extra details for the coordinator.
                        </p>
                        <textarea
                          id="notes"
                          rows={3}
                          maxLength={NOTES_MAX}
                          value={notes}
                          onChange={(e) => setNotes(e.target.value.slice(0, NOTES_MAX))}
                          aria-describedby="notes-help notes-count"
                          className={`${inputClass} resize-none border-slate-200`}
                        />
                        <p id="notes-count" className="mt-1 text-right text-xs text-slate-500">
                          {notes.length} / {NOTES_MAX}
                        </p>
                      </div>
                    </div>

                    {submitError && (
                      <div
                        role="alert"
                        className="mt-4 flex items-start gap-2 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700"
                      >
                        <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
                        {submitError}
                      </div>
                    )}

                    <div className="mt-4 flex gap-3">
                      <button
                        type="button"
                        onClick={() => navigate(-1)}
                        disabled={submitting}
                        className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        Cancel
                      </button>
                      <button
                        type="submit"
                        disabled={submitting}
                        className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-70"
                      >
                        {submitting ? (
                          <>
                            <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />
                            Submitting
                          </>
                        ) : (
                          <>
                            <Send className="h-4 w-4" aria-hidden="true" />
                            Submit Request
                          </>
                        )}
                      </button>
                    </div>

                    <p className="mt-3 text-xs text-slate-400">
                      By submitting, you agree to the campus resource usage policy.
                    </p>
                  </form>
                </>
              )}
            </div>

            {/* RIGHT: sidebar */}
            <aside className="space-y-6 lg:col-span-2">
              <section aria-labelledby="how-heading" className={cardClass}>
                <h2
                  id="how-heading"
                  className="mb-3 flex items-center gap-2 text-sm font-semibold"
                >
                  <Info className="h-4 w-4 text-indigo-600" aria-hidden="true" />
                  How this works
                </h2>
                <ol className="space-y-3 text-sm text-slate-600">
                  {[
                    "DSA engine preprocesses your request and checks campus connectivity.",
                    "OS scheduler runs FCFS + Priority based on your selected reason.",
                    "DBMS commits the booking and updates resource status.",
                  ].map((text, i) => (
                    <li key={i} className="flex gap-3">
                      <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-indigo-50 text-xs font-semibold text-indigo-700">
                        {i + 1}
                      </span>
                      <span>{text}</span>
                    </li>
                  ))}
                </ol>
                <p className="mt-4 flex items-center gap-1.5 border-t border-slate-200 pt-3 text-xs text-slate-500">
                  <CheckCircle2 className="h-4 w-4 text-green-600" aria-hidden="true" />
                  Typical response time: &lt; 3 seconds
                </p>
              </section>

              {priority && (
                <section aria-labelledby="priority-heading" className={cardClass}>
                  <h2 id="priority-heading" className="mb-3 text-sm font-semibold">
                    Priority
                  </h2>
                  <span
                    className={`inline-block rounded-full px-3 py-1 text-xs font-medium ${priority.className}`}
                  >
                    {priority.label}
                  </span>
                  <p className="mt-3 text-xs text-slate-500">
                    Higher priority requests are scheduled first.
                  </p>
                </section>
              )}
            </aside>
          </div>
        )}
      </main>
    </div>
  );
}