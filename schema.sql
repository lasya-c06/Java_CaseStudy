-- Create Database
CREATE DATABASE IF NOT EXISTS marksheet_system;
USE marksheet_system;

-- Create User
CREATE USER IF NOT EXISTS 'marksheet_user'@'localhost'
IDENTIFIED BY 'marksheet123';

GRANT ALL PRIVILEGES ON marksheet_system.* 
TO 'marksheet_user'@'localhost';

FLUSH PRIVILEGES;

-- =========================================
-- USERS TABLE
-- =========================================
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(100) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(150) NOT NULL DEFAULT '',
    role VARCHAR(30) NOT NULL,
    teacher_id VARCHAR(100),
    department VARCHAR(100),
    student_id VARCHAR(100),
    security_question VARCHAR(255),
    security_answer VARCHAR(255)
);

-- =========================================
-- MARKSHEETS TABLE
-- =========================================
CREATE TABLE IF NOT EXISTS marksheets (
    student_id VARCHAR(100) PRIMARY KEY,
    username VARCHAR(100),
    student_name VARCHAR(150) NOT NULL,
    department VARCHAR(100),
    total_classes INT DEFAULT 0,
    attended_classes INT DEFAULT 0,
    status VARCHAR(50),
    remarks TEXT,
    institution_name VARCHAR(255),
    institution_address VARCHAR(255),
    teacher_id VARCHAR(100),
    max_marks_per_subject INT DEFAULT 100,
    correction_requested BOOLEAN DEFAULT FALSE,
    correction_details TEXT
);

-- =========================================
-- MARKSHEET SUBJECTS TABLE
-- =========================================
CREATE TABLE IF NOT EXISTS marksheet_subjects (
    student_id VARCHAR(100) NOT NULL,
    subject_index INT NOT NULL,
    subject_name VARCHAR(150) NOT NULL,
    marks INT DEFAULT 0,
    highest_marks INT DEFAULT 0,

    PRIMARY KEY (student_id, subject_index),

    CONSTRAINT fk_marksheet_subjects_student
    FOREIGN KEY (student_id)
    REFERENCES marksheets(student_id)
    ON DELETE CASCADE
);

-- =========================================
-- DEPARTMENT SETTINGS TABLE
-- =========================================
CREATE TABLE IF NOT EXISTS department_settings (
    department VARCHAR(100) PRIMARY KEY,
    total_classes INT DEFAULT 40
);

-- =========================================
-- DEPARTMENT SUBJECTS TABLE
-- =========================================
CREATE TABLE IF NOT EXISTS department_subjects (
    department VARCHAR(100) NOT NULL,
    subject_index INT NOT NULL,
    subject_name VARCHAR(150) NOT NULL,

    PRIMARY KEY (department, subject_index),

    CONSTRAINT fk_department_subjects_department
    FOREIGN KEY (department)
    REFERENCES department_settings(department)
    ON DELETE CASCADE
);