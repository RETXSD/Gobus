-- ============================================
-- GoBus Database Setup
-- Run this in MySQL before starting the app
-- ============================================

CREATE DATABASE IF NOT EXISTS gobus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gobus;

CREATE TABLE IF NOT EXISTS users (
  id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  name     VARCHAR(100) NOT NULL,
  email    VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role     ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER'
);

CREATE TABLE IF NOT EXISTS bus (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  brand        VARCHAR(100) NOT NULL,
  plate_number VARCHAR(20)  NOT NULL UNIQUE,
  total_seats  INT          NOT NULL DEFAULT 40
);

CREATE TABLE IF NOT EXISTS schedule (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  bus_id         BIGINT          NOT NULL,
  route          VARCHAR(200)    NOT NULL,
  departure_time DATETIME        NOT NULL,
  price          DECIMAL(10,2)   NOT NULL,
  FOREIGN KEY (bus_id) REFERENCES bus(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id        BIGINT       NOT NULL,
  schedule_id    BIGINT       NOT NULL,
  seat_number    INT          NOT NULL,
  booking_code   VARCHAR(20)  NOT NULL UNIQUE,
  payment_status ENUM('PENDING','PAID') NOT NULL DEFAULT 'PENDING',
  created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id)     REFERENCES users(id)    ON DELETE CASCADE,
  FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking_seat (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id  BIGINT NOT NULL,
  seat_number INT    NOT NULL,
  UNIQUE KEY uq_booking_seat (booking_id, seat_number),
  FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notification (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id    BIGINT  NOT NULL,
  booking_id BIGINT  NOT NULL,
  message    TEXT    NOT NULL,
  send_time  DATETIME,
  is_read    BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (user_id)    REFERENCES users(id)   ON DELETE CASCADE,
  FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE
);

-- ============================================
-- Seed: default admin account
-- Email: admin@gobus.com | Password: admin123
-- ============================================
INSERT IGNORE INTO users (name, email, password, role)
VALUES ('Admin GoBus', 'admin@gobus.com', 'admin123', 'ADMIN');

-- ============================================
-- Seed: sample buses
-- ============================================
INSERT IGNORE INTO bus (brand, plate_number, total_seats) VALUES
  ('Haryanto',     'B 1234 HAR', 40),
  ('Sinar Jaya',   'B 5678 SIN', 44),
  ('Rosalia Indah','K 9012 ROS', 36);

-- ============================================
-- Seed: more buses
-- ============================================
INSERT IGNORE INTO bus (brand, plate_number, total_seats) VALUES
  ('Lorena',        'B 3344 LRN', 40),
  ('Primajasa',     'D 7788 PRM', 45),
  ('Pahala Kencana','B 9090 PKC', 40),
  ('ALS',           'BK 1122 ALS', 36),
  ('Damri',         'B 4455 DMR', 48);

-- ============================================
-- Seed: sample schedules
-- ============================================
INSERT INTO schedule (bus_id, route, departure_time, price)
SELECT b.id, 'Jakarta - Bandung', '2026-06-18 07:00:00', 125000.00
FROM bus b
WHERE b.plate_number = 'B 1234 HAR'
  AND NOT EXISTS (
    SELECT 1 FROM schedule s
    WHERE s.bus_id = b.id
      AND s.route = 'Jakarta - Bandung'
      AND s.departure_time = '2026-06-18 07:00:00'
  );

INSERT INTO schedule (bus_id, route, departure_time, price)
SELECT b.id, 'Jakarta - Yogyakarta', '2026-06-18 19:30:00', 285000.00
FROM bus b
WHERE b.plate_number = 'B 5678 SIN'
  AND NOT EXISTS (
    SELECT 1 FROM schedule s
    WHERE s.bus_id = b.id
      AND s.route = 'Jakarta - Yogyakarta'
      AND s.departure_time = '2026-06-18 19:30:00'
  );

INSERT INTO schedule (bus_id, route, departure_time, price)
SELECT b.id, 'Semarang - Surabaya', '2026-06-19 08:15:00', 180000.00
FROM bus b
WHERE b.plate_number = 'K 9012 ROS'
  AND NOT EXISTS (
    SELECT 1 FROM schedule s
    WHERE s.bus_id = b.id
      AND s.route = 'Semarang - Surabaya'
      AND s.departure_time = '2026-06-19 08:15:00'
  );

INSERT INTO schedule (bus_id, route, departure_time, price)
SELECT b.id, 'Jakarta - Surabaya', '2026-06-19 18:00:00', 350000.00
FROM bus b
WHERE b.plate_number = 'B 3344 LRN'
  AND NOT EXISTS (
    SELECT 1 FROM schedule s
    WHERE s.bus_id = b.id
      AND s.route = 'Jakarta - Surabaya'
      AND s.departure_time = '2026-06-19 18:00:00'
  );

INSERT INTO schedule (bus_id, route, departure_time, price)
SELECT b.id, 'Bandung - Cirebon', '2026-06-20 06:30:00', 95000.00
FROM bus b
WHERE b.plate_number = 'D 7788 PRM'
  AND NOT EXISTS (
    SELECT 1 FROM schedule s
    WHERE s.bus_id = b.id
      AND s.route = 'Bandung - Cirebon'
      AND s.departure_time = '2026-06-20 06:30:00'
  );

INSERT INTO schedule (bus_id, route, departure_time, price)
SELECT b.id, 'Jakarta - Malang', '2026-06-20 17:45:00', 375000.00
FROM bus b
WHERE b.plate_number = 'B 9090 PKC'
  AND NOT EXISTS (
    SELECT 1 FROM schedule s
    WHERE s.bus_id = b.id
      AND s.route = 'Jakarta - Malang'
      AND s.departure_time = '2026-06-20 17:45:00'
  );

INSERT INTO schedule (bus_id, route, departure_time, price)
SELECT b.id, 'Medan - Padang', '2026-06-21 09:00:00', 240000.00
FROM bus b
WHERE b.plate_number = 'BK 1122 ALS'
  AND NOT EXISTS (
    SELECT 1 FROM schedule s
    WHERE s.bus_id = b.id
      AND s.route = 'Medan - Padang'
      AND s.departure_time = '2026-06-21 09:00:00'
  );

INSERT INTO schedule (bus_id, route, departure_time, price)
SELECT b.id, 'Jakarta - Lampung', '2026-06-21 13:00:00', 165000.00
FROM bus b
WHERE b.plate_number = 'B 4455 DMR'
  AND NOT EXISTS (
    SELECT 1 FROM schedule s
    WHERE s.bus_id = b.id
      AND s.route = 'Jakarta - Lampung'
      AND s.departure_time = '2026-06-21 13:00:00'
  );
