# Hostel Management System - Verification & Testing Guide

This guide details testing procedures, boundary checkers, and SQL auditing statements to certify that the system's business rules and database constraints function flawlessly.

---

## 1. Authentication & Security Flow Testing

### Boundary Case: Direct Resource URL Bypass
- **Objective:** Ensure unauthenticated guests cannot load administrators' metrics cards simply by typing URLs.
- **Action:** Open an Incognito browser window. Do not login. Directly type:
  `http://localhost:8080/hostel-manager/dashboard` or `http://localhost:8080/hostel-manager/students`
- **Expected Outcome:** `AuthFilter` intercepts the request. It cancels forwarding and instantly redirects the user back to `login.jsp`.

### Test Case: Incorrect Login credentials
- **Objective:** Verify failure message handling.
- **Action:** Try to log in with username `admin` and incorrect password `wrongpwd`.
- **Expected Outcome:** Error parameters trigger. The browser remains on `login.jsp` and presents a red warning: *"Invalid username or password credentials..."*

---

## 2. Room Allocations & Capacity Constraints Testing

We must enforce that rooms never exceed their maximum capacity or accept reservations when offline.

### Test Case: Room Maintenance Safeguard
1. Go to **Rooms** > **Add New Room Unit**. Set `room_number` to `501`, capacity to `2`, and status to `Maintenance`.
2. Go to **Students** > **Register Student**.
3. Fill out academic profiling details. In the **Room Allocation** dropdown, select `Room 501`. Save.
4. **Expected Outcome:** The system displays an alert: *"Selected room is currently undergoing Maintenance"* and cancels the transaction database commit.

### Test Case: Room Capacity Overflow Safeguard
1. Pick an empty room with capacity `1`. (e.g. `Room 101`).
2. Register a student and assign them to `Room 101`.
3. Try to register a second student and assign them to the same `Room 101`.
4. **Expected Outcome:** The second registration is blocked with a clear warning: *"Selected room has reached full capacity"*. The database remains stable and protected from overflows.

---

## 3. Financial Ledger Integrations Testing

### Test Case: Automatic Metrics Adjustments on Status Swap
1. Open the **Payments** tab. Observe total Monthly Revenue on the dashboard card.
2. Spot an invoice with a warning state of `Pending` (e.g., student Camila Ruiz owes `$600.00`).
3. Swap the status selector from `Mark Pend` to `Mark Paid`.
4. Return to the core **Dashboard**.
5. **Expected Outcome:** Look at the Monthly Revenue metric. It should automatically increase by exactly `$600.00` because the database registered the transaction status update.

---

## 4. SQL Auditting Queries
Operators can run these raw SQL statements inside **MySQL Workbench 8.0** to double-check backend entity relationships and reconcile beds stats.

```sql
-- Reconcile and view active student roommates allocations
SELECT s.student_name, s.email, r.room_number, r.room_type, r.monthly_rent
FROM students s
LEFT JOIN rooms r ON s.room_id = r.room_id
WHERE s.status = 'Active';

-- Reconcile outstanding arrears balances
SELECT s.student_name, p.amount, p.payment_date, p.payment_status
FROM payments p
JOIN students s ON p.student_id = s.student_id
WHERE p.payment_status = 'Pending';
```
...
