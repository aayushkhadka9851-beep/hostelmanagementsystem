package controller;

import dao.PaymentDAO;
import dao.RoomDAO;
import dao.StudentDAO;
import model.Room;
import model.Student;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet handling calculations to fuel Dashboard cards and statistical trends.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDao = new StudentDAO();
    private final RoomDAO roomDao = new RoomDAO();
    private final PaymentDAO paymentDao = new PaymentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Count active students
            List<Student> students = studentDao.getAllStudents();
            int totalStudents = 0;
            for (Student s : students) {
                if ("Active".equalsIgnoreCase(s.getStatus())) {
                    totalStudents++;
                }
            }

            // Room configurations
            List<Room> rooms = roomDao.getAllRooms();
            int totalRooms = rooms.size();
            
            int occupiedRooms = 0;
            int availableRooms = 0;
            int totalBeds = 0;
            int occupiedBeds = 0;

            for (Room r : rooms) {
                totalBeds += r.getCapacity();
                occupiedBeds += r.getOccupiedBeds();
                
                if (r.getOccupiedBeds() > 0) {
                    occupiedRooms++;
                }
                
                if ("Available".equalsIgnoreCase(r.getStatus()) || r.getOccupiedBeds() < r.getCapacity()) {
                    if (!"Maintenance".equalsIgnoreCase(r.getStatus())) {
                        availableRooms++;
                    }
                }
            }

            int availableBeds = Math.max(0, totalBeds - occupiedBeds);
            int bedsPct = totalBeds > 0 ? (int) Math.round(((double) occupiedBeds / totalBeds) * 100) : 0;

            // Financial reporting sums
            int pendingPayments = paymentDao.getPendingPaymentsCount();
            double monthlyRevenue = paymentDao.getMonthlyRevenue();

            // Set aggregate statistical parameters
            request.setAttribute("totalStudents", totalStudents);
            request.setAttribute("totalRooms", totalRooms);
            request.setAttribute("occupiedRooms", occupiedRooms);
            request.setAttribute("availableRooms", availableRooms);
            request.setAttribute("pendingPayments", pendingPayments);
            request.setAttribute("monthlyRevenue", monthlyRevenue);
            
            request.setAttribute("totalBeds", totalBeds);
            request.setAttribute("occupiedBeds", occupiedBeds);
            request.setAttribute("availableBeds", availableBeds);
            request.setAttribute("bedsPct", bedsPct);
            request.setAttribute("roomsList", rooms);

            // Forward execution back to dashboard.jsp
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            System.err.println("DashboardServlet Exception: " + e.getMessage());
            request.setAttribute("dbError", "Database error occurred while fetching stats. Make sure your MySQL service is active.");
            request.getRequestDispatcher("dashboard.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
