package cahayakurnia.cahayakurnia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class FileUploadServiceImp implements FileUploadService {

    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.apiKey}")
    private String supabaseApiKey;

    @Value("${supabase.bucketName}")
    private String bucketName;

    public FileUploadServiceImp(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String uploadImage(MultipartFile image) throws Exception {
        if (image == null || image.isEmpty()) {
            throw new Exception("File gambar kosong atau tidak ada");
        }

        // Validasi tipe file
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new Exception("File harus berupa gambar (JPG, PNG, GIF)");
        }

        // Generate unique filename
        String originalFilename = image.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            extension = ".jpg"; // default extension
        }
        
        String fileName = UUID.randomUUID().toString() + extension;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

        System.out.println("=== UPLOAD DEBUG ===");
        System.out.println("Original filename: " + originalFilename);
        System.out.println("Generated filename: " + fileName);
        System.out.println("Upload URL: " + uploadUrl);
        System.out.println("File size: " + image.getSize() + " bytes");
        System.out.println("Content type: " + contentType);

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseApiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Prepare body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", image.getResource());

        // Make request
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        System.out.println("==================");

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
            System.out.println("SUCCESS! Public URL: " + publicUrl);
            return publicUrl;
        } else {
            throw new Exception("Upload gagal. Status: " + response.getStatusCode() + ", Response: " + response.getBody());
        }
    }

    @Override
    public void deleteImage(String imageUrl) throws Exception {
        if (imageUrl == null || imageUrl.isEmpty()) {
            System.out.println("Delete skipped: imageUrl kosong");
            return;
        }

        // Skip delete untuk placeholder images
        if (imageUrl.startsWith("data:") || imageUrl.contains("placeholder")) {
            System.out.println("Delete skipped: placeholder image");
            return;
        }

        System.out.println("=== DELETE DEBUG ===");
        System.out.println("Original URL: " + imageUrl);

        // Extract path from public URL
        String publicMarker = "/storage/v1/object/public/" + bucketName + "/";
        int index = imageUrl.indexOf(publicMarker);
        
        if (index == -1) {
            System.out.println("Delete skipped: bukan public URL yang valid");
            return;
        }

        String fileName = imageUrl.substring(index + publicMarker.length());
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;
        
        System.out.println("File name: " + fileName);
        System.out.println("Delete URL: " + deleteUrl);

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseApiKey);

        // Make request
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, String.class);

        System.out.println("Delete response status: " + response.getStatusCode());
        System.out.println("Delete response body: " + response.getBody());
        System.out.println("==================");

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NO_CONTENT) {
            System.out.println("DELETE SUCCESS!");
        } else {
            throw new Exception("Delete gagal. Status: " + response.getStatusCode() + ", Response: " + response.getBody());
        }
    }
}