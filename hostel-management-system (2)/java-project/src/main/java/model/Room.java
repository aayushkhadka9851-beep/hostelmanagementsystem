package model;

import java.io.Serializable;

/**
 * Model class representing 'rooms' table (Individual Inventory Units).
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private int roomId;
    private String roomNumber;
    private String roomType;
    private int capacity;
    private double monthlyRent;
    private int occupiedBeds;
    private String status; // Available, Full, Maintenance

    // Constructors
    public Room() {}

    public Room(int roomId, String roomNumber, String roomType, int capacity, double monthlyRent, int occupiedBeds, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.monthlyRent = monthlyRent;
        this.occupiedBeds = occupiedBeds;
        this.status = status;
    }

    public Room(String roomNumber, String roomType, int capacity, double monthlyRent, String status) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.monthlyRent = monthlyRent;
        this.occupiedBeds = 0;
        this.status = status;
    }

    // Getters and Setters
    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(double monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public int getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(int occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", roomNumber='" + roomNumber + '\'' +
                ", roomType='" + roomType + '\'' +
                ", capacity=" + capacity +
                ", monthlyRent=" + monthlyRent +
                ", occupiedBeds=" + occupiedBeds +
                ", status='" + status + '\'' +
                '}';
    }
}
