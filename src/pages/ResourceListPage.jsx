import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
  ArrowRight,
  LogOut,
  AlertCircle,
  ChevronRight,
  FlaskConical,
  GraduationCap,
  Users,
  BookOpen,
} from "lucide-react";

/* ------------------------------------------------------------------ */
/* Constants                                                           */
/* ------------------------------------------------------------------ */

const DEPT_NAMES = { A1: "CSE", B1: "Electrical", C1: "AIML", D1: "Mechanical" };
const TYPE_NAMES = { 1: "Labs", 2: "Classrooms", 3: "Conference Halls" };
const TYPE_ICONS = { 1: FlaskConical, 2: GraduationCap, 3: Users };

const DAY_START_HOUR = 10; // 10:00 AM
const DAY_END_HOUR = 18; // 6:00 PM
const MAX_DAYS_AHEAD = 30;

/* ------------------------------------------------------------------ */
/* Helpers                                                             */
/* ------------------------------------------------------------------ */

const pad = (n) => String(n).padStart(2, "0");

// Local-time "YYYY-MM-DD" (avoids the UTC shift that toISOString() causes)
function toDateString(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function getToday() {
  return toDateString(new Date());
}

function getMaxDate() {
  const d = new Date();
  d.setDate(d.getDate() + MAX_DAYS_AHEAD);
  return toDateString(d);
}

function isSunday(dateString) {
  const [y, m, d] = dateString.split("-").map(Number);
  return new Date(y, m - 1, d).getDay() === 0;
}

// Today, or Monday if today happens to be a Sunday
function getDefaultDate() {
  const d = new Date();
  if (d.getDay() === 0) d.setDate(d.getDate() + 1);
  return toDateString(d);
}

function formatTime(hhmm) {
  const [h, m] = hhmm.split(":").map(Number);
  const period = h >= 12 ? "PM" : "AM";
  const hour12 = h % 12 === 0 ? 12 : h % 12;
  return `${hour12}:${pad(m)} ${period}`;
}

function formatTimeRange(start, end) {
  return `${formatTime(start)} \u2013 ${formatTime(end)}`;
}

// Classroom: 1-hour slots. Lab / Conference Hall: 2-hour slots.
function generateSlots(typeId) {
  const duration = Number(typeId) === 2 ? 1 : 2;
  const slots = [];
  for (let h = DAY_START_HOUR; h + duration <= DAY_END_HOUR; h += duration) {
    const start = `${pad(h)}:00`;
    const end = `${pad(h + duration)}:00`;
    slots.push({ start, end, label: formatTimeRange(start, end) });
  }
  return slots;
}

/* ------------------------------------------------------------------ */
/* Component                                                           */
/* ------------------------------------------------------------------ */

export default function ResourceListPage({ user, onLogout }) {
  const { deptId, typeId } = useParams();
  const navigate = useNavigate();

  const [date, setDate] = useState(getDefaultDate);
  const [slotIndex, setSlotIndex] = useState(0);
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [dateError, setDateError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  const deptName = DEPT_NAMES[deptId] ?? deptId;
  const typeName = TYPE_NAMES[typeId] ?? "Resources";
  const TypeIcon = TYPE_ICONS[typeId] ?? BookOpen;

  const slots = generateSlots(typeId);
  const selectedSlot = slots[slotIndex] ?? slots[0];
  const startTime = selectedSlot.start;
  const endTime = selectedSlot.end;

  const minDate = getToday();
  const maxDate = getMaxDate();

  // Reset the slot when navigating between categories (different slot lists)
  useEffect(() => {
    setSlotIndex(0);
  }, [typeId]);

  // Fetch resources on mount and whenever the filters change
  useEffect(() => {
    const controller = new AbortController();

    async function load() {
      setLoading(true);
      setError(false);
      try {
        const params = new URLSearchParams({
          deptId,
          typeId,
          date,
          startTime,
          endTime,
        });
        const res = await fetch(`/api/resources?${params.toString()}`, {
          signal: controller.signal,
        });
        if (!res.ok) throw new Error(`Request failed: ${res.status}`);
        const data = await res.json();
        if (!Array.isArray(data)) throw new Error("Unexpected response");
        setResources(data);
        setLoading(false);
      } catch (err) {
        if (err.name === "AbortError") return;
        setResources([]);
        setError(true);
        setLoading(false);
      }
    }

    load();
    return () => controller.abort();
  }, [deptId, typeId, date, startTime, endTime, reloadKey]);

  function handleDateChange(e) {
    const value = e.target.value;
    if (!value) return; // user cleared the field; keep the current date

    if (isSunday(value)) {
      setDateError("Sundays are not available");
      setDate(getDefaultDate());
      return;
    }
    if (value < minDate || value > maxDate) {
      setDateError(`Pick a date between ${minDate} and ${maxDate}`);
      setDate(getDefaultDate());
      return;
    }
    setDateError("");
    setDate(value);
  }

  function handleBook(resourceId) {
    const params = new URLSearchParams({ date, startTime, endTime });
    navigate(`/dashboard/book/${resourceId}?${params.toString()}`);
  }

  const fieldClass =
    "rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-800 " +
    "focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600";

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
              {user?.role && (
                <span className="text-slate-500"> &middot; {user.role}</span>
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

      <main className="mx-auto max-w-5xl px-4 py-6 sm:px-6">
        {/* Breadcrumb */}
        <nav aria-label="Breadcrumb" className="mb-5">
          <ol className="flex flex-wrap items-center gap-1 text-sm text-slate-500">
            <li>
              <Link
                to="/dashboard"
                className="rounded hover:text-indigo-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
              >
                Dashboard
              </Link>
            </li>
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
            <li>
              <Link
                to={`/dashboard/department/${deptId}`}
                className="rounded hover:text-indigo-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
              >
                {deptName}
              </Link>
            </li>
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
            <li aria-current="page" className="font-medium text-slate-800">
              {typeName}
            </li>
          </ol>
        </nav>

        {/* Page header */}
        <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="text-xs uppercase text-slate-500">{deptName}</p>
            <h1 className="text-3xl font-bold">{typeName}</h1>
            <p className="text-sm text-slate-500">
              Select a date and time to see availability
            </p>
          </div>

          <div className="flex gap-3">
            <div className="flex flex-1 flex-col md:flex-none">
              <label htmlFor="booking-date" className="mb-1 text-xs text-slate-500">
                Date
              </label>
              <input
                id="booking-date"
                type="date"
                value={date}
                min={minDate}
                max={maxDate}
                onChange={handleDateChange}
                aria-invalid={dateError ? "true" : "false"}
                aria-describedby={dateError ? "booking-date-error" : undefined}
                className={fieldClass}
              />
            </div>
            <div className="flex flex-1 flex-col md:flex-none">
              <label htmlFor="booking-slot" className="mb-1 text-xs text-slate-500">
                Time Slot
              </label>
              <select
                id="booking-slot"
                value={slotIndex}
                onChange={(e) => setSlotIndex(Number(e.target.value))}
                className={fieldClass}
              >
                {slots.map((slot, i) => (
                  <option key={slot.start} value={i}>
                    {slot.label}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {dateError && (
          <p
            id="booking-date-error"
            role="alert"
            className="mt-2 text-xs text-red-600 md:text-right"
          >
            {dateError}
          </p>
        )}

        {/* Legend */}
        <div className="mt-5 mb-3 flex items-center gap-4 text-xs text-slate-500">
          <span className="inline-flex items-center gap-1.5">
            <span className="h-2.5 w-2.5 rounded-full bg-green-500" aria-hidden="true" />
            Available
          </span>
          <span className="inline-flex items-center gap-1.5">
            <span className="h-2.5 w-2.5 rounded-full bg-red-500" aria-hidden="true" />
            Occupied
          </span>
        </div>

        {/* Resource list */}
        <div aria-busy={loading}>
          {loading ? (
            <div className="space-y-3">
              {[0, 1, 2, 3, 4].map((i) => (
                <div
                  key={i}
                  className="h-20 animate-pulse rounded-xl bg-slate-200"
                />
              ))}
            </div>
          ) : error ? (
            <div
              role="alert"
              className="flex flex-col gap-3 rounded-xl border border-red-200 bg-red-50 p-4 sm:flex-row sm:items-center sm:justify-between"
            >
              <div className="flex items-center gap-2 text-sm text-red-700">
                <AlertCircle className="h-5 w-5 shrink-0" aria-hidden="true" />
                Couldn&apos;t load resources. Please try again.
              </div>
              <button
                type="button"
                onClick={() => setReloadKey((k) => k + 1)}
                className="self-start rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2 sm:self-auto"
              >
                Retry
              </button>
            </div>
          ) : resources.length === 0 ? (
            <div className="flex flex-col items-center rounded-xl border border-slate-200 bg-white px-4 py-12 text-center">
              <BookOpen className="mb-3 h-8 w-8 text-slate-400" aria-hidden="true" />
              <p className="text-sm text-slate-500">
                No resources found in this category.
              </p>
            </div>
          ) : (
            <ul className="space-y-3">
              {resources.map((r) => {
                const available = r.status === "Available";

                const content = (
                  <>
                    <div className="flex min-w-0 items-center gap-4">
                      <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-indigo-50">
                        <TypeIcon
                          className="h-6 w-6 text-indigo-600"
                          aria-hidden="true"
                        />
                      </div>
                      <div className="min-w-0 text-left">
                        <p className="truncate text-lg font-semibold">
                          {r.resourceName}
                        </p>
                        <p className="text-xs text-slate-500">
                          Capacity: {r.capacity}
                        </p>
                      </div>
                    </div>

                    <div className="flex shrink-0 items-center gap-3">
                      <span
                        className={`rounded-full px-3 py-1 text-xs font-medium ${
                          available
                            ? "bg-green-100 text-green-700"
                            : "bg-red-100 text-red-700"
                        }`}
                      >
                        {available ? "Available" : "Occupied"}
                      </span>
                      {available && (
                        <ArrowRight
                          className="h-5 w-5 text-slate-400"
                          aria-hidden="true"
                        />
                      )}
                    </div>
                  </>
                );

                const base =
                  "flex w-full items-center justify-between gap-3 rounded-xl border border-slate-200 bg-white p-4 " +
                  "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600";

                return (
                  <li key={r.resourceId}>
                    {available ? (
                      <button
                        type="button"
                        aria-label={`Book ${r.resourceName}`}
                        onClick={() => handleBook(r.resourceId)}
                        className={`${base} cursor-pointer transition-shadow hover:border-indigo-300 hover:shadow-md`}
                      >
                        {content}
                      </button>
                    ) : (
                      <div
                        aria-disabled="true"
                        className={`${base} cursor-not-allowed opacity-70`}
                      >
                        {content}
                      </div>
                    )}
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