-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: smartcampus
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `smartcampus`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `smartcampus` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `smartcampus`;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `BookingId` int NOT NULL AUTO_INCREMENT,
  `UserId` int NOT NULL,
  `ResourceId` int NOT NULL,
  `SlotId` int NOT NULL,
  `BookingDate` date NOT NULL,
  `Purpose` varchar(255) DEFAULT NULL,
  `Status` varchar(20) DEFAULT 'Pending',
  PRIMARY KEY (`BookingId`),
  UNIQUE KEY `ResourceId` (`ResourceId`,`SlotId`,`BookingDate`),
  KEY `UserId` (`UserId`),
  KEY `SlotId` (`SlotId`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `users` (`UserId`),
  CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`ResourceId`) REFERENCES `resources` (`ResourceId`),
  CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`SlotId`) REFERENCES `time_slot` (`SlotId`),
  CONSTRAINT `bookings_chk_1` CHECK ((`Status` in (_utf8mb4'Pending',_utf8mb4'Confirmed',_utf8mb4'Cancelled')))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES (2,2403173,5,2,'2026-10-08','Practical / Lab Session ΓÇö ui','Confirmed'),(3,2403173,6,4,'2026-10-15','Project Work ΓÇö asd','Confirmed'),(4,2403173,6,2,'2026-10-08','Project Work ΓÇö asd','Cancelled');
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `campus_edge`
--

