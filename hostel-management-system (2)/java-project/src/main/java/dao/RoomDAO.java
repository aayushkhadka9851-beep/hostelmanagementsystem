package dao;

import model.Room;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing tables 'rooms'.
 */
public class RoomDAO {

    /**
     * Lists all rooms with custom search filters mapping directly to parameters.
     */
    public List<Room> getAllRooms(String query) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.getAllRooms(query);
        }
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        if (query != null && !query.trim().isEmpty()) {
            sql += " WHERE room_number LIKE ? OR room_type LIKE ? OR status LIKE ?";
        }
        sql += " ORDER BY room_number ASC";

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
                list.add(new Room(
                    rs.getInt("room_id"),
                    rs.getString("room_number"),
                    rs.getString("room_type"),
                    rs.getInt("capacity"),
                    rs.getDouble("monthly_rent"),
                    rs.getInt("occupied_beds"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO Exception - getAllRooms: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return list;
    }

    public List<Room> getAllRooms() {
        return getAllRooms(null);
    }

    /**
     * Retrieves specific room details by ID.
     */
    public Room getRoomById(int roomId) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.getRoomById(roomId);
        }
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, roomId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return new Room(
                    rs.getInt("room_id"),
                    rs.getString("room_number"),
                    rs.getString("room_type"),
                    rs.getInt("capacity"),
                    rs.getDouble("monthly_rent"),
                    rs.getInt("occupied_beds"),
                    rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO Exception - getRoomById: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return null;
    }

    /**
     * Checks if room label number exists to prevent duplicates.
     */
    public boolean isRoomNumberExists(String roomNumber, int excludeRoomId) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.isRoomNumberExists(roomNumber, excludeRoomId);
        }
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ? AND room_id != ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, roomNumber);
            stmt.setInt(2, excludeRoomId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO Exception - isRoomNumberExists: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return false;
    }

    /**
     * Creates a new room inventory configuration.
     */
    public boolean addRoom(Room room) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.addRoom(room);
        }
        String sql = "INSERT INTO rooms (room_number, room_type, capacity, monthly_rent, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType());
            stmt.setInt(3, room.getCapacity());
            stmt.setDouble(4, room.getMonthlyRent());
            stmt.setString(5, room.getStatus());

            int rows = stmt.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            System.err.println("RoomDAO Exception - addRoom: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }

    /**
     * Updates an existing room specifications.
     */
    public boolean updateRoom(Room room) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.updateRoom(room);
        }
        String sql = "UPDATE rooms SET room_number = ?, room_type = ?, capacity = ?, monthly_rent = ?, status = ? WHERE room_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType());
            stmt.setInt(3, room.getCapacity());
            stmt.setDouble(4, room.getMonthlyRent());
            stmt.setString(5, room.getStatus());
            stmt.setInt(6, room.getRoomId());

            int rows = stmt.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            System.err.println("RoomDAO Exception - updateRoom: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }

    /**
     * Safely deletes a room unit, if it has no children dependencies.
     */
    public boolean deleteRoom(int roomId) {
        if (util.DBConnection.isFallbackMode()) {
            return util.InMemoryStore.deleteRoom(roomId);
        }
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, roomId);

            int rows = stmt.executeUpdate();
            success = (rows > 0);
        } catch (SQLException e) {
            System.err.println("RoomDAO Exception - deleteRoom: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(stmt);
            DBConnection.closeConnection(conn);
        }
        return success;
    }

    /**
     * Reconcile specific room bed allocation statistics dynamically.
     * Keeps 'occupied_beds' matches in lockstep alignment with registered students.
     */
    public void reconcileOccupancy(int roomId) {
        if (util.DBConnection.isFallbackMode()) {
            util.InMemoryStore.reconcileOccupancy(roomId);
            return;
        }
        Connection conn = null;
        PreparedStatement countStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            
            // 1. Count active residents in room
            String countSql = "SELECT COUNT(*) FROM students WHERE room_id = ? AND status = 'Active'";
            countStmt = conn.prepareStatement(countSql);
            countStmt.setInt(1, roomId);
            rs = countStmt.executeQuery();
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }

            // 2. Fetch room details
            Room r = getRoomById(roomId);
            if (r != null) {
                String status = r.getStatus();
                if (!status.equals("Maintenance")) {
                    status = (count >= r.getCapacity()) ? "Full" : "Available";
                }

                // 3. Perform atomic update
                String updateSql = "UPDATE rooms SET occupied_beds = ?, status = ? WHERE room_id = ?";
                updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, count);
                updateStmt.setString(2, status);
                updateStmt.setInt(3, roomId);
                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("RoomDAO Exception - reconcileOccupancy: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(rs);
            DBConnection.closeConnection(countStmt);
            DBConnection.closeConnection(updateStmt);
            DBConnection.closeConnection(conn);
        }
    }
}
