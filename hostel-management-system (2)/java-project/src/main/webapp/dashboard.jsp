<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Room" %>
<%
    // Inject Page Settings
    request.setAttribute("activeTab", "dashboard");
    request.setAttribute("pageTitle", "Operations Dashboard");
%>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/sidebar.jsp" />

<%
    // Safely Extract statistics with defaults
    int totalStudents = request.getAttribute("totalStudents") != null ? (Integer) request.getAttribute("totalStudents") : 0;
    int totalRooms = request.getAttribute("totalRooms") != null ? (Integer) request.getAttribute("totalRooms") : 0;
    int occupiedRooms = request.getAttribute("occupiedRooms") != null ? (Integer) request.getAttribute("occupiedRooms") : 0;
    int availableRooms = request.getAttribute("availableRooms") != null ? (Integer) request.getAttribute("availableRooms") : 0;
    int pendingPayments = request.getAttribute("pendingPayments") != null ? (Integer) request.getAttribute("pendingPayments") : 0;
    double monthlyRevenue = request.getAttribute("monthlyRevenue") != null ? (Double) request.getAttribute("monthlyRevenue") : 0.0;
    
    int totalBeds = request.getAttribute("totalBeds") != null ? (Integer) request.getAttribute("totalBeds") : 0;
    int occupiedBeds = request.getAttribute("occupiedBeds") != null ? (Integer) request.getAttribute("occupiedBeds") : 0;
    int availableBeds = request.getAttribute("availableBeds") != null ? (Integer) request.getAttribute("availableBeds") : 0;
    int bedsPct = request.getAttribute("bedsPct") != null ? (Integer) request.getAttribute("bedsPct") : 0;
    List<Room> rooms = (List<Room>) request.getAttribute("roomsList");
%>

<!-- Metrics Grid -->
<div class="row g-4 mb-4">
    <!-- Card 1: Students -->
    <div class="col-md-3 col-sm-6">
        <div class="card border-0 shadow-sm rounded-3 p-3 bg-white h-100">
            <div class="d-flex align-items-center justify-content-between mb-2">
                <span class="text-muted small fw-medium text-uppercase tracking-wider">Active Hosteliers</span>
                <div class="p-2 bg-primary bg-opacity-10 rounded text-primary animate-pulse">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                </div>
            </div>
            <h3 class="fw-bold mb-0 text-dark"><%= totalStudents %></h3>
            <span class="text-xs text-secondary mt-1 block small text-muted">Currently checked-in residents</span>
        </div>
    </div>

    <!-- Card 2: Rooms -->
    <div class="col-md-3 col-sm-6">
        <div class="card border-0 shadow-sm rounded-3 p-3 bg-white h-100">
            <div class="d-flex align-items-center justify-content-between mb-2">
                <span class="text-muted small fw-medium text-uppercase tracking-wider">Occupancy Rate</span>
                <div class="p-2 bg-success bg-opacity-10 rounded text-success">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                </div>
            </div>
            <h3 class="fw-bold mb-0 text-dark">
                <%= totalRooms > 0 ? (int) Math.round(((double) occupiedRooms / totalRooms) * 100) : 0 %>%
            </h3>
            <span class="text-xs mt-1 block small text-muted"><%= occupiedRooms %> / <%= totalRooms %> rooms assigned</span>
        </div>
    </div>

    <!-- Card 3: Payments -->
    <div class="col-md-3 col-sm-6">
        <div class="card border-0 shadow-sm rounded-3 p-3 bg-white h-100">
            <div class="d-flex align-items-center justify-content-between mb-2">
                <span class="text-muted small fw-medium text-uppercase tracking-wider">Pending Invoices</span>
                <div class="p-2 bg-warning bg-opacity-10 rounded text-warning">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect width="20" height="14" x="2" y="5" rx="2"/><line x1="2" x2="22" y1="10" y2="10"/></svg>
                </div>
            </div>
            <h3 class="fw-bold mb-0 text-dark"><%= pendingPayments %></h3>
            <span class="text-xs mt-1 block small text-muted">Awaiting administrator collection</span>
        </div>
    </div>

    <!-- Card 4: Revenue -->
    <div class="col-md-3 col-sm-6">
        <div class="card border-0 shadow-sm rounded-3 p-3 bg-white h-100">
            <div class="d-flex align-items-center justify-content-between mb-2">
                <span class="text-muted small fw-medium text-uppercase tracking-wider">Monthly Revenue</span>
                <div class="p-2 bg-dark bg-opacity-10 rounded text-dark">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" x2="12" y1="2" y2="22"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                </div>
            </div>
            <h3 class="fw-bold mb-0 text-dark">$<%= String.format("%.2f", monthlyRevenue) %></h3>
            <span class="text-xs mt-1 block small text-muted">Deposited successfully to bank</span>
        </div>
    </div>
