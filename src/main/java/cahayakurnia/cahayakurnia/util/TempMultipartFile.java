package cahayakurnia.cahayakurnia.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class TempMultipartFile implements MultipartFile {
    
    private final File file;
    private final String originalFilename;
    private final String contentType;
    
    public TempMultipartFile(File file, String originalFilename, String contentType) {
        this.file = file;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }
    
    @Override
    public String getName() {
        return originalFilename;
    }
    
    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    @Override
    public String getContentType() {
        return contentType;
    }
    
    @Override
    public boolean isEmpty() {
        return !file.exists() || file.length() == 0;
    }
    
    @Override
    public long getSize() {
        return file.length();
    }
    
    @Override
    public byte[] getBytes() throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
    
    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileInputStream fis = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }
}
