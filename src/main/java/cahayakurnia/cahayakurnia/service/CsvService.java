package cahayakurnia.cahayakurnia.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class CsvService {

    private static final Logger logger = LoggerFactory.getLogger(CsvService.class);

    @Autowired
    private InputValidationService validationService;

    public List<Map<String, String>> parseCsvPreview(MultipartFile file) throws IOException {
        List<Map<String, String>> products = new ArrayList<>();
        
        logger.info("Starting Excel parsing for file: " + file.getOriginalFilename());
        logger.info("File size: " + file.getSize() + " bytes");
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            
            int rowNumber = 0;
            for (Row row : sheet) {
                rowNumber++;
                
                // Skip header row
                if (rowNumber == 1) {
                    logger.info("Skipping header row");
                    continue;
                }
                
                // Stop after 10 products
                if (products.size() >= 10) {
                    break;
                }
                
                // Parse row data
                String[] values = new String[7];
                for (int i = 0; i < 7 && i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    values[i] = getCellValueAsString(cell);
                }
                
                logger.debug("Parsed values: " + Arrays.toString(values));
                
                if (values.length >= 7) {
                    Map<String, String> product = new HashMap<>();
                    product.put("sku", values[0] != null ? values[0].trim() : "");
                    product.put("name", values[1] != null ? values[1].trim() : "");
                    product.put("description", values[2] != null ? values[2].trim() : "");
                    product.put("price", values[3] != null ? values[3].trim() : "");
                    product.put("category", values[4] != null ? values[4].trim() : "");
                    product.put("stock", values[5] != null ? values[5].trim() : "");
                    product.put("imageFileName", values[6] != null ? values[6].trim() : "");
                    
                    // Only add if SKU is not empty
                    if (!product.get("sku").isEmpty()) {
                        products.add(product);
                        logger.debug("Added product: " + product);
                    } else {
                        logger.warn("Skipping product with empty SKU at row " + rowNumber);
                    }
                } else {
                    logger.warn("Row " + rowNumber + " has insufficient columns: " + values.length + " (expected 7)");
                }
            }
        }
        
        logger.info("Finished parsing. Total products: " + products.size());
        return products;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Convert numeric to string without decimal if it's a whole number
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public List<String> validateCsvData(List<Map<String, String>> products, Set<String> uploadedImages) {
        List<String> errors = new ArrayList<>();
        Set<String> skus = new HashSet<>();
        
        for (int i = 0; i < products.size(); i++) {
            Map<String, String> product = products.get(i);
            int rowNumber = i + 2; // +2 because we start from row 2 (after header)
            
            // Sanitize inputs
            String sku = validationService.sanitizeString(product.get("sku"));
            String name = validationService.sanitizeString(product.get("name"));
            String description = validationService.sanitizeString(product.get("description"));
            String price = validationService.sanitizeString(product.get("price"));
            String category = validationService.sanitizeString(product.get("category"));
            String stock = validationService.sanitizeString(product.get("stock"));
            String imageFileName = validationService.sanitizeFilename(product.get("imageFileName"));
            
            // Check for suspicious content
            if (validationService.containsSuspiciousContent(sku) || 
                validationService.containsSuspiciousContent(name) ||
                validationService.containsSuspiciousContent(description)) {
                errors.add("Baris " + rowNumber + ": Konten mencurigakan terdeteksi");
                continue;
            }
            
            // Check required fields with validation
            if (isEmpty(sku)) {
                errors.add("Baris " + rowNumber + ": SKU tidak boleh kosong");
            } else if (!validationService.isValidSku(sku)) {
                errors.add("Baris " + rowNumber + ": Format SKU tidak valid (3-10 karakter, huruf besar dan angka)");
            } else if (skus.contains(sku)) {
                errors.add("Baris " + rowNumber + ": SKU '" + sku + "' duplikat");
            } else {
                skus.add(sku);
            }
            
            if (isEmpty(name)) {
                errors.add("Baris " + rowNumber + ": Nama Produk tidak boleh kosong");
            } else if (!validationService.isValidName(name)) {
                errors.add("Baris " + rowNumber + ": Format nama produk tidak valid");
            }
            
            if (isEmpty(price)) {
                errors.add("Baris " + rowNumber + ": Harga tidak boleh kosong");
            } else if (!validationService.isValidPrice(price)) {
                errors.add("Baris " + rowNumber + ": Format harga tidak valid (0-999,999,999.99)");
            }
            
            if (isEmpty(category)) {
                errors.add("Baris " + rowNumber + ": Kategori tidak boleh kosong");
            } else if (!validationService.isValidCategory(category)) {
                errors.add("Baris " + rowNumber + ": Format kategori tidak valid");
            }
            
            if (isEmpty(stock)) {
                errors.add("Baris " + rowNumber + ": Stok tidak boleh kosong");
            } else if (!validationService.isValidStock(stock)) {
                errors.add("Baris " + rowNumber + ": Format stok tidak valid (0-999,999)");
            }
            
            // Check image file
            if (!isEmpty(imageFileName) && !validationService.isValidFilename(imageFileName)) {
                errors.add("Baris " + rowNumber + ": Format nama file gambar tidak valid");
            } else if (!isEmpty(imageFileName) && !uploadedImages.contains(imageFileName)) {
                errors.add("Baris " + rowNumber + ": File gambar '" + imageFileName + "' tidak ditemukan");
            }
        }
        
        return errors;
    }



    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
