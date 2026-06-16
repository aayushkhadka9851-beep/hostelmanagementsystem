<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hostel Management System - Secure Login</title>
    <!-- Load Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', system-ui, sans-serif;
            background-color: #f8fafc;
        }
    </style>
</head>
<body class="bg-light min-vh-100 d-flex align-items-center justify-content-center py-5">

    <div class="card shadow-lg border-0 rounded-4 overflow-hidden w-100" style="max-width: 480px;">
        <!-- Header banner -->
        <div class="p-5 text-white text-center position-relative" style="background-color: #1e293b;">
            <div class="mb-3 d-inline-flex p-3 rounded-circle bg-primary text-white shadow" style="background-color: #2563eb !important;">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="lucide lucide-hotel"><path d="M10 22v-6.57c0-.42.17-.82.48-1.12l3.04-3.04c.3-.3.7-.47 1.12-.47h.1c.88 0 1.6.72 1.6 1.6V22"/><path d="M10 10V5c0-1.1.9-2 2-2h8a2 2 0 0 1 2 2v17H10"/><path d="M14 6h1"/><path d="M14 10h1"/><path d="M18 6h1"/><path d="M18 10h1"/><path d="M18 14h1"/><path d="M22 22H2"/><path d="M12 22V14M12 10a2 2 0 0 1-2-2V7H2v15h8"/></svg>
            </div>
            <h2 class="fw-bold mb-1 tracking-tight">Hostel Royal Oak</h2>
            <p class="text-slate-400 fs-7 mb-0 text-muted">Unified Operations &amp; Management Portal</p>
        </div>

        <div class="card-body p-5 bg-white">
            <!-- Checking for login status indicators -->
            <% 
                String loginErr = (String) request.getAttribute("loginError");
                if (loginErr != null) {
            %>
                <div class="alert alert-danger border-0 small py-2.5 rounded-3 mb-3" role="alert">
                    <%= loginErr %>
                </div>
            <% 
                } 
            %>

            <% 
                String loggedOutParam = request.getParameter("loggedOut");
                if ("true".equals(loggedOutParam)) {
            %>
                <div class="alert alert-success border-0 small py-2.5 rounded-3 mb-3" role="alert">
                    You have been logged out successfully.
                </div>
            <% 
                } 
            %>
            
            <form action="<%= request.getContextPath() %>/login" method="POST">
                <div class="mb-3.5 mb-3">
                    <label for="username" class="form-label text-secondary fw-medium small">Administrator Username</label>
                    <input type="text" class="form-control rounded-3" id="username" name="username" placeholder="Enter username" required value="admin">
                </div>
                <div class="mb-4">
                    <label for="password" class="form-label text-secondary fw-medium small">Security Password</label>
                    <input type="password" class="form-control rounded-3" id="password" name="password" placeholder="••••••••" required value="admin123">
                </div>
                <button type="submit" class="btn btn-primary w-100 py-2.5 fw-medium rounded-3 border-0" style="background-color: #2563eb !important;">
                    Secure Sign In
                </button>
            </form>
            
            <div class="mt-4 pt-3 border-top text-center text-muted small">
                <p class="mb-1 fw-medium uppercase text-muted tracking-wider" style="font-size: 0.725rem;">Default IntelliJ Tomcat Sandbox</p>
                <p class="mb-0 font-mono small">ID: <span class="text-dark fw-bold">admin</span> / Password: <span class="text-dark fw-bold">admin123</span></p>
            </div>
        </div>
    </div>

</body>
</html>
