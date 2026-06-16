<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.RoomType" %>
<%
    // Inject menu contexts
    request.setAttribute("activeTab", "rooms");
    request.setAttribute("pageTitle", "Add New Room Node");
%>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/sidebar.jsp" />

<%
    List<RoomType> types = (List<RoomType>) request.getAttribute("typesList");
    String formErr = (String) request.getAttribute("formError");
%>

<div class="col-lg-7 mx-auto">
    <div class="card border-0 shadow-sm rounded-4 p-4 bg-white">
        <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
            <div>
                <h5 class="fw-bold mb-1 text-dark">Room Inventory Creator Node</h5>
                <p class="text-slate-400 mb-0 small text-muted">Register structural rooms and associate initial properties.</p>
            </div>
            <a href="<%= request.getContextPath() %>/rooms" class="btn btn-outline-secondary btn-sm px-3 rounded-2">
                Back to Inventory
            </a>
        </div>

        <% if (formErr != null) { %>
            <div class="alert alert-danger border-0 small py-2.5 rounded-3 mb-4" role="alert">
                <strong>Inventory Warning:</strong> <%= formErr %>
            </div>
        <% } %>

        <form action="<%= request.getContextPath() %>/rooms?action=add" method="POST">
            <div class="mb-3.5 mb-3">
                <label class="form-label text-secondary fw-semibold small">Room Number Label</label>
                <input type="text" class="form-control rounded-3" name="room_number" placeholder="E.g. 101, 203A, 405" required>
                <div class="form-text fs-9 text-muted">A uniquely indexable code identifying this distinct hostel room space.</div>
            </div>

            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="form-label text-secondary fw-semibold small">Room Type Classification</label>
                    <select class="form-select rounded-3" name="room_type" required>
                        <% if (types == null || types.isEmpty()) { %>
                            <option value="Single Room">Single Room</option>
                            <option value="Double Room">Double Room</option>
                            <option value="AC Room">AC Room</option>
                        <% } else { 
                            for (RoomType rt : types) {
                        %>
                            <option value="<%= rt.getTypeName() %>"><%= rt.getTypeName() %></option>
                        <% } } %>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label text-secondary fw-semibold small">Maximum Capacity Bed Count</label>
                    <input type="number" class="form-control rounded-3" name="capacity" min="1" max="10" value="2" required>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-md-6">
                    <label class="form-label text-secondary fw-semibold small">Monthly Rent Pricing Price ($)</label>
                    <div class="input-group">
                        <span class="input-group-text">$</span>
                        <input type="number" step="0.01" class="form-control" name="monthly_rent" placeholder="350.00" min="0" required>
                    </div>
                </div>
                <div class="col-md-6">
                    <label class="form-label text-secondary fw-semibold small">Initial Operating Status</label>
                    <select class="form-select rounded-3" name="status" required>
                        <option value="Available" selected>Available (Vacant rooms)</option>
                        <option value="Maintenance">Maintenance (Off-line node)</option>
                    </select>
                </div>
            </div>

            <button type="submit" class="btn btn-primary px-4 py-2.5 rounded-3 border-0" style="background-color: #2563eb !important;">
                Initialize Room Node
            </button>
        </form>
    </div>
</div>

<jsp:include page="common/footer.jsp" />
