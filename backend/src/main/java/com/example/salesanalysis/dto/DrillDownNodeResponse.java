package com.example.salesanalysis.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DrillDownNodeResponse {
    private String name;
    private BigDecimal value;
}
