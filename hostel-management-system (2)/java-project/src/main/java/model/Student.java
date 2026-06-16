package model;

import java.io.Serializable;

/**
 * Model class representing 'students' table (Registered Core Residents).
 */
public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private int studentId;
    private String studentName;
    private String email;
    private String contact;
    private String emergencyContact;
    private String address;
    private Integer roomId; // Use Integer to represent nullable DB column
    private String admissionDate; // Represented as "yyyy-MM-dd" for form binding friendliness
    private String status; // Active, Inactive

    // Un-mapped Join Field for easy JSP displays
    private String roomNumber;

    // Constructors
    public Student() {}

    public Student(int studentId, String studentName, String email, String contact, String emergencyContact, String address, Integer roomId, String admissionDate, String status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.contact = contact;
        this.emergencyContact = emergencyContact;
        this.address = address;
        this.roomId = roomId;
        this.admissionDate = admissionDate;
        this.status = status;
    }

    public Student(String studentName, String email, String contact, String emergencyContact, String address, Integer roomId, String admissionDate, String status) {
        this.studentName = studentName;
        this.email = email;
        this.contact = contact;
        this.emergencyContact = emergencyContact;
        this.address = address;
        this.roomId = roomId;
        this.admissionDate = admissionDate;
        this.status = status;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(String admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", email='" + email + '\'' +
                ", roomId=" + roomId +
                ", status='" + status + '\'' +
                '}';
    }
}
