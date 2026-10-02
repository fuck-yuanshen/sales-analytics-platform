package com.example.salesanalysis.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GeoDistributionResponse {
    private String regionName;
    private BigDecimal salesAmount;
}
