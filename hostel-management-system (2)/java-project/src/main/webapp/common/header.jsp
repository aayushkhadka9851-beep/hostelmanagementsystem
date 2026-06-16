<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.User" %>
<%
    // Security Guard: Check Session within headers
    User admin = (User) session.getAttribute("adminUser");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hostel Management System - Admin Panel</title>
    
    <!-- Load Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
    
    <!-- Bootstrap 5.3 CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Custom Theme styles matching required layout styling guides -->
    <style>
        :root {
          --primary-color: #2563eb;
          --sidebar-bg: #1e293b;
          --sidebar-active: #2563eb;
          --sidebar-text: #94a3b8;
          --sidebar-text-hover: #f1f5f9;
          --app-bg: #f8fafc;
          --card-bg: #ffffff;
          --text-main: #334155;
          --border-color: #e2e8f0;
          --slate-100: #f1f5f9;
          --slate-500: #64748b;
        }

        body {
          font-family: 'Inter', system-ui, -apple-system, sans-serif;
          color: var(--text-main);
          background-color: var(--app-bg);
          overflow-x: hidden;
        }

        /* Nav link structures */
        .nav-link-custom {
          color: var(--sidebar-text);
          font-weight: 500;
          font-size: 0.85rem;
          transition: all 0.2s ease-in-out;
          display: flex;
          align-items: center;
          border: none;
          background: transparent;
          text-decoration: none;
          width: 100%;
          border-radius: 0.5rem;
        }

        .nav-link-custom:hover {
          color: var(--sidebar-text-hover);
          background-color: rgba(255, 255, 255, 0.05) !important;
          transform: translateX(3px);
        }

        .nav-link-custom.active {
          color: #ffffff !important;
          background-color: var(--primary-color) !important;
          font-weight: 500;
          box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
        }

        /* Clean Minimalism card & border overrides */
        .card {
          background-color: #ffffff !important;
          border: 1px solid var(--slate-100) !important;
          border-radius: 0.75rem !important; /* rounded-xl */
          box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05), 0 1px 2px -1px rgba(0, 0, 0, 0.05) !important; /* shadow-sm */
        }

        /* Minimalism badge style defaults */
        .badge {
          font-size: 10px !important;
          font-weight: 700 !important;
          text-transform: uppercase !important;
          letter-spacing: 0.05em !important;
          border-radius: 9999px !important; /* rounded-full */
          padding: 0.25rem 0.625rem !important;
          background-color: transparent !important;
          border: 1px solid currentColor !important;
        }

        /* Soft status badges matching the design spec */
        .badge.bg-success, .text-success {
          background-color: #dcfce7 !important; /* green-100 */
          color: #15803d !important; /* green-700 */
          border: none !important;
        }
        .badge.bg-warning, .text-warning {
          background-color: #ffedd5 !important; /* orange-100 */
          color: #c2410c !important; /* orange-700 */
          border: none !important;
        }
        .badge.bg-danger, .text-danger {
          background-color: #fee2e2 !important; /* red-100 */
          color: #b91c1c !important; /* red-700 */
          border: none !important;
        }
        .badge.bg-primary, .text-primary {
          background-color: #dbeafe !important; /* blue-100 */
          color: #1d4ed8 !important; /* blue-700 */
          border: none !important;
        }

        /* Minimalist input controls */
        .form-control, .form-select {
          background-color: #ffffff !important;
          border: 1px solid #e2e8f0 !important;
          color: #334155 !important;
          font-size: 0.875rem !important;
          padding: 0.625rem 0.875rem !important;
          border-radius: 0.5rem !important; /* rounded-lg */
          transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out !important;
        }

        .form-control:focus, .form-select:focus {
          border-color: #3b82f6 !important;
          box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1) !important;
          background-color: #ffffff !important;
          color: #1e293b !important;
        }

        .input-group-text {
          background-color: #f8fafc !important;
          border: 1px solid #e2e8f0 !important;
          color: #64748b !important;
          border-radius: 0.5rem 0 0 0.5rem !important;
        }
        .input-group .form-control {
          border-radius: 0 0.5rem 0.5rem 0 !important;
        }

        /* Table custom styling standard */
        .table {
          width: 100% !important;
          border-collapse: collapse !important;
        }
        .table thead th, .table thead tr th {
          background-color: #f8fafc !important; /* bg-slate-50 */
          color: #94a3b8 !important; /* text-slate-400 */
          font-size: 10px !important;
          font-weight: 700 !important;
          text-transform: uppercase !important;
          letter-spacing: 0.05em !important;
          padding: 1rem 1.5rem !important;
          border-bottom: 1px solid #f1f5f9 !important;
        }
        .table tbody td {
          padding: 1rem 1.5rem !important;
          border-bottom: 1px solid #f8fafc !important; /* border-slate-50 */
          color: #475569 !important;
          font-size: 0.875rem !important;
        }
        .table tbody tr:hover td {
          background-color: #f8fafc !important;
        }

        /* Primary action button design matching HostelPro */
        .btn-primary {
          background-color: #2563eb !important;
          border-color: #2563eb !important;
          color: #ffffff !important;
          font-weight: 500 !important;
          font-size: 0.875rem !important;
          padding: 0.5rem 1rem !important;
          border-radius: 0.5rem !important; /* rounded-lg */
          transition: background-color 0.2s ease !important;
          box-shadow: none !important;
        }
        .btn-primary:hover {
          background-color: #1d4ed8 !important;
          border-color: #1d4ed8 !important;
        }

        .btn-secondary {
          background-color: #475569 !important;
          border-color: #475569 !important;
          color: #ffffff !important;
          font-weight: 500 !important;
          font-size: 0.875rem !important;
          padding: 0.5rem 1rem !important;
          border-radius: 0.5rem !important;
        }
        .btn-secondary:hover {
          background-color: #334155 !important;
          border-color: #334155 !important;
        }

        .btn-outline-secondary {
          border-color: #cbd5e1 !important;
          color: #475569 !important;
          background-color: transparent !important;
          font-weight: 500 !important;
          font-size: 0.875rem !important;
          padding: 0.5rem 1rem !important;
          border-radius: 0.5rem !important;
        }
        .btn-outline-secondary:hover {
          background-color: #f8fafc !important;
          border-color: #cbd5e1 !important;
          color: #1e293b !important;
        }

        .btn-outline-danger {
          border-color: #fee2e2 !important;
          color: #ef4444 !important;
          background-color: transparent !important;
          font-weight: 500 !important;
          border-radius: 0.5rem !important;
        }
        .btn-outline-danger:hover {
          background-color: #fee2e2 !important;
          color: #991b1b !important;
          border-color: #fca5a5 !important;
        }

        /* Specific Badge Designs & Helpers */
        .fs-7.5 { font-size: 0.85rem !important; }
        .fs-8 { font-size: 0.775rem !important; }
        .fs-9 { font-size: 0.7rem !important; }
        .fs-9.5 { font-size: 0.725rem !important; }
        .gap-1.5 { gap: 0.375rem !important; }
        
        .text-slate-500 {
          color: #64748b !important;
        }
        .text-slate-400 {
          color: #94a3b8 !important;
        }

        /* Custom Scrollbar */
        ::-webkit-scrollbar {
          width: 5px;
          height: 5px;
        }
        ::-webkit-scrollbar-thumb {
          background: #cbd5e1;
          border-radius: 50rem;
        }
    </style>
</head>
<body class="bg-[#f8fafc]">
    
    <div class="d-flex min-vh-100">
        
        <!-- SIDEBAR IMPORTED IN NEXT STEP -->
