package com.example.salesanalysis.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TrendPointResponse {
    private String period;
    private BigDecimal salesAmount;
    private Long salesQuantity;
}
