package com.example.salesanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SyncFrequencyRequest {

    @NotBlank(message = "同步频率不能为空")
    @Pattern(regexp = "(?i)HOURLY|DAILY|WEEKLY", message = "同步频率仅支持 HOURLY、DAILY、WEEKLY")
    private String frequency;
}

