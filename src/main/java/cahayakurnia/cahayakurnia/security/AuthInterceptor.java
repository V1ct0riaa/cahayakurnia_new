package cahayakurnia.cahayakurnia.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // Allow all public routes and static resources
        if (requestURI.equals("/") || 
            requestURI.startsWith("/products") || 
            requestURI.equals("/about") || 
            requestURI.equals("/contact") ||
            requestURI.startsWith("/images/") ||
            requestURI.startsWith("/css/") ||
            requestURI.startsWith("/js/") ||
            requestURI.startsWith("/static/") ||
            requestURI.startsWith("/favicon.ico") ||
            requestURI.startsWith("/error")) {
            return true;
        }
        
        // Only check authentication for admin routes
        if (requestURI.startsWith("/admin/")) {
            // Skip login page itself
            if (requestURI.equals("/admin/login")) {
                return true;
            }
            
            // TEMPORARILY DISABLE AUTH FOR TESTING
            // TODO: Re-enable authentication later
            return true;
            
            /*
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("adminLoggedIn") == null) {
                // Redirect to login page
                response.sendRedirect("/admin/login");
                return false;
            }
            */
        }
        
        // Allow all other routes (non-admin)
        return true;
    }
}
