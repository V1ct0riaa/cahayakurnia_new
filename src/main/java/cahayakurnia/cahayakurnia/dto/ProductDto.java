package cahayakurnia.cahayakurnia.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductDto {
    
    private Long id;
    
    @NotBlank(message = "SKU tidak boleh kosong")
    @Size(min = 3, max = 50, message = "SKU harus 3-50 karakter")
    private String sku;
    
    @NotBlank(message = "Nama produk tidak boleh kosong")
    @Size(min = 2, max = 100, message = "Nama produk harus 2-100 karakter")
    private String name;
    
    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    private String description;
    
    @NotNull(message = "Harga tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Harga harus lebih dari 0")
    private BigDecimal price;
    
    @NotNull(message = "Stok tidak boleh kosong")
    @Min(value = 0, message = "Stok tidak boleh negatif")
    private Integer stock = 0;
    
    private String category;
    private String imageUrl;
    
    // Constructors
    public ProductDto() {}
    
    public ProductDto(String sku, String name, String description, BigDecimal price, Integer stock, String category) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
