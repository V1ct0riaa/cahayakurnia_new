package cahayakurnia.cahayakurnia.service;

import cahayakurnia.cahayakurnia.model.Product;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ProductService {
    
    List<Product> getAllProducts();
    
    Product getProductById(Long id);
    
    Product saveProduct(Product product);
    
    void deleteProduct(Long id);
    
    boolean skuExists(String sku);
    
    long getTotalProductCount();
    
    List<Product> getProductsWithLowStock(int threshold);
    
    List<Product> getRecentProducts(int limit);
    
    void updateStock(Long productId, int newStock);
    
    List<Product> searchProducts(String searchTerm);
    
    List<Product> getProductsByCategory(String category);
    
    // Additional methods for featured and best-selling products
    List<Product> getFeaturedProducts(int limit);
    
    List<Product> getBestSellingProducts(int limit);
    
    // =========================
    // PAGINATION METHODS
    // =========================
    
    // Basic pagination with sorting
    Page<Product> getProductsWithPagination(int page, int size, String sortBy, String direction);
    
    // Search with pagination
    Page<Product> searchProductsWithPagination(String searchTerm, int page, int size, String sortBy, String direction);
    
    // Category filter with pagination
    Page<Product> getProductsByCategoryWithPagination(String category, int page, int size, String sortBy, String direction);
    
    // Low stock products with pagination
    Page<Product> getProductsWithLowStockWithPagination(int threshold, int page, int size, String sortBy, String direction);
}
