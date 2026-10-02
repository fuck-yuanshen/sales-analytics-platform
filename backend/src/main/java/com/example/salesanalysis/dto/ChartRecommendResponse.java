package com.example.salesanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartRecommendResponse {
    private String chartType;
    private String reason;
}
