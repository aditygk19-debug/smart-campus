import express from 'express';
import { callSearch } from '../engines.js';

const router = express.Router();

// ----------------------------------------------------------------
// POST /api/search
// Body: { date, startTime, endTime, resourceType, minCapacity, sourceNodeId }
// Proxies to Team B /search
// ----------------------------------------------------------------
router.post('/', async (req, res) => {
  const { date, startTime, endTime, resourceType, minCapacity, sourceNodeId } = req.body || {};

  if (!date || !startTime || !endTime) {
    return res.status(400).json({ message: 'date, startTime, endTime are required' });
  }

  const result = await callSearch({
    date,
    startTime,
    endTime,
    resourceType: resourceType || null,
    minCapacity: Number(minCapacity) || 0,
    sourceNodeId: sourceNodeId || 'N1',
  });

  if (!result.ok) {
    return res.status(502).json({
      message: result.error || 'Search failed',
      detail: result.data,
    });
  }

  res.json(result.data);
});

export default router;