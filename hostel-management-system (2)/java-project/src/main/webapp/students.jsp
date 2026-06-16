<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Student" %>
<%
    // Inject menu contexts
    request.setAttribute("activeTab", "students");
    request.setAttribute("pageTitle", "Student Register Management");
%>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/sidebar.jsp" />

<%
    // Retrieve students list
    List<Student> students = (List<Student>) request.getAttribute("studentsList");
    String searchVal = (String) request.getAttribute("searchQuery");
    if (searchVal == null) {
        searchVal = "";
    }
    
    // Success notifications mapping
    String successParam = request.getParameter("success");
    String errorParam = request.getParameter("error");
    String alertMsg = null;
    String alertClass = "alert-success";

    if ("StudentAdded".equals(successParam)) {
        alertMsg = "Hosteller registered successfully! Bed occupancy reconciled.";
    } else if ("StudentUpdated".equals(successParam)) {
        alertMsg = "Hosteller profile updated successfully.";
    } else if ("StudentDeleted".equals(successParam)) {
        alertMsg = "Hosteller has been deleted from records.";
    } else if ("StudentNotFound".equals(errorParam)) {
        alertMsg = "Requested student register profile could not be located.";
        alertClass = "alert-danger";
    }
%>

<% if (alertMsg != null) { %>
    <div class="alert <%= alertClass %> alert-dismissible fade show border-0 small py-3 rounded-3 shadow-sm mb-4" role="alert">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-2 align-middle"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <strong>Notice:</strong> <%= alertMsg %>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<% } %>

<!-- Register Grid controls -->
<div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
    <div class="d-flex flex-column flex-md-row align-items-center justify-content-between gap-3 mb-3 border-bottom pb-3.5 pb-3">
        <div>
            <h5 class="fw-bold mb-1 text-dark">Hostel Occupant Records</h5>
            <p class="text-slate-400 mb-0 small text-muted">Register and allocate room resources to arriving students securely.</p>
        </div>
        <a href="<%= request.getContextPath() %>/students?action=add-form" class="btn btn-primary d-inline-flex align-items-center gap-1.5 px-3.5 py-2 fs-7.5 border-0 rounded-3" style="background-color: #2563eb !important;">
            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" x2="19" y1="8" y2="14"/><line x1="16" x2="22" y1="11" y2="11"/></svg>
            Register Student
        </a>
    </div>

    <!-- Search block -->
    <form action="<%= request.getContextPath() %>/students" method="GET" class="row g-2 mb-3">
        <div class="col-md-9 col-sm-8">
            <div class="input-group">
                <span class="input-group-text bg-light border-end-0" style="border-color: #cbd5e1;"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" x2="16.65" y1="21" y2="16.65"/></svg></span>
                <input type="text" name="search" class="form-control bg-light border-start-0" placeholder="Search by name, contact phone, or room numbers..." value="<%= searchVal %>" style="border-color: #cbd5e1;">
            </div>
        </div>
        <div class="col-md-3 col-sm-4 d-grid">
            <button type="submit" class="btn btn-secondary border-0 text-white" style="background-color: #475569 !important;">Apply Filters</button>
        </div>
    </form>

    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
                <tr class="fs-8 text-secondary">
                    <th>ID</th>
                    <th>Students Name</th>
                    <th>Coordinates &amp; Tel</th>
                    <th>Current Allocation</th>
                    <th>Checked Date</th>
                    <th>Status</th>
                    <th class="text-end">Actions</th>
                </tr>
            </thead>
            <tbody class="fs-7.5">
                <% if (students == null || students.isEmpty()) { %>
                    <tr>
                        <td colspan="7" class="text-center py-5 text-muted">No student records match existing filters.</td>
                    </tr>
                <% } else { 
                    for (Student s : students) {
                        String stClass = s.getStatus().equals("Active") ? "bg-success" : "bg-warning";
                %>
                    <tr>
                        <td class="font-mono text-muted"><%= s.getStudentId() %></td>
                        <td>
                            <span class="fw-bold text-dark d-block"><%= s.getStudentName() %></span>
                            <span class="text-muted small"><%= s.getEmail() %></span>
                        </td>
                        <td>
                            <span class="d-block text-dark small font-mono"><%= s.getContact() %></span>
                            <span class="text-muted text-xs d-block">Emergency: <%= s.getEmergencyContact() %></span>
                        </td>
                        <td>
                            <% if (s.getRoomId() == null) { %>
                                <span class="badge bg-danger bg-opacity-10 text-danger fs-9 px-2.5 py-1" style="background-color: transparent !important; border: 1px solid currentColor;">Unassigned</span>
                            <% } else { %>
                                <span class="badge bg-primary bg-opacity-10 text-primary fs-9 px-2.5 py-1" style="background-color: transparent !important; border: 1px solid #2563eb;">
                                    Room: <%= s.getRoomNumber() %>
                                </span>
                            <% } %>
                        </td>
                        <td class="font-mono text-muted"><%= s.getAdmissionDate() %></td>
                        <td>
                            <span class="badge <%= stClass %> bg-opacity-10 text-<%= stClass.substring(3) %> fs-9 rounded-2 px-2.5 py-1" style="background-color: transparent !important; border: 1px solid currentColor;">
                                <%= s.getStatus() %>
                            </span>
                        </td>
                        <td class="text-end">
                            <div class="btn-group gap-2 justify-content-end">
                                <a href="<%= request.getContextPath() %>/students?action=edit-form&id=<%= s.getStudentId() %>" class="btn btn-sm btn-outline-secondary rounded-2  d-inline-flex align-items-center justify-content-center p-2" title="Edit Student Profile">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 20h9"/><path d="M16.5 3.5a2.12c1.31-1.31 3.5-1.31 4.82 0l1.18 1.18a3.5 3.5 0 0 1 0 4.82L10 21.5l-6 1 1-6z"/></svg>
                                </a>
                                <a href="<%= request.getContextPath() %>/students?action=delete&id=<%= s.getStudentId() %>" class="btn btn-sm btn-outline-danger rounded-2 d-inline-flex align-items-center justify-content-center p-2" title="Delete Hosteller" onclick="return confirm('Are you sure you want to permanently delete user: <%= s.getStudentName() %>? This will clear related transaction invoices!');">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
                                </a>
                            </div>
                        </td>
                    </tr>
                <% } } %>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="common/footer.jsp" />
