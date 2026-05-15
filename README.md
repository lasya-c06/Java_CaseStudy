# Digital Marksheet Generator

A role-based desktop application built in Java that automates student marksheet generation, attendance tracking, and academic result management — backed by a MySQL database and a Swing GUI.

---

## Overview

The Digital Marksheet Generator is a full-stack Java desktop application designed to streamline academic record management for educational institutions. It supports two distinct user roles — **Teacher** and **Student** — each with a dedicated dashboard and controlled access to features. All data is persisted to a MySQL database with automatic schema creation on first launch.

---

## Features

### Authentication
- Secure registration and login for both Teachers and Students
- Role-based access control — Teachers and Students see different dashboards
- Password reset via security question and answer
- Legacy seeded account cleanup on startup

### Teacher dashboard
- Configure department-wide subject list and total class count
- Add students to the system
- Generate and manage marksheets for any student in their department
- Enter subject-wise marks with automatic total and grade calculation
- Set institution name and address on marksheets
- Approve or reject student correction requests with remarks
- View all student marksheets with attendance eligibility status

### Student dashboard
- View personal marksheet with subject-wise marks, total, and grade
- View attendance percentage and eligibility status (≥ 75% required)
- Request marksheet corrections with details
- Track correction request status

### Result calculation
- Automatic total marks computation across all subjects
- Grade assigned by percentage bracket: A+ (≥90%), A (≥80%), B (≥70%), C (≥60%), D (≥50%), F (below 50%)
- Attendance eligibility gate — students below 75% attendance are flagged

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| GUI | Java Swing with CardLayout |
| Database | MySQL 8.x |
| JDBC Driver | mysql-connector-j |
| Build | Manual / IDE-based |
| Persistence | JDBC with prepared statements and batch operations |

---

## Architecture

The project follows a clean three-layer architecture:

```
gui/          → Swing panels (LoginPanel, TeacherDashboard, StudentDashboard, MainApplication)
models/       → Domain entities (User, Student, Teacher, Result, Marksheet, Subject, Attendance)
services/     → Business and persistence logic (AuthService, DatabaseManager)
```

### Key design decisions

**Inheritance hierarchy** — `User` is an abstract base class extended by `Student` and `Teacher`, sharing common fields like username, name, role, and security credentials. `Result` is an abstract class extended by `Marksheet`, enforcing a contract for `calculateTotal()` and `calculateGrade()` — designed to support future result types such as consolidated or supplementary results.

**Static utility class** — `DatabaseManager` uses a private constructor to prevent instantiation. All methods are static and share a single `Connection` instance, ensuring one database connection is maintained across the entire application lifecycle. This also protects transaction integrity — `saveMarksheet()` and `saveDepartmentSettings()` both use explicit `commit()` / `rollback()` blocks on the shared connection.

**Service layer separation** — `AuthService` handles all authentication logic (login, registration, password reset) and delegates all persistence to `DatabaseManager`, keeping business rules separate from SQL.

**Automatic schema management** — `DatabaseManager.createTables()` runs once on first connection, creating all required tables (`users`, `marksheets`, `marksheet_subjects`, `department_settings`, `department_subjects`) with `IF NOT EXISTS` guards. `ensureColumn()` adds missing columns to existing tables, making the app forward-compatible with schema evolution.

**Configurable database credentials** — connection URL, username, and password are resolved in priority order: JVM system property → environment variable → `database.properties` file → hardcoded default. This makes the app portable across development and deployment environments without code changes.

---

## Object-oriented concepts demonstrated

| Concept | Where |
|---|---|
| Inheritance | `Student`, `Teacher` extend `User`; `Marksheet` extends `Result` |
| Abstraction | `User` and `Result` are abstract classes with enforced contracts |
| Encapsulation | All fields private with getters/setters; mark validation in `Subject.setMarks()` |
| Polymorphism | `Result r = new Marksheet(...)` — `calculateGrade()` dispatched at runtime |
| Aggregation | `Student` holds an `Attendance` object injected externally |
| Composition | `Marksheet` owns a `List<Subject>` created and managed internally |
| Static utility class | `DatabaseManager` — private constructor, all-static methods, single shared connection |
| Serializable | `User`, `Attendance`, `Subject` implement `Serializable` for safe object handling |

---

## Database schema

```
users
  username (PK), password, name, role, teacher_id,
  department, student_id, security_question, security_answer

marksheets
  student_id (PK), username, student_name, department,
  total_classes, attended_classes, status, remarks,
  institution_name, institution_address, teacher_id,
  max_marks_per_subject, correction_requested, correction_details

marksheet_subjects
  student_id (FK), subject_index, subject_name, marks, highest_marks

department_settings
  department (PK), total_classes

department_subjects
  department (FK), subject_index, subject_name
```

---

## Setup and run

### Prerequisites
- Java 17 or higher
- MySQL 8.x running locally
- `mysql-connector-j` JAR on the classpath

### Database configuration

Create a `database.properties` file in the project root:

```properties
marksheet.db.url=jdbc:mysql://localhost:3306/marksheet_system?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
marksheet.db.user=your_mysql_username
marksheet.db.password=your_mysql_password
```

Alternatively, set environment variables:

```bash
export MARKSHEET_DB_URL=jdbc:mysql://localhost:3306/marksheet_system...
export MARKSHEET_DB_USER=your_username
export MARKSHEET_DB_PASSWORD=your_password
```

Or pass JVM properties at runtime:

```bash
java -Dmarksheet.db.user=root -Dmarksheet.db.password=secret -cp ".:mysql-connector-j.jar" gui.MainApplication
```

### Compile and run

```bash
# Compile
javac -cp ".:mysql-connector-j.jar" models/*.java services/*.java gui/*.java

# Run
java -cp ".:mysql-connector-j.jar" gui.MainApplication
```

> On Windows, replace `:` with `;` in the classpath.

The application will automatically create all required database tables on first launch.

---

## Project structure

```
Java_Project/
├── models/
│   ├── User.java               # Abstract base class for all users
│   ├── Student.java            # Extends User, holds Attendance
│   ├── Teacher.java            # Extends User, holds department
│   ├── Result.java             # Abstract class with calculateTotal/Grade contract
│   ├── Marksheet.java          # Extends Result, owns Subject list
│   ├── Subject.java            # Value object with mark validation
│   └── Attendance.java         # Tracks total and attended classes
├── services/
│   ├── AuthService.java        # Login, register, password reset logic
│   └── DatabaseManager.java    # All MySQL operations, static utility class
├── gui/
│   ├── MainApplication.java    # Entry point, CardLayout screen manager
│   ├── LoginPanel.java         # Login and registration UI
│   ├── TeacherDashboard.java   # Teacher features UI
│   └── StudentDashboard.java   # Student features UI
└── database.properties         # DB credentials (not committed to version control)
```

---

## Author

**Lasya C** <br>
**Prathijna** <br>
**Nagalakshmi** <br>
**Gayatri** <br>
B.Tech Computer Science — Amrita Vishwa Vidyapeetham, Amritapuri  

