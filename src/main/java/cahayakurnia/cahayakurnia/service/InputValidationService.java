package cahayakurnia.cahayakurnia.service;

import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class InputValidationService {
    
    // Regex patterns for validation
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9]{3,10}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_\\.]{1,100}$");
    private static final Pattern PRICE_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    private static final Pattern STOCK_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[a-zA-Z\\s]{1,50}$");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-_\\.]{1,100}\\.(jpg|jpeg|png|gif|webp)$");
    
    public String sanitizeString(String input) {
        if (input == null) {
            return "";
        }
        
        // Remove potential XSS vectors
        return input
            .replaceAll("<script[^>]*>.*?</script>", "")
            .replaceAll("<[^>]*>", "")
            .replaceAll("javascript:", "")
            .replaceAll("on\\w+\\s*=", "")
            .trim();
    }
    
    public boolean isValidSku(String sku) {
        return sku != null && SKU_PATTERN.matcher(sku).matches();
    }
    
    public boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }
    
    public boolean isValidPrice(String price) {
        if (price == null) return false;
        
        try {
            double value = Double.parseDouble(price);
            return value > 0 && value <= 999999999.99; // Max 999M
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public boolean isValidStock(String stock) {
        if (stock == null) return false;
        
        try {
            int value = Integer.parseInt(stock);
            return value >= 0 && value <= 999999; // Max 999K
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public boolean isValidCategory(String category) {
        return category != null && CATEGORY_PATTERN.matcher(category).matches();
    }
    
    public boolean isValidFilename(String filename) {
        return filename != null && FILENAME_PATTERN.matcher(filename.toLowerCase()).matches();
    }
    
    public String sanitizeFilename(String filename) {
        if (filename == null) return "";
        
        // Remove path traversal attempts
        filename = filename.replaceAll("\\.\\.", "");
        filename = filename.replaceAll("/", "");
        filename = filename.replaceAll("\\\\", "");
        
        // Remove special characters except allowed ones
        filename = filename.replaceAll("[^a-zA-Z0-9\\-_\\.]", "");
        
        return filename;
    }
    
    public boolean containsSuspiciousContent(String input) {
        if (input == null) return false;
        
        String lowerInput = input.toLowerCase();
        
        // Check for common attack patterns
        return lowerInput.contains("script") ||
               lowerInput.contains("javascript:") ||
               lowerInput.contains("vbscript:") ||
               lowerInput.contains("onload") ||
               lowerInput.contains("onerror") ||
               lowerInput.contains("onclick") ||
               lowerInput.contains("eval(") ||
               lowerInput.contains("document.cookie") ||
               lowerInput.contains("window.location");
    }
}
