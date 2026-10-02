package com.example.salesanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChartRecommendRequest {
    @NotBlank(message = "数据类型不能为空")
    private String dataType;
}

