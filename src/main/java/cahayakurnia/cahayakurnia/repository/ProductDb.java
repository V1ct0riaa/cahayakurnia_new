package cahayakurnia.cahayakurnia.repository;

import cahayakurnia.cahayakurnia.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDb extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    List<Product> findByStockLessThanEqual(int threshold);
    
    @Query("SELECT p FROM Product p ORDER BY p.id DESC LIMIT :limit")
    List<Product> findTopNByOrderByIdDesc(@Param("limit") int limit);
    
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name, String description);
    
    // =========================
    // PAGINATION METHODS
    // =========================
    
    // Basic pagination with sorting
    Page<Product> findAll(Pageable pageable);
    
    // Search with pagination
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Category filter with pagination
    Page<Product> findByCategory(String category, Pageable pageable);
    
    // Combined search (name or description) with pagination
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name, String description, Pageable pageable);
    
    // Stock filter with pagination
    Page<Product> findByStockLessThanEqual(int threshold, Pageable pageable);
}