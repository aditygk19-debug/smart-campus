import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  LogOut,
  AlertCircle,
  ChevronRight,
  Loader2,
  CalendarCheck,
  TrendingUp,
  Clock,
  Timer,
  FlaskConical,
  GraduationCap,
  Users,
  Download,
  Printer,
  ArrowLeft,
  BarChart3,
  Activity,
} from "lucide-react";

/* ------------------------------------------------------------------ */
/* Constants                                                           */
/* ------------------------------------------------------------------ */

const TREND_BAR_AREA_PX = 200; // height that the tallest bar reaches
const TREND_LABEL_ROOM_PX = 24; // space above the tallest bar for its count

const TYPE_FILL = {
  lab: "bg-indigo-500",
  classroom: "bg-emerald-500",
  hall: "bg-amber-500",
  other: "bg-slate-500",
};

const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

/* ------------------------------------------------------------------ */
/* Helpers                                                             */
/* ------------------------------------------------------------------ */

function calcMax(arr, key) {
  return arr.reduce((max, item) => Math.max(max, Number(item[key]) || 0), 0);
}

function percent(value, max) {
  return max > 0 ? (value / max) * 100 : 0;
}

const clampPct = (n) => Math.min(100, Math.max(0, n));

// "14:00" -> "2:00 PM"
function formatHour(hhmm) {
  if (!hhmm) return "\u2013";
  const [h, m] = String(hhmm).split(":").map(Number);
  if (Number.isNaN(h) || Number.isNaN(m)) return hhmm;
  const period = h >= 12 ? "PM" : "AM";
  const hour12 = h % 12 === 0 ? 12 : h % 12;
  return `${hour12}:${String(m).padStart(2, "0")} ${period}`;
}

// "2026-10-02" -> "Oct 02"
function formatDateShort(dateString) {
  const [y, m, d] = String(dateString).split("-").map(Number);
  if (!y || !m || !d) return dateString;
  return `${MONTHS[m - 1]} ${String(d).padStart(2, "0")}`;
}

function getTypeKey(typeName) {
  const t = String(typeName).toLowerCase();
  if (t.includes("lab")) return "lab";
  if (t.includes("class")) return "classroom";
  if (t.includes("hall") || t.includes("conference")) return "hall";
  return "other";
}

function getTypeIcon(typeName) {
  const key = getTypeKey(typeName);
  if (key === "lab") return FlaskConical;
  if (key === "classroom") return GraduationCap;
  return Users;
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

/* ------------------------------------------------------------------ */
/* Small presentational pieces                                         */
/* ------------------------------------------------------------------ */

function StatCard({ icon: Icon, value, suffix, label, hint }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5 print:break-inside-avoid">
      <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-50">
        <Icon className="h-5 w-5 text-indigo-600" aria-hidden="true" />
      </div>
      <p className="text-3xl font-bold">
        {value}
        {suffix && value !== "\u2013" && (
          <span className="ml-1 text-base font-medium text-slate-500">{suffix}</span>
        )}
      </p>
      <p className="text-sm text-slate-500">{label}</p>
      {hint && <p className="mt-1 text-xs text-slate-500">{hint}</p>}
    </div>
  );
}

function ProgressBar({ value, fillClass, label }) {
  const pct = Math.round(clampPct(value));
  return (
    <div
      role="progressbar"
      aria-valuenow={pct}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-label={label}
      title={`${label}: ${pct}%`}
      className="h-2.5 w-full overflow-hidden rounded-full bg-slate-100"
    >
      <div className={`h-full rounded-full ${fillClass}`} style={{ width: `${pct}%` }} />
    </div>
  );
}

function EmptyMessage({ children }) {
  return (
    <div className="flex flex-col items-center py-10 text-center">
      <BarChart3 className="mb-2 h-8 w-8 text-slate-300" aria-hidden="true" />
      <p className="text-sm text-slate-500">{children}</p>
    </div>
  );
}

function SkeletonBlock({ className }) {
  return <div className={`animate-pulse rounded-xl bg-slate-200 ${className}`} />;
}

