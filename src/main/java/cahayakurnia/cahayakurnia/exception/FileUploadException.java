package cahayakurnia.cahayakurnia.exception;

public class FileUploadException extends RuntimeException {
    
    public FileUploadException(String message) {
        super(message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FileUploadException(String fileName, String operation) {
        super("Gagal " + operation + " file: " + fileName);
    }
}
