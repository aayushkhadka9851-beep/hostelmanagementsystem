package controller;

import dao.RoomTypeDAO;
import model.RoomType;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Controller servlet for orchestrating system brand settings and room types.
 */
@WebServlet(name = "SettingsServlet", urlPatterns = {"/settings"})
public class SettingsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final RoomTypeDAO typeDao = new RoomTypeDAO();

    @Override
    public void init() throws ServletException {
        // Initialize default hostel metadata profile in ServletContext if not preconfigured
        ServletContext context = getServletContext();
        if (context.getAttribute("hostelName") == null) {
            context.setAttribute("hostelName", "Royal Oak Student Living");
            context.setAttribute("hostelDate", "2018-04-12");
            context.setAttribute("hostelContact", "+1-234-567-890");
            context.setAttribute("hostelEmail", "contact@royaloakhostel.com");
            context.setAttribute("hostelAddress", "452 University Avenue, Suite B, Palo Alto, CA");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            List<RoomType> types = typeDao.getAllRoomTypes();
            request.setAttribute("typesList", types);
            request.getRequestDispatcher("settings.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("SettingsServlet Exception: " + e.getMessage());
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
                case "add-type":
                    processAddType(request, response);
                    break;
                case "delete-type":
                    processDeleteType(request, response);
                    break;
                case "save-info":
                    processSaveInfo(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/settings");
                    break;
            }
        } catch (Exception e) {
            System.err.println("SettingsServlet doPost Exception: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void processAddType(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String typeName = request.getParameter("type_name");
        if (typeName != null && !typeName.trim().isEmpty()) {
            RoomType rt = new RoomType(typeName.trim());
            typeDao.addRoomType(rt);
            response.sendRedirect(request.getContextPath() + "/settings?success=TypeAdded");
        } else {
            response.sendRedirect(request.getContextPath() + "/settings?error=InvalidTypeName");
        }
    }

    private void processDeleteType(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int typeId = Integer.parseInt(request.getParameter("id"));
        boolean success = typeDao.deleteRoomType(typeId);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/settings?success=TypeDeleted");
        } else {
            response.sendRedirect(request.getContextPath() + "/settings?error=TypeDeleteFailed");
        }
    }

    private void processSaveInfo(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        ServletContext context = getServletContext();
        context.setAttribute("hostelName", request.getParameter("hostelName"));
        context.setAttribute("hostelDate", request.getParameter("hostelDate"));
        context.setAttribute("hostelContact", request.getParameter("hostelContact"));
        context.setAttribute("hostelEmail", request.getParameter("hostelEmail"));
        context.setAttribute("hostelAddress", request.getParameter("hostelAddress"));

        response.sendRedirect(request.getContextPath() + "/settings?success=InfoSaved");
    }
}
