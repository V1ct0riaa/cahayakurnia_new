package cahayakurnia.cahayakurnia.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String uploadImage(MultipartFile image) throws Exception;
    void deleteImage(String imageUrl) throws Exception;
}
