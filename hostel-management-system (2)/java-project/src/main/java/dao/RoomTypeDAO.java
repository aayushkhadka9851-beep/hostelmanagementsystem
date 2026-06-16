package dao;

import model.RoomType;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing tables 'room_types'.
 */
public class RoomTypeDAO {

    /**
     * Lists all custom room classifications.
     */
    public List<RoomType> getAllRoomTypes() {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.getAllRoomTypes();
        }
        List<RoomType> list = new ArrayList<>();
        String sql = "SELECT * FROM room_types ORDER BY type_name ASC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new RoomType(
                    rs.getInt("type_id"),
                    rs.getString("type_name")
                ));
            }
        } catch (SQLException e) {
            System.err.println("RoomTypeDAO Exception - getAllRoomTypes: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return list;
    }

    /**
     * Registers a new room type category.
     */
    public boolean addRoomType(RoomType type) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.addRoomType(type);
        }
        String sql = "INSERT INTO room_types (type_name) VALUES (?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, type.getTypeName());
            
            int rows = stmt.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            System.err.println("RoomTypeDAO Exception - addRoomType: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }

    /**
     * Deletes a custom classification definition by ID.
     */
    public boolean deleteRoomType(int typeId) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.deleteRoomType(typeId);
        }
        String sql = "DELETE FROM room_types WHERE type_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, typeId);

            int rows = stmt.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            System.err.println("RoomTypeDAO Exception - deleteRoomType: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }
}
