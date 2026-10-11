import express from 'express';
import pool from '../db.js';
import bcrypt from 'bcryptjs';

const router = express.Router();

// ----------------------------------------------------------------
// Simplified admin guard: requires header X-Role: Admin
// ----------------------------------------------------------------
function requireAdmin(req, res, next) {
  const role = req.header('X-Role') || req.query.role;
  if (role !== 'Admin') {
    return res.status(403).json({ message: 'Admin role required' });
  }
  next();
}

router.use(requireAdmin);

// GET /api/admin/users
router.get('/users', async (req, res) => {
  try {
    const [rows] = await pool.query(
      `SELECT UserId AS userId, Role AS role, Status AS status
         FROM USERS
         ORDER BY UserId`
    );
    res.json(rows);
  } catch (err) {
    console.error('admin list users:', err);
    res.status(500).json({ message: 'Failed to list users' });
  }
});

// POST /api/admin/users  body: { userId, password, role }
router.post('/users', async (req, res) => {
  const { userId, password, role } = req.body || {};
  if (!userId || !password || !role) {
    return res.status(400).json({ message: 'userId, password, role required' });
  }
  try {
    const hash = await bcrypt.hash(password, 10);
    await pool.query(`CALL ADD_USER_TRANSACTION(?, ?, ?)`, [Number(userId), hash, role]);
    res.status(201).json({ ok: true, message: 'User added' });
  } catch (err) {
    if (err.sqlState === '45000' || err.code === 'ER_SIGNAL_EXCEPTION') {
      return res.status(409).json({ message: err.sqlMessage });
    }
    console.error('admin add user:', err);
    res.status(500).json({ message: 'Failed to add user' });
  }
});

// PUT /api/admin/users/:id  body: { role, status }
router.put('/users/:id', async (req, res) => {
  const { id } = req.params;
  const { role, status } = req.body || {};
  try {
    await pool.query(`CALL UPDATE_USER_TRANSACTION(?, ?, ?)`, [Number(id), role || null, status || null]);
    res.json({ ok: true, message: 'User updated' });
  } catch (err) {
    if (err.sqlState === '45000' || err.code === 'ER_SIGNAL_EXCEPTION') {
      return res.status(409).json({ message: err.sqlMessage });
    }
    console.error('admin update user:', err);
    res.status(500).json({ message: 'Failed to update user' });
  }
});

// DELETE /api/admin/users/:id
router.delete('/users/:id', async (req, res) => {
  const { id } = req.params;
  try {
    await pool.query(`CALL DELETE_USER_TRANSACTION(?)`, [Number(id)]);
    res.json({ ok: true, message: 'User deleted' });
  } catch (err) {
    if (err.sqlState === '45000' || err.code === 'ER_SIGNAL_EXCEPTION') {
      return res.status(409).json({ message: err.sqlMessage });
    }
    console.error('admin delete user:', err);
    res.status(500).json({ message: 'Failed to delete user' });
  }
});

// POST /api/admin/users/:id/reset-password  body: { newPassword }
router.post('/users/:id/reset-password', async (req, res) => {
  const { id } = req.params;
  const { newPassword } = req.body || {};
  if (!newPassword) return res.status(400).json({ message: 'newPassword required' });
  try {
    const hash = await bcrypt.hash(newPassword, 10);
    await pool.query(`CALL RESET_PASSWORD_TRANSACTION(?, ?)`, [Number(id), hash]);
    res.json({ ok: true, message: 'Password reset' });
  } catch (err) {
    if (err.sqlState === '45000' || err.code === 'ER_SIGNAL_EXCEPTION') {
      return res.status(409).json({ message: err.sqlMessage });
    }
    console.error('admin reset password:', err);
    res.status(500).json({ message: 'Failed to reset password' });
  }
});

// GET /api/admin/audit-logs?limit=100
router.get('/audit-logs', async (req, res) => {
  const limit = Math.min(Number(req.query.limit) || 100, 500);
  try {
    const [rows] = await pool.query(
      `SELECT AuditId     AS auditId,
              UserId      AS userId,
              Action      AS action,
              EntityType  AS entityType,
              EntityId    AS entityId,
              OldValue    AS oldValue,
              NewValue    AS newValue,
              CreatedAt   AS createdAt
         FROM AUDIT_LOG
         ORDER BY AuditId DESC
         LIMIT ?`,
      [limit]
    );
    res.json(rows);
  } catch (err) {
    console.error('admin audit logs:', err);
    res.status(500).json({ message: 'Failed to fetch audit logs' });
  }
});

export default router;