/* ------------------------------------------------------------------ */
/* Component                                                           */
/* ------------------------------------------------------------------ */

export default function AdminReportsPage({ user, onLogout }) {
  const [summary, setSummary] = useState(null);
  const [byDepartment, setByDepartment] = useState([]);
  const [byType, setByType] = useState([]);
  const [peakHours, setPeakHours] = useState([]);
  const [trend, setTrend] = useState([]);

  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [failed, setFailed] = useState({
    summary: false,
    byDepartment: false,
    byType: false,
    peakHours: false,
    trend: false,
  });
  const [reloadKey, setReloadKey] = useState(0);
  const [range, setRange] = useState("7");

  // Load all five datasets in parallel. A failed call keeps any data it had.
  useEffect(() => {
    const controller = new AbortController();
    const { signal } = controller;

    async function loadAll() {
      setRefreshing(true);

      const [s, d, t, p, tr] = await Promise.all([
        request("/api/admin/reports/summary", signal, isObject),
        request("/api/admin/reports/by-department", signal, Array.isArray),
        request("/api/admin/reports/by-resource-type", signal, Array.isArray),
        request("/api/admin/reports/peak-hours", signal, Array.isArray),
        request("/api/admin/reports/booking-trend?days=7", signal, Array.isArray),
      ]);

      if (signal.aborted) return;

      if (s.ok) setSummary(s.data);
      if (d.ok) setByDepartment(d.data);
      if (t.ok) setByType(t.data);
      if (p.ok) setPeakHours(p.data);
      if (tr.ok) setTrend(tr.data);

      setFailed({
        summary: !s.ok,
        byDepartment: !d.ok,
        byType: !t.ok,
        peakHours: !p.ok,
        trend: !tr.ok,
      });
      setLoading(false);
      setRefreshing(false);
    }

    loadAll();
    return () => controller.abort();
  }, [reloadKey]);

  const retry = () => setReloadKey((k) => k + 1);

  const failedCount = Object.values(failed).filter(Boolean).length;
  const allFailed = failedCount === 5;
  const partialFailed = failedCount > 0 && !allFailed;

  // Derived chart data
  const trendMax = calcMax(trend, "count");
  const peakMax = calcMax(peakHours, "count");
  const typeTotal = byType.reduce((sum, t) => sum + (Number(t.totalBookings) || 0), 0);

  const peakHourKey =
    summary?.peakHour ??
    (peakMax > 0 ? peakHours.find((h) => Number(h.count) === peakMax)?.hour : undefined);

  const cardClass = "rounded-xl border border-slate-200 bg-white p-5 print:break-inside-avoid";
  const linkClass =
    "rounded hover:text-indigo-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600";
  const outlineBtn =
    "inline-flex items-center gap-2 rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 " +
    "hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600";

  const retryButton = (tone) => (
    <button
      type="button"
      onClick={retry}
      disabled={refreshing}
      className={`inline-flex items-center gap-2 self-start rounded-lg px-4 py-2 text-sm font-medium focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 disabled:cursor-not-allowed disabled:opacity-70 sm:self-auto ${
        tone === "red"
          ? "bg-indigo-600 text-white hover:bg-indigo-700 focus-visible:ring-offset-2"
          : "border border-amber-300 bg-white text-amber-800 hover:bg-amber-100"
      }`}
    >
      {refreshing && <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />}
      Retry
    </button>
  );

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 print:bg-white">
      {/* Top bar */}
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white print:hidden">
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
            <div className="rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700">
              Admin
              {user?.userId && (
                <span className="hidden font-normal text-indigo-600 sm:inline">
                  {" "}
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
        {/* Breadcrumb */}
        <nav aria-label="Breadcrumb" className="mb-5 print:hidden">
          <ol className="flex flex-wrap items-center gap-1 text-sm text-slate-500">
            <li>
              <Link to="/admin" className={linkClass}>
                Admin
              </Link>
            </li>
            <ChevronRight className="h-4 w-4" aria-hidden="true" />
            <li aria-current="page" className="font-medium text-slate-800">
              Reports
            </li>
          </ol>
        </nav>

        {/* Error banners */}
        {allFailed && !loading && (
          <div
            role="alert"
            className="mb-5 flex flex-col gap-3 rounded-xl border border-red-200 bg-red-50 p-4 sm:flex-row sm:items-center sm:justify-between print:hidden"
          >
            <div className="flex items-center gap-2 text-sm text-red-700">
              <AlertCircle className="h-5 w-5 shrink-0" aria-hidden="true" />
              Couldn&apos;t load reports. Retry.
            </div>
            {retryButton("red")}
          </div>
        )}
        {partialFailed && (
          <div
            role="alert"
            className="mb-5 flex flex-col gap-3 rounded-xl border border-amber-200 bg-amber-50 p-4 sm:flex-row sm:items-center sm:justify-between print:hidden"
          >
            <div className="flex items-center gap-2 text-sm text-amber-800">
              <AlertCircle className="h-5 w-5 shrink-0" aria-hidden="true" />
              Some reports couldn&apos;t be loaded. Retry.
            </div>
            {retryButton("amber")}
          </div>
        )}

        {/* Page header */}
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-xs uppercase text-slate-500">Analytics</p>
            <h1 className="text-3xl font-bold">Reports &amp; Analytics</h1>
            <p className="text-sm text-slate-500">
              Utilization trends, peak hours, and department-wise statistics.
            </p>
          </div>

          <div className="flex flex-col print:hidden">
            <label htmlFor="date-range" className="mb-1 text-xs text-slate-500">
              Date range
            </label>
            <select
              id="date-range"
              value={range}
              onChange={(e) => setRange(e.target.value)}
              className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-800 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
            >
              <option value="7">Last 7 days</option>
              <option value="30" disabled>
                Last 30 days (coming soon)
              </option>
              <option value="all" disabled>
                All time (coming soon)
              </option>
            </select>
          </div>
        </div>

        {/* Stats row */}
        <section
          aria-label="Report summary"
          className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4"
        >
          {loading ? (
            [0, 1, 2, 3].map((i) => <SkeletonBlock key={i} className="h-36" />)
          ) : (
            <>
              <StatCard
                icon={CalendarCheck}
                value={summary ? Number(summary.totalBookings).toLocaleString("en-IN") : "\u2013"}
                label="Total Bookings"
                hint={
                  summary &&
                  summary.completedBookings != null &&
                  summary.cancelledBookings != null
                    ? `${summary.completedBookings} completed \u00B7 ${summary.cancelledBookings} cancelled`
                    : undefined
                }
              />
              <StatCard
                icon={TrendingUp}
                value={summary ? `${summary.utilizationRate}%` : "\u2013"}
                label="Utilization Rate"
              />
              <StatCard
                icon={Clock}
                value={summary ? formatHour(summary.peakHour) : "\u2013"}
                label="Peak Hour"
              />
              <StatCard
                icon={Timer}
                value={summary ? String(summary.avgBookingDuration) : "\u2013"}
                suffix="hrs"
                label="Avg. Duration"
              />
            </>
          )}
        </section>

        {/* Chart 1: booking trend */}
        <section aria-labelledby="trend-heading" className={`${cardClass} mt-6`}>
          <div className="mb-4 flex items-baseline gap-2">
            <h2 id="trend-heading" className="flex items-center gap-2 text-sm font-semibold">
              <BarChart3 className="h-4 w-4 text-indigo-600" aria-hidden="true" />
              Booking Trend
            </h2>
            <span className="text-xs text-slate-500">Last 7 days</span>
          </div>

          {loading ? (
            <SkeletonBlock className="h-64" />
          ) : trend.length === 0 || trendMax === 0 ? (
            <EmptyMessage>
              {failed.trend
                ? "Trend data is unavailable right now."
                : "No booking trend data available."}
            </EmptyMessage>
          ) : (
            <div className="flex gap-2">
              {/* y-axis */}
              <div
                className="relative w-7 shrink-0 text-right text-xs text-slate-500"
                style={{ height: `${TREND_BAR_AREA_PX + TREND_LABEL_ROOM_PX}px` }}
                aria-hidden="true"
              >
                <span
                  className="absolute right-0 -translate-y-1/2"
                  style={{ top: `${TREND_LABEL_ROOM_PX}px` }}
                >
                  {trendMax}
                </span>
                <span className="absolute bottom-0 right-0 translate-y-1/2">0</span>
              </div>

              {/* bars */}
              <div className="relative flex-1">
                <div
                  className="absolute left-0 right-0 border-t border-dashed border-slate-200"
                  style={{ top: `${TREND_LABEL_ROOM_PX}px` }}
                  aria-hidden="true"
                />
                <ul
                  className="flex items-end gap-1.5 border-b border-slate-200 sm:gap-3"
                  style={{ height: `${TREND_BAR_AREA_PX + TREND_LABEL_ROOM_PX}px` }}
                >
                  {trend.map((day) => {
                    const count = Number(day.count) || 0;
                    const label = `${formatDateShort(day.date)}: ${count} bookings`;
                    return (
                      <li
                        key={day.date}
                        aria-label={label}
                        className="flex min-w-0 flex-1 flex-col items-center justify-end"
                      >
                        <span className="mb-1 text-xs font-medium">{count}</span>
                        <div
                          title={label}
                          className="w-full max-w-[44px] rounded-t-md bg-indigo-500 hover:bg-indigo-600"
                          style={{ height: `${percent(count, trendMax) * (TREND_BAR_AREA_PX / 100)}px` }}
                        />
                      </li>
                    );
                  })}
                </ul>
                <ul className="mt-2 flex gap-1.5 sm:gap-3" aria-hidden="true">
                  {trend.map((day) => (
                    <li
                      key={day.date}
                      className="min-w-0 flex-1 text-center text-[10px] text-slate-500 sm:text-xs"
                    >
                      {formatDateShort(day.date)}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          )}
        </section>

        {/* Chart 2: peak hours + resource types */}
        <div className="mt-6 grid gap-6 lg:grid-cols-2">
          <section aria-labelledby="peak-heading" className={cardClass}>
            <h2 id="peak-heading" className="mb-4 flex items-center gap-2 text-sm font-semibold">
              <Activity className="h-4 w-4 text-indigo-600" aria-hidden="true" />
              Peak Hours
            </h2>

            {loading ? (
              <SkeletonBlock className="h-64" />
            ) : peakHours.length === 0 ? (
              <EmptyMessage>
                {failed.peakHours ? "Peak-hour data is unavailable right now." : "No peak-hour data yet."}
              </EmptyMessage>
            ) : (
              <ul className="space-y-2">
                {peakHours.map((h) => {
                  const count = Number(h.count) || 0;
                  const isPeak = peakHourKey !== undefined && h.hour === peakHourKey;
                  return (
                    <li key={h.hour} className="flex items-center gap-3">
                      <span className="w-16 shrink-0 text-xs text-slate-600">{formatHour(h.hour)}</span>
                      <div className="h-6 flex-1 rounded-md bg-slate-100 sm:h-7">
                        <div
                          title={`${formatHour(h.hour)}: ${count} bookings`}
                          className={`h-full rounded-r-md ${
                            isPeak ? "bg-indigo-700" : "bg-gradient-to-r from-indigo-400 to-indigo-600"
                          }`}
                          style={{ width: `${percent(count, peakMax)}%` }}
                        />
                      </div>
                      <span className="flex w-16 shrink-0 items-center justify-end gap-1.5 text-xs">
                        <span className="font-medium">{count}</span>
                        {isPeak && (
                          <span className="rounded-full bg-indigo-100 px-1.5 py-0.5 text-[10px] font-medium text-indigo-700">
                            Peak
                          </span>
                        )}
                      </span>
                    </li>
                  );
                })}
              </ul>
            )}
          </section>

          <section aria-labelledby="type-heading" className={cardClass}>
            <h2 id="type-heading" className="mb-4 text-sm font-semibold">
              By Resource Type
            </h2>

            {loading ? (
              <SkeletonBlock className="h-64" />
            ) : byType.length === 0 ? (
              <EmptyMessage>
                {failed.byType ? "Resource type data is unavailable right now." : "No resource type data yet."}
              </EmptyMessage>
            ) : (
              <>
                <ul className="space-y-5">
                  {byType.map((t) => {
                    const count = Number(t.totalBookings) || 0;
                    const pct = Math.round(percent(count, typeTotal));
                    const Icon = getTypeIcon(t.typeName);
                    return (
                      <li key={t.typeName}>
                        <div className="mb-2 flex items-center justify-between">
                          <span className="flex items-center gap-2 text-sm font-medium">
                            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-50">
                              <Icon className="h-4 w-4 text-indigo-600" aria-hidden="true" />
                            </span>
                            {t.typeName}
                          </span>
                          <span className="text-sm font-semibold">{count}</span>
                        </div>
                        <div className="flex items-center gap-3">
                          <ProgressBar
                            value={pct}
                            fillClass={TYPE_FILL[getTypeKey(t.typeName)]}
                            label={`${t.typeName} share of bookings`}
                          />
                          <span className="w-10 shrink-0 text-right text-xs text-slate-500">{pct}%</span>
                        </div>
                      </li>
                    );
                  })}
                </ul>
                <p className="mt-5 border-t border-slate-200 pt-3 text-sm text-slate-600">
                  Total: <span className="font-semibold">{typeTotal}</span> bookings
                </p>
              </>
            )}
          </section>
        </div>

        {/* Chart 3: department utilization */}
        <section aria-labelledby="dept-heading" className={`${cardClass} mt-6`}>
          <h2 id="dept-heading" className="mb-2 text-sm font-semibold">
            Department Utilization
          </h2>

          {loading ? (
            <SkeletonBlock className="h-48" />
          ) : byDepartment.length === 0 ? (
            <EmptyMessage>
              {failed.byDepartment ? "Department data is unavailable right now." : "No department data yet."}
            </EmptyMessage>
          ) : (
            <ul>
              {byDepartment.map((d) => {
                const rate = Number(d.utilizationRate) || 0;
                return (
                  <li key={d.departmentName} className="border-b border-slate-200 py-4 last:border-b-0 last:pb-0">
                    <div className="mb-2 flex items-center justify-between gap-3">
                      <span className="text-sm font-medium">{d.departmentName}</span>
                      <span className="text-xs text-slate-500">
                        {d.totalBookings} bookings &middot; {rate}%
                      </span>
                    </div>
                    <ProgressBar
                      value={rate}
                      fillClass="bg-indigo-500"
                      label={`${d.departmentName} utilization`}
                    />
                  </li>
                );
              })}
            </ul>
          )}
        </section>

        {/* Actions */}
        <div className="mt-6 flex flex-wrap items-center gap-3 print:hidden">
          <span title="Coming soon">
            <button
              type="button"
              disabled
              aria-label="Export as CSV (coming soon)"
              className={`${outlineBtn} cursor-not-allowed opacity-60 hover:bg-white`}
            >
              <Download className="h-4 w-4" aria-hidden="true" />
              Export as CSV
            </button>
          </span>
          <button
            type="button"
            onClick={() => window.print()}
            aria-label="Print report"
            className={outlineBtn}
          >
            <Printer className="h-4 w-4" aria-hidden="true" />
            Print
          </button>
          <Link
            to="/admin"
            className="ml-auto inline-flex items-center gap-1.5 rounded text-sm font-medium text-indigo-600 hover:text-indigo-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600"
          >
            <ArrowLeft className="h-4 w-4" aria-hidden="true" />
            Back to Dashboard
          </Link>
        </div>
      </main>
    </div>
  );
}