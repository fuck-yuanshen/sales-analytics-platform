package com.example.salesanalysis.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnalyticsQueryParam {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> provinces;
    private List<String> cities;
    private List<String> districts;
    private List<String> paymentStatuses;
    private List<String> orderTypes;
    private List<String> userTags;
    private List<String> spendingTiers;
    private String granularity;
    private String parentRegion;
    private String level;
}
