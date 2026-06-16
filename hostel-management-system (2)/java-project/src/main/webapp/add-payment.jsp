<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Student" %>
<%
    // Inject menu contexts
    request.setAttribute("activeTab", "payments");
    request.setAttribute("pageTitle", "Acknowledge Billing Revenue");
%>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/sidebar.jsp" />

<%
    List<Student> students = (List<Student>) request.getAttribute("studentsList");
    String formErr = (String) request.getAttribute("formError");
%>

<div class="col-lg-6 mx-auto">
    <div class="card border-0 shadow-sm rounded-4 p-4 bg-white">
        <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
            <div>
                <h5 class="fw-bold mb-1 text-dark">Aquire Bill Voucher Form</h5>
                <p class="text-slate-400 mb-0 small text-muted">Acknowledge arrivals rents or record miscellaneous utilities.</p>
            </div>
            <a href="<%= request.getContextPath() %>/payments" class="btn btn-outline-secondary btn-sm px-3 rounded-2">
                Back to Ledger
            </a>
        </div>

        <% if (formErr != null) { %>
            <div class="alert alert-danger border-0 small py-2.5 rounded-3 mb-4" role="alert">
                <strong>Accounts Warning:</strong> <%= formErr %>
            </div>
        <% } %>

        <form action="<%= request.getContextPath() %>/payments?action=add" method="POST">
            <div class="mb-3.5 mb-3">
                <label class="form-label text-secondary fw-semibold small">Target Hosteller Resident</label>
                <select class="form-select rounded-3 font-sans" name="student_id" required>
                    <option value="" disabled selected>-- Select Resident Occupant --</option>
                    <% if (students != null) { 
                        for (Student s : students) {
                    %>
                        <option value="<%= s.getStudentId() %>"><%= s.getStudentName() %> (Allocated: <%= s.getRoomNumber() %>)</option>
                    <% } } %>
                </select>
                <div class="form-text fs-9 text-muted">Links the transaction invoice directly to the selected occupant's portfolio ledger.</div>
            </div>

            <div class="mb-3.5 mb-3">
                <label class="form-label text-secondary fw-semibold small">Billing Statement Amount ($)</label>
                <div class="input-group">
                    <span class="input-group-text">$</span>
                    <input type="number" step="0.01" class="form-control rounded-3" name="amount" placeholder="E.g. 500.00" min="0.01" required>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-md-6">
                    <label class="form-label text-secondary fw-semibold small">Settled/Logged Date</label>
                    <input type="date" class="form-control rounded-3 font-mono" name="payment_date" required value="2026-06-08">
                </div>
                <div class="col-md-6">
                    <label class="form-label text-secondary fw-semibold small">Initial Billing Status</label>
                    <select class="form-select rounded-3 font-sans" name="payment_status" required>
                        <option value="Paid" selected>Paid (Statement Settled)</option>
                        <option value="Pending">Pending (Outstanding Balance)</option>
                    </select>
                </div>
            </div>

            <button type="submit" class="btn btn-primary px-4 py-2.5 rounded-3 border-0" style="background-color: #2563eb !important;">
                Save Accounting Journal entry
            </button>
        </form>
    </div>
</div>

<jsp:include page="common/footer.jsp" />
