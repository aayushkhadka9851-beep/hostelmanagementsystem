package controller;

import dao.PaymentDAO;
import dao.StudentDAO;
import model.Payment;
import model.Student;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Controller servlet for orchestrating Payment Ledger and billing logs.
 */
@WebServlet(name = "PaymentServlet", urlPatterns = {"/payments"})
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final PaymentDAO paymentDao = new PaymentDAO();
    private final StudentDAO studentDao = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        try {
            switch (action) {
                case "add-form":
                    showAddForm(request, response);
                    break;
                case "delete":
                    processDelete(request, response);
                    break;
                case "list":
                default:
                    listPayments(request, response);
                    break;
            }
        } catch (Exception e) {
            System.err.println("PaymentServlet doGet error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        try {
            switch (action) {
                case "add":
                    processAdd(request, response);
                    break;
                case "edit-status":
                    processEditStatus(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/payments");
                    break;
            }
        } catch (Exception e) {
            System.err.println("PaymentServlet doPost error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // LIST SENSE
    private void listPayments(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String query = request.getParameter("search");
        List<Payment> list = paymentDao.getAllPayments(query);
        
        request.setAttribute("paymentsList", list);
        request.setAttribute("searchQuery", query);
        request.getRequestDispatcher("payments.jsp").forward(request, response);
    }

    // FORM LOADS
    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // List only active checked-in students
        List<Student> students = studentDao.getAllStudents();
        request.setAttribute("studentsList", students);
        request.getRequestDispatcher("add-payment.jsp").forward(request, response);
    }

    // ACTIONS
    private void processAdd(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int studentId = Integer.parseInt(request.getParameter("student_id"));
        double amount = Double.parseDouble(request.getParameter("amount"));
        String date = request.getParameter("payment_date");
        String status = request.getParameter("payment_status");

        Payment p = new Payment(studentId, amount, date, status);
        boolean success = paymentDao.addPayment(p);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/payments?success=PaymentRecorded");
        } else {
            request.setAttribute("formError", "Database error occurred during book logging.");
            showAddForm(request, response);
        }
    }

    private void processEditStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int paymentId = Integer.parseInt(request.getParameter("id"));
        String status = request.getParameter("payment_status");

        Payment p = paymentDao.getPaymentById(paymentId);
        if (p != null) {
            p.setPaymentStatus(status);
            paymentDao.updatePayment(p);
            response.sendRedirect(request.getContextPath() + "/payments?success=PaymentStatusUpdated");
        } else {
            response.sendRedirect(request.getContextPath() + "/payments?error=PaymentNotFound");
        }
    }

    private void processDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        boolean success = paymentDao.deletePayment(id);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/payments?success=PaymentVoided");
        } else {
            response.sendRedirect(request.getContextPath() + "/payments?error=VoidFailed");
        }
    }
}
