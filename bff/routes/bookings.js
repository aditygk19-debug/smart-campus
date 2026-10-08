import express from 'express';
import pool from '../db.js';

const router = express.Router();

// ----------------------------------------------------------------
// POST /api/bookings
// Body: { userId, resourceId, date, startTime, endTime,
//         sourceNodeId, reasonId, reasonLabel, notes }
// ----------------------------------------------------------------
router.post('/', async (req, res) => {
  const {
    userId,
    resourceId,
    date,
    startTime,
    endTime,
    reasonLabel,
    notes,
  } = req.body || {};

  if (!userId || !resourceId || !date || !startTime || !endTime) {
    return res.status(400).json({
      message: 'userId, resourceId, date, startTime, endTime are required',
    });
  }

  try {
    // 1. Find the slot that matches startTime / endTime
    //    Normalize "10:00" → "10:00:00" to match MySQL TIME format
    const normalizeTime = (t) => {
      const s = String(t).trim();
      if (/^\d{2}:\d{2}$/.test(s)) return `${s}:00`;
      return s;
    };

    const [slotRows] = await pool.query(
      `SELECT SlotId FROM TIME_SLOT WHERE StartTime = ? AND EndTime = ? LIMIT 1`,
      [normalizeTime(startTime), normalizeTime(endTime)]
    );

    if (slotRows.length === 0) {
      return res.status(400).json({ message: 'No matching time slot' });
    }
    const slotId = slotRows[0].SlotId;

    // 2. Compose the purpose string from reason + notes
    const purpose = [reasonLabel, notes].filter(Boolean).join(' — ').slice(0, 255) || 'Booking';

    // 3. Call the stored procedure
    const [result] = await pool.query(
      `CALL CREATE_BOOKING_TRANSACTION(?, ?, ?, ?, ?)`,
      [Number(userId), Number(resourceId), slotId, date, purpose]
    );

    // result[0] = [{ BookingId, Message }]
    const first = Array.isArray(result) && Array.isArray(result[0]) ? result[0][0] : null;

    return res.status(201).json({
      bookingId: first ? first.BookingId : null,
      message: first ? first.Message : 'Booking created',
    });
  } catch (err) {
    // SIGNAL SQLSTATE '45000' errors come back with err.sqlMessage
    if (err.sqlState === '45000' || err.code === 'ER_SIGNAL_EXCEPTION') {
      return res.status(409).json({ message: err.sqlMessage || 'Booking rejected' });
    }
    console.error('POST /api/bookings error:', err);
    return res.status(500).json({ message: 'Failed to create booking' });
  }
});

// ----------------------------------------------------------------
// GET /api/bookings?userId=X
// ----------------------------------------------------------------
router.get('/', async (req, res) => {
  const { userId } = req.query;

  try {
    const params = [];
    let sql = `
      SELECT
        b.BookingId    AS bookingId,
        b.UserId       AS userId,
        b.ResourceId   AS resourceId,
        r.ResourceName AS resourceName,
        d.DepartmentName AS departmentName,
        l.BuildingName AS buildingName,
        l.RoomNumber   AS roomNumber,
        b.BookingDate  AS date,
        ts.StartTime   AS startTime,
        ts.EndTime     AS endTime,
        b.Purpose      AS purpose,
        b.Status       AS status,
        b.BookingDate  AS createdAt
      FROM BOOKINGS b
      JOIN RESOURCES r ON r.ResourceId = b.ResourceId
      JOIN DEPARTMENT d ON d.DepartmentId = r.DepartmentId
      JOIN LOCATION l ON l.LocationId = r.LocationId
      JOIN TIME_SLOT ts ON ts.SlotId = b.SlotId
    `;

    if (userId) {
      sql += ' WHERE b.UserId = ? ';
      params.push(Number(userId));
    }

    sql += ' ORDER BY b.BookingDate DESC, ts.StartTime DESC ';

    const [rows] = await pool.query(sql, params);
    res.json(rows);
  } catch (err) {
    console.error('GET /api/bookings error:', err);
    res.status(500).json({ message: 'Failed to fetch bookings' });
  }
});

// ----------------------------------------------------------------
// POST /api/bookings/:id/cancel
// ----------------------------------------------------------------
router.post('/:id/cancel', async (req, res) => {
  const { id } = req.params;

  try {
    const [result] = await pool.query(
      `CALL CANCEL_BOOKING_TRANSACTION(?)`,
      [Number(id)]
    );

    const first = Array.isArray(result) && Array.isArray(result[0]) ? result[0][0] : null;

    res.json({
      bookingId: first ? first.BookingId : Number(id),
      message: first ? first.Message : 'Booking cancelled',
    });
  } catch (err) {
    if (err.sqlState === '45000' || err.code === 'ER_SIGNAL_EXCEPTION') {
      return res.status(409).json({ message: err.sqlMessage || 'Cancel rejected' });
    }
    console.error('POST /api/bookings/:id/cancel error:', err);
    res.status(500).json({ message: 'Failed to cancel booking' });
  }
});

export default router;