package cahayakurnia.cahayakurnia.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cahayakurnia.cahayakurnia.model.Product;
import cahayakurnia.cahayakurnia.repository.ProductDb;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    
    private final ProductDb productRepository;
    
    public ProductServiceImpl(ProductDb productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    @Override
    public boolean skuExists(String sku) {
        return productRepository.findBySku(sku).isPresent();
    }
    
    @Override
    public long getTotalProductCount() {
        return productRepository.count();
    }
    
    @Override
    public List<Product> getProductsWithLowStock(int threshold) {
        return productRepository.findByStockLessThanEqual(threshold);
    }
    
    @Override
    public List<Product> getRecentProducts(int limit) {
        return productRepository.findTopNByOrderByIdDesc(limit);
    }
    
    @Override
    public void updateStock(Long productId, int newStock) {
        Product product = getProductById(productId);
        if (product != null) {
            product.setStock(newStock);
            saveProduct(product);
        }
    }
    
    @Override
    public List<Product> searchProducts(String searchTerm) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            searchTerm, searchTerm);
    }
    
    @Override
    public List<Product> getProductsByCategory(String category) {
        // For now, return all products. This will be updated when Category entity is implemented
        return getAllProducts();
    }
    
    @Override
    public List<Product> getFeaturedProducts(int limit) {
        // For now, return recent products as featured products
        // This can be enhanced later with actual featured product logic
        return getRecentProducts(limit);
    }
    
    @Override
    public List<Product> getBestSellingProducts(int limit) {
        // For now, return recent products as best-selling products
        // This can be enhanced later with actual best-selling product logic based on sales data
        return getRecentProducts(limit);
    }
}
