package com.example.salesanalysis.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChartTemplate {
    private Long id;
    private String templateName;
    private String chartType;
    private String title;
    private String colorScheme;
    private String axisConfig;
    private String dataFormat;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
