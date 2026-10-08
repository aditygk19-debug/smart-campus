import express from 'express';
import pool from '../db.js';

const router = express.Router();

// GET /api/departments
// Returns all departments with a resource count for each
router.get('/', async (req, res) => {
  try {
    const [rows] = await pool.query(`
      SELECT
        d.DepartmentId   AS departmentId,
        d.DepartmentName AS departmentName,
        COUNT(r.ResourceId) AS resourceCount
      FROM DEPARTMENT d
      LEFT JOIN RESOURCES r ON r.DepartmentId = d.DepartmentId
      GROUP BY d.DepartmentId, d.DepartmentName
      ORDER BY d.DepartmentId
    `);

    res.json(rows);
  } catch (err) {
    console.error('GET /api/departments error:', err);
    res.status(500).json({ message: 'Failed to fetch departments' });
  }
});

export default router;