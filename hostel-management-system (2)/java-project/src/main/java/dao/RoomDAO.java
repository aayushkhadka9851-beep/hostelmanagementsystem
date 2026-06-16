package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Room;
import util.DBConnection;
import util.InMemoryStore;

public class RoomDAO {
    public List<Room> getAllRooms(String query) {
        if (DBConnection.isFallbackMode()) {
            return InMemoryStore.getAllRooms(query);
        } else {
            List<Room> list = new ArrayList();
            String sql = "SELECT * FROM rooms";
            if (query != null && !query.trim().isEmpty()) {
                sql = sql + " WHERE room_number LIKE ? OR room_type LIKE ? OR status LIKE ?";
            }

            sql = sql + " ORDER BY room_number ASC";
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

                while(rs.next()) {
                    list.add(new Room(rs.getInt("room_id"), rs.getString("room_number"), rs.getString("room_type"), rs.getInt("capacity"), rs.getDouble("monthly_rent"), rs.getInt("occupied_beds"), rs.getString("status")));
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
    }

    public List<Room> getAllRooms() {
        return this.getAllRooms((String)null);
    }

    public Room getRoomById(int roomId) {
        if (DBConnection.isFallbackMode()) {
            return InMemoryStore.getRoomById(roomId);
        } else {
            String sql = "SELECT * FROM rooms WHERE room_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            Room var6;
            try {
                conn = DBConnection.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, roomId);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    return null;
                }

                var6 = new Room(rs.getInt("room_id"), rs.getString("room_number"), rs.getString("room_type"), rs.getInt("capacity"), rs.getDouble("monthly_rent"), rs.getInt("occupied_beds"), rs.getString("status"));
            } catch (SQLException e) {
                System.err.println("RoomDAO Exception - getRoomById: " + e.getMessage());
                return null;
            } finally {
                DBConnection.closeConnection(rs);
                DBConnection.closeConnection(stmt);
                DBConnection.closeConnection(conn);
            }

            return var6;
        }
    }

    public boolean isRoomNumberExists(String roomNumber, int excludeRoomId) {
        if (DBConnection.isFallbackMode()) {
            return InMemoryStore.isRoomNumberExists(roomNumber, excludeRoomId);
        } else {
            String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ? AND room_id != ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            boolean var7;
            try {
                conn = DBConnection.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, roomNumber);
                stmt.setInt(2, excludeRoomId);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    return false;
                }

                var7 = rs.getInt(1) > 0;
            } catch (SQLException e) {
                System.err.println("RoomDAO Exception - isRoomNumberExists: " + e.getMessage());
                return false;
            } finally {
                DBConnection.closeConnection(rs);
                DBConnection.closeConnection(stmt);
                DBConnection.closeConnection(conn);
            }

            return var7;
        }
    }

    public boolean addRoom(Room room) {
        if (DBConnection.isFallbackMode()) {
            return InMemoryStore.addRoom(room);
        } else {
            // ✅ FIX: explicitly include occupied_beds to satisfy chk_occupied constraint
            String sql = "INSERT INTO rooms (room_number, room_type, capacity, monthly_rent, occupied_beds, status) VALUES (?, ?, ?, ?, ?, ?)";
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
                stmt.setInt(5, 0); // ✅ FIX: always 0 for a new room
                stmt.setString(6, room.getStatus());
                int rows = stmt.executeUpdate();
                success = rows > 0;
            } catch (SQLException e) {
                System.err.println("RoomDAO Exception - addRoom: " + e.getMessage());
            } finally {
                DBConnection.closeConnection(stmt);
                DBConnection.closeConnection(conn);
            }

            return success;
        }
    }

    public boolean updateRoom(Room room) {
        if (DBConnection.isFallbackMode()) {
            return InMemoryStore.updateRoom(room);
        } else {
            // ✅ FIX: include occupied_beds to preserve current occupancy on edit
            String sql = "UPDATE rooms SET room_number = ?, room_type = ?, capacity = ?, monthly_rent = ?, occupied_beds = ?, status = ? WHERE room_id = ?";
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
                stmt.setInt(5, room.getOccupiedBeds()); // ✅ FIX: preserve current occupancy
                stmt.setString(6, room.getStatus());
                stmt.setInt(7, room.getRoomId());
                int rows = stmt.executeUpdate();
                success = rows > 0;
            } catch (SQLException e) {
                System.err.println("RoomDAO Exception - updateRoom: " + e.getMessage());
            } finally {
                DBConnection.closeConnection(stmt);
                DBConnection.closeConnection(conn);
            }

            return success;
        }
    }

    public boolean deleteRoom(int roomId) {
        if (DBConnection.isFallbackMode()) {
            return InMemoryStore.deleteRoom(roomId);
        } else {
            String sql = "DELETE FROM rooms WHERE room_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            boolean success = false;

            try {
                conn = DBConnection.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, roomId);
                int rows = stmt.executeUpdate();
                success = rows > 0;
            } catch (SQLException e) {
                System.err.println("RoomDAO Exception - deleteRoom: " + e.getMessage());
            } finally {
                DBConnection.closeConnection(stmt);
                DBConnection.closeConnection(conn);
            }

            return success;
        }
    }

    public void reconcileOccupancy(int roomId) {
        if (DBConnection.isFallbackMode()) {
            InMemoryStore.reconcileOccupancy(roomId);
        } else {
            Connection conn = null;
            PreparedStatement countStmt = null;
            PreparedStatement updateStmt = null;
            ResultSet rs = null;

            try {
                conn = DBConnection.getConnection();
                String countSql = "SELECT COUNT(*) FROM students WHERE room_id = ? AND status = 'Active'";
                countStmt = conn.prepareStatement(countSql);
                countStmt.setInt(1, roomId);
                rs = countStmt.executeQuery();
                int count = 0;
                if (rs.next()) {
                    count = rs.getInt(1);
                }

                Room r = this.getRoomById(roomId);
                if (r != null) {
                    String status = r.getStatus();
                    if (!status.equals("Maintenance")) {
                        status = count >= r.getCapacity() ? "Full" : "Available";
                    }

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
}
