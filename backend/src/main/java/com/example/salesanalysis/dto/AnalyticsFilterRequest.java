package com.example.salesanalysis.dto;

import com.example.salesanalysis.enums.TimeGranularity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AnalyticsFilterRequest {
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private LocalDate compareStartDate;
    private LocalDate compareEndDate;

    private TimeGranularity granularity = TimeGranularity.DAY;

    private List<String> provinces;
    private List<String> cities;
    private List<String> districts;
    private List<String> paymentStatuses;
    private List<String> orderTypes;
    private List<String> userTags;
    private List<String> spendingTiers;
}

