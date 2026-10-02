package com.example.salesanalysis.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertRule {
    private Long id;
    private String ruleName;
    private String metricCode;
    private String comparator;
    private BigDecimal threshold;
    private Boolean enabled;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
