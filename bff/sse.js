// sse.js — Server-Sent Events stream for live notifications.
//
// Clients connect to GET /api/stream?userId=X
// Server keeps the response open and pushes new notifications.
//
// Polling strategy:
//   - Every 3 seconds, for each connected user, query NOTIFICATIONS
//     for rows newer than the last seen NotificationId.
//   - If new rows exist, push them as SSE events.

import pool from './db.js';

const clients = new Map(); // userId → Set of res objects
let lastSeenByUser = new Map(); // userId → last NotificationId delivered

export function registerSseRoute(app) {
  app.get('/api/stream', (req, res) => {
    const userId = Number(req.query.userId);
    if (!userId || Number.isNaN(userId)) {
      res.status(400).json({ message: 'userId is required' });
      return;
    }

    res.set({
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache, no-transform',
      Connection: 'keep-alive',
      'Access-Control-Allow-Origin': '*',
      'X-Accel-Buffering': 'no',
    });
    res.flushHeaders();

    // Send an initial hello event
    res.write(`event: hello\n`);
    res.write(`data: ${JSON.stringify({ userId, ts: Date.now() })}\n\n`);

    if (!clients.has(userId)) clients.set(userId, new Set());
    clients.get(userId).add(res);

    req.on('close', () => {
      clients.get(userId)?.delete(res);
      if (clients.get(userId)?.size === 0) clients.delete(userId);
    });
  });

  // Background poller
  setInterval(pollNotifications, 3000);
}

async function pollNotifications() {
  for (const [userId, set] of clients.entries()) {
    if (set.size === 0) continue;
    try {
      const last = lastSeenByUser.get(userId) || 0;
      const [rows] = await pool.query(
        `SELECT NotificationId, BookingId, Message, CreatedAt
           FROM NOTIFICATIONS
          WHERE UserId = ? AND NotificationId > ?
          ORDER BY NotificationId ASC`,
        [userId, last]
      );
      if (rows.length === 0) continue;

      const maxId = rows[rows.length - 1].NotificationId;
      lastSeenByUser.set(userId, maxId);

      const payload = JSON.stringify(rows);
      for (const res of set) {
        res.write(`event: notification\n`);
        res.write(`data: ${payload}\n\n`);
      }
    } catch (err) {
      // Silently skip on error (DB may be down)
    }
  }
}