import express from 'express';
import pool from '../db.js';

const router = express.Router();

function requireAdmin(req, res, next) {
  const role = req.header('X-Role') || req.query.role;
  if (role !== 'Admin') {
    return res.status(403).json({ message: 'Admin role required' });
  }
  next();
}

// GET /api/admin/stats
router.get('/stats', requireAdmin, async (req, res) => {
  try {
    const [[deptRow]] = await pool.query(`SELECT COUNT(*) AS c FROM DEPARTMENT`);
    const [[resRow]] = await pool.query(`SELECT COUNT(*) AS c FROM RESOURCES`);
    const [[userRow]] = await pool.query(`SELECT COUNT(*) AS c FROM USERS`);
    const [[bookRow]] = await pool.query(`SELECT COUNT(*) AS c FROM BOOKINGS`);

    res.json({
      totalDepartments: deptRow.c,
      totalResources: resRow.c,
      totalUsers: userRow.c,
      totalBookings: bookRow.c,
    });
  } catch (err) {
    console.error('admin stats:', err);
    res.status(500).json({ message: 'Failed to load stats' });
  }
});

// GET /api/admin/resources/status
router.get('/resources/status', requireAdmin, async (req, res) => {
  try {
    const [rows] = await pool.query(`
      SELECT
        r.ResourceId     AS resourceId,
        r.ResourceName   AS resourceName,
        r.Status         AS status,
        d.DepartmentName AS departmentName,
        rt.TypeName      AS typeName
      FROM RESOURCES r
      JOIN DEPARTMENT d ON d.DepartmentId = r.DepartmentId
      JOIN RESOURCE_TYPE rt ON rt.TypeId = r.TypeId
      ORDER BY d.DepartmentName, r.ResourceName
    `);
    res.json(rows);
  } catch (err) {
    console.error('admin resources status:', err);
    res.status(500).json({ message: 'Failed to load resource status' });
  }
});

export default router;

