package com.example.salesanalysis.controller;

import com.example.salesanalysis.dto.*;
import com.example.salesanalysis.service.AnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/summary")
    public ApiResponse<AnalyticsSummaryResponse> summary(@RequestBody @Valid AnalyticsFilterRequest request) {
        return ApiResponse.ok(analyticsService.summary(request));
    }

    @PostMapping("/trend")
    public ApiResponse<List<TrendPointResponse>> trend(@RequestBody @Valid AnalyticsFilterRequest request) {
        return ApiResponse.ok(analyticsService.trend(request));
    }

    @PostMapping("/top-products")
    public ApiResponse<List<TopProductResponse>> topProducts(@RequestBody @Valid AnalyticsFilterRequest request) {
        return ApiResponse.ok(analyticsService.topProducts(request));
    }

    @PostMapping("/user-distribution/gender")
    public ApiResponse<List<UserDistributionResponse>> genderDistribution(@RequestBody @Valid AnalyticsFilterRequest request) {
        return ApiResponse.ok(analyticsService.userGenderDistribution(request));
    }

    @PostMapping("/user-distribution/tag")
    public ApiResponse<List<UserDistributionResponse>> tagDistribution(@RequestBody @Valid AnalyticsFilterRequest request) {
        return ApiResponse.ok(analyticsService.userTagDistribution(request));
    }

    @PostMapping("/geo/{level}")
    public ApiResponse<List<GeoDistributionResponse>> geoDistribution(@PathVariable String level,
                                                                      @RequestBody @Valid AnalyticsFilterRequest request) {
        return ApiResponse.ok(analyticsService.geoDistribution(level, request));
    }

    @PostMapping("/drill-down")
    public ApiResponse<List<DrillDownNodeResponse>> drillDown(@RequestBody @Valid DrillDownRequest request) {
        return ApiResponse.ok(analyticsService.drillDown(request));
    }
}
