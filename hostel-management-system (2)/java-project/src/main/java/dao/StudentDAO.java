package dao;

import model.Student;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing tables 'students'.
 */
public class StudentDAO {
    
    private final RoomDAO roomDao = new RoomDAO();

    /**
     * Lists all students joining with 'rooms' to display friendly room labels.
     * Supports multi-keyword search scopes.
     */
    public List<Student> getAllStudents(String query) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.getAllStudents(query);
        }
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, r.room_number FROM students s " +
                     "LEFT JOIN rooms r ON s.room_id = r.room_id";
        
        if (query != null && !query.trim().isEmpty()) {
            sql += " WHERE s.student_name LIKE ? OR s.email LIKE ? OR s.contact LIKE ? OR r.room_number LIKE ?";
        }
        sql += " ORDER BY s.student_id DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            if (query != null && !query.trim().isEmpty()) {
                String searchBound = "%" + query.trim() + "%";
                stmt.setString(1, searchBound);
                stmt.setString(2, searchBound);
                stmt.setString(3, searchBound);
                stmt.setString(4, searchBound);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                Student s = new Student(
                    rs.getInt("student_id"),
                    rs.getString("student_name"),
                    rs.getString("email"),
                    rs.getString("contact"),
                    rs.getString("emergency_contact"),
                    rs.getString("address"),
                    rs.getObject("room_id") != null ? rs.getInt("room_id") : null,
                    rs.getString("admission_date"),
                    rs.getString("status")
                );
                s.setRoomNumber(rs.getString("room_number") != null ? rs.getString("room_number") : "Unassigned");
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO Exception - getAllStudents: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return list;
    }

    public List<Student> getAllStudents() {
        return getAllStudents(null);
    }

    /**
     * Retrieves student profiling information by ID.
     */
    public Student getStudentById(int id) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.getStudentById(id);
        }
        String sql = "SELECT s.*, r.room_number FROM students s " +
                     "LEFT JOIN rooms r ON s.room_id = r.room_id " +
                     "WHERE s.student_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                Student s = new Student(
                    rs.getInt("student_id"),
                    rs.getString("student_name"),
                    rs.getString("email"),
                    rs.getString("contact"),
                    rs.getString("emergency_contact"),
                    rs.getString("address"),
                    rs.getObject("room_id") != null ? rs.getInt("room_id") : null,
                    rs.getString("admission_date"),
                    rs.getString("status")
                );
                s.setRoomNumber(rs.getString("room_number") != null ? rs.getString("room_number") : "Unassigned");
                return s;
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO Exception - getStudentById: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    /**
     * Registers a new student and reconciles room bed counts.
     */
    public boolean addStudent(Student student) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.addStudent(student);
        }
        String sql = "INSERT INTO students (student_name, email, contact, emergency_contact, address, room_id, admission_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, student.getStudentName());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getContact());
            stmt.setString(4, student.getEmergencyContact());
            stmt.setString(5, student.getAddress());
            
            if (student.getRoomId() != null) {
                stmt.setInt(6, student.getRoomId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            stmt.setString(7, student.getAdmissionDate());
            stmt.setString(8, student.getStatus());

            int rows = stmt.executeUpdate();
            success = (rows > 0);

            // Reconcile and recalculate bed availability counts for the target room
            if (success && student.getRoomId() != null) {
                roomDao.reconcileOccupancy(student.getRoomId());
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO Exception - addStudent: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }

    /**
     * Updates student properties and recalculates room occupancy capacities across both departments.
     */
    public boolean updateStudent(Student student, Integer oldRoomId) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.updateStudent(student, oldRoomId);
        }
        String sql = "UPDATE students SET student_name = ?, email = ?, contact = ?, emergency_contact = ?, address = ?, room_id = ?, admission_date = ?, status = ? WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, student.getStudentName());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getContact());
            stmt.setString(4, student.getEmergencyContact());
            stmt.setString(5, student.getAddress());
            
            if (student.getRoomId() != null) {
                stmt.setInt(6, student.getRoomId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            stmt.setString(7, student.getAdmissionDate());
            stmt.setString(8, student.getStatus());
            stmt.setInt(9, student.getStudentId());

            int rows = stmt.executeUpdate();
            success = (rows > 0);

            // Fire dual-reconciliation events for historical room and new assigned room
            if (success) {
                if (oldRoomId != null) {
                    roomDao.reconcileOccupancy(oldRoomId);
                }
                if (student.getRoomId() != null && !student.getRoomId().equals(oldRoomId)) {
                    roomDao.reconcileOccupancy(student.getRoomId());
                }
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO Exception - updateStudent: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }

    /**
     * Removes student profile and updates room occupancy loads.
     */
    public boolean deleteStudent(int id, Integer roomId) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.deleteStudent(id, roomId);
        }
        String sql = "DELETE FROM students WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int rows = stmt.executeUpdate();
            success = (rows > 0);

            // Re-reconcile room counts post removal
            if (success && roomId != null) {
                roomDao.reconcileOccupancy(roomId);
            }
        } catch (SQLException e) {
            System.err.println("StudentDAO Exception - deleteStudent: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }
}
