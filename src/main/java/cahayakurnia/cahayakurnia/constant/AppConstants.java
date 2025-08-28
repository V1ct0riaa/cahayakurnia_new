package cahayakurnia.cahayakurnia.constant;

public final class AppConstants {
    
    // Private constructor to prevent instantiation
    private AppConstants() {}
    
    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_DIRECTION = "desc";
    
    // File Upload
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };
    public static final String[] ALLOWED_FILE_EXTENSIONS = {
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    };
    
    // Bulk Import
    public static final int MAX_BULK_IMPORT_SIZE = 1000;
    public static final String[] REQUIRED_CSV_COLUMNS = {
        "sku", "name", "description", "price", "category", "stock", "imageFileName"
    };
    
    // Security
    public static final String ADMIN_SESSION_KEY = "adminLoggedIn";
    public static final String ADMIN_USERNAME_KEY = "adminUsername";
    
    // Messages
    public static final String SUCCESS_MESSAGE = "successMessage";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String WARNING_MESSAGE = "warningMessage";
    
    // Company Info
    public static final String COMPANY_NAME = "Cahaya Kurnia";
    public static final String COMPANY_TAGLINE = "Toko Perkakas dan Elektronik Terpercaya";
    public static final String COMPANY_DESCRIPTION = 
        "Menyediakan berbagai macam perkakas, elektronik, dan peralatan rumah tangga berkualitas tinggi dengan harga terjangkau.";
}
