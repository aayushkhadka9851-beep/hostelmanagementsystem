import express from "express";
import path from "path";
import { createServer as createViteServer } from "vite";

// Interfaces mirroring the database design
interface User {
  id: number;
  username: string;
  role: string;
}

interface RoomType {
  type_id: number;
  type_name: string;
}

interface Room {
  room_id: number;
  room_number: string;
  room_type: string;
  capacity: number;
  monthly_rent: number;
  occupied_beds: number;
  status: "Available" | "Full" | "Maintenance";
}

interface Student {
  student_id: number;
  student_name: string;
  email: string;
  contact: string;
  emergency_contact: string;
  address: string;
  room_id: number | null; // null if unassigned
  admission_date: string;
  status: "Active" | "Inactive";
}

interface Payment {
  payment_id: number;
  student_id: number;
  student_name?: string; // resolved at API consumption level
  room_number?: string; // resolved at API consumption level
  amount: number;
  payment_date: string;
  payment_status: "Paid" | "Pending";
}

// Initial In-Memory State Mock Database
let roomTypes: RoomType[] = [
  { type_id: 1, type_name: "Single Room" },
  { type_id: 2, type_name: "Double Room" },
  { type_id: 3, type_name: "Triple Room" },
  { type_id: 4, type_name: "AC Room" },
  { type_id: 5, type_name: "Non-AC Room" },
];

let rooms: Room[] = [
  { room_id: 1, room_number: "101", room_type: "Single Room", capacity: 1, monthly_rent: 500, occupied_beds: 1, status: "Full" },
  { room_id: 2, room_number: "102", room_type: "Double Room", capacity: 2, monthly_rent: 350, occupied_beds: 1, status: "Available" },
  { room_id: 3, room_number: "103", room_type: "AC Room", capacity: 2, monthly_rent: 600, occupied_beds: 2, status: "Full" },
  { room_id: 4, room_number: "104", room_type: "Non-AC Room", capacity: 3, monthly_rent: 400, occupied_beds: 0, status: "Available" },
  { room_id: 5, room_number: "105", room_type: "Double Room", capacity: 2, monthly_rent: 350, occupied_beds: 0, status: "Maintenance" }
];

let students: Student[] = [
  { student_id: 1, student_name: "Aayush Khadka", email: "aayush.khadka9851@gmail.com", contact: "+977-9851000000", emergency_contact: "+977-9801000000", address: "Kathmandu, Nepal", room_id: 1, admission_date: "2026-06-01", status: "Active" },
  { student_id: 2, student_name: "Bishal Dev", email: "bishal.dev@example.com", contact: "+1-555-0199", emergency_contact: "+1-555-0100", address: "New York, USA", room_id: 2, admission_date: "2026-05-15", status: "Active" },
  { student_id: 3, student_name: "Camila Ruiz", email: "camila@example.com", contact: "+1-555-0188", emergency_contact: "+1-555-0122", address: "San Francisco, USA", room_id: 3, admission_date: "2026-06-01", status: "Active" },
  { student_id: 4, student_name: "David Kim", email: "david@example.com", contact: "+1-555-0177", emergency_contact: "+1-555-0133", address: "Los Angeles, USA", room_id: 3, admission_date: "2026-06-02", status: "Active" }
];

let payments: Payment[] = [
  { payment_id: 1, student_id: 1, amount: 500, payment_date: "2026-06-01", payment_status: "Paid" },
  { payment_id: 2, student_id: 2, amount: 350, payment_date: "2026-05-15", payment_status: "Paid" },
  { payment_id: 3, student_id: 3, amount: 600, payment_date: "2026-06-01", payment_status: "Pending" },
  { payment_id: 4, student_id: 4, amount: 600, payment_date: "2026-06-02", payment_status: "Paid" },
  { payment_id: 5, student_id: 1, amount: 500, payment_date: "2026-06-08", payment_status: "Pending" }
];

let hostelInfo = {
  hostelName: "Royal Oak Student Living",
  establishedDate: "2018-04-12",
  contactNo: "+1-234-567-890",
  email: "contact@royaloakhostel.com",
  address: "452 University Avenue, Suite B, Palo Alto, CA"
};

