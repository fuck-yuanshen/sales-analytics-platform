package com.example.salesanalysis.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String sku;
    private String productName;
    private String category;
    private BigDecimal unitPrice;
    private LocalDateTime createdAt;
}
