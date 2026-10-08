// utils/nodeMap.js
// Resolves ResourceId → NodeId and campus node lookups from the DB.

import pool from '../db.js';

let cache = null;
let cachedAt = 0;
const CACHE_TTL_MS = 60_000; // 1 minute

/**
 * Fetch the ResourceId → NodeId mapping once, cache it briefly.
 * Returns: Map<ResourceId(int), { nodeId, roomNumber, resourceName }>
 */
export async function getResourceNodeMap() {
  const now = Date.now();
  if (cache && now - cachedAt < CACHE_TTL_MS) {
    return cache;
  }

  const [rows] = await pool.query(`
    SELECT
      r.ResourceId   AS resourceId,
      r.ResourceName AS resourceName,
      l.RoomNumber   AS roomNumber,
      n.NodeId       AS nodeId,
      n.NodeName     AS nodeName
    FROM RESOURCES r
    JOIN LOCATION l ON l.LocationId = r.LocationId
    LEFT JOIN CAMPUS_NODE n ON n.NodeId = l.RoomNumber
    ORDER BY r.ResourceId
  `);

  const map = new Map();
  for (const row of rows) {
    map.set(row.resourceId, {
      nodeId: row.nodeId,
      nodeName: row.nodeName,
      roomNumber: row.roomNumber,
      resourceName: row.resourceName,
    });
  }

  cache = map;
  cachedAt = now;
  return map;
}

/**
 * Get the campus NodeId for a resource. Returns null if not found.
 */
export async function getNodeIdForResource(resourceId) {
  const map = await getResourceNodeMap();
  const entry = map.get(Number(resourceId));
  return entry ? entry.nodeId : null;
}

/**
 * Get the node info for a resource (name, nodeId, room number).
 */
export async function getResourceNode(resourceId) {
  const map = await getResourceNodeMap();
  return map.get(Number(resourceId)) || null;
}