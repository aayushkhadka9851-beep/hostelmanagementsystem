-- =====================================================================
-- HOSTEL MANAGEMENT SYSTEM - DATABASE SCHEMA SCRIPT
-- Tested on: MySQL 8.0+ / MariaDB 10.4+
-- Authors: System Operations Team
-- =====================================================================

CREATE DATABASE IF NOT EXISTS hostel_db;
USE hostel_db;

-- Clear previous tables if existing (Dependency order resolved)
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS room_types;
DROP TABLE IF EXISTS users;

-- 1. USERS TABLE (Admin Authentication Management)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'Admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_auth (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. ROOM TYPES TABLE (General Configuration Catalog)
CREATE TABLE room_types (
    type_id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. ROOMS TABLE (Inventory Unit Records)
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(50) NOT NULL UNIQUE,
    room_type VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    monthly_rent DECIMAL(10,2) NOT NULL,
    occupied_beds INT NOT NULL DEFAULT 0,
    status ENUM('Available', 'Full', 'Maintenance') NOT NULL DEFAULT 'Available',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_capacity CHECK (capacity > 0),
    CONSTRAINT chk_occupied CHECK (occupied_beds >= 0 AND occupied_beds <= capacity),
    CONSTRAINT chk_rent CHECK (monthly_rent >= 0),
    INDEX idx_room_query (room_number, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. STUDENTS TABLE (Registered Core Residents)
CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(200) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    contact VARCHAR(50) NOT NULL,
    emergency_contact VARCHAR(50) NOT NULL,
    address TEXT NOT NULL,
    room_id INT DEFAULT NULL,
    admission_date DATE NOT NULL,
    status ENUM('Active', 'Inactive') NOT NULL DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE SET NULL,
    INDEX idx_student_name (student_name),
    INDEX idx_student_room (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. PAYMENTS TABLE (Financial Ledger System)
CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_status ENUM('Paid', 'Pending') NOT NULL DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT chk_amount CHECK (amount > 0),
    INDEX idx_payment_status (payment_status),
    INDEX idx_payment_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =====================================================================
-- DATABASE SEED DATA (SAMPLE INSERTS)
-- =====================================================================

-- Seed Administrators (Password is plain-text/bcrypt based. Note: 'admin123')
INSERT INTO users (username, password, role) VALUES 
('admin', 'admin123', 'Admin'),
('manager', 'manager123', 'Manager');

-- Seed Room types configurations
INSERT INTO room_types (type_name) VALUES 
('Single Room'),
('Double Room'),
('Triple Room'),
('AC Room'),
('Non-AC Room'),
('Attached Bathroom');

-- Seed Rooms Units
INSERT INTO rooms (room_number, room_type, capacity, monthly_rent, occupied_beds, status) VALUES 
('101', 'Single Room', 1, 500.00, 1, 'Full'),
('102', 'Double Room', 2, 350.00, 1, 'Available'),
('103', 'AC Room', 2, 600.00, 2, 'Full'),
('104', 'Non-AC Room', 3, 400.00, 0, 'Available'),
('105', 'Double Room', 2, 350.00, 0, 'Maintenance');

-- Seed Hosteller students
INSERT INTO students (student_name, email, contact, emergency_contact, address, room_id, admission_date, status) VALUES 
('Aayush Khadka', 'aayush.khadka9851@gmail.com', '+977-9851000000', '+977-9801000000', 'Kathmandu, Nepal', 1, '2026-06-01', 'Active'),
('Bishal Dev', 'bishal.dev@example.com', '+1-555-0199', '+1-555-0100', 'New York, USA', 2, '2026-05-15', 'Active'),
('Camila Ruiz', 'camila@example.com', '+1-555-0188', '+1-555-0122', 'San Francisco, USA', 3, '2026-06-01', 'Active'),
('David Kim', 'david@example.com', '+1-555-0177', '+1-555-0133', 'Los Angeles, USA', 3, '2026-06-02', 'Active');

-- Seed Financial ledger
INSERT INTO payments (student_id, amount, payment_date, payment_status) VALUES 
(1, 500.00, '2026-06-01', 'Paid'),
(2, 350.00, '2026-05-15', 'Paid'),
(3, 600.00, '2026-06-01', 'Pending'),
(4, 600.00, '2026-06-02', 'Paid'),
(1, 500.00, '2026-06-08', 'Pending');

-- =====================================================================
-- DATABASE AUDITS & CONSTRAINTS TESTING HELPER
-- =====================================================================
-- SELECT students.student_name, rooms.room_number FROM students 
-- JOIN rooms ON students.room_id = rooms.room_id;
