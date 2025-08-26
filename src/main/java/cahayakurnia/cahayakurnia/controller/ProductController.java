package cahayakurnia.cahayakurnia.controller;

import cahayakurnia.cahayakurnia.model.Product;
import cahayakurnia.cahayakurnia.service.CsvService;
import cahayakurnia.cahayakurnia.service.FileUploadService;
import cahayakurnia.cahayakurnia.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import cahayakurnia.cahayakurnia.util.TempMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    private final ProductService productService;
    private final FileUploadService fileUploadService;
    private final CsvService csvService;

    public ProductController(ProductService productService, FileUploadService fileUploadService, CsvService csvService) {
        this.productService = productService;
        this.fileUploadService = fileUploadService;
        this.csvService = csvService;
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
                logger.error("Error uploading image for product creation", e);
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
            logger.error("Error saving product: " + product.getName(), e);
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
                logger.error("Error uploading image for product update", e);
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
            logger.error("Error deleting product with ID: " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus produk: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }

    // =========================
    // BULK IMPORT ENDPOINTS
    // =========================
    
    @GetMapping("/admin/download-format-guide")
    public ResponseEntity<String> downloadFormatGuide() {
        String guide = """
            FORMAT EXCEL/CSV UNTUK IMPORT PRODUK
            
            Format kolom (dipisahkan dengan koma):
            SKU,Nama Produk,Deskripsi,Harga,Kategori,Stok,Nama File Gambar
            
            Contoh data:
            LAP001,Laptop Gaming ASUS ROG,High performance gaming laptop dengan RTX 4080,25000000,Electronics,15,LAP001.jpg
            PHN002,iPhone 15 Pro Max,Smartphone Apple terbaru dengan desain titanium,18000000,Electronics,8,PHN002.png
            TAB003,iPad Pro 12.9,Tablet premium untuk profesional dan kreator,15000000,Electronics,12,TAB003.jpg
            
            Aturan:
            1. SKU: 3-10 karakter, huruf besar dan angka saja
            2. Nama Produk: 1-100 karakter, huruf, angka, spasi, tanda hubung, underscore, titik
            3. Deskripsi: 1-500 karakter
            4. Harga: angka positif (tanpa koma atau titik ribuan)
            5. Kategori: 1-50 karakter, huruf dan spasi saja
            6. Stok: angka 0-999999
            7. Nama File Gambar: nama file yang sudah diupload (opsional)
            
            Catatan:
            - Simpan file dalam format CSV atau Excel (.xlsx)
            - Pastikan nama file gambar sesuai dengan file yang diupload
            - Jika deskripsi mengandung koma, gunakan tanda kutip: "Deskripsi dengan, koma"
            """;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "format-import-produk.txt");
        return new ResponseEntity<>(guide, headers, HttpStatus.OK);
    }
    
    @GetMapping("/admin/bulk-import")
    public String showBulkImportPage(Model model) {
        return "admin/bulk-import";
    }
    
    @PostMapping("/admin/upload-excel-preview")
    public String uploadExcelPreview(@RequestParam(value = "excelFile", required = false) MultipartFile excelFile,
                                     @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                                     HttpServletRequest request,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        
        // Debug logging
        System.out.println("=== UPLOAD EXCEL PREVIEW DEBUG ===");
        System.out.println("excelFile: " + (excelFile != null ? excelFile.getOriginalFilename() : "NULL"));
        System.out.println("imageFiles: " + (imageFiles != null ? imageFiles.length : "NULL"));
        
        if (excelFile == null || excelFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "File Excel tidak ditemukan. Pastikan file sudah dipilih.");
            return "redirect:/admin/bulk-import";
        }
        
        if (imageFiles == null || imageFiles.length == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "File gambar tidak ditemukan. Pastikan minimal satu gambar dipilih.");
            return "redirect:/admin/bulk-import";
        }
        
        try {
            // Parse CSV/Excel
            logger.info("Starting CSV parsing for file: " + excelFile.getOriginalFilename());
            List<Map<String, String>> products = csvService.parseCsvPreview(excelFile);
            logger.info("Parsed " + products.size() + " products from CSV");
            
            // Debug: Print first few products
            for (int i = 0; i < Math.min(3, products.size()); i++) {
                Map<String, String> product = products.get(i);
                logger.info("Product " + (i+1) + ": " + product);
            }
            
            // Get uploaded image filenames
            Set<String> uploadedImages = new HashSet<>();
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty()) {
                    uploadedImages.add(imageFile.getOriginalFilename());
                }
            }
            logger.info("Uploaded images: " + uploadedImages);
            
            // Validate data
            List<String> errors = csvService.validateCsvData(products, uploadedImages);
            logger.info("Validation errors: " + errors.size());
            if (!errors.isEmpty()) {
                logger.warn("Validation errors found: " + errors);
            }
            
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                model.addAttribute("products", products);
                model.addAttribute("uploadedImages", uploadedImages);
                return "admin/bulk-import";
            }
            
                        // Store data in session for confirmation
            HttpSession session = request.getSession();
            session.setAttribute("bulkImportProducts", products);
            session.setAttribute("bulkImportUploadedImages", uploadedImages);
            
            // Save image files to temporary directory
            String tempDir = System.getProperty("java.io.tmpdir") + "/bulk-import-" + session.getId();
            java.io.File tempDirFile = new java.io.File(tempDir);
            if (!tempDirFile.exists()) {
                tempDirFile.mkdirs();
            }
            
            // Save each image file
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty()) {
                    try {
                        java.io.File tempFile = new java.io.File(tempDir, imageFile.getOriginalFilename());
                        imageFile.transferTo(tempFile);
                        System.out.println("Saved temp image: " + tempFile.getAbsolutePath());
                    } catch (Exception e) {
                        System.err.println("Failed to save temp image: " + imageFile.getOriginalFilename());
                        e.printStackTrace();
                    }
                }
            }
            
            // Store temp directory path in session
            session.setAttribute("bulkImportTempDir", tempDir);
            
            // Store in model for display
            model.addAttribute("products", products);
            model.addAttribute("uploadedImages", uploadedImages);
            model.addAttribute("excelFile", excelFile.getOriginalFilename());
            model.addAttribute("imageFiles", Arrays.stream(imageFiles)
                    .map(MultipartFile::getOriginalFilename)
                    .collect(Collectors.toList()));
            
            logger.info("Model attributes set for preview:");
            logger.info("- products: " + products.size() + " items");
            logger.info("- uploadedImages: " + uploadedImages.size() + " items");
            logger.info("- excelFile: " + excelFile.getOriginalFilename());
            logger.info("- imageFiles: " + Arrays.stream(imageFiles)
                    .map(MultipartFile::getOriginalFilename)
                    .collect(Collectors.toList()));
            
            return "admin/bulk-import-preview";
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal membaca file Excel: " + e.getMessage());
            return "redirect:/admin/bulk-import";
        }
    }
    
    // Test multipart upload
    @PostMapping("/test-upload")
    public String testUpload(@RequestParam(value = "file", required = false) MultipartFile file,
                            Model model) {
        System.out.println("=== TEST UPLOAD DEBUG ===");
        System.out.println("file: " + (file != null ? file.getOriginalFilename() : "NULL"));
        System.out.println("file size: " + (file != null ? file.getSize() : "NULL"));
        
        if (file != null && !file.isEmpty()) {
            model.addAttribute("message", "File uploaded successfully: " + file.getOriginalFilename());
        } else {
            model.addAttribute("message", "No file uploaded");
        }
        
        return "admin/test-upload";
    }
    
    @PostMapping("/admin/confirm-excel-import")
    public String confirmExcelImport(HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        
        // Debug logging
        System.out.println("=== CONFIRM EXCEL IMPORT DEBUG ===");
        
        // Get data from session
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> products = (List<Map<String, String>>) session.getAttribute("bulkImportProducts");
        @SuppressWarnings("unchecked")
        Set<String> uploadedImages = (Set<String>) session.getAttribute("bulkImportUploadedImages");
        String tempDir = (String) session.getAttribute("bulkImportTempDir");
        
        System.out.println("products from session: " + (products != null ? products.size() : "NULL"));
        System.out.println("uploadedImages from session: " + (uploadedImages != null ? uploadedImages.size() : "NULL"));
        System.out.println("tempDir from session: " + tempDir);
        
        if (products == null || products.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Data produk tidak ditemukan di session.");
            return "redirect:/admin/bulk-import";
        }
        
        try {
            // Validate data
            List<String> errors = csvService.validateCsvData(products, uploadedImages);
            
            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Data tidak valid: " + String.join(", ", errors));
                return "redirect:/admin/bulk-import";
            }
            
            // Process import
            int successCount = 0;
            List<String> importErrors = new ArrayList<>();
            
            for (Map<String, String> productData : products) {
                try {
                    System.out.println("Processing product: " + productData.get("sku"));
                    
                    // Create product
                    Product product = new Product();
                    product.setSku(productData.get("sku"));
                    product.setName(productData.get("name"));
                    product.setDescription(productData.get("description"));
                    product.setPrice(BigDecimal.valueOf(Double.parseDouble(productData.get("price"))));
                    product.setCategory(productData.get("category"));
                    product.setStock(Integer.parseInt(productData.get("stock")));
                    
                    // Upload image if exists
                    String imageFileName = productData.get("imageFileName");
                    if (imageFileName != null && !imageFileName.isEmpty() && tempDir != null) {
                        java.io.File imageFile = new java.io.File(tempDir, imageFileName);
                        if (imageFile.exists()) {
                            try {
                                // Create TempMultipartFile wrapper
                                TempMultipartFile tempMultipartFile = new TempMultipartFile(
                                    imageFile, 
                                    imageFileName, 
                                    "image/jpeg" // Default content type
                                );
                                
                                // Upload to Supabase
                                String imageUrl = fileUploadService.uploadImage(tempMultipartFile);
                                product.setImageUrl(imageUrl);
                                System.out.println("Image uploaded for " + productData.get("sku") + ": " + imageUrl);
                            } catch (Exception e) {
                                System.err.println("Failed to upload image for " + productData.get("sku") + ": " + e.getMessage());
                                product.setImageUrl(null);
                            }
                        } else {
                            System.out.println("Image file not found: " + imageFile.getAbsolutePath());
                            product.setImageUrl(null);
                        }
                    } else {
                        product.setImageUrl(null);
                    }
                    
                    // Save product
                    productService.saveProduct(product);
                    successCount++;
                    System.out.println("Successfully saved product: " + product.getSku());
                    
                } catch (Exception e) {
                    System.err.println("Error saving product " + productData.get("sku") + ": " + e.getMessage());
                    e.printStackTrace();
                    importErrors.add("Produk " + productData.get("sku") + ": " + e.getMessage());
                }
            }
            
            if (successCount > 0) {
                // Clean up session
                session.removeAttribute("bulkImportProducts");
                session.removeAttribute("bulkImportUploadedImages");
                session.removeAttribute("bulkImportTempDir");
                
                // Clean up temporary directory
                if (tempDir != null) {
                    try {
                        java.io.File tempDirFile = new java.io.File(tempDir);
                        if (tempDirFile.exists()) {
                            for (java.io.File file : tempDirFile.listFiles()) {
                                file.delete();
                            }
                            tempDirFile.delete();
                            System.out.println("Cleaned up temp directory: " + tempDir);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to clean up temp directory: " + e.getMessage());
                    }
                }
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Berhasil import " + successCount + " produk dari " + products.size() + " total");
            }
            
            if (!importErrors.isEmpty()) {
                redirectAttributes.addFlashAttribute("warningMessage", 
                    "Beberapa produk gagal diimport: " + String.join(", ", importErrors));
            }
            
        } catch (Exception e) {
            System.err.println("Error during bulk import: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal import produk: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
}