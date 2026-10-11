import express from 'express';
import pool from '../db.js';

const router = express.Router();

// GET /api/notifications?userId=X
router.get('/', async (req, res) => {
  const { userId } = req.query;
  if (!userId) return res.status(400).json({ message: 'userId is required' });
  try {
    const [rows] = await pool.query(
      `SELECT NotificationId AS notificationId,
              UserId         AS userId,
              BookingId      AS bookingId,
              Message        AS message,
              IsRead         AS isRead,
              CreatedAt      AS createdAt
         FROM NOTIFICATIONS
        WHERE UserId = ?
        ORDER BY CreatedAt DESC
        LIMIT 100`,
      [Number(userId)]
    );
    res.json(rows);
  } catch (err) {
    console.error('GET /api/notifications error:', err);
    res.status(500).json({ message: 'Failed to fetch notifications' });
  }
});

// GET /api/notifications/unread-count?userId=X
router.get('/unread-count', async (req, res) => {
  const { userId } = req.query;
  if (!userId) return res.status(400).json({ message: 'userId is required' });
  try {
    const [rows] = await pool.query(
      `SELECT COUNT(*) AS cnt FROM NOTIFICATIONS
        WHERE UserId = ? AND IsRead = 0`,
      [Number(userId)]
    );
    res.json({ count: rows[0].cnt });
  } catch (err) {
    console.error('unread-count error:', err);
    res.status(500).json({ message: 'Failed' });
  }
});

// POST /api/notifications/:id/read
router.post('/:id/read', async (req, res) => {
  const { id } = req.params;
  try {
    await pool.query(
      `UPDATE NOTIFICATIONS SET IsRead = 1 WHERE NotificationId = ?`,
      [Number(id)]
    );
    res.json({ ok: true });
  } catch (err) {
    console.error('mark read error:', err);
    res.status(500).json({ message: 'Failed to mark read' });
  }
});

// POST /api/notifications/read-all — body: { userId }
router.post('/read-all', async (req, res) => {
  const { userId } = req.body || {};
  if (!userId) return res.status(400).json({ message: 'userId required' });
  try {
    await pool.query(
      `UPDATE NOTIFICATIONS SET IsRead = 1 WHERE UserId = ?`,
      [Number(userId)]
    );
    res.json({ ok: true });
  } catch (err) {
    console.error('read-all error:', err);
    res.status(500).json({ message: 'Failed' });
  }
});

export default router;