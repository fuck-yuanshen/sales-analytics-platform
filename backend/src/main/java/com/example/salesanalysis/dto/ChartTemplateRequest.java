package com.example.salesanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChartTemplateRequest {
    @NotBlank(message = "模板名称不能为空")
    private String templateName;
    @NotBlank(message = "图表类型不能为空")
    private String chartType;
    @NotBlank(message = "图表标题不能为空")
    private String title;
    private String colorScheme;
    private String axisConfig;
    private String dataFormat;
    private String createdBy;
}

