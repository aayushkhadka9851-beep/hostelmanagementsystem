package dao;

import model.Payment;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing tables 'payments'.
 */
public class PaymentDAO {

    /**
     * Retrieves all payments joining student names and room units.
     * Supports search keywords across parameters.
     */
    public List<Payment> getAllPayments(String query) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.getAllPayments(query);
        }
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, s.student_name, r.room_number FROM payments p " +
                     "JOIN students s ON p.student_id = s.student_id " +
                     "LEFT JOIN rooms r ON s.room_id = r.room_id";

        if (query != null && !query.trim().isEmpty()) {
            sql += " WHERE s.student_name LIKE ? OR r.room_number LIKE ? OR p.payment_status LIKE ?";
        }
        sql += " ORDER BY p.payment_id DESC";

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
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                Payment p = new Payment(
                    rs.getInt("payment_id"),
                    rs.getInt("student_id"),
                    rs.getDouble("amount"),
                    rs.getString("payment_date"),
                    rs.getString("payment_status")
                );
                p.setStudentName(rs.getString("student_name"));
                p.setRoomNumber(rs.getString("room_number") != null ? rs.getString("room_number") : "Unassigned");
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("PaymentDAO Exception - getAllPayments: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return list;
    }

    public List<Payment> getAllPayments() {
        return getAllPayments(null);
    }

    /**
     * Fetches details of a single transaction voucher.
     */
    public Payment getPaymentById(int id) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.getPaymentById(id);
        }
        String sql = "SELECT p.*, s.student_name, r.room_number FROM payments p " +
                     "JOIN students s ON p.student_id = s.student_id " +
                     "LEFT JOIN rooms r ON s.room_id = r.room_id " +
                     "WHERE p.payment_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                Payment p = new Payment(
                    rs.getInt("payment_id"),
                    rs.getInt("student_id"),
                    rs.getDouble("amount"),
                    rs.getString("payment_date"),
                    rs.getString("payment_status")
                );
                p.setStudentName(rs.getString("student_name"));
                p.setRoomNumber(rs.getString("room_number") != null ? rs.getString("room_number") : "Unassigned");
                return p;
            }
        } catch (SQLException e) {
            System.err.println("PaymentDAO Exception - getPaymentById: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    /**
     * Records a new payment transaction.
     */
    public boolean addPayment(Payment payment) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.addPayment(payment);
        }
        String sql = "INSERT INTO payments (student_id, amount, payment_date, payment_status) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, payment.getStudentId());
            stmt.setDouble(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentDate());
            stmt.setString(4, payment.getPaymentStatus());

            int rows = stmt.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            System.err.println("PaymentDAO Exception - addPayment: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }

    /**
     * Updates payment information (such as marking invoice as Paid).
     */
    public boolean updatePayment(Payment payment) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.updatePayment(payment);
        }
        String sql = "UPDATE payments SET amount = ?, payment_date = ?, payment_status = ? WHERE payment_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, payment.getAmount());
            stmt.setString(2, payment.getPaymentDate());
            stmt.setString(3, payment.getPaymentStatus());
            stmt.setInt(4, payment.getPaymentId());

            int rows = stmt.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            System.err.println("PaymentDAO Exception - updatePayment: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }

    /**
     * Void statement history entries entirely.
     */
    public boolean deletePayment(int id) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.deletePayment(id);
        }
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int rows = stmt.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            System.err.println("PaymentDAO Exception - deletePayment: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }

    /**
     * Sum of paid financial logs.
     */
    public double getMonthlyRevenue() {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.getMonthlyRevenue();
        }
        String sql = "SELECT SUM(amount) FROM payments WHERE payment_status = 'Paid'";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        double sum = 0;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                sum = rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("PaymentDAO Exception - getMonthlyRevenue: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return sum;
    }

    /**
     * Count outstanding invoices.
     */
    public int getPendingPaymentsCount() {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.getPendingPaymentsCount();
        }
        String sql = "SELECT COUNT(*) FROM payments WHERE payment_status = 'Pending'";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("PaymentDAO Exception - getPendingPaymentsCount: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return count;
    }
}
