import express from 'express';
import pool from '../db.js';

const router = express.Router();

// GET /api/resources?deptId=A1&typeId=1&date=2026-10-15&startTime=10:00&endTime=12:00
// Returns all resources of a given department + type with availability status
// for the specified date + time window.
router.get('/', async (req, res) => {
  const { deptId, typeId, date, startTime, endTime } = req.query;

  if (!deptId || !typeId) {
    return res.status(400).json({ message: 'deptId and typeId are required' });
  }

  try {
    // 1. Get all resources for this dept + type, with location info
    const [resources] = await pool.query(
      `
      SELECT
        r.ResourceId    AS resourceId,
        r.ResourceName  AS resourceName,
        r.Capacity      AS capacity,
        r.Status        AS status,
        l.BuildingName  AS buildingName,
        l.RoomNumber    AS roomNumber,
        l.FloorNumber   AS floorNumber,
        rt.TypeName     AS typeName
      FROM RESOURCES r
      JOIN LOCATION l ON l.LocationId = r.LocationId
      JOIN RESOURCE_TYPE rt ON rt.TypeId = r.TypeId
      WHERE r.DepartmentId = ?
        AND r.TypeId = ?
      ORDER BY r.ResourceId
      `,
      [deptId, typeId]
    );

    // 2. If no date/slot provided, return all as "Available" (or with base status)
    if (!date || !startTime || !endTime) {
      const basic = resources.map((r) => ({ ...r, status: 'Available' }));
      return res.json(basic);
    }

    // 3. Find bookings that overlap the requested window on that date.
    //    A booking conflicts if it overlaps with [startTime, endTime).
    const [conflicts] = await pool.query(
      `
      SELECT
        b.ResourceId AS resourceId
      FROM BOOKINGS b
      JOIN TIME_SLOT ts ON ts.SlotId = b.SlotId
      WHERE b.BookingDate = ?
        AND b.Status IN ('Pending', 'Confirmed')
        AND ts.StartTime < ?
        AND ts.EndTime > ?
      `,
      [date, endTime, startTime]
    );

    const occupiedIds = new Set(conflicts.map((c) => c.resourceId));

    // 4. Annotate availability
    const annotated = resources.map((r) => ({
      ...r,
      status: occupiedIds.has(r.resourceId) ? 'Occupied' : 'Available',
    }));

    res.json(annotated);
  } catch (err) {
    console.error('GET /api/resources error:', err);
    res.status(500).json({ message: 'Failed to fetch resources' });
  }
});

// GET /api/resources/:id
// Returns a single resource with full details (used by Booking Form page)
router.get('/:id', async (req, res) => {
  const { id } = req.params;

  try {
    const [rows] = await pool.query(
      `
      SELECT
        r.ResourceId    AS resourceId,
        r.ResourceName  AS resourceName,
        r.Capacity      AS capacity,
        r.Status        AS status,
        d.DepartmentId   AS departmentId,
        d.DepartmentName AS departmentName,
        l.BuildingName   AS buildingName,
        l.RoomNumber     AS roomNumber,
        l.FloorNumber    AS floorNumber,
        rt.TypeId        AS typeId,
        rt.TypeName      AS typeName
      FROM RESOURCES r
      JOIN DEPARTMENT d ON d.DepartmentId = r.DepartmentId
      JOIN LOCATION l ON l.LocationId = r.LocationId
      JOIN RESOURCE_TYPE rt ON rt.TypeId = r.TypeId
      WHERE r.ResourceId = ?
      `,
      [id]
    );

    if (rows.length === 0) {
      return res.status(404).json({ message: 'Resource not found' });
    }

    res.json(rows[0]);
  } catch (err) {
    console.error('GET /api/resources/:id error:', err);
    res.status(500).json({ message: 'Failed to fetch resource' });
  }
});

export default router;