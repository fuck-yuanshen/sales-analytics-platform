package com.example.salesanalysis.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnalyticsSummaryResponse {
    private BigDecimal salesTotal;
    private BigDecimal salesMomChangeRate;
    private BigDecimal salesYoyChangeRate;
    private Long salesVolume;
    private BigDecimal averageOrderValue;
    private BigDecimal repurchaseRate;
    private BigDecimal orderConversionRate;
}
