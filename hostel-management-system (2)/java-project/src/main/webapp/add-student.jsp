<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Room" %>
<%
    // Inject menu contexts
    request.setAttribute("activeTab", "students");
    request.setAttribute("pageTitle", "Register New Hosteller Record");
%>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/sidebar.jsp" />

<%
    List<Room> rooms = (List<Room>) request.getAttribute("roomsList");
    String formErr = (String) request.getAttribute("formError");
%>

<div class="col-lg-8 mx-auto">
    <div class="card border-0 shadow-sm rounded-4 p-4 bg-white">
        <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
            <div>
                <h5 class="fw-bold mb-1 text-dark">Arriving Student Profiling Form</h5>
                <p class="text-slate-400 mb-0 small text-muted">Complete accurate metadata regarding the resident student.</p>
            </div>
            <a href="<%= request.getContextPath() %>/students" class="btn btn-outline-secondary btn-sm px-3 rounded-2">
                Back to Register
            </a>
        </div>

        <% if (formErr != null) { %>
            <div class="alert alert-danger border-0 small py-2.5 rounded-3 mb-4" role="alert">
                <strong>Registrar Warning:</strong> <%= formErr %>
            </div>
        <% } %>

        <form action="<%= request.getContextPath() %>/students?action=add" method="POST">
            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label text-secondary fw-semibold small">Full Name of Student</label>
                    <input type="text" class="form-control rounded-3" name="name" placeholder="E.g. Aayush Khadka" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label text-secondary fw-semibold small">E-mail Address (Primary contact)</label>
                    <input type="email" class="form-control rounded-3" name="email" placeholder="student@university.edu" required>
                </div>
            </div>

            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label text-secondary fw-semibold small">Personal Phone Contact</label>
                    <input type="tel" class="form-control rounded-3" name="contact" placeholder="+977-9851000000" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label text-secondary fw-semibold small">Emergency Relative Contact</label>
                    <input type="tel" class="form-control rounded-3" name="emergency_contact" placeholder="+977-9801000000" required>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label text-secondary fw-semibold small">Home Address (Permanent Residence)</label>
                <textarea class="form-control rounded-3" name="address" rows="2" placeholder="Street, City, Province, Country ZIP" required></textarea>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-md-4">
                    <label class="form-label text-secondary fw-semibold small">Room Allocation</label>
                    <select class="form-select rounded-3" name="room_id">
                        <option value="">Unassigned (Awaiting placement)</option>
                        <% if (rooms != null) { 
                            for (Room r : rooms) {
                                if ("Available".equalsIgnoreCase(r.getStatus()) && r.getOccupiedBeds() < r.getCapacity()) {
                        %>
                            <option value="<%= r.getRoomId() %>">Room <%= r.getRoomNumber() %> (<%= r.getRoomType() %> - $<%= String.format("%.0f", r.getMonthlyRent()) %>/mo)</option>
                        <% } } } %>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label text-secondary fw-semibold small">Check-In Date</label>
                    <input type="date" class="form-control rounded-3" name="admission_date" required value="2026-06-08">
                </div>
                <div class="col-md-4">
                    <label class="form-label text-secondary fw-semibold small">Admission Status</label>
                    <select class="form-select rounded-3" name="status">
                        <option value="Active" selected>Active Resident</option>
                        <option value="Inactive">Inactive/Deferred</option>
                    </select>
                </div>
            </div>

            <button type="submit" class="btn btn-primary px-4 py-2.5 rounded-3 border-0" style="background-color: #2563eb !important;">
                Save &amp; Complete Placement
            </button>
        </form>
    </div>
</div>

<jsp:include page="common/footer.jsp" />
