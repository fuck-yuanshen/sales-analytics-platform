package com.example.salesanalysis.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertEvent {
    private Long id;
    private Long ruleId;
    private String metricCode;
    private BigDecimal metricValue;
    private String periodLabel;
    private String severity;
    private String message;
    private LocalDateTime triggeredAt;
}
