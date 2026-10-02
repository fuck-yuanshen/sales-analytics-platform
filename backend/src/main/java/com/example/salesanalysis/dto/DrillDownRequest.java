package com.example.salesanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DrillDownRequest {
    @NotBlank(message = "钻取层级不能为空")
    private String level;

    private String parentRegion;

    private AnalyticsFilterRequest filter;
}

