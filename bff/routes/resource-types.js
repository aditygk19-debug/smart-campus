import express from 'express';
import pool from '../db.js';

const router = express.Router({ mergeParams: true });

// GET /api/departments/:deptId/resource-types
// Returns the 3 types (Lab, Classroom, Conference Hall) with a count of
// resources in that department for each type.
router.get('/', async (req, res) => {
  const { deptId } = req.params;

  if (!deptId) {
    return res.status(400).json({ message: 'Department ID is required' });
  }

  try {
    const [rows] = await pool.query(
      `
      SELECT
        rt.TypeId     AS typeId,
        rt.TypeName   AS typeName,
        rt.Description AS description,
        COUNT(r.ResourceId) AS count
      FROM RESOURCE_TYPE rt
      LEFT JOIN RESOURCES r
        ON r.TypeId = rt.TypeId
       AND r.DepartmentId = ?
      GROUP BY rt.TypeId, rt.TypeName, rt.Description
      ORDER BY rt.TypeId
      `,
      [deptId]
    );

    res.json(rows);
  } catch (err) {
    console.error('GET resource-types error:', err);
    res.status(500).json({ message: 'Failed to fetch resource types' });
  }
});

export default router;