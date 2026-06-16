package controller;

import dao.RoomDAO;
import dao.RoomTypeDAO;
import model.Room;
import model.RoomType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Controller servlet for orchestrating Room Inventory workflows.
 */
@WebServlet(name = "RoomServlet", urlPatterns = {"/rooms"})
public class RoomServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final RoomDAO roomDao = new RoomDAO();
    private final RoomTypeDAO typeDao = new RoomTypeDAO();

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
                    listRooms(request, response);
                    break;
            }
        } catch (Exception e) {
            System.err.println("RoomServlet doGet error: " + e.getMessage());
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
                    response.sendRedirect(request.getContextPath() + "/rooms");
                    break;
            }
        } catch (Exception e) {
            System.err.println("RoomServlet doPost error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // LIST VIEW
    private void listRooms(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String query = request.getParameter("search");
        List<Room> rooms = roomDao.getAllRooms(query);
        
        request.setAttribute("roomsList", rooms);
        request.setAttribute("searchQuery", query);
        request.getRequestDispatcher("rooms.jsp").forward(request, response);
    }

    // FORM LOADS
    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<RoomType> types = typeDao.getAllRoomTypes();
        request.setAttribute("typesList", types);
        request.getRequestDispatcher("add-room.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Room room = roomDao.getRoomById(id);

        if (room != null) {
            List<RoomType> types = typeDao.getAllRoomTypes();
            request.setAttribute("room", room);
            request.setAttribute("typesList", types);
            request.getRequestDispatcher("edit-room.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/rooms?error=RoomNotFound");
        }
    }

    // ACTIONS
    private void processAdd(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String number = request.getParameter("room_number");
        String type = request.getParameter("room_type");
        int capacity = Integer.parseInt(request.getParameter("capacity"));
        double rent = Double.parseDouble(request.getParameter("monthly_rent"));
        String status = request.getParameter("status");

        // Duplicate Check
        if (roomDao.isRoomNumberExists(number, -1)) {
            request.setAttribute("formError", "A room unit labeled " + number + " already exists in the catalog.");
            showAddForm(request, response);
            return;
        }

        Room r = new Room(number, type, capacity, rent, status);
        boolean success = roomDao.addRoom(r);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/rooms?success=RoomAdded");
        } else {
            request.setAttribute("formError", "Database error occurred. Verify inputs.");
            showAddForm(request, response);
        }
    }

    private void processEdit(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        String number = request.getParameter("room_number");
        String type = request.getParameter("room_type");
        int capacity = Integer.parseInt(request.getParameter("capacity"));
        double rent = Double.parseDouble(request.getParameter("monthly_rent"));
        String status = request.getParameter("status");

        Room original = roomDao.getRoomById(id);
        if (original == null) {
            response.sendRedirect(request.getContextPath() + "/rooms?error=RoomNotFound");
            return;
        }

        // Duplicate Check
        if (roomDao.isRoomNumberExists(number, id)) {
            request.setAttribute("formError", "A room unit labeled " + number + " already exists.");
            showEditForm(request, response);
            return;
        }

        // Safety validator: Bed capacity can't be set below currently active allocations
        if (capacity < original.getOccupiedBeds()) {
            request.setAttribute("formError", "Cannot decrease capacity below current active seat load (" + original.getOccupiedBeds() + " beds occupied).");
            showEditForm(request, response);
            return;
        }

        Room updated = new Room(id, number, type, capacity, rent, original.getOccupiedBeds(), status);
        // Force fully full status
        if (!"Maintenance".equals(status)) {
            if (original.getOccupiedBeds() >= capacity) {
                updated.setStatus("Full");
            } else {
                updated.setStatus(status);
            }
        }

        boolean success = roomDao.updateRoom(updated);
        if (success) {
            response.sendRedirect(request.getContextPath() + "/rooms?success=RoomUpdated");
        } else {
            request.setAttribute("formError", "Database edit failed.");
            showEditForm(request, response);
        }
    }

    private void processDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        Room room = roomDao.getRoomById(id);

        if (room != null) {
            // Prevent deletions of active allocations
            if (room.getOccupiedBeds() > 0) {
                response.sendRedirect(request.getContextPath() + "/rooms?error=RoomOccupied");
                return;
            }

            roomDao.deleteRoom(id);
            response.sendRedirect(request.getContextPath() + "/rooms?success=RoomDeleted");
        } else {
            response.sendRedirect(request.getContextPath() + "/rooms?error=RoomNotFound");
        }
    }
}
