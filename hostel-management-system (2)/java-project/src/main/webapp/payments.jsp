<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Payment" %>
<%
    // Inject menu contexts
    request.setAttribute("activeTab", "payments");
    request.setAttribute("pageTitle", "Accounts Ledger & Payments");
%>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/sidebar.jsp" />

<%
    List<Payment> payments = (List<Payment>) request.getAttribute("paymentsList");
    String searchVal = (String) request.getAttribute("searchQuery");
    if (searchVal == null) {
        searchVal = "";
    }

    String successParam = request.getParameter("success");
    String errorParam = request.getParameter("error");
    String alertMsg = null;
    String alertClass = "alert-success";

    if ("PaymentRecorded".equals(successParam)) {
        alertMsg = "Financial payment record logged successfully!";
    } else if ("PaymentStatusUpdated".equals(successParam)) {
        alertMsg = "Billing status modified successfully.";
    } else if ("PaymentVoided".equals(successParam)) {
        alertMsg = "Transaction payment voucher successfully voided.";
    } else if ("PaymentNotFound".equals(errorParam)) {
        alertMsg = "Target transaction payment voucher was not located.";
        alertClass = "alert-danger";
    }
%>

<% if (alertMsg != null) { %>
    <div class="alert <%= alertClass %> alert-dismissible fade show border-0 small py-3 rounded-3 shadow-sm mb-4" role="alert">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-2 align-middle"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
        <strong>Ledger Update:</strong> <%= alertMsg %>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
<% } %>

<div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
    <div class="d-flex flex-column flex-md-row align-items-center justify-content-between gap-3 mb-3 border-bottom pb-3.5 pb-3">
        <div>
            <h5 class="fw-bold mb-1 text-dark">Arreas &amp; Receivables ledger</h5>
            <p class="text-slate-400 mb-0 small text-muted">Acknowledge arrivals receipts, record invoices, and dispatch clear accounting entries.</p>
        </div>
        <a href="<%= request.getContextPath() %>/payments?action=add-form" class="btn btn-primary d-inline-flex align-items-center gap-1.5 px-3.5 py-2 fs-7.5 border-0 rounded-3" style="background-color: #2563eb !important;">
            <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" x2="12" y1="2" y2="22"/><rect width="18" height="18" x2="3" y2="3" rx="2"/></svg>
            Record Bill Receipt
        </a>
    </div>

    <!-- Search block -->
    <form action="<%= request.getContextPath() %>/payments" method="GET" class="row g-2 mb-3">
        <div class="col-md-9 col-sm-8">
            <div class="input-group">
                <span class="input-group-text bg-light border-end-0" style="border-color: #cbd5e1;"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" x2="16.65" y1="21" y2="16.65"/></svg></span>
                <input type="text" name="search" class="form-control bg-light border-start-0" placeholder="Filter invoices by student name, room label, status (e.g. Paid, Pending)..." value="<%= searchVal %>" style="border-color: #cbd5e1;">
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
                    <th>Invoice ID</th>
                    <th>Students Name</th>
                    <th>Associated Room</th>
                    <th>Assessed Bill Value ($)</th>
                    <th>Payment Date</th>
                    <th>Collected status</th>
                    <th class="text-end">Record Actions</th>
                </tr>
            </thead>
            <tbody class="fs-7.5">
                <% if (payments == null || payments.isEmpty()) { %>
                    <tr>
                        <td colspan="7" class="text-center py-5 text-muted">No accounting ledgers match search criteria.</td>
                    </tr>
                <% } else { 
                    for (Payment p : payments) {
                        String stClass = p.getPaymentStatus().equals("Paid") ? "bg-success" : "bg-warning";
                %>
                    <tr>
                        <td class="font-mono text-muted">INV-#<%= String.format("%05d", p.getPaymentId()) %></td>
                        <td class="fw-bold text-dark"><%= p.getStudentName() %></td>
                        <td class="text-muted font-medium">Room <%= p.getRoomNumber() %></td>
                        <td class="font-mono text-dark fw-bold">$<%= String.format("%.2f", p.getAmount()) %></td>
                        <td class="font-mono text-muted"><%= p.getPaymentDate() %></td>
                        <td>
                            <span class="badge <%= stClass %> bg-opacity-10 text-<%= stClass.substring(3) %> fs-9 rounded-2 px-2.5 py-1" style="background-color: transparent !important; border: 1px solid currentColor;">
                                <%= p.getPaymentStatus() %>
                            </span>
                        </td>
                        <td class="text-end">
                            <div class="d-inline-flex gap-2 justify-content-end align-items-center">
                                <!-- Status updater Form -->
                                <form action="<%= request.getContextPath() %>/payments?action=edit-status" method="POST" class="d-inline-block">
                                    <input type="hidden" name="id" value="<%= p.getPaymentId() %>">
                                    <select name="payment_status" class="form-select form-select-sm fs-9 font-sans p-1 rounded-2" onchange="this.form.submit();" style="max-width: 95px;">
                                        <option value="Paid" <%= "Paid".equals(p.getPaymentStatus()) ? "selected" : "" %>>Mark Paid</option>
                                        <option value="Pending" <%= "Pending".equals(p.getPaymentStatus()) ? "selected" : "" %>>Mark Pend</option>
                                    </select>
                                </form>
                                <a href="<%= request.getContextPath() %>/payments?action=delete&id=<%= p.getPaymentId() %>" class="btn btn-sm btn-outline-danger rounded-2 d-inline-flex align-items-center justify-content-center p-2" title="Void Invoices Ledger" onclick="return confirm('Confirm voiding Invoice Voucher INV-#<%= String.format("%05d", p.getPaymentId()) %>? This cannot be undone.');">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12"/></svg>
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
