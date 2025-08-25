package cahayakurnia.cahayakurnia.controller;

import cahayakurnia.cahayakurnia.model.Product;
import cahayakurnia.cahayakurnia.service.FileUploadService;
import cahayakurnia.cahayakurnia.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private final ProductService productService;
    private final FileUploadService fileUploadService;

    public ProductController(ProductService productService, FileUploadService fileUploadService) {
        this.productService = productService;
        this.fileUploadService = fileUploadService;
    }

    // =========================
    // PUBLIC ENDPOINTS
    // =========================
    
    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String category,
                               @RequestParam(required = false) String search,
                               Model model) {
        List<Product> products;

        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search);
        } else if (category != null && !category.trim().isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }

        // Filter out products with no stock for public view
        products = products.stream()
                .filter(product -> product.getStock() > 0)
                .collect(Collectors.toList());

        model.addAttribute("products", products);
        return "products";
    }

    @GetMapping("/products/{id}")
    public String showProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null || product.getStock() <= 0) {
            return "redirect:/products";
        }

        // Get related products
        List<Product> relatedProducts = productService.getProductsByCategory(product.getCategory());
        relatedProducts = relatedProducts.stream()
                .filter(p -> !p.getId().equals(product.getId()) && p.getStock() > 0)
                .limit(4)
                .collect(Collectors.toList());

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        return "product-detail";
    }

    // =========================
    // ADMIN ENDPOINTS
    // =========================
    
    @GetMapping("/admin/products")
    public String adminListProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Model model) {
        
        Page<Product> productPage;
        
        // Handle search functionality
        if (search != null && !search.trim().isEmpty()) {
            productPage = productService.searchProductsWithPagination(search.trim(), page, size, sortBy, direction);
        } else if (category != null && !category.trim().isEmpty()) {
            productPage = productService.getProductsByCategoryWithPagination(category.trim(), page, size, sortBy, direction);
        } else {
            productPage = productService.getProductsWithPagination(page, size, sortBy, direction);
        }
        
        // Add pagination info to model
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        
        // Calculate pagination info
        int startItem = page * size + 1;
        int endItem = Math.min(startItem + size - 1, (int) productPage.getTotalElements());
        model.addAttribute("startItem", startItem);
        model.addAttribute("endItem", endItem);
        
        // Add flash messages if any
        model.addAttribute("successMessage", model.getAttribute("successMessage"));
        model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        model.addAttribute("warningMessage", model.getAttribute("warningMessage"));
        
        return "admin/products";
    }

    // Debug endpoint to check raw data
    @GetMapping("/admin/debug/products")
    @ResponseBody
    public String debugProductsRaw() {
        List<Product> products = productService.getAllProducts();
        StringBuilder result = new StringBuilder();
        result.append("<h2>Raw Products Data</h2>");
        result.append("<p><strong>Total products:</strong> ").append(products.size()).append("</p>");
        
        if (products.isEmpty()) {
            result.append("<p style='color: red;'>No products found!</p>");
        } else {
            result.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
            result.append("<tr><th>ID</th><th>Name</th><th>SKU</th><th>Price</th><th>Stock</th><th>Category</th><th>Description</th><th>ImageUrl</th></tr>");
            
            for (Product product : products) {
                result.append("<tr>");
                result.append("<td>").append(product.getId()).append("</td>");
                result.append("<td>").append(product.getName() != null ? product.getName() : "NULL").append("</td>");
                result.append("<td>").append(product.getSku() != null ? product.getSku() : "NULL").append("</td>");
                result.append("<td>").append(product.getPrice() != null ? product.getPrice() : "NULL").append("</td>");
                result.append("<td>").append(product.getStock() != null ? product.getStock() : "NULL").append("</td>");
                result.append("<td>").append(product.getCategory() != null ? product.getCategory() : "NULL").append("</td>");
                result.append("<td>").append(product.getDescription() != null ? product.getDescription() : "NULL").append("</td>");
                result.append("<td>").append(product.getImageUrl() != null ? "Yes" : "No").append("</td>");
                result.append("</tr>");
            }
            result.append("</table>");
        }
        
        return result.toString();
    }

    @GetMapping("/admin/products/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

    @PostMapping("/admin/products")
    public String createProduct(@Valid @ModelAttribute Product product,
                                BindingResult result,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        // Validation errors
        if (result.hasErrors()) {
            return "admin/product-form";
        }

        // Check SKU
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            model.addAttribute("skuError", "SKU tidak boleh kosong");
            return "admin/product-form";
        }

        if (productService.skuExists(product.getSku())) {
            model.addAttribute("skuError", "SKU sudah ada, gunakan SKU yang berbeda");
            return "admin/product-form";
        }

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                System.out.println("=== CREATING PRODUCT WITH IMAGE ===");
                System.out.println("File name: " + imageFile.getOriginalFilename());
                System.out.println("File size: " + imageFile.getSize());
                System.out.println("Content type: " + imageFile.getContentType());
                
                String imageUrl = fileUploadService.uploadImage(imageFile);
                product.setImageUrl(imageUrl);
                System.out.println("Image uploaded successfully: " + imageUrl);
                
            } catch (Exception e) {
                System.err.println("Upload error: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("imageError", "Gagal upload gambar: " + e.getMessage());
                return "admin/product-form";
            }
        } else {
            System.out.println("No image file provided");
            product.setImageUrl(null);
        }

        // Save product
        try {
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil ditambahkan!");
            System.out.println("Product saved successfully: " + product.getName());
        } catch (Exception e) {
            System.err.println("Failed to save product: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Gagal menyimpan produk: " + e.getMessage());
            return "admin/product-form";
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/admin/products";
        }
        model.addAttribute("product", product);
        return "admin/product-form";
    }

    @PostMapping("/admin/products/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute Product product,
                                BindingResult result,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/product-form";
        }

        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return "redirect:/admin/products";
        }

        // Check SKU
        if (!existingProduct.getSku().equals(product.getSku()) && productService.skuExists(product.getSku())) {
            model.addAttribute("skuError", "SKU sudah ada, gunakan SKU yang berbeda");
            return "admin/product-form";
        }

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                System.out.println("=== UPDATING PRODUCT IMAGE ===");
                System.out.println("File name: " + imageFile.getOriginalFilename());
                System.out.println("File size: " + imageFile.getSize());
                
                // Delete old image if exists
                if (existingProduct.getImageUrl() != null && !existingProduct.getImageUrl().isEmpty()) {
                    try {
                        fileUploadService.deleteImage(existingProduct.getImageUrl());
                        System.out.println("Old image deleted successfully");
                    } catch (Exception e) {
                        System.err.println("Failed to delete old image: " + e.getMessage());
                        // Continue with upload
                    }
                }
                
                // Upload new image
                String imageUrl = fileUploadService.uploadImage(imageFile);
                product.setImageUrl(imageUrl);
                System.out.println("New image uploaded successfully: " + imageUrl);
                
            } catch (Exception e) {
                System.err.println("Upload error: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("imageError", "Gagal upload gambar: " + e.getMessage());
                return "admin/product-form";
            }
        } else {
            // Keep existing image
            product.setImageUrl(existingProduct.getImageUrl());
        }

        // Save product
        product.setId(id);
        try {
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil diupdate!");
            System.out.println("Product updated successfully: " + product.getName());
        } catch (Exception e) {
            System.err.println("Failed to update product: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Gagal mengupdate produk: " + e.getMessage());
            return "admin/product-form";
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== DELETING PRODUCT ===");
            System.out.println("Product ID: " + id);
            
            Product product = productService.getProductById(id);
            if (product == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan");
                return "redirect:/admin/products";
            }
            
            System.out.println("Product found: " + product.getName());
            
            // Delete image first if exists
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                try {
                    fileUploadService.deleteImage(product.getImageUrl());
                    System.out.println("Image deleted successfully");
                } catch (Exception e) {
                    System.err.println("Failed to delete image: " + e.getMessage());
                    // Continue with product deletion
                }
            }
            // Delete product
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil dihapus!");
            System.out.println("Product deleted successfully");
            
        } catch (Exception e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus produk: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
}