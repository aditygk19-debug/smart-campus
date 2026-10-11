import { useEffect, useRef, useState } from 'react';

/**
 * useSSE — subscribes to the BFF SSE stream for a user.
 * Returns { events, lastEvent, connected }.
 * Auto-reconnects on drop.
 */
export default function useSSE(userId) {
  const [events, setEvents] = useState([]);
  const [lastEvent, setLastEvent] = useState(null);
  const [connected, setConnected] = useState(false);
  const esRef = useRef(null);

  useEffect(() => {
    if (!userId) return;

    let cancelled = false;
    let retryTimer = null;

    function connect() {
      if (cancelled) return;
      const es = new EventSource(`/api/stream?userId=${encodeURIComponent(userId)}`);
      esRef.current = es;

      es.addEventListener('hello', () => {
        setConnected(true);
      });

      es.addEventListener('notification', (e) => {
        try {
          const data = JSON.parse(e.data);
          const list = Array.isArray(data) ? data : [data];
          setEvents((prev) => [...list, ...prev].slice(0, 50));
          setLastEvent(list[0] || null);
        } catch {
          // ignore malformed
        }
      });

      es.onerror = () => {
        setConnected(false);
        es.close();
        if (!cancelled) {
          retryTimer = setTimeout(connect, 3000);
        }
      };
    }

    connect();

    return () => {
      cancelled = true;
      if (retryTimer) clearTimeout(retryTimer);
      esRef.current?.close();
    };
  }, [userId]);

  return { events, lastEvent, connected };
}
