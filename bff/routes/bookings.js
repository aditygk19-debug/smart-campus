import express from 'express';
import pool from '../db.js';
import { callScheduler, callPromote, priorityFromReason } from '../engines.js';
import { getResourceNode } from '../utils/nodeMap.js';

const router = express.Router();

// ----------------------------------------------------------------
// POST /api/bookings
// Body: { userId, resourceId, date, startTime, endTime,
//         reasonId, reasonLabel, notes }
//
// Flow (no Team B involvement):
//   1. Resolve resourceId → labId (node id like "L1")
//   2. Map reason → priority
//   3. Call Team A /schedule → Team A writes directly to MySQL
//   4. Return Team A's response { bookingId, status, waitlistPosition, message }
// ----------------------------------------------------------------
router.post('/', async (req, res) => {
  const {
    userId,
    resourceId,
    date,
    startTime,
    endTime,
    reasonId,
    reasonLabel,
    notes,
  } = req.body || {};

  if (!userId || !resourceId || !date || !startTime || !endTime) {
    return res.status(400).json({
      message: 'userId, resourceId, date, startTime, endTime are required',
    });
  }

  // Resolve resource's campus node (labId Team A uses: L1, C1, CH1...)
  const nodeInfo = await getResourceNode(resourceId);
  if (!nodeInfo || !nodeInfo.nodeId) {
    return res.status(400).json({
      message: `No campus node mapped for resource ${resourceId}`,
    });
  }
  const labId = nodeInfo.nodeId;

  const priority = priorityFromReason(reasonId);
  const purpose = [reasonLabel, notes].filter(Boolean).join(' — ').slice(0, 255) || 'Booking';

  const result = await callScheduler({
    userId: Number(userId),
    labId,
    date,
    startTime,
    endTime,
    purpose,
    priority,
  });

  if (!result.ok) {
    // Team A rejected — pass through the message and reason
    return res.status(409).json({
      message: result.data?.message || result.error || 'Booking rejected',
      status: result.data?.status || 'Rejected',
    });
  }

  return res.status(201).json({
    bookingId: result.data.bookingId,
    status: result.data.status,
    waitlistPosition: result.data.waitlistPosition || 0,
    priority: result.data.priority || priority,
    message: result.data.message,
    labId,
    resourceName: nodeInfo.resourceName,
  });
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
        b.BookingId      AS bookingId,
        b.UserId         AS userId,
        b.ResourceId     AS resourceId,
        r.ResourceName   AS resourceName,
        d.DepartmentName AS departmentName,
        l.BuildingName   AS buildingName,
        l.RoomNumber     AS roomNumber,
        b.BookingDate    AS date,
        ts.StartTime     AS startTime,
        ts.EndTime       AS endTime,
        b.Purpose        AS purpose,
        b.Status         AS status,
        b.Priority       AS priority,
        b.WaitlistPosition AS waitlistPosition
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
//   1. Cancel via DB stored procedure
//   2. Look up resource slot/date so we can promote
//   3. Call Team A /promote
// ----------------------------------------------------------------
router.post('/:id/cancel', async (req, res) => {
  const { id } = req.params;

  try {
    // Look up booking BEFORE cancelling so we have slot/date/resource
    const [lookup] = await pool.query(
      `SELECT b.ResourceId, b.SlotId, b.BookingDate,
              ts.StartTime, ts.EndTime,
              l.RoomNumber AS labId
         FROM BOOKINGS b
         JOIN TIME_SLOT ts ON ts.SlotId = b.SlotId
         JOIN RESOURCES r ON r.ResourceId = b.ResourceId
         JOIN LOCATION l ON l.LocationId = r.LocationId
        WHERE b.BookingId = ?`,
      [Number(id)]
    );

    const [result] = await pool.query(`CALL CANCEL_BOOKING_TRANSACTION(?)`, [Number(id)]);
    const first = Array.isArray(result) && Array.isArray(result[0]) ? result[0][0] : null;

    let promotion = null;
    if (lookup.length > 0) {
      const row = lookup[0];
      const promoteResp = await callPromote({
        labId: row.labId,
        date: row.BookingDate,
        startTime: row.StartTime.slice(0, 5),
        endTime: row.EndTime.slice(0, 5),
      });
      if (promoteResp.ok) promotion = promoteResp.data;
    }

    res.json({
      bookingId: first ? first.BookingId : Number(id),
      message: first ? first.Message : 'Booking cancelled',
      promotion,
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