package dao;

import model.User;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) for managing tables 'users'.
 */
public class UserDAO {

    /**
     * Validates administrator credentials.
     * Prevents SQL Injection via bound placeholders.
     */
    public User validateUser(String username, String password) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.validateUser(username, password);
        }
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password); // Note: Simple comparison, or hash match if using encryption
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("UserDAO Exception - validateUser: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    /**
     * Registers a new administrator.
     */
    public boolean addUser(User user) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.addUser(user);
        }
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean isSuccess = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());

            int affectedRows = stmt.executeUpdate();
            isSuccess = (affectedRows > 0);
        } catch (SQLException e) {
            System.err.println("UserDAO Exception - addUser: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return isSuccess;
    }
}
