package com.example.salesanalysis.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RetentionPolicyRequest {
    @Min(value = 1, message = "保留天数最小为1天")
    private Integer retentionDays;
}

