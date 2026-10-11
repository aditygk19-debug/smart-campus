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