// GET /api/admin/reports?range=7d|30d|90d
router.get('/reports', requireAdmin, async (req, res) => {
  const range = String(req.query.range || '30d').toLowerCase();
  const days = range === '90d' ? 90 : range === '7d' ? 7 : 30;

  try {
    const where = `BookingDate BETWEEN DATE_SUB(CURDATE(), INTERVAL ? DAY)
                                    AND DATE_ADD(CURDATE(), INTERVAL ? DAY)`;

    // ---------- Totals ----------
    const [[totalRow]] = await pool.query(
      `SELECT COUNT(*) AS c FROM BOOKINGS WHERE ${where}`,
      [days, days]
    );
    const [[confirmedRow]] = await pool.query(
      `SELECT COUNT(*) AS c FROM BOOKINGS
        WHERE Status = 'Confirmed' AND ${where}`,
      [days, days]
    );
    const [[cancelledRow]] = await pool.query(
      `SELECT COUNT(*) AS c FROM BOOKINGS
        WHERE Status = 'Cancelled' AND ${where}`,
      [days, days]
    );

    const totalBookings = totalRow.c;
    const confirmedBookings = confirmedRow.c;
    const cancelledBookings = cancelledRow.c;
    const utilizationRate = totalBookings
      ? Math.round((confirmedBookings / totalBookings) * 100)
      : 0;

    // ---------- Average duration (minutes) ----------
    const [[avgRow]] = await pool.query(
      `SELECT AVG(TIMESTAMPDIFF(MINUTE, ts.StartTime, ts.EndTime)) AS avgMin
         FROM BOOKINGS b
         JOIN TIME_SLOT ts ON ts.SlotId = b.SlotId
        WHERE b.Status = 'Confirmed'
          AND b.BookingDate BETWEEN DATE_SUB(CURDATE(), INTERVAL ? DAY)
                                AND DATE_ADD(CURDATE(), INTERVAL ? DAY)`,
      [days, days]
    );
    const avgDuration = avgRow.avgMin ? Math.round(Number(avgRow.avgMin)) : 0;

    // ---------- Peak hour ----------
    const [peakHourRows] = await pool.query(
      `SELECT HOUR(ts.StartTime) AS hour, COUNT(*) AS c
         FROM BOOKINGS b
         JOIN TIME_SLOT ts ON ts.SlotId = b.SlotId
        WHERE b.Status = 'Confirmed'
          AND b.BookingDate BETWEEN DATE_SUB(CURDATE(), INTERVAL ? DAY)
                                AND DATE_ADD(CURDATE(), INTERVAL ? DAY)
        GROUP BY HOUR(ts.StartTime)
        ORDER BY c DESC LIMIT 1`,
      [days, days]
    );
    const peakHour = peakHourRows.length ? Number(peakHourRows[0].hour) : null;

    // ---------- Booking trend by day ----------
    const [trendRows] = await pool.query(
      `SELECT DATE(BookingDate) AS d, COUNT(*) AS c
         FROM BOOKINGS
        WHERE BookingDate BETWEEN DATE_SUB(CURDATE(), INTERVAL ? DAY)
                              AND DATE_ADD(CURDATE(), INTERVAL ? DAY)
        GROUP BY DATE(BookingDate)
        ORDER BY d ASC`,
      [days, days]
    );

    // ---------- Hourly distribution ----------
    const [hourlyRows] = await pool.query(
      `SELECT HOUR(ts.StartTime) AS hour, COUNT(*) AS c
         FROM BOOKINGS b
         JOIN TIME_SLOT ts ON ts.SlotId = b.SlotId
        WHERE b.BookingDate BETWEEN DATE_SUB(CURDATE(), INTERVAL ? DAY)
                                AND DATE_ADD(CURDATE(), INTERVAL ? DAY)
        GROUP BY HOUR(ts.StartTime)
        ORDER BY hour ASC`,
      [days, days]
    );

    // ---------- By resource type ----------
    const [typeRows] = await pool.query(
      `SELECT rt.TypeName AS typeName, COUNT(*) AS c
         FROM BOOKINGS b
         JOIN RESOURCES r ON r.ResourceId = b.ResourceId
         JOIN RESOURCE_TYPE rt ON rt.TypeId = r.TypeId
        WHERE b.BookingDate BETWEEN DATE_SUB(CURDATE(), INTERVAL ? DAY)
                                AND DATE_ADD(CURDATE(), INTERVAL ? DAY)
        GROUP BY rt.TypeName
        ORDER BY c DESC`,
      [days, days]
    );

    // ---------- By department ----------
    const [deptRows] = await pool.query(
      `SELECT d.DepartmentName AS departmentName, COUNT(*) AS c
         FROM BOOKINGS b
         JOIN RESOURCES r ON r.ResourceId = b.ResourceId
         JOIN DEPARTMENT d ON d.DepartmentId = r.DepartmentId
        WHERE b.BookingDate BETWEEN DATE_SUB(CURDATE(), INTERVAL ? DAY)
                                AND DATE_ADD(CURDATE(), INTERVAL ? DAY)
        GROUP BY d.DepartmentName
        ORDER BY c DESC`,
      [days, days]
    );

    res.json({
      range,
      days,
      totalBookings,
      confirmedBookings,
      cancelledBookings,
      utilizationRate,
      avgDuration,
      peakHour,
      trend: trendRows.map((r) => ({ date: r.d, count: r.c })),
      hourly: hourlyRows.map((r) => ({ hour: r.hour, count: r.c })),
      byResourceType: typeRows.map((r) => ({ name: r.typeName, count: r.c })),
      byDepartment: deptRows.map((r) => ({ name: r.departmentName, count: r.c })),
    });
  } catch (err) {
    console.error('admin reports:', err);
    res.status(500).json({ message: 'Failed to load reports' });
  }
});