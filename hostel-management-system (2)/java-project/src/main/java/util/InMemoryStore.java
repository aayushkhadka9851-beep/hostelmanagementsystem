package util;

import model.User;
import model.Room;
import model.RoomType;
import model.Student;
import model.Payment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * High-fidelity in-memory storage manager that simulates the database tables completely.
 * Used automatically when physical MySQL connection is unavailable.
 */
public class InMemoryStore {
    private static final List<User> users = new ArrayList<>();
    private static final List<RoomType> roomTypes = new ArrayList<>();
    private static final List<Room> rooms = new ArrayList<>();
    private static final List<Student> students = new ArrayList<>();
    private static final List<Payment> payments = new ArrayList<>();

    private static int nextUserId = 3;
    private static int nextRoomTypeId = 7;
    private static int nextRoomId = 6;
    private static int nextStudentId = 5;
    private static int nextPaymentId = 6;

    private static boolean initialized = false;

    static {
        ensureInitialized();
    }

    public static synchronized void ensureInitialized() {
        if (initialized) return;

        // Seed users
        users.add(new User(1, "admin", "admin123", "Admin"));
        users.add(new User(2, "manager", "manager123", "Manager"));

        // Seed room types
        roomTypes.add(new RoomType(1, "Single Room"));
        roomTypes.add(new RoomType(2, "Double Room"));
        roomTypes.add(new RoomType(3, "Triple Room"));
        roomTypes.add(new RoomType(4, "AC Room"));
        roomTypes.add(new RoomType(5, "Non-AC Room"));
        roomTypes.add(new RoomType(6, "Attached Bathroom"));

        // Seed rooms
        rooms.add(new Room(1, "101", "Single Room", 1, 500.00, 1, "Full"));
        rooms.add(new Room(2, "102", "Double Room", 2, 350.00, 1, "Available"));
        rooms.add(new Room(3, "103", "AC Room", 2, 600.00, 2, "Full"));
        rooms.add(new Room(4, "104", "Non-AC Room", 3, 400.00, 0, "Available"));
        rooms.add(new Room(5, "105", "Double Room", 2, 350.00, 0, "Maintenance"));

        // Seed students
        students.add(new Student(1, "Aayush Khadka", "aayush.khadka9851@gmail.com", "+977-9851000000", "+977-9801000000", "Kathmandu, Nepal", 1, "2026-06-01", "Active"));
        students.add(new Student(2, "Bishal Dev", "bishal.dev@example.com", "+1-555-0199", "+1-555-0100", "New York, USA", 2, "2026-05-15", "Active"));
        students.add(new Student(3, "Camila Ruiz", "camila@example.com", "+1-555-0188", "+1-555-0122", "San Francisco, USA", 3, "2026-06-01", "Active"));
        students.add(new Student(4, "David Kim", "david@example.com", "+1-555-0177", "+1-555-0133", "Los Angeles, USA", 3, "2026-06-02", "Active"));

        for (Student s : students) {
            String nr = getRoomNo(s.getRoomId());
            s.setRoomNumber(nr);
        }

        // Seed payments
        payments.add(new Payment(1, 1, 500.00, "2026-06-01", "Paid"));
        payments.add(new Payment(2, 2, 350.00, "2026-05-15", "Paid"));
        payments.add(new Payment(3, 3, 600.00, "2026-06-01", "Pending"));
        payments.add(new Payment(4, 4, 600.00, "2026-06-02", "Paid"));
        payments.add(new Payment(5, 1, 500.00, "2026-06-08", "Pending"));

        for (Payment p : payments) {
            String sName = getStudentNameById(p.getStudentId());
            p.setStudentName(sName);
            p.setRoomNumber(getStudentRoomNo(p.getStudentId()));
        }

        initialized = true;
    }

    private static String getRoomNo(Integer roomId) {
        if (roomId == null) return "Unassigned";
        for (Room r : rooms) {
            if (r.getRoomId() == roomId) return r.getRoomNumber();
        }
        return "Unassigned";
    }

    private static String getStudentNameById(int studentId) {
        for (Student s : students) {
            if (s.getStudentId() == studentId) return s.getStudentName();
        }
        return "Unknown";
    }

    private static String getStudentRoomNo(int studentId) {
        for (Student s : students) {
            if (s.getStudentId() == studentId) {
                return getRoomNo(s.getRoomId());
            }
        }
        return "Unassigned";
    }