// Recent Activities Helper Queue (max 10)
let recentActivities: { id: number; action: string; time: string; category: "student" | "room" | "payment" | "settings" }[] = [
  { id: 1, action: "Admin logged into the dashboard.", time: "Just now", category: "settings" },
  { id: 2, action: "Payment of $600 recorded for David Kim.", time: "1 hour ago", category: "payment" },
  { id: 3, action: "Student Camila Ruiz assigned to Room 103.", time: "3 hours ago", category: "student" },
  { id: 4, action: "Room 105 state updated to Maintenance.", time: "5 hours ago", category: "room" },
  { id: 5, action: "New student registration: Aayush Khadka recorded.", time: "2 days ago", category: "student" }
];

function logActivity(action: string, category: "student" | "room" | "payment" | "settings") {
  recentActivities.unshift({
    id: Date.now(),
    action,
    time: "A moment ago",
    category
  });
  if (recentActivities.length > 10) {
    recentActivities.pop();
  }
}

// Recalculate occupied beds function
function updateOccupiedBedsCount() {
  rooms.forEach(room => {
    const activeStudentBedsCount = students.filter(
      s => s.room_id === room.room_id && s.status === "Active"
    ).length;
    room.occupied_beds = activeStudentBedsCount;

    if (room.status !== "Maintenance") {
      if (room.occupied_beds >= room.capacity) {
        room.status = "Full";
      } else {
        room.status = "Available";
      }
    }
  });
}

