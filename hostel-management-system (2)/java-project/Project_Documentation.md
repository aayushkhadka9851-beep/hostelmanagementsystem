# Hostel Management System - Project Documentation

This document provides complete architecture blueprinting and project guidelines for the **Hostel Management System** built with **Java Server Pages (JSP), Java Servlets, JDBC, MySQL, Tomcat 10, and MVC Architecture**.

---

## 1. System Overview & Purpose
The Hostel Management System is a robust enterprise platform designed to coordinate student lodging, room inventory allocations, dynamic bed occupancy accounting, and financial payment ledgers.

---

## 2. Model-View-Controller (MVC) Architectural Design
The codebase implements true separation of concerns as modeled below:

```
                  ┌─────────────────────────────────┐
                  │              VIEW               │
                  │   (JSP pages + Bootstrap CSS)   │
                  └────────────────┬─────────▲──────┘
                                   │         │
                 Forwards Requests │         │ Returns Data Context
                 (GET/POST forms)  │         │ (Request Attributes)
                                   ▼         │
                  ┌──────────────────────────┴──────┐
                  │           CONTROLLER            │
                  │     (Jakarta EE Servlets)       │
                  └────────────────┬─────────▲──────┘
                                   │         │
                Calls DAO Methods  │         │ Instantiates Model Objects 
                (Business Logic)   │         │ (Bean mappings)
                                   ▼         │
                  ┌──────────────────────────┴──────┐
                  │             MODEL               │
                  │    (Java DAOs, JDBC, MySQL)     │
                  └─────────────────────────────────┘
```

1. **Model Layer (`model/` & `dao/`):** Represents entity objects (JavaBeans mapping database rows) and Data Access Objects (DAOs) executing direct database SQL operations securely.
2. **View Layer (`webapp/` JSPs):** Renders HTML views constructed dynamically on the server and delivered as standard CSS/JS assets.
3. **Controller Layer (`controller/`):** Intercepts requests, validates authorization profiles, executes appropriate service calls, and binds returned collections to JSP rendering scopes.

---

## 3. Database Schema Blueprinting & Entity Attributes

The database contains five relational tables representing core objects:

```
  ┌──────────────┐          ┌──────────────┐          ┌──────────────┐
  │    USERS     │          │  ROOM_TYPES  │          │    ROOMS     │
  ├──────────────┤          ├──────────────┤          ├──────────────┤
  │ PK id        │          │ PK type_id   │          │ PK room_id   │
  │  username    │          │  type_name   │          │  room_number │
  │  password    │          └──────────────┘          │  room_type   │
  │  role        │                                    │  capacity    │
  └──────────────┘                                    │  monthly_rent│
                                                      │  occupied_bed│
                                                      │  status      │
                                                      └──────┬───────┘
                                                             │
                                                             │ 1
                                                             │
                                                             │ 0..* (Nullable)
                                                      ┌──────▼───────┐
  ┌──────────────┐                                    │   STUDENTS   │
  │   PAYMENTS   │                                    ├──────────────┤
  ├──────────────┤                                    │ PK student_id│
  │ PK payment_id│*                                  1│  student_name│
  │ FK student_id├────────────────────────────────────┤  email       │
  │  amount      │                                    │  contact     │
  │  payment_date│                                    │  emergency_co│
  │  status      │                                    │  address     │
  └──────────────┘                                    │ FK room_id   │
                                                      │  admission_da│
                                                      │  status      │
                                                      └──────────────┘
```

### Table 1: `users`
Represents administrative operators.
- `id` (INT, Primary Key, Auto-Increment)
- `username` (VARCHAR, Unique, NOT NULL)
- `password` (VARCHAR, NOT NULL)
- `role` (VARCHAR, NOT NULL, DEFAULT "Admin")

### Table 2: `room_types`
Configurable categorization dictionary.
- `type_id` (INT, Primary Key, Auto-Increment)
- `type_name` (VARCHAR, Unique, NOT NULL)

### Table 3: `rooms`
Physical room records.
- `room_id` (INT, Primary Key, Auto-Increment)
- `room_number` (VARCHAR, Unique, NOT NULL)
- `room_type` (VARCHAR, NOT NULL)
- `capacity` (INT, NOT NULL)
- `monthly_rent` (DECIMAL, NOT NULL)
- `occupied_beds` (INT, NOT NULL, DEFAULT 0)
- `status` (ENUM: 'Available', 'Full', 'Maintenance')

### Table 4: `students`
Registered hostel residents.
- `student_id` (INT, Primary Key, Auto-Increment)
- `student_name` (VARCHAR, NOT NULL)
- `email` (VARCHAR, Unique, NOT NULL)
- `contact` (VARCHAR, NOT NULL)
- `emergency_contact` (VARCHAR, NOT NULL)
- `address` (TEXT, NOT NULL)
- `room_id` (INT, Foreign Key referencing `rooms.room_id`, Nullable)
- `admission_date` (DATE, NOT NULL)
- `status` (ENUM: 'Active', 'Inactive')

### Table 5: `payments`
Ledger recording transactions and invoices.
- `payment_id` (INT, Primary Key, Auto-Increment)
- `student_id` (INT, Foreign Key referencing `students.student_id`, Cascade Delete)
- `amount` (DECIMAL, NOT NULL)
- `payment_date` (DATE, NOT NULL)
- `payment_status` (ENUM: 'Paid', 'Pending')

---

## 4. Key Architectural Patterns & Safety Features

### SQL Injection Protection: Prepared Statements
Every DAO query binds inputs strictly through placeholders (`?`). This isolates query structures from input strings, eliminating standard SQL Injection vectors:
```java
String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, username);
```

### Resource Closing & Memory Safety
Database connections and query threads are securely released inside `finally` blocks, resolving connection leaks completely:
```java
} finally {
    DBConnection.closeConnection(rs);
    DBConnection.closeConnection(stmt);
    DBConnection.closeConnection(conn);
}
```

### Automatic Cap Limits (Dual Reconciliation)
`StudentDAO` and `RoomDAO` work together: whenever an operator reassigns, registers, or deletes a student, the system triggers a background task that counts active roommates and updates the room's status (`Available` / `Full` / `Maintenance`) and bed count.
- Allocations are rejected automatically if `current_occupancy >= room_capacity`.
- Deletions are blocked automatically if a room has residents assigned to it.
- Rooms set to `Maintenance` are filtered out of student selection lists.
