import { useEffect, useRef, useState } from 'react';
import { Bell, X } from 'lucide-react';
import useSSE from '../hooks/useSSE';

export default function NotificationBell({ userId }) {
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unread, setUnread] = useState(0);
  const [loading, setLoading] = useState(false);
  const popRef = useRef(null);

  const { lastEvent } = useSSE(userId);

  // Load notifications on mount
  useEffect(() => {
    if (!userId) return;
    let cancelled = false;
    async function load() {
      setLoading(true);
      try {
        const [listRes, countRes] = await Promise.all([
          fetch(`/api/notifications?userId=${userId}`),
          fetch(`/api/notifications/unread-count?userId=${userId}`),
        ]);
        if (cancelled) return;
        if (listRes.ok) setNotifications(await listRes.json());
        if (countRes.ok) {
          const d = await countRes.json();
          setUnread(d.count || 0);
        }
      } catch {
        // silent
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, [userId]);

  // When SSE pushes a new notification, prepend + bump count
  useEffect(() => {
    if (!lastEvent) return;
    setNotifications((prev) => [lastEvent, ...prev].slice(0, 50));
    setUnread((u) => u + 1);
  }, [lastEvent]);

  // Close on outside click
  useEffect(() => {
    function handle(e) {
      if (popRef.current && !popRef.current.contains(e.target)) setOpen(false);
    }
    if (open) document.addEventListener('mousedown', handle);
    return () => document.removeEventListener('mousedown', handle);
  }, [open]);

  async function markAllRead() {
    try {
      await fetch('/api/notifications/read-all', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId }),
      });
      setUnread(0);
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: 1 })));
    } catch {
      // silent
    }
  }

  return (
    <div className="relative" ref={popRef}>
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        aria-label={`Notifications${unread > 0 ? ` (${unread} unread)` : ''}`}
        className="relative inline-flex items-center justify-center rounded-lg border border-slate-200 bg-white p-2 text-slate-700 transition-colors hover:bg-slate-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-600 focus-visible:ring-offset-2"
      >
        <Bell className="h-5 w-5" aria-hidden="true" />
        {unread > 0 && (
          <span className="absolute -right-1 -top-1 flex h-5 min-w-[1.25rem] items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
            {unread > 99 ? '99+' : unread}
          </span>
        )}
      </button>

      {open && (
        <div
          role="dialog"
          aria-label="Notifications"
          className="absolute right-0 z-20 mt-2 w-80 max-w-[calc(100vw-2rem)] rounded-xl border border-slate-200 bg-white shadow-lg"
        >
          <div className="flex items-center justify-between border-b border-slate-200 px-4 py-3">
            <p className="text-sm font-semibold">Notifications</p>
            <div className="flex items-center gap-2">
              {notifications.length > 0 && (
                <button
                  type="button"
                  onClick={markAllRead}
                  className="text-xs text-indigo-600 hover:underline"
                >
                  Mark all read
                </button>
              )}
              <button
                type="button"
                onClick={() => setOpen(false)}
                aria-label="Close"
                className="rounded p-1 text-slate-400 hover:bg-slate-100"
              >
                <X className="h-4 w-4" aria-hidden="true" />
              </button>
            </div>
          </div>

          <div className="max-h-96 overflow-y-auto">
            {loading ? (
              <div className="space-y-2 p-4">
                <div className="h-4 animate-pulse rounded bg-slate-200" />
                <div className="h-4 animate-pulse rounded bg-slate-200" />
              </div>
            ) : notifications.length === 0 ? (
              <p className="p-6 text-center text-sm text-slate-500">
                No notifications yet.
              </p>
            ) : (
              <ul className="divide-y divide-slate-100">
                {notifications.map((n) => (
                  <li
                    key={n.notificationId}
                    className={`px-4 py-3 text-sm ${
                      n.isRead ? 'bg-white' : 'bg-indigo-50/50'
                    }`}
                  >
                    <p className="text-slate-800">{n.message}</p>
                    <p className="mt-1 text-xs text-slate-500">
                      {n.createdAt}
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
