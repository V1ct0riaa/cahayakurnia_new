package cahayakurnia.cahayakurnia.controller;

import cahayakurnia.cahayakurnia.model.Product;
import cahayakurnia.cahayakurnia.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    // =========================
    // Landing page "/"
    // =========================
    @GetMapping("/")
    public String home(Model model) {
        // Atribut yang dipakai home.html
        model.addAttribute("companyName", "Cahaya Kurnia");
        model.addAttribute("companyTagline", "Toko Perkakas dan Elektronik Terpercaya");
        model.addAttribute("companyDescription",
                "Menyediakan berbagai macam perkakas, elektronik, dan peralatan rumah tangga berkualitas tinggi dengan harga terjangkau.");

        // Produk unggulan & terlaris
        List<Product> featuredProducts = productService.getFeaturedProducts(8);
        List<Product> bestSellingProducts = productService.getBestSellingProducts(8);
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("bestSellingProducts", bestSellingProducts);

        return "home"; // templates/home.html
    }

    // =========================
    // Admin Dashboard "/admin"
    // =========================
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        // Angka-angka ringkas
        long totalProducts = productService.getTotalProductCount();
        List<Product> lowStockProducts = productService.getProductsWithLowStock(5);
        int lowStockCount = lowStockProducts.size();
        int outOfStockCount = (int) productService.getAllProducts().stream()
                .filter(p -> p.getStock() <= 0)
                .count();

        // Tabel ringkas (recent & out-of-stock list)
        List<Product> recentProducts = productService.getRecentProducts(10);
        List<Product> outOfStockProducts = productService.getAllProducts().stream()
                .filter(p -> p.getStock() <= 0)
                .collect(Collectors.toList());

        // Atribut yang dipakai dashboard.html
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("lowStockProducts", lowStockCount);
        model.addAttribute("lowStockAlertProducts", lowStockProducts); // List produk dengan stok menipis
        model.addAttribute("recentProducts", recentProducts);
        model.addAttribute("outOfStockProducts", outOfStockCount);

        return "admin/dashboard"; // templates/admin/dashboard.html
    }

    // =========================
    // About page "/about"
    // =========================
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("companyName", "Cahaya Kurnia");
        model.addAttribute("companyTagline", "Toko Perkakas dan Elektronik Terpercaya");
        return "about";
    }

    // =========================
    // Contact page "/contact"
    // =========================
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("companyName", "Cahaya Kurnia");
        return "contact";
    }
    
    // =========================
    // Test endpoint "/test"
    // =========================
    @GetMapping("/test")
    public String test() {
        return "Test endpoint working!";
    }
}