DROP TABLE IF EXISTS `campus_edge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campus_edge` (
  `EdgeId` varchar(10) NOT NULL,
  `FromNodeId` varchar(50) NOT NULL,
  `ToNodeId` varchar(50) NOT NULL,
  `Distance` decimal(10,2) NOT NULL,
  `Latency` int DEFAULT NULL,
  `Capacity` int DEFAULT NULL,
  `Availability` varchar(30) DEFAULT 'Available',
  PRIMARY KEY (`EdgeId`),
  KEY `FromNodeId` (`FromNodeId`),
  KEY `ToNodeId` (`ToNodeId`),
  CONSTRAINT `campus_edge_ibfk_1` FOREIGN KEY (`FromNodeId`) REFERENCES `campus_node` (`NodeId`),
  CONSTRAINT `campus_edge_ibfk_2` FOREIGN KEY (`ToNodeId`) REFERENCES `campus_node` (`NodeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `campus_edge`
--

LOCK TABLES `campus_edge` WRITE;
/*!40000 ALTER TABLE `campus_edge` DISABLE KEYS */;
/*!40000 ALTER TABLE `campus_edge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `campus_node`
--

DROP TABLE IF EXISTS `campus_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `campus_node` (
  `NodeId` varchar(50) NOT NULL,
  `NodeName` varchar(100) NOT NULL,
  `NodeType` varchar(50) DEFAULT NULL,
  `Status` varchar(50) DEFAULT 'Active',
  PRIMARY KEY (`NodeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `campus_node`
--

LOCK TABLES `campus_node` WRITE;
/*!40000 ALTER TABLE `campus_node` DISABLE KEYS */;
INSERT INTO `campus_node` VALUES ('AC1','AIML Classroom 1','Classroom','Active'),('AC2','AIML Classroom 2','Classroom','Active'),('AC3','AIML Classroom 3','Classroom','Active'),('AL1','AIML Lab 1','Lab','Active'),('AL2','AIML Lab 2','Lab','Active'),('AL3','AIML Lab 3','Lab','Active'),('C1','CSE Classroom 1','Classroom','Active'),('C2','CSE Classroom 2','Classroom','Active'),('C3','CSE Classroom 3','Classroom','Active'),('CH1','Conference Hall','Conference Hall','Active'),('EC1','Electrical Classroom 1','Classroom','Active'),('EC2','Electrical Classroom 2','Classroom','Active'),('EC3','Electrical Classroom 3','Classroom','Active'),('EL1','Electrical Lab 1','Lab','Active'),('EL2','Electrical Lab 2','Lab','Active'),('L1','CSE Lab 1','Lab','Active'),('L2','CSE Lab 2','Lab','Active'),('L3','CSE Lab 3','Lab','Active'),('L4','CSE Lab 4','Lab','Active'),('MC1','Mechanical Classroom 1','Classroom','Active'),('MC2','Mechanical Classroom 2','Classroom','Active'),('ML1','Mechanical Lab 1','Lab','Active'),('ML2','Mechanical Lab 2','Lab','Active'),('N1','Main Gate','Gate','Active'),('N2','Gate 1','Gate','Active'),('N3','Library','Library','Active'),('N4','CSE Department','Department','Active'),('N5','Office','Office','Active'),('N6','Electrical Department','Department','Active'),('N7','AIML Building','Department','Active'),('N8','Mechanical Department','Department','Active');
/*!40000 ALTER TABLE `campus_node` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `department` (
  `DepartmentId` varchar(10) NOT NULL,
  `DepartmentName` varchar(100) NOT NULL,
  PRIMARY KEY (`DepartmentId`),
  UNIQUE KEY `DepartmentName` (`DepartmentName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES ('C1','AIML'),('A1','CSE'),('B1','ELECTRICAL'),('D1','MECHANICAL');
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `location` (
  `LocationId` int NOT NULL AUTO_INCREMENT,
  `BuildingName` varchar(100) NOT NULL,
  `FloorNumber` int DEFAULT NULL,
  `RoomNumber` varchar(20) NOT NULL,
  PRIMARY KEY (`LocationId`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
INSERT INTO `location` VALUES (1,'CSE Building',1,'L1'),(2,'CSE Building',1,'L2'),(3,'CSE Building',1,'L3'),(4,'CSE Building',1,'L4'),(5,'CSE Building',1,'C1'),(6,'CSE Building',1,'C2'),(7,'CSE Building',1,'C3'),(8,'Main Campus',1,'CH1'),(9,'Electrical Building',1,'EL1'),(10,'Electrical Building',1,'EL2'),(11,'Electrical Building',1,'EC1'),(12,'Electrical Building',1,'EC2'),(13,'Electrical Building',1,'EC3'),(14,'AIML Building',1,'AL1'),(15,'AIML Building',1,'AL2'),(16,'AIML Building',1,'AL3'),(17,'AIML Building',1,'AC1'),(18,'AIML Building',1,'AC2'),(19,'AIML Building',1,'AC3'),(20,'Mechanical Building',1,'ML1'),(21,'Mechanical Building',1,'ML2'),(22,'Mechanical Building',1,'MC1'),(23,'Mechanical Building',1,'MC2');
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource_type`
--

DROP TABLE IF EXISTS `resource_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resource_type` (
  `TypeId` int NOT NULL AUTO_INCREMENT,
  `TypeName` varchar(50) NOT NULL,
  `Description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`TypeId`),
  UNIQUE KEY `TypeName` (`TypeName`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource_type`
--

LOCK TABLES `resource_type` WRITE;
/*!40000 ALTER TABLE `resource_type` DISABLE KEYS */;
INSERT INTO `resource_type` VALUES (1,'Lab','Laboratory'),(2,'Classroom','Classroom'),(3,'Conference Hall','Conference Hall');
/*!40000 ALTER TABLE `resource_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resources`
--

DROP TABLE IF EXISTS `resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resources` (
  `ResourceId` int NOT NULL AUTO_INCREMENT,
  `ResourceName` varchar(100) NOT NULL,
  `DepartmentId` varchar(10) NOT NULL,
  `LocationId` int NOT NULL,
  `TypeId` int NOT NULL,
  `Capacity` int NOT NULL,
  `Status` varchar(30) DEFAULT 'Available',
  PRIMARY KEY (`ResourceId`),
  KEY `DepartmentId` (`DepartmentId`),
  KEY `LocationId` (`LocationId`),
  KEY `TypeId` (`TypeId`),
  CONSTRAINT `resources_ibfk_1` FOREIGN KEY (`DepartmentId`) REFERENCES `department` (`DepartmentId`),
  CONSTRAINT `resources_ibfk_2` FOREIGN KEY (`LocationId`) REFERENCES `location` (`LocationId`),
  CONSTRAINT `resources_ibfk_3` FOREIGN KEY (`TypeId`) REFERENCES `resource_type` (`TypeId`),
  CONSTRAINT `resources_chk_1` CHECK ((`Capacity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resources`
--

LOCK TABLES `resources` WRITE;
/*!40000 ALTER TABLE `resources` DISABLE KEYS */;
INSERT INTO `resources` VALUES (1,'CSE Lab 1','A1',1,1,50,'Available'),(2,'CSE Lab 2','A1',2,1,50,'Available'),(3,'CSE Lab 3','A1',3,1,50,'Available'),(4,'CSE Lab 4','A1',4,1,50,'Available'),(5,'CSE Classroom 1','A1',5,2,50,'Available'),(6,'CSE Classroom 2','A1',6,2,50,'Available'),(7,'CSE Classroom 3','A1',7,2,50,'Available'),(8,'Conference Hall','A1',8,3,100,'Available'),(9,'Electrical Lab 1','B1',9,1,50,'Available'),(10,'Electrical Lab 2','B1',10,1,50,'Available'),(11,'Electrical Classroom 1','B1',11,2,50,'Available'),(12,'Electrical Classroom 2','B1',12,2,50,'Available'),(13,'Electrical Classroom 3','B1',13,2,50,'Available'),(14,'AIML Lab 1','C1',14,1,50,'Available'),(15,'AIML Lab 2','C1',15,1,50,'Available'),(16,'AIML Lab 3','C1',16,1,50,'Available'),(17,'AIML Classroom 1','C1',17,2,50,'Available'),(18,'AIML Classroom 2','C1',18,2,50,'Available'),(19,'AIML Classroom 3','C1',19,2,50,'Available'),(20,'Mechanical Lab 1','D1',20,1,50,'Available'),(21,'Mechanical Lab 2','D1',21,1,50,'Available'),(22,'Mechanical Classroom 1','D1',22,2,50,'Available'),(23,'Mechanical Classroom 2','D1',23,2,50,'Available');
/*!40000 ALTER TABLE `resources` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route_history`
--

DROP TABLE IF EXISTS `route_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `route_history` (
  `RouteHistoryId` int NOT NULL AUTO_INCREMENT,
  `UserId` int NOT NULL,
  `StartNodeId` varchar(50) NOT NULL,
  `EndNodeId` varchar(50) NOT NULL,
  `RouteDate` date NOT NULL,
  `Distance` decimal(10,2) DEFAULT NULL,
  `RouteStatus` varchar(30) DEFAULT 'Completed',
  PRIMARY KEY (`RouteHistoryId`),
  KEY `UserId` (`UserId`),
  KEY `StartNodeId` (`StartNodeId`),
  KEY `EndNodeId` (`EndNodeId`),
  CONSTRAINT `route_history_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `users` (`UserId`),
  CONSTRAINT `route_history_ibfk_2` FOREIGN KEY (`StartNodeId`) REFERENCES `campus_node` (`NodeId`),
  CONSTRAINT `route_history_ibfk_3` FOREIGN KEY (`EndNodeId`) REFERENCES `campus_node` (`NodeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `route_history`
--

LOCK TABLES `route_history` WRITE;
/*!40000 ALTER TABLE `route_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `route_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `time_slot`
--

DROP TABLE IF EXISTS `time_slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `time_slot` (
  `SlotId` int NOT NULL AUTO_INCREMENT,
  `StartTime` time NOT NULL,
  `EndTime` time NOT NULL,
  PRIMARY KEY (`SlotId`),
  CONSTRAINT `time_slot_chk_1` CHECK ((`EndTime` > `StartTime`))
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `time_slot`
--

LOCK TABLES `time_slot` WRITE;
/*!40000 ALTER TABLE `time_slot` DISABLE KEYS */;
INSERT INTO `time_slot` VALUES (1,'09:00:00','10:00:00'),(2,'10:00:00','11:00:00'),(3,'11:00:00','12:00:00'),(4,'12:00:00','13:00:00'),(5,'13:00:00','14:00:00'),(6,'14:00:00','15:00:00'),(7,'15:00:00','16:00:00'),(8,'16:00:00','17:00:00');
/*!40000 ALTER TABLE `time_slot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `UserId` int NOT NULL,
  `Password` varchar(255) NOT NULL,
  `Role` varchar(20) NOT NULL,
  `Status` varchar(20) DEFAULT 'Active',
  PRIMARY KEY (`UserId`),
  CONSTRAINT `users_chk_1` CHECK ((`Role` in (_utf8mb4'Student',_utf8mb4'Faculty',_utf8mb4'Admin')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (10001,'10001','Faculty','Active'),(10002,'10002','Faculty','Active'),(10003,'10003','Faculty','Active'),(10004,'10004','Faculty','Active'),(10005,'10005','Faculty','Active'),(10006,'10006','Faculty','Active'),(10007,'10007','Faculty','Active'),(10008,'10008','Faculty','Active'),(10009,'10009','Faculty','Active'),(10010,'10010','Faculty','Active'),(10011,'10011','Faculty','Active'),(10012,'10012','Faculty','Active'),(10013,'10013','Faculty','Active'),(10014,'10014','Faculty','Active'),(10015,'10015','Faculty','Active'),(10016,'10016','Faculty','Active'),(10017,'10017','Faculty','Active'),(10018,'10018','Faculty','Active'),(10019,'10019','Faculty','Active'),(10020,'10020','Faculty','Active'),(99999,'99999','Admin','Active'),(2403140,'2403140','Student','Active'),(2403142,'2403142','Student','Active'),(2403143,'2403143','Student','Active'),(2403144,'2403144','Student','Active'),(2403146,'2403146','Student','Active'),(2403147,'2403147','Student','Active'),(2403149,'2403149','Student','Active'),(2403150,'2403150','Student','Active'),(2403151,'2403151','Student','Active'),(2403152,'2403152','Student','Active'),(2403153,'2403153','Student','Active'),(2403154,'2403154','Student','Active'),(2403155,'2403155','Student','Active'),(2403156,'2403156','Student','Active'),(2403157,'2403157','Student','Active'),(2403158,'2403158','Student','Active'),(2403159,'2403159','Student','Active'),(2403161,'2403161','Student','Active'),(2403163,'2403163','Student','Active'),(2403164,'2403164','Student','Active'),(2403165,'2403165','Student','Active'),(2403167,'2403167','Student','Active'),(2403168,'2403168','Student','Active'),(2403169,'2403169','Student','Active'),(2403170,'2403170','Student','Active'),(2403171,'2403171','Student','Active'),(2403172,'2403172','Student','Active'),(2403173,'2403173','Student','Active'),(2403174,'2403174','Student','Active'),(2403175,'2403175','Student','Active');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'smartcampus'
--
/*!50003 DROP PROCEDURE IF EXISTS `CANCEL_BOOKING_TRANSACTION` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CANCEL_BOOKING_TRANSACTION`(IN p_BookingId INT)
BEGIN
    DECLARE v_status VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT Status INTO v_status
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

    COMMIT;

    SELECT p_BookingId AS BookingId, 'Booking cancelled successfully' AS Message;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `CREATE_BOOKING_TRANSACTION` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CREATE_BOOKING_TRANSACTION`(
    IN p_UserId INT,
    IN p_ResourceId INT,
    IN p_SlotId INT,
    IN p_BookingDate DATE,
    IN p_Purpose VARCHAR(255)
)
BEGIN
    DECLARE v_user_count INT DEFAULT 0;
    DECLARE v_slot_count INT DEFAULT 0;
    DECLARE v_resource_status VARCHAR(30);
    DECLARE v_booking_count INT DEFAULT 0;

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
    (UserId, ResourceId, SlotId, BookingDate, Purpose, Status)
    VALUES
    (p_UserId, p_ResourceId, p_SlotId, p_BookingDate, p_Purpose, 'Confirmed');

    COMMIT;

    SELECT LAST_INSERT_ID() AS BookingId, 'Booking committed successfully' AS Message;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-08 22:18:00

--
-- Stored procedures for smartcampus
--

DROP PROCEDURE IF EXISTS `CREATE_BOOKING_TRANSACTION`;
DROP PROCEDURE IF EXISTS `CANCEL_BOOKING_TRANSACTION`;

DELIMITER $$
CREATE PROCEDURE `CREATE_BOOKING_TRANSACTION`(
    IN p_UserId INT,
    IN p_ResourceId INT,
    IN p_SlotId INT,
    IN p_BookingDate DATE,
    IN p_Purpose VARCHAR(255)
)
BEGIN
    DECLARE v_user_count INT DEFAULT 0;
    DECLARE v_slot_count INT DEFAULT 0;
    DECLARE v_resource_status VARCHAR(30);
    DECLARE v_booking_count INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT COUNT(*) INTO v_user_count FROM USERS
    WHERE UserId = p_UserId AND Status = 'Active';

    IF v_user_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid or inactive user';
    END IF;

    SELECT Status INTO v_resource_status FROM RESOURCES
    WHERE ResourceId = p_ResourceId FOR UPDATE;

    IF v_resource_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Resource does not exist';
    END IF;

    IF v_resource_status <> 'Available' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Resource is not available';
    END IF;

    SELECT COUNT(*) INTO v_slot_count FROM TIME_SLOT WHERE SlotId = p_SlotId;

    IF v_slot_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid time slot';
    END IF;

    SELECT COUNT(*) INTO v_booking_count FROM BOOKINGS
    WHERE ResourceId = p_ResourceId
      AND SlotId = p_SlotId
      AND BookingDate = p_BookingDate
      AND Status IN ('Pending', 'Confirmed');

    IF v_booking_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Resource already booked for this slot and date';
    END IF;

    INSERT INTO BOOKINGS
    (UserId, ResourceId, SlotId, BookingDate, Purpose, Status)
    VALUES
    (p_UserId, p_ResourceId, p_SlotId, p_BookingDate, p_Purpose, 'Confirmed');

    COMMIT;

    SELECT LAST_INSERT_ID() AS BookingId, 'Booking committed successfully' AS Message;
END$$

CREATE PROCEDURE `CANCEL_BOOKING_TRANSACTION`(IN p_BookingId INT)
BEGIN
    DECLARE v_status VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT Status INTO v_status FROM BOOKINGS
    WHERE BookingId = p_BookingId FOR UPDATE;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking does not exist';
    END IF;

    IF v_status = 'Cancelled' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking is already cancelled';
    END IF;

    UPDATE BOOKINGS SET Status = 'Cancelled' WHERE BookingId = p_BookingId;

    COMMIT;

    SELECT p_BookingId AS BookingId, 'Booking cancelled successfully' AS Message;
END$$
DELIMITER ;
