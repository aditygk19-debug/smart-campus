import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * BookingService — orchestrates booking decisions for Team A.
 *
 * Flow:
 *   1. Resolve slotId + resourceId from request
 *   2. If resource is free → insert Confirmed with FCFS/priority rule
 *   3. If resource is taken → insert Waitlisted with computed position
 *
 * All DB access goes through Db.java.
 */
public class BookingService {

    // ----------------------------------------------------------------
    // Result carrier
    // ----------------------------------------------------------------
    public static class Result {
        public int bookingId = -1;
        public String status;         // "Confirmed", "Waitlisted", "Rejected"
        public String message;
        public int waitlistPosition;  // 0 if not waitlisted
        public int priority;
        public int queueSize;
        public boolean ok = true;
    }

    // ----------------------------------------------------------------
    // createOrWaitlist
    // ----------------------------------------------------------------
    public static Result createOrWaitlist(
            int userId,
            String labId,        // e.g. "L1"
            String date,         // "2026-10-15"
            String startTime,    // "10:00"
            String endTime,      // "12:00"
            String purpose,
            int priority
    ) {
        Result r = new Result();
        r.priority = priority;

        try {
            int resourceId = Db.findResourceIdByNode(labId);
            if (resourceId < 0) {
                r.ok = false;
                r.status = "Rejected";
                r.message = "Unknown labId: " + labId;
                return r;
            }

            int slotId = Db.findSlotId(startTime, endTime);
            if (slotId < 0) {
                r.ok = false;
                r.status = "Rejected";
                r.message = "No matching time slot for " + startTime + " - " + endTime;
                return r;
            }

            if (Db.userHasActiveBooking(userId, resourceId, slotId, date)) {
                r.ok = false;
                r.status = "Rejected";
                r.message = "You already have a request for this resource and slot";
                return r;
            }

            int confirmed = Db.countConfirmed(resourceId, slotId, date);

            if (confirmed == 0) {
                // Free → Confirmed
                int id = Db.insertBooking(userId, resourceId, slotId, date, purpose, "Confirmed", priority);
                if (id < 0) {
                    r.ok = false;
                    r.status = "Rejected";
                    r.message = "Insert failed";
                    return r;
                }
                Db.insertAudit(userId, "CREATE_BOOKING", "BOOKING", id, "Confirmed priority=" + priority);
                r.bookingId = id;
                r.status = "Confirmed";
                r.message = "Booking confirmed";
                return r;
            }

            // Occupied → Waitlisted
            int id = Db.insertBooking(userId, resourceId, slotId, date, purpose, "Waitlisted", priority);
            if (id < 0) {
                r.ok = false;
                r.status = "Rejected";
                r.message = "Insert failed";
                return r;
            }

            int pos = Db.computeWaitlistPosition(resourceId, slotId, date, priority, id);
            Db.updateWaitlistPosition(id, pos);
            Db.insertAudit(userId, "WAITLIST_BOOKING", "BOOKING", id,
                    "position=" + pos + " priority=" + priority);

            r.bookingId = id;
            r.status = "Waitlisted";
            r.waitlistPosition = pos;
            r.message = "Resource is occupied. You are waitlist #" + pos;
            return r;

        } catch (SQLException e) {
            r.ok = false;
            r.status = "Rejected";
            r.message = "DB error: " + e.getMessage();
            return r;
        }
    }

    // ----------------------------------------------------------------
    // promoteAfterCancel
    // ----------------------------------------------------------------
    public static Map<String, Object> promoteAfterCancel(
            String labId, String date, String startTime, String endTime
    ) {
        Map<String, Object> out = new HashMap<>();
        out.put("promoted", false);
        try {
            int resourceId = Db.findResourceIdByNode(labId);
            if (resourceId < 0) {
                out.put("message", "Unknown labId: " + labId);
                return out;
            }
            int slotId = Db.findSlotId(startTime, endTime);
            if (slotId < 0) {
                out.put("message", "No matching slot");
                return out;
            }

            int next = Db.pickNextWaiter(resourceId, slotId, date);
            if (next < 0) {
                out.put("message", "No waitlist entries");
                return out;
            }

            int winnerUserId = Db.getBookingUserId(next);
            Db.promoteBooking(next);
            Db.insertNotification(winnerUserId, next,
                    "Good news! Your waitlisted booking #" + next + " is now confirmed.");
            Db.insertAudit(winnerUserId, "PROMOTE_WAITLIST", "BOOKING", next, "Confirmed");

            out.put("promoted", true);
            out.put("bookingId", next);
            out.put("winnerUserId", winnerUserId);
            out.put("message", "Promoted booking #" + next);
            return out;

        } catch (SQLException e) {
            out.put("message", "DB error: " + e.getMessage());
            return out;
        }
    }
}