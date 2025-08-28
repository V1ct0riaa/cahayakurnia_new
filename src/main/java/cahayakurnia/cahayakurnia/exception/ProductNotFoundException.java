package cahayakurnia.cahayakurnia.exception;

public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(String message) {
        super(message);
    }
    
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ProductNotFoundException(Long productId) {
        super("Produk dengan ID " + productId + " tidak ditemukan");
    }
    
    public ProductNotFoundException(String sku, String type) {
        super("Produk dengan " + type + " '" + sku + "' tidak ditemukan");
    }
}
