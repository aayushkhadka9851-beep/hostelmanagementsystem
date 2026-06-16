<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" language="java"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Room" %>
<%
    // Inject menu contexts
    request.setAttribute("activeTab", "rooms");
    request.setAttribute("pageTitle", "Room Inventory Management");
%>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/sidebar.jsp" />

<%
    List<Room> rooms = (List<Room>) request.getAttribute("roomsList");
    String searchVal = (String) request.getAttribute("searchQuery");
    if (searchVal == null) {
        searchVal = "";
    }

    String successParam = request.getParameter("success");
    String errorParam = request.getParameter("error");
    String alertMsg = null;
    String alertClass = "alert-success";

    if ("RoomAdded".equals(successParam)) {
         alertMsg = "New room inventory record created successfully!";
    } else if ("RoomUpdated".equals(successParam)) {
         alertMsg = "Room configurations updated successfully.";
    } else if ("RoomDeleted".equals(successParam)) {
         alertMsg = "Room deleted from catalogs.";
    } else if ("RoomOccupied".equals(errorParam)) {
         alertMsg = "Deletion Blocked: You cannot delete a room with active sleeping occupants! Reassign residents first.";
         alertClass = "alert-danger";
    } else if ("RoomNotFound".equals(errorParam)) {
         alertMsg = "Requested room inventory item was not found.";
         alertClass = "alert-danger";
    }
%>

<% if (alertMsg != null) { %>
    <div class="alert <%= alertClass %> alert-dismissible fade show border-0 small py-3 rounded-3 shadow-sm mb-4" role="alert">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-2 align-middle"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" x2="12" y1="9" y2="13"/><line x1="12" x2="12.01" y1="17" y2="17"/></svg>
        <strong>Notice:</strong> <%= alertMsg %>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<% } %>

<div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
    <div class="d-flex flex-column flex-md-row align-items-center justify-content-between gap-3 mb-3 border-bottom pb-3.5 pb-3">
        <div>
            <h5 class="fw-bold mb-1 text-dark">Hostel Room Units &amp; Occupancy</h5>
            <p class="text-slate-400 mb-0 small text-muted">Manage active inventory nodes, rental pricing rates, and bed capacity thresholds.</p>
        </div>
        <a href="<%= request.getContextPath() %>/rooms?action=add-form" class="btn btn-primary d-inline-flex align-items-center gap-1.5 px-3.5 py-2 fs-7.5 border-0 rounded-3" style="background-color: #2563eb !important;">
            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><line x1="12" x2="12" y1="11" y2="17"/><line x1="9" x2="15" y1="14" y2="14"/></svg>
            Add New Room Unit
        </a>
    </div>

    <!-- Search block -->
    <form action="<%= request.getContextPath() %>/rooms" method="GET" class="row g-2 mb-3">
        <div class="col-md-9 col-sm-8">
            <div class="input-group">
                <span class="input-group-text bg-light border-end-0" style="border-color: #cbd5e1;"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" x2="16.65" y1="21" y2="16.65"/></svg></span>
                <input type="text" name="search" class="form-control bg-light border-start-0" placeholder="Filter by room number label, category type (e.g. Single, Double, AC, Non-AC), or status..." value="<%= searchVal %>" style="border-color: #cbd5e1;">
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
                    <th>Room Number</th>
                    <th>Classification Subtype</th>
                    <th>Bed Capacity</th>
                    <th>Monthly Rental Rent</th>
                    <th>Status</th>
                    <th class="text-end">Actions</th>
                </tr>
            </thead>
            <tbody class="fs-7.5">
                <% if (rooms == null || rooms.isEmpty()) { %>
                    <tr>
                        <td colspan="7" class="text-center py-5 text-muted">No room inventory matches query.</td>
                    </tr>
                <% } else { 
                    for (Room r : rooms) {
                        String stClass = r.getStatus().equals("Available") ? "bg-success" : (r.getStatus().equals("Full") ? "bg-danger" : "bg-warning");
                %>
                    <tr>
                        <td class="font-mono text-muted"><%= r.getRoomId() %></td>
                        <td class="fw-bold text-dark fs-7">Room <%= r.getRoomNumber() %></td>
                        <td class="text-muted"><%= r.getRoomType() %></td>
                        <td>
                            <div class="d-flex align-items-center gap-2">
                                <span class="fw-bold font-mono text-dark"><%= r.getOccupiedBeds() %> / <%= r.getCapacity() %></span>
                                <span class="text-muted small">beds booked</span>
                            </div>
                        </td>
                        <td class="font-mono text-dark font-medium">$<%= String.format("%.2f", r.getMonthlyRent()) %></td>
                        <td>
                            <span class="badge <%= stClass %> bg-opacity-10 text-<%= stClass.substring(3) %> fs-9 rounded-2 px-2.5 py-1" style="background-color: transparent !important; border: 1px solid currentColor;">
                                <%= r.getStatus() %>
                            </span>
                        </td>
                        <td class="text-end">
                            <div class="btn-group gap-2 justify-content-end">
                                <a href="<%= request.getContextPath() %>/rooms?action=edit-form&id=<%= r.getRoomId() %>" class="btn btn-sm btn-outline-secondary rounded-2 d-inline-flex align-items-center justify-content-center p-2" title="Edit Room Properties">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 20h9"/><path d="M16.5 3.5a2.12c1.31-1.31 3.5-1.31 4.82 0l1.18 1.18a3.5 3.5 0 0 1 0 4.82L10 21.5l-6 1 1-6z"/></svg>
                                </a>
                                <a href="<%= request.getContextPath() %>/rooms?action=delete&id=<%= r.getRoomId() %>" class="btn btn-sm btn-outline-danger rounded-2 d-inline-flex align-items-center justify-content-center p-2" title="Delete Room" onclick="return confirm('Confirm deletion of Room <%= r.getRoomNumber() %>? Deletion is allowed only if occupied beds equal zero.');">
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
