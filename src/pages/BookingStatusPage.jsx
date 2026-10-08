import { useEffect, useState } from "react";
import { useSearchParams, useNavigate, Link } from "react-router-dom";
import {
  LogOut,
  AlertCircle,
  ChevronRight,
  CalendarClock,
  Clock,
  CheckCircle2,
  X,
  Loader2,
  Building2,
  MapPin,
} from "lucide-react";

/* ------------------------------------------------------------------ */
/* Constants                                                           */
/* ------------------------------------------------------------------ */

const TABS = ["All", "Pending", "Confirmed", "Cancelled"];

/* ------------------------------------------------------------------ */
/* Helpers                                                             */
/* ------------------------------------------------------------------ */

function parseLocalDate(dateString) {
  const [y, m, d] = String(dateString).split("-").map(Number);
  return new Date(y, m - 1, d);
}

function formatDate(dateString) {
  const dt = parseLocalDate(dateString);
  if (Number.isNaN(dt.getTime())) return dateString;
  return dt.toLocaleDateString("en-IN", {
    weekday: "short",
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

function formatTime(hhmm) {
  const [h, m] = String(hhmm).split(":").map(Number);
  if (Number.isNaN(h) || Number.isNaN(m)) return hhmm;
  const period = h >= 12 ? "PM" : "AM";
  const hour12 = h % 12 === 0 ? 12 : h % 12;
  return `${hour12}:${String(m).padStart(2, "0")} ${period}`;
}

function formatTimeRange(start, end) {
  return `${formatTime(start)} \u2013 ${formatTime(end)}`;
}

// True if the date is today or later (local time)
function isFutureDate(dateString) {
  const dt = parseLocalDate(dateString);
  if (Number.isNaN(dt.getTime())) return false;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return dt.getTime() >= today.getTime();
}

function getStatusStyles(status) {
  switch (String(status).toLowerCase()) {
    case "pending":
      return { className: "bg-amber-100 text-amber-700", label: "Pending" };
    case "confirmed":
      return { className: "bg-green-100 text-green-700", label: "Confirmed" };
    case "cancelled":
      return { className: "bg-red-100 text-red-700", label: "Cancelled" };
    default:
      return { className: "bg-slate-100 text-slate-700", label: status || "Unknown" };
  }
}

function normalizeStatus(status) {
  const s = String(status).toLowerCase();
  return TABS.find((t) => t.toLowerCase() === s) ?? status;
}

/* ------------------------------------------------------------------ */
/* Component                                                           */
/* ------------------------------------------------------------------ */

export default function BookingStatusPage({ user, onLogout }) {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  const successStatus = searchParams.get("status"); // "confirmed" | null
  const highlightId = searchParams.get("id"); // bookingId or null

  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [activeTab, setActiveTab] = useState("All");
  const [cancelError, setCancelError] = useState("");
  const [cancellingId, setCancellingId] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  const userId = user?.userId;

  // Fetch the user's bookings
  useEffect(() => {
    const controller = new AbortController();

    async function load() {
      setLoading(true);
      setError(false);
      try {
        const params = new URLSearchParams({ userId: String(userId ?? "") });
        const res = await fetch(`/api/bookings?${params.toString()}`, {
          signal: controller.signal,
        });
        if (!res.ok) throw new Error(`Request failed: ${res.status}`);
        const data = await res.json();
        if (!Array.isArray(data)) throw new Error("Unexpected response");

        const sorted = [...data].sort(
          (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        setBookings(sorted);
        setLoading(false);
      } catch (err) {
        if (err.name === "AbortError") return;
        setBookings([]);
        setError(true);
        setLoading(false);
      }
    }

    load();
    return () => controller.abort();
  }, [userId, reloadKey]);

  function dismissBanner() {
    const next = new URLSearchParams(searchParams);
    next.delete("status");
    setSearchParams(next, { replace: true });
  }

  async function handleCancel(booking) {
    if (!window.confirm("Cancel this booking?")) return;

    setCancelError("");
    setCancellingId(booking.bookingId);
    try {
      const res = await fetch(`/api/bookings/${encodeURIComponent(booking.bookingId)}/cancel`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId }),
      });
      if (!res.ok) throw new Error(`Request failed: ${res.status}`);

      setBookings((prev) =>
        prev.map((b) => (b.bookingId === booking.bookingId ? { ...b, status: "Cancelled" } : b))
      );
    } catch {
      setCancelError("Couldn't cancel. Please try again.");
    } finally {
      setCancellingId(null);
    }
  }

  // Counts and filtered list
  const counts = { All: bookings.length, Pending: 0, Confirmed: 0, Cancelled: 0 };
  bookings.forEach((b) => {
    const key = normalizeStatus(b.status);
    if (counts[key] !== undefined && key !== "All") counts[key] += 1;
  });

  const visible =
    activeTab === "All"
      ? bookings
      : bookings.filter((b) => normalizeStatus(b.status) === activeTab);

  const linkClass =
    "rounded hover:text-indigo-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600";

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">
      {/* Top bar */}
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3 sm:px-6">
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

      <main className="mx-auto max-w-5xl px-4 py-6 sm:px-6">
        {/* Success banner */}
        {successStatus === "confirmed" && (
          <div
            role="status"
            aria-live="polite"
            className="mb-5 flex items-start gap-4 rounded-xl border border-green-200 bg-green-50 p-4"
          >
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-green-100">
              <CheckCircle2 className="h-6 w-6 text-green-600" aria-hidden="true" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-lg font-semibold text-green-800">Booking Confirmed</p>
              <p className="text-sm text-green-700">
                Your request has been received and scheduled. You&apos;ll find it below.
              </p>
            </div>
            <button
              type="button"
              onClick={dismissBanner}
              aria-label="Dismiss confirmation"
              className="rounded-lg p-1.5 text-green-700 hover:bg-green-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
            >
              <X className="h-5 w-5" aria-hidden="true" />
            </button>
          </div>
        )}

        {/* Breadcrumb */}
        <nav aria-label="Breadcrumb" className="mb-5">
          <ol className="flex flex-wrap items-center gap-1 text-sm text-slate-500">
            <li>
              <Link to="/dashboard" className={linkClass}>
                Dashboard
              </Link>
            </li>
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
            <li aria-current="page" className="font-medium text-slate-800">
              My Bookings
            </li>
          </ol>
        </nav>

        {/* Page header */}
        <div className="mb-6">
          <p className="text-xs uppercase text-slate-500">Your Requests</p>
          <h1 className="text-3xl font-bold">My Bookings</h1>
          <p className="text-sm text-slate-500">
            View all your booking requests and their current status.
          </p>
        </div>

        {/* Filter tabs */}
        <div className="mb-5 overflow-x-auto border-b border-slate-200">
          <div role="tablist" aria-label="Filter bookings by status" className="flex min-w-max gap-6">
            {TABS.map((tab) => {
              const selected = activeTab === tab;
              return (
                <button
                  key={tab}
                  type="button"
                  role="tab"
                  id={`tab-${tab}`}
                  aria-selected={selected}
                  aria-controls="bookings-panel"
                  onClick={() => setActiveTab(tab)}
                  className={`-mb-px whitespace-nowrap border-b-2 px-1 pb-3 text-sm font-medium focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 ${
                    selected
                      ? "border-indigo-600 text-indigo-600"
                      : "border-transparent text-slate-500 hover:text-slate-800"
                  }`}
                >
                  {tab} ({counts[tab]})
                </button>
              );
            })}
          </div>
        </div>

        {/* Cancel error */}
        {cancelError && (
          <div
            role="alert"
            className="mb-4 flex items-center justify-between gap-3 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700"
          >
            <span className="flex items-center gap-2">
              <AlertCircle className="h-4 w-4 shrink-0" aria-hidden="true" />
              {cancelError}
            </span>
            <button
              type="button"
              onClick={() => setCancelError("")}
              aria-label="Dismiss error"
              className="rounded p-1 hover:bg-red-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
            >
              <X className="h-4 w-4" aria-hidden="true" />
            </button>
          </div>
        )}

        {/* Bookings list */}
        <div id="bookings-panel" role="tabpanel" aria-labelledby={`tab-${activeTab}`} aria-busy={loading}>
          {loading ? (
            <div className="space-y-3">
              {[0, 1, 2].map((i) => (
                <div key={i} className="h-32 animate-pulse rounded-xl bg-slate-200" />
              ))}
            </div>
          ) : error ? (
            <div
              role="alert"
              className="flex flex-col gap-3 rounded-xl border border-red-200 bg-red-50 p-4 sm:flex-row sm:items-center sm:justify-between"
            >
              <div className="flex items-center gap-2 text-sm text-red-700">
                <AlertCircle className="h-5 w-5 shrink-0" aria-hidden="true" />
                Couldn&apos;t load your bookings. Please try again.
              </div>
              <button
                type="button"
                onClick={() => setReloadKey((k) => k + 1)}
                className="self-start rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 sm:self-auto"
              >
                Retry
              </button>
            </div>
          ) : bookings.length === 0 ? (
            <div className="flex flex-col items-center rounded-xl border border-slate-200 bg-white px-4 py-12 text-center">
              <CalendarClock className="mb-3 h-10 w-10 text-slate-400" aria-hidden="true" />
              <p className="text-lg font-semibold">No bookings yet</p>
              <p className="mb-5 text-sm text-slate-500">
                Start by browsing a department from the dashboard.
              </p>
              <button
                type="button"
                onClick={() => navigate("/dashboard")}
                className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2"
              >
                Browse Departments
              </button>
            </div>
          ) : visible.length === 0 ? (
            <p className="py-12 text-center text-sm text-slate-500">
              No {activeTab.toLowerCase()} bookings.
            </p>
          ) : (
            <ul className="space-y-3">
              {visible.map((b) => {
                const badge = getStatusStyles(b.status);
                const statusKey = normalizeStatus(b.status);
                const cancellable = statusKey === "Pending" || statusKey === "Confirmed";
                const future = isFutureDate(b.date);
                const highlighted = highlightId !== null && String(b.bookingId) === highlightId;
                const isCancelling = cancellingId === b.bookingId;

                return (
                  <li
                    key={b.bookingId}
                    className={`flex flex-col gap-4 rounded-xl border border-slate-200 bg-white p-4 sm:flex-row sm:items-start sm:justify-between sm:p-5 ${
                      highlighted ? "ring-2 ring-indigo-400" : ""
                    }`}
                  >
                    {/* Left */}
                    <div className="min-w-0">
                      <p className="text-lg font-semibold">{b.resourceName}</p>
                      <p className="mt-0.5 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-slate-500">
                        <span>{b.departmentName}</span>
                        <span className="inline-flex items-center gap-1">
                          <Building2 className="h-3.5 w-3.5" aria-hidden="true" />
                          {b.buildingName}
                        </span>
                        <span className="inline-flex items-center gap-1">
                          <MapPin className="h-3.5 w-3.5" aria-hidden="true" />
                          Room {b.roomNumber}
                        </span>
                      </p>

                      <div className="mt-3 grid grid-cols-1 gap-2 text-sm sm:grid-cols-2 sm:gap-x-6">
                        <p className="flex items-center gap-2">
                          <CalendarClock className="h-4 w-4 shrink-0 text-indigo-600" aria-hidden="true" />
                          {formatDate(b.date)}
                        </p>
                        <p className="flex items-center gap-2">
                          <Clock className="h-4 w-4 shrink-0 text-indigo-600" aria-hidden="true" />
                          {formatTimeRange(b.startTime, b.endTime)}
                        </p>
                      </div>

                      {b.reasonLabel && (
                        <span className="mt-3 inline-block rounded-full bg-slate-100 px-2.5 py-0.5 text-xs text-slate-700">
                          {b.reasonLabel}
                        </span>
                      )}
                    </div>

                    {/* Right */}
                    <div className="flex shrink-0 items-center justify-between gap-3 sm:flex-col sm:items-end">
                      <span className={`rounded-full px-3 py-1 text-xs font-medium ${badge.className}`}>
                        {badge.label}
                      </span>

                      {cancellable &&
                        (future ? (
                          <button
                            type="button"
                            onClick={() => handleCancel(b)}
                            disabled={isCancelling}
                            aria-label={`Cancel booking for ${b.resourceName}`}
                            className="inline-flex items-center gap-1.5 rounded-lg border border-red-600 px-3 py-1.5 text-xs font-medium text-red-600 hover:bg-red-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 disabled:cursor-not-allowed disabled:opacity-60"
                          >
                            {isCancelling && (
                              <Loader2 className="h-3.5 w-3.5 animate-spin" aria-hidden="true" />
                            )}
                            Cancel
                          </button>
                        ) : (
                          <span className="text-xs text-slate-400">Past</span>
                        ))}
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </main>
    </div>
  );
}