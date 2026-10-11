import express from 'express';
import pool from '../db.js';

const router = express.Router();

// GET /api/campus-nodes
// Returns the full list of campus nodes for the source location dropdown.
router.get('/', async (req, res) => {
  try {
    const [rows] = await pool.query(
      `SELECT NodeId AS nodeId, NodeName AS nodeName, NodeType AS nodeType, Status AS status
         FROM CAMPUS_NODE
        ORDER BY NodeId`
    );

    if (rows.length > 0) {
      return res.json(rows);
    }

    res.json([
      { nodeId: 'N1', nodeName: 'Main Gate', nodeType: 'Gate', status: 'ACTIVE' },
      { nodeId: 'N2', nodeName: 'Gate 1', nodeType: 'Gate', status: 'ACTIVE' },
      { nodeId: 'N3', nodeName: 'Library', nodeType: 'Library', status: 'ACTIVE' },
      { nodeId: 'N5', nodeName: 'Office', nodeType: 'Office', status: 'ACTIVE' },
      { nodeId: 'N4', nodeName: 'CSE Department', nodeType: 'Department', status: 'ACTIVE' },
      { nodeId: 'N6', nodeName: 'Electrical Department', nodeType: 'Department', status: 'ACTIVE' },
      { nodeId: 'N7', nodeName: 'AIML Building', nodeType: 'Department', status: 'ACTIVE' },
      { nodeId: 'N8', nodeName: 'Mechanical Department', nodeType: 'Department', status: 'ACTIVE' },
      { nodeId: 'CH1', nodeName: 'Conference Hall', nodeType: 'Conference Hall', status: 'ACTIVE' }
    ]);
  } catch (err) {
    console.error('GET /api/campus-nodes error:', err);
    res.status(500).json({ message: 'Failed to fetch campus nodes' });
  }
});

export default router;