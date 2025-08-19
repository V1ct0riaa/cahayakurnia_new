package cahayakurnia.cahayakurnia.service;

import cahayakurnia.cahayakurnia.model.Product;
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
}
