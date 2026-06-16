<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%
    // Select active menu item set by main page controller
    String activePage = (String) request.getAttribute("activeTab");
    if (activePage == null) {
        activePage = "dashboard";
    }
    String sContextPath = request.getContextPath();
    User sAdmin = (User) session.getAttribute("adminUser");
    String sName = (sAdmin != null) ? sAdmin.getUsername() : "Admin";
    String brandName = (String) application.getAttribute("hostelName");
    if (brandName == null) {
        brandName = "Royal Oak Student Living";
    }
%>
<aside class="bg-[#1e293b] text-slate-300 d-flex flex-column" style="width: 250px; min-width: 250px; background-color: #1e293b;">
    <!-- Logo area -->
    <div class="p-4 border-bottom border-secondary d-flex align-items-center justify-content-between" style="border-color: #334155 !important;">
        <div class="d-flex align-items-center">
            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2.5" class="me-2"><path d="M12 22V14M12 10a2 2 0 0 1-2-2V7H2v15h8"/><path d="M10 22v-6.57c0-.42.17-.82.48-1.12l3.04-3.04c.3-.3.7-.47 1.12-.47h.1c.88 0 1.6.72 1.6 1.6V22"/><path d="M10 10V5c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v17H10"/></svg>
            <span class="fw-bold text-white fs-5 font-sans" style="letter-spacing: -0.025em;">Royal Oak</span>
        </div>
        <span class="badge bg-primary text-uppercase font-mono fs-9 px-2 py-1" style="background-color: #2563eb !important;">Admin</span>
    </div>
    
    <!-- Sidebar navigation -->
    <nav class="flex-grow-1 p-3 d-flex flex-column gap-1.5">
        <a href="<%= sContextPath %>/dashboard" class="nav-link-custom px-3 py-2.5 <%= "dashboard".equals(activePage) ? "active" : "" %>">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-3"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="14" rx="1"/></svg>
            Dashboard
        </a>
        <a href="<%= sContextPath %>/students" class="nav-link-custom px-3 py-2.5 <%= "students".equals(activePage) ? "active" : "" %>">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-3"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            Students
        </a>
        <a href="<%= sContextPath %>/rooms" class="nav-link-custom px-3 py-2.5 <%= "rooms".equals(activePage) ? "active" : "" %>">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-3"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            Rooms
        </a>
        <a href="<%= sContextPath %>/payments" class="nav-link-custom px-3 py-2.5 <%= "payments".equals(activePage) ? "active" : "" %>">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-3"><line x1="12" x2="12" y1="2" y2="22"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
            Payments
        </a>
        <a href="<%= sContextPath %>/settings" class="nav-link-custom px-3 py-2.5 <%= "settings".equals(activePage) ? "active" : "" %>">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-3"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.1a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>
            Settings
        </a>
    </nav>
    
    <!-- Sidebar operator metadata footer -->
    <div class="p-3 border-top border-secondary" style="border-color: #334155 !important;">
        <div class="d-flex align-items-center mb-3">
            <div class="avatar text-white rounded-circle d-flex align-items-center justify-content-center me-3" style="width: 36px; height: 36px; font-weight: 600; background-color: #2563eb;">
                <%= sName.substring(0, 1).toUpperCase() %>
            </div>
            <div>
                <span class="d-block fw-semibold text-white fs-7.5"><%= sName %></span>
                <span class="d-block text-slate-500 fs-8 text-muted">System Operator</span>
            </div>
        </div>
        <a href="<%= sContextPath %>/logout" class="btn btn-outline-danger btn-sm border-0 d-flex align-items-center w-100 fs-7.5 py-1.5 font-sans" style="color: #ef4444;">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/></svg>
            Secure Sign-Out
        </a>
    </div>
</aside>

<!-- MAIN WORK AREA -->
<main class="flex-grow-1 d-flex flex-column overflow-auto">
    <!-- Navbar / Header top line -->
    <header class="bg-white d-flex align-items-center justify-content-between px-4 py-3 border-bottom" style="border-color: #f1f5f9 !important;">
        <div>
            <h5 class="fw-bold mb-0 text-dark tracking-tight" style="letter-spacing: -0.015em;"><%= request.getAttribute("pageTitle") %></h5>
        </div>
        <div class="d-flex align-items-center gap-4">
            <div class="d-flex align-items-center fs-8 font-mono bg-light text-muted px-3 py-1.5 rounded-3">
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="me-2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                Time: <strong class="text-dark ms-1">UTC Active</strong>
            </div>
            <div class="fs-7.5 fw-medium text-secondary bg-light px-3 py-1.5 rounded-full d-flex align-items-center" style="border: 1px solid #e1e8f0;">
                <span class="bg-primary me-2 rounded-circle" style="width: 7px; height: 7px; display: inline-block; background-color: #2563eb !important;"></span>
                <span><%= brandName %></span>
            </div>
        </div>
    </header>
    
    <!-- Canvas padding container -->
    <div class="p-4">
