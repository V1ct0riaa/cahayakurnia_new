package cahayakurnia.cahayakurnia.repository;

import cahayakurnia.cahayakurnia.model.Product;
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
}