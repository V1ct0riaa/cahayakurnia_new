package cahayakurnia.cahayakurnia.components;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;

import cahayakurnia.cahayakurnia.service.ProductService;
import cahayakurnia.cahayakurnia.model.Product;

import java.math.BigDecimal;
import java.time.Duration;

@Configuration
public class Appconfig {
    @Bean
    ApplicationRunner seedProducts(ProductService productService) {
        return args -> {
            if (productService.getTotalProductCount() == 0) {
                Product p = new Product();
                p.setSku("SKU-001");
                p.setName("Bor Listrik");
                p.setDescription("Bor listrik serbaguna");
                p.setPrice(new BigDecimal("350000"));
                p.setStock(10);
                productService.saveProduct(p);
            }
        };
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(20))
                .setReadTimeout(Duration.ofSeconds(60))
                .build();
    }


}