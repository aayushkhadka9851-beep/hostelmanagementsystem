<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.RoomType" %>
<%
    // Inject menu contexts
    request.setAttribute("activeTab", "settings");
    request.setAttribute("pageTitle", "System Brand Preferences");
%>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/sidebar.jsp" />

<%
    List<RoomType> types = (List<RoomType>) request.getAttribute("typesList");
    
    // Extract branding properties securely from Application scope (ServletContext)
    String hostelName = (String) application.getAttribute("hostelName");
    String hostelDate = (String) application.getAttribute("hostelDate");
    String hostelContact = (String) application.getAttribute("hostelContact");
    String hostelEmail = (String) application.getAttribute("hostelEmail");
    String hostelAddress = (String) application.getAttribute("hostelAddress");

    String successParam = request.getParameter("success");
    String errorParam = request.getParameter("error");
    String alertMsg = null;
    String alertClass = "alert-success";

    if ("TypeAdded".equals(successParam)) {
        alertMsg = "Custom room classification category registered successfully!";
    } else if ("TypeDeleted".equals(successParam)) {
        alertMsg = "Classification category successfully deleted.";
    } else if ("InfoSaved".equals(successParam)) {
        alertMsg = "Hostel brand system properties saved successfully across all JSPs.";
    } else if ("InvalidTypeName".equals(errorParam)) {
        alertMsg = "Form rejected: Room type label cannot be empty.";
        alertClass = "alert-danger";
    } else if ("TypeDeleteFailed".equals(errorParam)) {
        alertMsg = "Database error: Cannot delete active classification.";
        alertClass = "alert-danger";
    }
%>

<% if (alertMsg != null) { %>
    <div class="alert <%= alertClass %> alert-dismissible fade show border-0 small py-3 rounded-3 shadow-sm mb-4" role="alert">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-2 align-middle"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.1a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>
        <strong>Settings update:</strong> <%= alertMsg %>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<% } %>

<div class="row g-4">
    <!-- Hostel Brand Properties -->
    <div class="col-lg-7">
        <div class="card border-0 shadow-sm rounded-4 p-4 bg-white h-100">
            <div class="border-bottom pb-3 mb-4">
                <h5 class="fw-bold mb-1 text-dark">Hostel Brand &amp; Profile Metadata</h5>
                <p class="text-slate-400 mb-0 small text-muted">Revise key information parameters displayed on student receipts.</p>
            </div>

            <form action="<%= request.getContextPath() %>/settings?action=save-info" method="POST">
                <div class="mb-3">
                    <label class="form-label text-secondary fw-semibold small">Hostel Corporate Name</label>
                    <input type="text" class="form-control rounded-3" name="hostelName" value="<%= hostelName %>" required placeholder="E.g. Royal Oak Student Living">
                </div>
                
                <div class="row g-3 mb-3">
                    <div class="col-md-6">
                        <label class="form-label text-secondary fw-semibold small">Established Date</label>
                        <input type="date" class="form-control rounded-3 font-mono" name="hostelDate" value="<%= hostelDate %>" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label text-secondary fw-semibold small">Corporate Contact Support Phone</label>
                        <input type="text" class="form-control rounded-3" name="hostelContact" value="<%= hostelContact %>" required placeholder="E.g. +1 (310) 555-0322">
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label text-secondary fw-semibold small">Official Email Address</label>
                    <input type="email" class="form-control rounded-3" name="hostelEmail" value="<%= hostelEmail %>" required placeholder="E.g. manager@royal-oak-living.org">
                </div>

                <div class="mb-4">
                    <label class="form-label text-secondary fw-semibold small">Physical Address (Street Location Line)</label>
                    <input type="text" class="form-control rounded-3" name="hostelAddress" value="<%= hostelAddress %>" required placeholder="E.g. 452 University Avenue, Suite B, Palo Alto, CA">
                </div>

                <button type="submit" class="btn btn-primary d-inline-flex align-items-center gap-2 px-4 py-2.5 border-0 rounded-3 small text-white fw-medium shadow-sm transition" style="background-color: #2563eb !important;">
                    Save Profile Changes
                </button>
            </form>
        </div>
    </div>

    <!-- Room Types configurations list -->
    <div class="col-lg-5">
        <div class="card border-0 shadow-sm rounded-4 p-4 bg-white h-100">
            <div class="border-bottom pb-3 mb-4">
                <h5 class="fw-bold mb-1 text-dark">Room Custom Classifications</h5>
                <p class="text-slate-400 mb-0 small text-muted">Register structural sublet layouts categories.</p>
            </div>

            <!-- Create new type -->
            <form action="<%= request.getContextPath() %>/settings?action=add-type" method="POST" class="mb-4 d-flex gap-2">
                <input type="text" class="form-control rounded-3 small" name="type_name" required placeholder="E.g. Quadruple Bed, Attached Loft" style="font-size: 0.825rem;">
                <button type="submit" class="btn btn-primary btn-sm px-3 border-0 rounded-3 text-white fw-semibold" style="background-color: #2563eb !important; white-space: nowrap;">
                    Add Category
                </button>
            </form>

            <label class="form-label text-secondary fw-bold small mb-2 uppercase text-muted tracking-wider" style="font-size: 0.725rem;">Existing classifications catalog</label>
            <div class="list-group rounded-3 overflow-hidden shadow-sm border border-light">
                <% if (types == null || types.isEmpty()) { %>
                    <div class="list-group-item text-center py-4 text-muted small bg-light">No room types defined. Standard presets will override.</div>
                <% } else { 
                    for (RoomType rt : types) {
                %>
                    <div class="list-group-item d-flex align-items-center justify-content-between py-2.5 px-3 bg-white border-light fs-7.5">
                        <span class="text-dark fw-medium"><%= rt.getTypeName() %></span>
                        <!-- Custom sub-form trigger posts deletes cleanly -->
                        <form action="<%= request.getContextPath() %>/settings?action=delete-type" method="POST" class="m-0">
                            <input type="hidden" name="id" value="<%= rt.getTypeId() %>">
                            <button type="submit" class="btn btn-sm btn-outline-danger border-0 p-1 rounded-2" onclick="return confirm('Confirm deletion of Category <%= rt.getTypeName() %>?');">
                                <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12"/></svg>
                            </button>
                        </form>
                    </div>
                <% } } %>
            </div>
        </div>
    </div>
</div>

<jsp:include page="common/footer.jsp" />