</div>

<!-- Progress metrics row -->
<div class="row g-4 mb-4">
    <div class="col-lg-8">
        <!-- Room Inventory overview table -->
        <div class="card border-0 shadow-sm rounded-4 p-4 bg-white">
            <div class="d-flex align-items-center justify-content-between mb-3.5 border-bottom pb-3">
                <h5 class="fw-bold mb-0 text-dark">Room Capacity Indicators</h5>
                <a href="<%= request.getContextPath() %>/rooms" class="btn btn-primary btn-sm px-3 border-0 py-1.5 fs-8 rounded-2" style="background-color: #2563eb !important;">
                    Configure Inventory
                </a>
            </div>
            
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr class="fs-8 text-secondary">
                            <th>Room No.</th>
                            <th>Subtype</th>
                            <th>Rent Rate</th>
                            <th>Active Occupancy</th>
                            <th>Operating status</th>
                        </tr>
                    </thead>
                    <tbody class="fs-7.5">
                        <% if (rooms == null || rooms.isEmpty()) { %>
                            <tr>
                                <td colspan="5" class="text-center py-4 text-muted">No rooms loaded in the system yet.</td>
                            </tr>
                        <% } else { 
                            int limit = Math.min(rooms.size(), 5);
                            for (int i=0; i < limit; i++) {
                                Room r = rooms.get(i);
                                String badgeClass = r.getStatus().equals("Available") ? "bg-success" : (r.getStatus().equals("Full") ? "bg-danger" : "bg-warning");
                        %>
                            <tr>
                                <td class="fw-bold"><%= r.getRoomNumber() %></td>
                                <td class="text-muted"><%= r.getRoomType() %></td>
                                <td class="font-mono text-dark font-medium">$<%= String.format("%.2f", r.getMonthlyRent()) %>/mo</td>
                                <td>
                                    <div class="d-flex align-items-center gap-2">
                                        <div class="progress flex-grow-1" style="height: 6px; max-width: 80px;">
                                            <div class="progress-bar bg-primary" role="progressbar" style="width: <%= (int)Math.round(((double)r.getOccupiedBeds() / r.getCapacity())*100) %>%;"></div>
                                        </div>
                                        <span class="small font-mono fw-bold"><%= r.getOccupiedBeds() %>/<%= r.getCapacity() %></span>
                                    </div>
                                </td>
                                <td>
                                    <span class="badge <%= badgeClass %> bg-opacity-10 text-<%= badgeClass.substring(3) %> fs-9 rounded-2 px-2.5 py-1" style="background-color: transparent !important; border: 1px solid currentColor;">
                                        <%= r.getStatus() %>
                                    </span>
                                </td>
                            </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Bed Allocation Progress -->
    <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4 p-4 bg-white h-100">
            <h5 class="fw-bold mb-3 border-bottom pb-3 text-dark">Bed space distribution</h5>
            
            <div class="text-center my-4">
                <h1 class="fw-bold font-mono text-primary display-4 mb-1" style="color: #2563eb !important;"><%= bedsPct %>%</h1>
                <span class="text-muted small uppercase fw-semibold">Aggregate Occupancy</span>
            </div>

            <div class="progress mb-4" style="height: 12px; border-radius: 50rem;">
                <div class="progress-bar bg-primary" role="progressbar" style="width: <%= bedsPct %>%; border-radius: 50rem;" aria-valuenow="<%= bedsPct %>" aria-valuemin="0" aria-valuemax="100"></div>
            </div>

            <div class="d-flex flex-column gap-3 fs-7.5 border-top pt-3.5">
                <div class="d-flex justify-content-between align-items-center">
                    <span class="text-muted">Total Bed Spaces</span>
                    <span class="font-mono fw-bold text-dark"><%= totalBeds %></span>
                </div>
                <div class="d-flex justify-content-between align-items-center">
                    <span class="text-muted">Occupied Beds</span>
                    <span class="font-mono fw-bold text-success"><%= occupiedBeds %></span>
                </div>
                <div class="d-flex justify-content-between align-items-center">
                    <span class="text-muted">Vacant Beds Available</span>
                    <span class="font-mono fw-bold text-primary" style="color: #2563eb !important;"><%= availableBeds %></span>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="common/footer.jsp" />
