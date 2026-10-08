import express from 'express';
import pool from '../db.js';

const router = express.Router();

// GET /api/campus-nodes
// Returns all campus nodes (locations used for routing)
router.get('/', async (req, res) => {
  try {
    const [rows] = await pool.query(
      `
      SELECT
        NodeId   AS nodeId,
        NodeName AS nodeName,
        NodeType AS nodeType,
        Status   AS status
      FROM CAMPUS_NODE
      WHERE Status = 'Active'
      ORDER BY NodeId
      `
    );
    res.json(rows);
  } catch (err) {
    console.error('GET /api/campus-nodes error:', err);
    res.status(500).json({ message: 'Failed to fetch campus nodes' });
  }
});

export default router;