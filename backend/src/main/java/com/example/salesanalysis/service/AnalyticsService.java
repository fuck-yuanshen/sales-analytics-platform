package com.example.salesanalysis.service;

import com.example.salesanalysis.dto.*;

import java.math.BigDecimal;
import java.util.List;

public interface AnalyticsService {

    AnalyticsSummaryResponse summary(AnalyticsFilterRequest filterRequest);

    List<TrendPointResponse> trend(AnalyticsFilterRequest filterRequest);

    List<TopProductResponse> topProducts(AnalyticsFilterRequest filterRequest);

    List<UserDistributionResponse> userGenderDistribution(AnalyticsFilterRequest filterRequest);

    List<UserDistributionResponse> userTagDistribution(AnalyticsFilterRequest filterRequest);

    List<GeoDistributionResponse> geoDistribution(String level, AnalyticsFilterRequest filterRequest);

    List<DrillDownNodeResponse> drillDown(DrillDownRequest request);

    BigDecimal resolveMetricValue(String metricCode, AnalyticsFilterRequest filterRequest);
}
