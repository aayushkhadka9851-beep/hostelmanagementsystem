package controller;

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
 * Controller servlet for orchestrating Student Management workflows (MVC).
 */
@WebServlet(name = "StudentServlet", urlPatterns = {"/students"})
public class StudentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDao = new StudentDAO();
    private final RoomDAO roomDao = new RoomDAO();

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
                case "edit-form":
                    showEditForm(request, response);
                    break;
                case "delete":
                    processDelete(request, response);
                    break;
                case "list":
                default:
                    listStudents(request, response);
                    break;
            }
        } catch (Exception e) {
            System.err.println("StudentServlet doGet error: " + e.getMessage());
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
                case "edit":
                    processEdit(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/students");
                    break;
            }
        } catch (Exception e) {
            System.err.println("StudentServlet doPost error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // LIST SENSE
    private void listStudents(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String searchQuery = request.getParameter("search");
        List<Student> students = studentDao.getAllStudents(searchQuery);
        
        request.setAttribute("studentsList", students);
        request.setAttribute("searchQuery", searchQuery);
        request.getRequestDispatcher("students.jsp").forward(request, response);
    }

    // SHOW FORMS
    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Room> rooms = roomDao.getAllRooms();
        request.setAttribute("roomsList", rooms);
        request.getRequestDispatcher("add-student.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Student student = studentDao.getStudentById(id);
        
        if (student != null) {
            List<Room> rooms = roomDao.getAllRooms();
            request.setAttribute("student", student);
            request.setAttribute("roomsList", rooms);
            request.getRequestDispatcher("edit-student.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/students?error=StudentNotFound");
        }
    }

    // PROCESS POST DATA
    private void processAdd(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String contact = request.getParameter("contact");
        String emergency = request.getParameter("emergency_contact");
        String address = request.getParameter("address");
        
        String roomIdStr = request.getParameter("room_id");
        Integer roomId = (roomIdStr != null && !roomIdStr.trim().isEmpty()) ? Integer.parseInt(roomIdStr) : null;
        
        String date = request.getParameter("admission_date");
        String status = request.getParameter("status");

        // Validate capacity constraints if room assignment exists
        if (roomId != null) {
            Room r = roomDao.getRoomById(roomId);
            if (r != null) {
                if ("Maintenance".equalsIgnoreCase(r.getStatus())) {
                    request.setAttribute("formError", "Selected room is currently undergoing Maintenance.");
                    showAddForm(request, response);
                    return;
                }
                if (r.getOccupiedBeds() >= r.getCapacity()) {
                    request.setAttribute("formError", "Selected room has reached full capacity.");
                    showAddForm(request, response);
                    return;
                }
            }
        }

        Student s = new Student(name, email, contact, emergency, address, roomId, date, status);
        boolean success = studentDao.addStudent(s);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/students?success=StudentAdded");
        } else {
            request.setAttribute("formError", "Registration failed. Verify student email is unique.");
            showAddForm(request, response);
        }
    }

    private void processEdit(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String contact = request.getParameter("contact");
        String emergency = request.getParameter("emergency_contact");
        String address = request.getParameter("address");
        
        String roomIdStr = request.getParameter("room_id");
        Integer roomId = (roomIdStr != null && !roomIdStr.trim().isEmpty()) ? Integer.parseInt(roomIdStr) : null;
        
        String date = request.getParameter("admission_date");
        String status = request.getParameter("status");

        Student original = studentDao.getStudentById(id);
        if (original == null) {
            response.sendRedirect(request.getContextPath() + "/students?error=StudentNotFound");
            return;
        }

        // Validate capacity check if room is modified
        if (roomId != null && !roomId.equals(original.getRoomId())) {
            Room r = roomDao.getRoomById(roomId);
            if (r != null) {
                if ("Maintenance".equalsIgnoreCase(r.getStatus())) {
                    request.setAttribute("formError", "Modified room selection is currently in maintenance.");
                    showEditForm(request, response);
                    return;
                }
                if (r.getOccupiedBeds() >= r.getCapacity()) {
                    request.setAttribute("formError", "Modified room selection has reached maximum capacity.");
                    showEditForm(request, response);
                    return;
                }
            }
        }

        Student updated = new Student(id, name, email, contact, emergency, address, roomId, date, status);
        boolean success = studentDao.updateStudent(updated, original.getRoomId());

        if (success) {
            response.sendRedirect(request.getContextPath() + "/students?success=StudentUpdated");
        } else {
            request.setAttribute("formError", "Updates failed. Verify email uniqueness.");
            showEditForm(request, response);
        }
    }

    private void processDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        Student s = studentDao.getStudentById(id);
        
        if (s != null) {
            studentDao.deleteStudent(id, s.getRoomId());
            response.sendRedirect(request.getContextPath() + "/students?success=StudentDeleted");
        } else {
            response.sendRedirect(request.getContextPath() + "/students?error=StudentNotFound");
        }
    }
}