async function startServer() {
  const app = express();
  const PORT = 3000;

  app.use(express.json());

  // 1. AUTHENTICATION ENDPOINTS
  app.post("/api/auth/login", (req, res) => {
    const { username, password } = req.body;
    if (username === "admin" && password === "admin123") {
      logActivity("Administrator successfully authenticated.", "settings");
      res.json({
        success: true,
        user: { id: 1, username: "admin", role: "Admin" },
        token: "session-mock-token-hostel-admin-12345"
      });
    } else {
      res.status(401).json({ success: false, message: "Invalid username or password" });
    }
  });

  app.post("/api/auth/logout", (req, res) => {
    logActivity("Admin successfully logged out.", "settings");
    res.json({ success: true, message: "Logged out successfully" });
  });

  // Helper route protections are modeled directly on the UI using current token states

  // 2. DASHBOARD ENDPOINTS
  app.get("/api/dashboard/stats", (req, res) => {
    updateOccupiedBedsCount();
    const totalStudents = students.filter(s => s.status === "Active").length;
    const totalRooms = rooms.length;
    const occupiedRooms = rooms.filter(r => r.occupied_beds > 0).length;
    const availableRooms = rooms.filter(r => r.status === "Available" || r.occupied_beds < r.capacity && r.status !== "Maintenance").length;
    const pendingPaymentsCount = payments.filter(p => p.payment_status === "Pending").length;
    
    // Revenue calculations
    const paidPayments = payments.filter(p => p.payment_status === "Paid");
    const monthlyRevenue = paidPayments.reduce((sum, p) => sum + p.amount, 0);

    // Bed distribution calculation
    const totalBeds = rooms.reduce((sum, r) => sum + r.capacity, 0);
    const occupiedBeds = rooms.reduce((sum, r) => sum + r.occupied_beds, 0);
    const availableBeds = Math.max(0, totalBeds - occupiedBeds);

    res.json({
      totalStudents,
      totalRooms,
      occupiedRooms,
      availableRooms,
      pendingPaymentsCount,
      monthlyRevenue,
      bedDistribution: {
        totalBeds,
        occupiedBeds,
        availableBeds
      },
      recentActivities,
      roomsOccupancy: rooms.map(r => ({
        room_number: r.room_number,
        occupied: r.occupied_beds,
        capacity: r.capacity,
        status: r.status
      }))
    });
  });

  // 3. STUDENT MANAGEMENT ENDPOINTS
  app.get("/api/students", (req, res) => {
    const query = (req.query.q || "").toString().toLowerCase();
    
    const formattedStudents = students.map(student => {
      const room = rooms.find(r => r.room_id === student.room_id);
      return {
        ...student,
        room_number: room ? room.room_number : "Unassigned",
        room_type: room ? room.room_type : "N/A"
      };
    });

    if (query) {
      const filtered = formattedStudents.filter(
        s => s.student_name.toLowerCase().includes(query) ||
             s.email.toLowerCase().includes(query) ||
             s.contact.toLowerCase().includes(query) ||
             s.room_number.toLowerCase().includes(query)
      );
      return res.json(filtered);
    }

    res.json(formattedStudents);
  });

  app.get("/api/students/:id", (req, res) => {
    const id = parseInt(req.params.id);
    const student = students.find(s => s.student_id === id);
    if (!student) {
      return res.status(404).json({ error: "Student not found" });
    }
    const room = rooms.find(r => r.room_id === student.room_id);
    res.json({
      ...student,
      room_number: room ? room.room_number : "Unassigned"
    });
  });

  app.post("/api/students", (req, res) => {
    const { student_name, email, contact, emergency_contact, address, room_id, admission_date, status } = req.body;
    
    if (!student_name || !email || !contact) {
      return res.status(400).json({ error: "Name, email and contact number are required." });
    }

    const newId = students.length > 0 ? Math.max(...students.map(s => s.student_id)) + 1 : 1;
    const targetRoomId = room_id ? parseInt(room_id) : null;

    // Check capacity if room is assigned
    if (targetRoomId) {
      const targetRoom = rooms.find(r => r.room_id === targetRoomId);
      if (targetRoom) {
        if (targetRoom.status === "Maintenance") {
          return res.status(400).json({ error: `Room ${targetRoom.room_number} is currently under maintenance.` });
        }
        const assignedCount = students.filter(s => s.room_id === targetRoomId && s.status === "Active").length;
        if (assignedCount >= targetRoom.capacity) {
          return res.status(400).json({ error: `Room ${targetRoom.room_number} has no available beds.` });
        }
      }
    }

    const newStudent: Student = {
      student_id: newId,
      student_name,
      email,
      contact,
      emergency_contact: emergency_contact || "",
      address: address || "",
      room_id: targetRoomId,
      admission_date: admission_date || new Date().toISOString().split("T")[0],
      status: status || "Active"
    };

    students.push(newStudent);
    updateOccupiedBedsCount();
    logActivity(`Added student ${student_name} and assigned to Room ${newStudent.room_id ? rooms.find(r => r.room_id === newStudent.room_id)?.room_number : "Unassigned"}.`, "student");
    res.status(201).json(newStudent);
  });

  app.put("/api/students/:id", (req, res) => {
    const id = parseInt(req.params.id);
    const index = students.findIndex(s => s.student_id === id);
    if (index === -1) {
      return res.status(404).json({ error: "Student not found" });
    }

    const { student_name, email, contact, emergency_contact, address, room_id, admission_date, status } = req.body;
    const targetRoomId = room_id ? parseInt(room_id) : null;

    // Capacity checking if room is changed
    if (targetRoomId && targetRoomId !== students[index].room_id) {
      const targetRoom = rooms.find(r => r.room_id === targetRoomId);
      if (targetRoom) {
        if (targetRoom.status === "Maintenance") {
          return res.status(400).json({ error: `Room ${targetRoom.room_number} is currently under maintenance.` });
        }
        const assignedCount = students.filter(s => s.room_id === targetRoomId && s.status === "Active").length;
        if (assignedCount >= targetRoom.capacity) {
          return res.status(400).json({ error: `Room ${targetRoom.room_number} has no available beds.` });
        }
      }
    }

    students[index] = {
      ...students[index],
      student_name: student_name || students[index].student_name,
      email: email || students[index].email,
      contact: contact || students[index].contact,
      emergency_contact: emergency_contact ?? students[index].emergency_contact,
      address: address ?? students[index].address,
      room_id: targetRoomId,
      admission_date: admission_date || students[index].admission_date,
      status: status || students[index].status
    };

    updateOccupiedBedsCount();
    logActivity(`Updated info for student ${students[index].student_name}.`, "student");
    res.json(students[index]);
  });

  app.delete("/api/students/:id", (req, res) => {
    const id = parseInt(req.params.id);
    const index = students.findIndex(s => s.student_id === id);
    if (index === -1) {
      return res.status(404).json({ error: "Student not found" });
    }

    const removedStudent = students[index];
    students.splice(index, 1);
    
    // Clean up related payments
    payments = payments.filter(p => p.student_id !== id);

    updateOccupiedBedsCount();
    logActivity(`Removed student records for ${removedStudent.student_name}.`, "student");
    res.json({ success: true, message: `Student ${removedStudent.student_name} deleted successfully.` });
  });

  // 4. ROOM MANAGEMENT ENDPOINTS
  app.get("/api/rooms", (req, res) => {
    updateOccupiedBedsCount();
    const query = (req.query.q || "").toString().toLowerCase();
    
    if (query) {
      const filtered = rooms.filter(
        r => r.room_number.toLowerCase().includes(query) ||
             r.room_type.toLowerCase().includes(query) ||
             r.status.toLowerCase().includes(query)
      );
      return res.json(filtered);
    }
    res.json(rooms);
  });

  app.get("/api/rooms/:id", (req, res) => {
    const id = parseInt(req.params.id);
    const room = rooms.find(r => r.room_id === id);
    if (!room) {
      return res.status(404).json({ error: "Room not found" });
    }
    res.json(room);
  });

  app.post("/api/rooms", (req, res) => {
    const { room_number, room_type, capacity, monthly_rent, status } = req.body;
    
    if (!room_number || !room_type || !capacity || !monthly_rent) {
      return res.status(400).json({ error: "Room number, type, capacity and monthly rent are required." });
    }

    // Check duplicate room numbers
    if (rooms.some(r => r.room_number === room_number)) {
      return res.status(400).json({ error: `Room number ${room_number} already exists.` });
    }

    const newId = rooms.length > 0 ? Math.max(...rooms.map(r => r.room_id)) + 1 : 1;
    const newRoom: Room = {
      room_id: newId,
      room_number,
      room_type,
      capacity: parseInt(capacity),
      monthly_rent: parseFloat(monthly_rent),
      occupied_beds: 0,
      status: status || "Available"
    };

    rooms.push(newRoom);
    logActivity(`Added Room ${room_number} (${room_type}, capacity: ${capacity}).`, "room");
    res.status(201).json(newRoom);
  });

  app.put("/api/rooms/:id", (req, res) => {
    const id = parseInt(req.params.id);
    const index = rooms.findIndex(r => r.room_id === id);
    if (index === -1) {
      return res.status(404).json({ error: "Room not found" });
    }

    const { room_number, room_type, capacity, monthly_rent, status } = req.body;

    // Duplicate check
    if (room_number && room_number !== rooms[index].room_number) {
      if (rooms.some(r => r.room_number === room_number)) {
        return res.status(400).json({ error: `Room number ${room_number} already exists.` });
      }
    }

    rooms[index] = {
      ...rooms[index],
      room_number: room_number || rooms[index].room_number,
      room_type: room_type || rooms[index].room_type,
      capacity: capacity ? parseInt(capacity) : rooms[index].capacity,
      monthly_rent: monthly_rent ? parseFloat(monthly_rent) : rooms[index].monthly_rent,
      status: status || rooms[index].status
    };

    updateOccupiedBedsCount();
    logActivity(`Updated configurations for Room ${rooms[index].room_number}.`, "room");
    res.json(rooms[index]);
  });

  app.delete("/api/rooms/:id", (req, res) => {
    const id = parseInt(req.params.id);
    const index = rooms.findIndex(r => r.room_id === id);
    if (index === -1) {
      return res.status(404).json({ error: "Room not found" });
    }

    // Prevent deletion if occupied
    const occupiedCount = students.filter(s => s.room_id === id && s.status === "Active").length;
    if (occupiedCount > 0) {
      return res.status(400).json({ error: `Cannot delete Room ${rooms[index].room_number} because it has active student assignments.` });
    }

    const removedRoom = rooms[index];
    rooms.splice(index, 1);

    // Unassign inactive students if any
    students.forEach(s => {
      if (s.room_id === id) {
        s.room_id = null;
      }
    });

    logActivity(`Removed Room ${removedRoom.room_number} from catalog.`, "room");
    res.json({ success: true, message: `Room ${removedRoom.room_number} deleted successfully.` });
  });

  // 5. PAYMENT ENDPOINTS
  app.get("/api/payments", (req, res) => {
    const query = (req.query.q || "").toString().toLowerCase();

    const formattedPayments = payments.map(payment => {
      const student = students.find(s => s.student_id === payment.student_id);
      const room = student ? rooms.find(r => r.room_id === student.room_id) : null;
      return {
        ...payment,
        student_name: student ? student.student_name : "Deleted Student",
        room_number: room ? room.room_number : "Unassigned"
      };
    });

    if (query) {
      const filtered = formattedPayments.filter(
        p => p.student_name.toLowerCase().includes(query) ||
             p.room_number.toLowerCase().includes(query) ||
             p.payment_status.toLowerCase().includes(query) ||
             p.amount.toString().includes(query)
      );
      return res.json(filtered);
    }

    res.json(formattedPayments);
  });

  app.post("/api/payments", (req, res) => {
    const { student_id, amount, payment_date, payment_status } = req.body;
    
    if (!student_id || !amount || !payment_status) {
      return res.status(400).json({ error: "Student selection, payment amount, and status are required." });
    }

    const studentMock = students.find(s => s.student_id === parseInt(student_id));
    if (!studentMock) {
      return res.status(404).json({ error: "Selected student does not exist." });
    }

    const newId = payments.length > 0 ? Math.max(...payments.map(p => p.payment_id)) + 1 : 1;
    const newPayment: Payment = {
      payment_id: newId,
      student_id: parseInt(student_id),
      amount: parseFloat(amount),
      payment_date: payment_date || new Date().toISOString().split("T")[0],
      payment_status: payment_status as "Paid" | "Pending"
    };

    payments.push(newPayment);
    logActivity(`Recorded $${amount} payment context for ${studentMock.student_name} (${payment_status}).`, "payment");
    res.status(201).json(newPayment);
  });

  app.put("/api/payments/:id", (req, res) => {
    const id = parseInt(req.params.id);
    const index = payments.findIndex(p => p.payment_id === id);
    if (index === -1) {
      return res.status(404).json({ error: "Payment record not found." });
    }

    const { amount, payment_date, payment_status } = req.body;
    payments[index] = {
      ...payments[index],
      amount: amount ? parseFloat(amount) : payments[index].amount,
      payment_date: payment_date || payments[index].payment_date,
      payment_status: payment_status || payments[index].payment_status
    };

    const studentMock = students.find(s => s.student_id === payments[index].student_id);
    logActivity(`Updated Payment #${id} for ${studentMock ? studentMock.student_name : "Student"}: Status is ${payments[index].payment_status}.`, "payment");
    res.json(payments[index]);
  });

  app.delete("/api/payments/:id", (req, res) => {
    const id = parseInt(req.params.id);
    const index = payments.findIndex(p => p.payment_id === id);
    if (index === -1) {
      return res.status(404).json({ error: "Payment record not found." });
    }

    const removedPayment = payments[index];
    payments.splice(index, 1);
    
    logActivity(`Voided Payment Record #${id} worth $${removedPayment.amount}.`, "payment");
    res.json({ success: true, message: `Payment record voided successfully.` });
  });

  // 6. SETTINGS ENDPOINTS
  app.get("/api/settings/room-types", (req, res) => {
    res.json(roomTypes);
  });

  app.post("/api/settings/room-types", (req, res) => {
    const { type_name } = req.body;
    if (!type_name) {
      return res.status(400).json({ error: "Room type name is required." });
    }
    
    if (roomTypes.some(t => t.type_name.toLowerCase() === type_name.toLowerCase())) {
      return res.status(400).json({ error: "Room type designator already exists." });
    }

    const newId = roomTypes.length > 0 ? Math.max(...roomTypes.map(t => t.type_id)) + 1 : 1;
    const newType = { type_id: newId, type_name };
    roomTypes.push(newType);
    logActivity(`Created standard Room Type: ${type_name}.`, "settings");
    res.status(201).json(newType);
  });

  app.delete("/api/settings/room-types/:id", (req, res) => {
    const id = parseInt(req.params.id);
    const index = roomTypes.findIndex(t => t.type_id === id);
    if (index === -1) {
      return res.status(404).json({ error: "Room type not found." });
    }

    const removedType = roomTypes[index];
    // Check if any rooms are configured with this type
    const mappedRooms = rooms.filter(r => r.room_type === removedType.type_name);
    if (mappedRooms.length > 0) {
      return res.status(400).json({ error: `Cannot remove Room Type "${removedType.type_name}" because it is currently assigned to Room(s): ${mappedRooms.map(r => r.room_number).join(", ")}.` });
    }

    roomTypes.splice(index, 1);
    logActivity(`Deleted Room Type configuration: ${removedType.type_name}.`, "settings");
    res.json({ success: true });
  });

  app.get("/api/settings/hostel-info", (req, res) => {
    res.json(hostelInfo);
  });

  app.put("/api/settings/hostel-info", (req, res) => {
    const { hostelName, establishedDate, contactNo, email, address } = req.body;
    hostelInfo = {
      hostelName: hostelName || hostelInfo.hostelName,
      establishedDate: establishedDate || hostelInfo.establishedDate,
      contactNo: contactNo || hostelInfo.contactNo,
      email: email || hostelInfo.email,
      address: address || hostelInfo.address
    };
    logActivity("Hostel profile configurations successfully revised.", "settings");
    res.json(hostelInfo);
  });

  // Integrate Vite for development, serve static files for production
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Express Dev Application server launched on http://localhost:${PORT}`);
  });
}

startServer();
