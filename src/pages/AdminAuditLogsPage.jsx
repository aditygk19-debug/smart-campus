import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ChevronRight, LogOut, AlertCircle, FileClock } from "lucide-react";

const ACTION_STYLES = {
  CREATE_BOOKING: "bg-green-100 text-green-700",
  CANCEL_BOOKING: "bg-red-100 text-red-700",
  WAITLIST_BOOKING: "bg-amber-100 text-amber-700",
  PROMOTE_WAITLIST: "bg-indigo-100 text-indigo-700",
  ADD_USER: "bg-indigo-100 text-indigo-700",
  UPDATE_USER: "bg-slate-100 text-slate-700",
  DELETE_USER: "bg-red-100 text-red-700",
  RESET_PASSWORD: "bg-slate-100 text-slate-700",
};

export default function AdminAuditLogsPage({ user, onLogout }) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  const load = useCallback(async (signal) => {
    setLoading(true);
    setError("");
    try {
      const res = await fetch("/api/admin/audit-logs?limit=100", {
        headers: { "X-Role": "Admin" },
        signal,
      });
      if (!res.ok) throw new Error(`Request failed: ${res.status}`);
      const data = await res.json();
      setLogs(Array.isArray(data) ? data : []);
    } catch (err) {
      if (err.name === "AbortError") return;
      setError("Couldn't load audit logs.");
    } finally {
      if (!signal || !signal.aborted) setLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    load(controller.signal);
    return () => controller.abort();
  }, [load, reloadKey]);

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6">
          <Link to="/admin" className="flex items-center gap-3 rounded-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600">
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-600 text-sm font-bold text-white">SC</span>
            <span className="hidden text-sm font-semibold sm:block">Smart Campus Admin</span>
          </Link>
          <div className="flex items-center gap-3">
            <div className="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs">
              <span className="font-medium">{user?.userId}</span>
              {user?.role && <span className="text-slate-500"> &middot; {user.role}</span>}
            </div>
            <button type="button" onClick={onLogout} className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600">
              <LogOut className="h-4 w-4" />
              <span className="hidden sm:inline">Logout</span>
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6">
        <nav aria-label="Breadcrumb" className="mb-5">
          <ol className="flex flex-wrap items-center gap-1 text-sm text-slate-500">
            <li><Link to="/admin" className="rounded hover:text-indigo-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600">Admin</Link></li>
            <ChevronRight className="h-4 w-4" />
            <li aria-current="page" className="font-medium text-slate-800">Audit Logs</li>
          </ol>
        </nav>

        <div className="mb-6">
          <p className="text-xs uppercase text-slate-500">History</p>
          <h1 className="text-3xl font-bold">Audit Logs</h1>
          <p className="text-sm text-slate-500">Every action taken by users and the system.</p>
        </div>

        {error && (
          <div role="alert" className="mb-4 flex items-center justify-between rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
            <span className="flex items-center gap-2"><AlertCircle className="h-4 w-4" /> {error}</span>
            <button onClick={() => setReloadKey((k) => k + 1)} className="rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-indigo-700">Retry</button>
          </div>
        )}

        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          {loading ? (
            <div className="space-y-2 p-4">
              {[0, 1, 2, 3, 4].map((i) => <div key={i} className="h-10 animate-pulse rounded bg-slate-200" />)}
            </div>
          ) : logs.length === 0 ? (
            <div className="flex flex-col items-center py-12 text-center">
              <FileClock className="mb-3 h-8 w-8 text-slate-400" />
              <p className="text-sm text-slate-500">No audit events yet.</p>
            </div>
          ) : (
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">#</th>
                  <th className="px-4 py-3 font-medium">Action</th>
                  <th className="px-4 py-3 font-medium">Entity</th>
                  <th className="px-4 py-3 font-medium">User</th>
                  <th className="px-4 py-3 font-medium">Details</th>
                  <th className="px-4 py-3 font-medium">When</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {logs.map((l) => (
                  <tr key={l.auditId} className="hover:bg-slate-50">
                    <td className="px-4 py-3 text-slate-500">{l.auditId}</td>
                    <td className="px-4 py-3">
                      <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${ACTION_STYLES[l.action] || "bg-slate-100 text-slate-700"}`}>
                        {l.action}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-slate-600">{l.entityType} #{l.entityId}</td>
                    <td className="px-4 py-3 text-slate-600">{l.userId ?? "—"}</td>
                    <td className="px-4 py-3 text-xs text-slate-500">
                      {l.oldValue && <span className="mr-2">old: {l.oldValue}</span>}
                      {l.newValue && <span>new: {l.newValue}</span>}
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-500">{l.createdAt}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </main>
    </div>
  );
}