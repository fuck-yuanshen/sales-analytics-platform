package com.example.salesanalysis.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopProductRow {
    private String productName;
    private Long quantity;
    private BigDecimal salesAmount;
}
