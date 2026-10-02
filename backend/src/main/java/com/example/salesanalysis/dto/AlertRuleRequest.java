package com.example.salesanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertRuleRequest {
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;
    @NotBlank(message = "指标编码不能为空")
    private String metricCode;
    @NotBlank(message = "比较符不能为空")
    private String comparator;
    @NotNull(message = "阈值不能为空")
    private BigDecimal threshold;
    private Boolean enabled;
    private String description;
}

