package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Route protection filter that intercepts Tomcat 10 requests.
 * Redirects unauthenticated session paths back to the login gateway.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.jsp", "/dashboard", "/students", "/rooms", "/payments", "/settings"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initializer block (Optional)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        HttpSession session = httpRequest.getSession(false);

        // Allow static assets, resources, and explicit authentication endpoints to bypass protection
        boolean isStaticAsset = requestURI.contains("/css/") || requestURI.contains("/js/") || requestURI.contains("/assets/");
        boolean isLoginPath = requestURI.endsWith("login.jsp") || requestURI.contains("LoginServlet") || requestURI.contains("/login");
        boolean isLogged = (session != null && session.getAttribute("adminUser") != null);

        if (isLogged || isStaticAsset || isLoginPath) {
            // Authorized or safe asset path, let it pass
            chain.doFilter(request, response);
        } else {
            // Unauthorized path, redirect to sign-in card
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
        }
    }

    @Override
    public void destroy() {
        // Destructor block (Optional)
    }
}
