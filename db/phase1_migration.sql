-- ============================================================
-- Phase 1 Migration (idempotent)
-- Safe to run multiple times. Adds columns/tables if missing,
-- always recreates procedures, seeds admin if missing.
-- Run AFTER db/smartcampus_dump.sql on a fresh machine.
-- ============================================================

USE smartcampus;

-- ------------------------------------------------------------
-- 1. BOOKINGS: add Priority and WaitlistPosition if missing
-- ------------------------------------------------------------

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'smartcampus'
    AND TABLE_NAME = 'BOOKINGS'
    AND COLUMN_NAME = 'Priority'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE BOOKINGS ADD COLUMN Priority INT DEFAULT 2',
  'SELECT ''Priority already exists'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'smartcampus'
    AND TABLE_NAME = 'BOOKINGS'
    AND COLUMN_NAME = 'WaitlistPosition'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE BOOKINGS ADD COLUMN WaitlistPosition INT NULL',
  'SELECT ''WaitlistPosition already exists'' AS msg'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2. NOTIFICATIONS table
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS NOTIFICATIONS (
  NotificationId INT NOT NULL AUTO_INCREMENT,
  UserId INT NOT NULL,
  BookingId INT NULL,
  Message VARCHAR(255) NOT NULL,
  IsRead TINYINT(1) NOT NULL DEFAULT 0,
  CreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (NotificationId),
  KEY idx_user_read (UserId, IsRead),
  CONSTRAINT fk_notif_user FOREIGN KEY (UserId)
    REFERENCES USERS(UserId) ON DELETE CASCADE,
  CONSTRAINT fk_notif_booking FOREIGN KEY (BookingId)
    REFERENCES BOOKINGS(BookingId) ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- 3. AUDIT_LOG table
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS AUDIT_LOG (
  AuditId INT NOT NULL AUTO_INCREMENT,
  UserId INT NULL,
  Action VARCHAR(64) NOT NULL,
  EntityType VARCHAR(64) NOT NULL,
  EntityId INT NULL,
  OldValue TEXT NULL,
  NewValue TEXT NULL,
  CreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (AuditId),
  KEY idx_entity (EntityType, EntityId),
  KEY idx_user (UserId)
);

-- ------------------------------------------------------------
-- 4. Procedures — always drop and recreate
-- ------------------------------------------------------------

DROP PROCEDURE IF EXISTS CREATE_BOOKING_TRANSACTION;
DROP PROCEDURE IF EXISTS CANCEL_BOOKING_TRANSACTION;
DROP PROCEDURE IF EXISTS WAITLIST_BOOKING_TRANSACTION;
DROP PROCEDURE IF EXISTS PROMOTE_WAITLIST_TRANSACTION;
DROP PROCEDURE IF EXISTS ADD_USER_TRANSACTION;
DROP PROCEDURE IF EXISTS UPDATE_USER_TRANSACTION;
DROP PROCEDURE IF EXISTS DELETE_USER_TRANSACTION;
DROP PROCEDURE IF EXISTS RESET_PASSWORD_TRANSACTION;

DELIMITER $$

CREATE PROCEDURE CREATE_BOOKING_TRANSACTION(
    IN p_UserId INT,
    IN p_ResourceId INT,
    IN p_SlotId INT,
    IN p_BookingDate DATE,
    IN p_Purpose VARCHAR(255),
    IN p_Priority INT
)
BEGIN
    DECLARE v_user_count INT DEFAULT 0;
    DECLARE v_slot_count INT DEFAULT 0;
    DECLARE v_resource_status VARCHAR(30);
    DECLARE v_booking_count INT DEFAULT 0;
    DECLARE v_new_id INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT COUNT(*) INTO v_user_count
    FROM USERS
    WHERE UserId = p_UserId AND Status = 'Active';

    IF v_user_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid or inactive user';
    END IF;

    SELECT Status INTO v_resource_status
    FROM RESOURCES
    WHERE ResourceId = p_ResourceId
    FOR UPDATE;

    IF v_resource_status IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource does not exist';
    END IF;

    IF v_resource_status <> 'Available' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource is not available';
    END IF;

    SELECT COUNT(*) INTO v_slot_count
    FROM TIME_SLOT
    WHERE SlotId = p_SlotId;

    IF v_slot_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid time slot';
    END IF;

    SELECT COUNT(*) INTO v_booking_count
    FROM BOOKINGS
    WHERE ResourceId = p_ResourceId
      AND SlotId = p_SlotId
      AND BookingDate = p_BookingDate
      AND Status IN ('Pending', 'Confirmed');

    IF v_booking_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource already booked for this slot and date';
    END IF;

    INSERT INTO BOOKINGS
    (UserId, ResourceId, SlotId, BookingDate, Purpose, Status, Priority)
    VALUES
    (p_UserId, p_ResourceId, p_SlotId, p_BookingDate, p_Purpose, 'Confirmed', COALESCE(p_Priority, 2));

    SET v_new_id = LAST_INSERT_ID();

    INSERT INTO AUDIT_LOG (UserId, Action, EntityType, EntityId, NewValue)
    VALUES (p_UserId, 'CREATE_BOOKING', 'BOOKING', v_new_id,
            CONCAT('resource=', p_ResourceId, ' date=', p_BookingDate));

    COMMIT;

    SELECT v_new_id AS BookingId, 'Booking committed successfully' AS Message;
END$$

CREATE PROCEDURE CANCEL_BOOKING_TRANSACTION(IN p_BookingId INT)
BEGIN
    DECLARE v_status VARCHAR(20);
    DECLARE v_userId INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT Status, UserId INTO v_status, v_userId
    FROM BOOKINGS
    WHERE BookingId = p_BookingId
    FOR UPDATE;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Booking does not exist';
    END IF;

    IF v_status = 'Cancelled' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Booking is already cancelled';
    END IF;

    UPDATE BOOKINGS
    SET Status = 'Cancelled'
    WHERE BookingId = p_BookingId;

    INSERT INTO AUDIT_LOG (UserId, Action, EntityType, EntityId, OldValue, NewValue)
    VALUES (v_userId, 'CANCEL_BOOKING', 'BOOKING', p_BookingId, v_status, 'Cancelled');

    INSERT INTO NOTIFICATIONS (UserId, BookingId, Message)
    VALUES (v_userId, p_BookingId,
            CONCAT('Your booking #', p_BookingId, ' has been cancelled.'));

    COMMIT;

    SELECT p_BookingId AS BookingId, 'Booking cancelled successfully' AS Message;
END$$

CREATE PROCEDURE WAITLIST_BOOKING_TRANSACTION(
    IN p_UserId INT,
    IN p_ResourceId INT,
    IN p_SlotId INT,
    IN p_BookingDate DATE,
    IN p_Purpose VARCHAR(255),
    IN p_Priority INT
)
BEGIN
    DECLARE v_user_count INT DEFAULT 0;
    DECLARE v_slot_count INT DEFAULT 0;
    DECLARE v_new_id INT DEFAULT 0;
    DECLARE v_position INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT COUNT(*) INTO v_user_count
    FROM USERS
    WHERE UserId = p_UserId AND Status = 'Active';

    IF v_user_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid or inactive user';
    END IF;

    SELECT COUNT(*) INTO v_slot_count
    FROM TIME_SLOT
    WHERE SlotId = p_SlotId;

    IF v_slot_count = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid time slot';
    END IF;

    IF EXISTS (
        SELECT 1 FROM BOOKINGS
        WHERE UserId = p_UserId
          AND ResourceId = p_ResourceId
          AND SlotId = p_SlotId
          AND BookingDate = p_BookingDate
          AND Status IN ('Waitlisted', 'Confirmed', 'Pending')
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'You already have a request for this resource and slot';
    END IF;

    INSERT INTO BOOKINGS
    (UserId, ResourceId, SlotId, BookingDate, Purpose, Status, Priority)
    VALUES
    (p_UserId, p_ResourceId, p_SlotId, p_BookingDate, p_Purpose,
     'Waitlisted', COALESCE(p_Priority, 2));

    SET v_new_id = LAST_INSERT_ID();

    SELECT COUNT(*) INTO v_position
    FROM BOOKINGS
    WHERE ResourceId = p_ResourceId
      AND SlotId = p_SlotId
      AND BookingDate = p_BookingDate
      AND Status = 'Waitlisted'
      AND (
        Priority < p_Priority
        OR (Priority = p_Priority AND BookingId < v_new_id)
      );

    UPDATE BOOKINGS
    SET WaitlistPosition = v_position + 1
    WHERE BookingId = v_new_id;

    INSERT INTO AUDIT_LOG (UserId, Action, EntityType, EntityId, NewValue)
    VALUES (p_UserId, 'WAITLIST_BOOKING', 'BOOKING', v_new_id,
            CONCAT('position=', v_position + 1, ' priority=', p_Priority));

    COMMIT;

    SELECT v_new_id AS BookingId,
           v_position + 1 AS WaitlistPosition,
           'You have been added to the waitlist' AS Message;
END$$

CREATE PROCEDURE PROMOTE_WAITLIST_TRANSACTION(
    IN p_ResourceId INT,
    IN p_SlotId INT,
    IN p_BookingDate DATE
)
BEGIN
    DECLARE v_winner_id INT DEFAULT NULL;
    DECLARE v_winner_user INT DEFAULT NULL;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT BookingId, UserId INTO v_winner_id, v_winner_user
    FROM BOOKINGS
    WHERE ResourceId = p_ResourceId
      AND SlotId = p_SlotId
      AND BookingDate = p_BookingDate
      AND Status = 'Waitlisted'
    ORDER BY Priority ASC, BookingId ASC
    LIMIT 1
    FOR UPDATE;

    IF v_winner_id IS NULL THEN
        COMMIT;
        SELECT NULL AS PromotedBookingId, NULL AS WinnerUserId,
               'No waitlist entries' AS Message;
    ELSE
        UPDATE BOOKINGS
        SET Status = 'Confirmed', WaitlistPosition = NULL
        WHERE BookingId = v_winner_id;

        INSERT INTO NOTIFICATIONS (UserId, BookingId, Message)
        VALUES (v_winner_user, v_winner_id,
                CONCAT('Good news! Your waitlisted booking #',
                       v_winner_id, ' is now confirmed.'));

        INSERT INTO AUDIT_LOG (UserId, Action, EntityType, EntityId, NewValue)
        VALUES (v_winner_user, 'PROMOTE_WAITLIST', 'BOOKING',
                v_winner_id, 'Confirmed');

        COMMIT;

        SELECT v_winner_id AS PromotedBookingId,
               v_winner_user AS WinnerUserId,
               'Waitlist entry promoted to Confirmed' AS Message;
    END IF;
END$$

CREATE PROCEDURE ADD_USER_TRANSACTION(
    IN p_UserId INT,
    IN p_Password VARCHAR(255),
    IN p_Role VARCHAR(20)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF EXISTS (SELECT 1 FROM USERS WHERE UserId = p_UserId) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'User already exists';
    END IF;

    INSERT INTO USERS (UserId, Password, Role, Status)
    VALUES (p_UserId, p_Password, p_Role, 'Active');

    INSERT INTO AUDIT_LOG (UserId, Action, EntityType, EntityId, NewValue)
    VALUES (p_UserId, 'ADD_USER', 'USER', p_UserId, CONCAT('role=', p_Role));

    COMMIT;

    SELECT p_UserId AS UserId, 'User added successfully' AS Message;
END$$

CREATE PROCEDURE UPDATE_USER_TRANSACTION(
    IN p_UserId INT,
    IN p_Role VARCHAR(20),
    IN p_Status VARCHAR(20)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF NOT EXISTS (SELECT 1 FROM USERS WHERE UserId = p_UserId) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'User does not exist';
    END IF;

    UPDATE USERS
    SET Role = COALESCE(p_Role, Role),
        Status = COALESCE(p_Status, Status)
    WHERE UserId = p_UserId;

    INSERT INTO AUDIT_LOG (UserId, Action, EntityType, EntityId, NewValue)
    VALUES (p_UserId, 'UPDATE_USER', 'USER', p_UserId,
            CONCAT('role=', p_Role, ' status=', p_Status));

    COMMIT;

    SELECT p_UserId AS UserId, 'User updated successfully' AS Message;
END$$

CREATE PROCEDURE DELETE_USER_TRANSACTION(IN p_UserId INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF NOT EXISTS (SELECT 1 FROM USERS WHERE UserId = p_UserId) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'User does not exist';
    END IF;

    DELETE FROM USERS WHERE UserId = p_UserId;

    INSERT INTO AUDIT_LOG (UserId, Action, EntityType, EntityId, OldValue)
    VALUES (p_UserId, 'DELETE_USER', 'USER', p_UserId, 'deleted');

    COMMIT;

    SELECT p_UserId AS UserId, 'User deleted successfully' AS Message;
END$$

CREATE PROCEDURE RESET_PASSWORD_TRANSACTION(
    IN p_UserId INT,
    IN p_NewPassword VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF NOT EXISTS (SELECT 1 FROM USERS WHERE UserId = p_UserId) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'User does not exist';
    END IF;

    UPDATE USERS SET Password = p_NewPassword WHERE UserId = p_UserId;

    INSERT INTO AUDIT_LOG (UserId, Action, EntityType, EntityId)
    VALUES (p_UserId, 'RESET_PASSWORD', 'USER', p_UserId);

    COMMIT;

    SELECT p_UserId AS UserId, 'Password reset successfully' AS Message;
END$$

DELIMITER ;

-- ------------------------------------------------------------
-- 5. Seed admin if none exists
-- ------------------------------------------------------------

INSERT INTO USERS (UserId, Password, Role, Status)
SELECT 99999, 'admin123', 'Admin', 'Active'
WHERE NOT EXISTS (SELECT 1 FROM USERS WHERE Role = 'Admin');