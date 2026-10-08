// engines.js
// Helper functions for calling Teams A and B as HTTP services.

const SCHEDULER_URL = process.env.SCHEDULER_URL || 'http://localhost:8090';
const ROUTER_URL    = process.env.ROUTER_URL    || 'http://localhost:8091';

/**
 * Call Team A's OS scheduler.
 * POST {SCHEDULER_URL}/schedule
 * Body: { userId, labId, computers, duration, priority }
 */
export async function callScheduler({ userId, labId, computers, duration, priority }) {
  try {
    const res = await fetch(`${SCHEDULER_URL}/schedule`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId, labId, computers, duration, priority }),
    });

    if (!res.ok) {
      const text = await res.text();
      console.error('[engines] Team A responded', res.status, text);
      return { ok: false, error: `Scheduler returned ${res.status}` };
    }

    const data = await res.json();
    return { ok: true, data };
  } catch (err) {
    console.error('[engines] Scheduler unreachable:', err.message);
    return { ok: false, error: 'Scheduler unreachable' };
  }
}

/**
 * Call Team B's routing engine.
 * POST {ROUTER_URL}/route
 * Body: { sourceNodeId, destNodeId }
 */
export async function callRouter({ sourceNodeId, destNodeId }) {
  try {
    const res = await fetch(`${ROUTER_URL}/route`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sourceNodeId, destNodeId }),
    });

    if (!res.ok) {
      const text = await res.text();
      console.error('[engines] Team B responded', res.status, text);
      return { ok: false, error: `Router returned ${res.status}` };
    }

    const data = await res.json();
    return { ok: true, data };
  } catch (err) {
    console.error('[engines] Router unreachable:', err.message);
    return { ok: false, error: 'Router unreachable' };
  }
}

/**
 * Map our 6 reason categories to Team A's priority scale.
 * Team A: smaller number = higher priority (1 = highest, 3 = lowest)
 */
export function priorityFromReason(reasonId) {
  switch ((reasonId || '').toLowerCase()) {
    case 'exam':
    case 'workshop':
      return 1; // highest
    case 'practical':
    case 'lecture':
    case 'project':
      return 2; // medium
    case 'meeting':
      return 3; // lowest
    default:
      return 2; // default medium
  }
}

/**
 * Convert slot times like "10:00" to duration in minutes by diffing end - start.
 */
export function durationMinutes(startTime, endTime) {
  const [sh, sm] = String(startTime).split(':').map(Number);
  const [eh, em] = String(endTime).split(':').map(Number);
  if ([sh, sm, eh, em].some(Number.isNaN)) return 60; // fallback
  return (eh * 60 + em) - (sh * 60 + sm);
}