package cahayakurnia.cahayakurnia.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    // Simple admin credentials (in production, use proper authentication)
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @GetMapping("/admin/login")
    public String showLoginPage() {
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        
        // Debug logging
        System.out.println("Login attempt - Username: " + username + ", Password: " + password);
        System.out.println("Expected - Username: " + ADMIN_USERNAME + ", Password: " + ADMIN_PASSWORD);
        
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("adminLoggedIn", true);
            session.setAttribute("adminUsername", username);
            redirectAttributes.addFlashAttribute("successMessage", "Login berhasil!");
            System.out.println("Login successful for user: " + username);
            return "redirect:/admin/products";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Username atau password salah!");
            System.out.println("Login failed for user: " + username);
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Logout berhasil!");
        return "redirect:/admin/login";
    }
}