    // --- Users ---
    public static synchronized User validateUser(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public static synchronized boolean addUser(User u) {
        u.setId(nextUserId++);
        users.add(u);
        return true;
    }

    // --- Room Types ---
    public static synchronized List<RoomType> getAllRoomTypes() {
        return new ArrayList<>(roomTypes);
    }

    public static synchronized boolean addRoomType(RoomType rt) {
        rt.setTypeId(nextRoomTypeId++);
        roomTypes.add(rt);
        return true;
    }

    public static synchronized boolean deleteRoomType(int typeId) {
        return roomTypes.removeIf(rt -> rt.getTypeId() == typeId);
    }

    // --- Rooms ---
    public static synchronized List<Room> getAllRooms(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(rooms);
        }
        String q = query.toLowerCase().trim();
        return rooms.stream()
                .filter(r -> r.getRoomNumber().toLowerCase().contains(q)
                        || r.getRoomType().toLowerCase().contains(q)
                        || r.getStatus().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public static synchronized Room getRoomById(int roomId) {
         for (Room r : rooms) {
             if (r.getRoomId() == roomId) return r;
         }
         return null;
    }

    public static synchronized boolean isRoomNumberExists(String roomNumber, int excludeRoomId) {
        for (Room r : rooms) {
            if (r.getRoomNumber().equalsIgnoreCase(roomNumber) && r.getRoomId() != excludeRoomId) {
                return true;
            }
        }
        return false;
    }

    public static synchronized boolean addRoom(Room r) {
        r.setRoomId(nextRoomId++);
        r.setOccupiedBeds(0);
        rooms.add(r);
        return true;
    }

    public static synchronized boolean updateRoom(Room r) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomId() == r.getRoomId()) {
                Room existing = rooms.get(i);
                existing.setRoomNumber(r.getRoomNumber());
                existing.setRoomType(r.getRoomType());
                existing.setCapacity(r.getCapacity());
                existing.setMonthlyRent(r.getMonthlyRent());
                if (!"Maintenance".equals(r.getStatus())) {
                    int occ = existing.getOccupiedBeds();
                    existing.setStatus(occ >= r.getCapacity() ? "Full" : "Available");
                } else {
                    existing.setStatus("Maintenance");
                }
                return true;
            }
        }
        return false;
    }

    public static synchronized boolean deleteRoom(int roomId) {
        return rooms.removeIf(r -> r.getRoomId() == roomId);
    }

    public static synchronized void reconcileOccupancy(int roomId) {
        long count = students.stream()
                .filter(s -> s.getRoomId() != null && s.getRoomId() == roomId && "Active".equalsIgnoreCase(s.getStatus()))
                .count();
        Room r = getRoomById(roomId);
        if (r != null) {
            r.setOccupiedBeds((int) count);
            if (!"Maintenance".equals(r.getStatus())) {
                r.setStatus(count >= r.getCapacity() ? "Full" : "Available");
            }
        }
    }

    // --- Students ---
    public static synchronized List<Student> getAllStudents(String query) {
        // First ensure any room updates are mapped to students
        for (Student s : students) {
            s.setRoomNumber(getRoomNo(s.getRoomId()));
        }
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(students);
        }
        String q = query.toLowerCase().trim();
        return students.stream()
                .filter(s -> s.getStudentName().toLowerCase().contains(q)
                        || s.getEmail().toLowerCase().contains(q)
                        || s.getContact().toLowerCase().contains(q)
                        || getRoomNo(s.getRoomId()).toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public static synchronized Student getStudentById(int id) {
        for (Student s : students) {
            if (s.getStudentId() == id) {
                s.setRoomNumber(getRoomNo(s.getRoomId()));
                return s;
            }
        }
        return null;
    }

    public static synchronized boolean addStudent(Student s) {
        s.setStudentId(nextStudentId++);
        students.add(s);
        if (s.getRoomId() != null) {
            reconcileOccupancy(s.getRoomId());
        }
        return true;
    }

    public static synchronized boolean updateStudent(Student s, Integer oldRoomId) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getStudentId() == s.getStudentId()) {
                students.set(i, s);
                if (oldRoomId != null) {
                    reconcileOccupancy(oldRoomId);
                }
                if (s.getRoomId() != null) {
                    reconcileOccupancy(s.getRoomId());
                }
                return true;
            }
        }
        return false;
    }

    public static synchronized boolean deleteStudent(int id, Integer roomId) {
        boolean removed = students.removeIf(s -> s.getStudentId() == id);
        if (removed && roomId != null) {
            reconcileOccupancy(roomId);
        }
        return removed;
    }

    // --- Payments ---
    public static synchronized List<Payment> getAllPayments(String query) {
        for (Payment p : payments) {
            p.setStudentName(getStudentNameById(p.getStudentId()));
            p.setRoomNumber(getStudentRoomNo(p.getStudentId()));
        }

        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(payments);
        }

        String q = query.toLowerCase().trim();
        return payments.stream()
                .filter(p -> p.getStudentName().toLowerCase().contains(q)
                        || p.getRoomNumber().toLowerCase().contains(q)
                        || p.getPaymentStatus().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public static synchronized Payment getPaymentById(int id) {
        for (Payment p : payments) {
            if (p.getPaymentId() == id) {
                p.setStudentName(getStudentNameById(p.getStudentId()));
                p.setRoomNumber(getStudentRoomNo(p.getStudentId()));
                return p;
            }
        }
        return null;
    }

    public static synchronized boolean addPayment(Payment p) {
        p.setPaymentId(nextPaymentId++);
        payments.add(p);
        return true;
    }

    public static synchronized boolean updatePayment(Payment p) {
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getPaymentId() == p.getPaymentId()) {
                Payment existing = payments.get(i);
                existing.setAmount(p.getAmount());
                existing.setPaymentDate(p.getPaymentDate());
                existing.setPaymentStatus(p.getPaymentStatus());
                return true;
            }
        }
        return false;
    }

    public static synchronized boolean deletePayment(int id) {
        return payments.removeIf(p -> p.getPaymentId() == id);
    }

    public static synchronized double getMonthlyRevenue() {
        return payments.stream()
                .filter(p -> "Paid".equalsIgnoreCase(p.getPaymentStatus()))
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public static synchronized int getPendingPaymentsCount() {
        return (int) payments.stream()
                .filter(p -> "Pending".equalsIgnoreCase(p.getPaymentStatus()))
                .count();
    }
}
