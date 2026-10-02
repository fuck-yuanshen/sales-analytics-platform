package com.example.salesanalysis.service.impl;

import com.example.salesanalysis.dto.*;
import com.example.salesanalysis.mapper.AnalyticsMapper;
import com.example.salesanalysis.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsMapper analyticsMapper;
    private final Executor analyticsExecutor;

    @Override
    @Cacheable(value = "analytics_summary", key = "#filterRequest.toString()")
    public AnalyticsSummaryResponse summary(AnalyticsFilterRequest filterRequest) {
        AnalyticsQueryParam current = toParam(filterRequest, filterRequest.getStartDate(), filterRequest.getEndDate());

        LocalDate compareStart = filterRequest.getCompareStartDate();
        LocalDate compareEnd = filterRequest.getCompareEndDate();
        if (compareStart == null || compareEnd == null) {
            long days = filterRequest.getEndDate().toEpochDay() - filterRequest.getStartDate().toEpochDay() + 1;
            compareEnd = filterRequest.getStartDate().minusDays(1);
            compareStart = compareEnd.minusDays(days - 1);
        }
        AnalyticsQueryParam compare = toParam(filterRequest, compareStart, compareEnd);

        LocalDate yoyStart = filterRequest.getStartDate().minusYears(1);
        LocalDate yoyEnd = filterRequest.getEndDate().minusYears(1);
        AnalyticsQueryParam yoy = toParam(filterRequest, yoyStart, yoyEnd);

        CompletableFuture<BigDecimal> salesFuture = CompletableFuture.supplyAsync(() -> defaultDecimal(analyticsMapper.sumSales(current)), analyticsExecutor);
        CompletableFuture<Long> quantityFuture = CompletableFuture.supplyAsync(() -> defaultLong(analyticsMapper.sumQuantity(current)), analyticsExecutor);
        CompletableFuture<Long> effectiveOrderFuture = CompletableFuture.supplyAsync(() -> defaultLong(analyticsMapper.countEffectiveOrders(current)), analyticsExecutor);
        CompletableFuture<Long> placedOrderFuture = CompletableFuture.supplyAsync(() -> defaultLong(analyticsMapper.countPlacedOrders(current)), analyticsExecutor);
        CompletableFuture<Long> paidOrderFuture = CompletableFuture.supplyAsync(() -> defaultLong(analyticsMapper.countPaidOrders(current)), analyticsExecutor);
        CompletableFuture<Long> paidUserFuture = CompletableFuture.supplyAsync(() -> defaultLong(analyticsMapper.countPaidUsers(current)), analyticsExecutor);
        CompletableFuture<Long> repeatUserFuture = CompletableFuture.supplyAsync(() -> defaultLong(analyticsMapper.countRepurchaseUsers(current)), analyticsExecutor);
        CompletableFuture<BigDecimal> compareSalesFuture = CompletableFuture.supplyAsync(() -> defaultDecimal(analyticsMapper.sumSales(compare)), analyticsExecutor);
        CompletableFuture<BigDecimal> yoySalesFuture = CompletableFuture.supplyAsync(() -> defaultDecimal(analyticsMapper.sumSales(yoy)), analyticsExecutor);

        CompletableFuture.allOf(salesFuture, quantityFuture, effectiveOrderFuture, placedOrderFuture, paidOrderFuture, paidUserFuture,
                repeatUserFuture, compareSalesFuture, yoySalesFuture).join();

        BigDecimal sales = salesFuture.join();
        BigDecimal compareSales = compareSalesFuture.join();
        BigDecimal yoySales = yoySalesFuture.join();

        AnalyticsSummaryResponse response = new AnalyticsSummaryResponse();
        response.setSalesTotal(sales);
        response.setSalesVolume(quantityFuture.join());
        response.setSalesMomChangeRate(growthRate(sales, compareSales));
        response.setSalesYoyChangeRate(growthRate(sales, yoySales));
        response.setAverageOrderValue(safeDivide(sales, BigDecimal.valueOf(effectiveOrderFuture.join()), 2));
        response.setRepurchaseRate(safeDivide(BigDecimal.valueOf(repeatUserFuture.join()), BigDecimal.valueOf(paidUserFuture.join()), 4)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        response.setOrderConversionRate(safeDivide(BigDecimal.valueOf(paidOrderFuture.join()), BigDecimal.valueOf(placedOrderFuture.join()), 4)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        return response;
    }

    @Override
    @Cacheable(value = "analytics_trend", key = "#filterRequest.toString()")
    public List<TrendPointResponse> trend(AnalyticsFilterRequest filterRequest) {
        AnalyticsQueryParam param = toParam(filterRequest, filterRequest.getStartDate(), filterRequest.getEndDate());
        return analyticsMapper.queryTrend(param).stream().map(row -> {
            TrendPointResponse response = new TrendPointResponse();
            response.setPeriod(row.getPeriod());
            response.setSalesAmount(defaultDecimal(row.getSalesAmount()));
            response.setSalesQuantity(defaultLong(row.getSalesQuantity()));
            return response;
        }).toList();
    }

    @Override
    @Cacheable(value = "analytics_top_products", key = "#filterRequest.toString()")
    public List<TopProductResponse> topProducts(AnalyticsFilterRequest filterRequest) {
        AnalyticsQueryParam param = toParam(filterRequest, filterRequest.getStartDate(), filterRequest.getEndDate());
        return analyticsMapper.queryTopProducts(param).stream().map(row -> {
            TopProductResponse response = new TopProductResponse();
            response.setProductName(row.getProductName());
            response.setQuantity(defaultLong(row.getQuantity()));
            response.setSalesAmount(defaultDecimal(row.getSalesAmount()));
            return response;
        }).toList();
    }

    @Override
    public List<UserDistributionResponse> userGenderDistribution(AnalyticsFilterRequest filterRequest) {
        AnalyticsQueryParam param = toParam(filterRequest, filterRequest.getStartDate(), filterRequest.getEndDate());
        return analyticsMapper.queryUserGenderDistribution(param).stream().map(row -> {
            UserDistributionResponse response = new UserDistributionResponse();
            response.setLabel(row.getLabel());
            response.setValue(defaultLong(row.getValue()));
            return response;
        }).toList();
    }

    @Override
    public List<UserDistributionResponse> userTagDistribution(AnalyticsFilterRequest filterRequest) {
        AnalyticsQueryParam param = toParam(filterRequest, filterRequest.getStartDate(), filterRequest.getEndDate());
        return analyticsMapper.queryUserTagDistribution(param).stream().map(row -> {
            UserDistributionResponse response = new UserDistributionResponse();
            response.setLabel(row.getLabel());
            response.setValue(defaultLong(row.getValue()));
            return response;
        }).toList();
    }

    @Override
    public List<GeoDistributionResponse> geoDistribution(String level, AnalyticsFilterRequest filterRequest) {
        AnalyticsQueryParam param = toParam(filterRequest, filterRequest.getStartDate(), filterRequest.getEndDate());
        param.setLevel(level == null ? "PROVINCE" : level.toUpperCase());
        return analyticsMapper.queryGeoDistribution(param).stream().map(row -> {
            GeoDistributionResponse response = new GeoDistributionResponse();
            response.setRegionName(row.getRegionName());
            response.setSalesAmount(defaultDecimal(row.getSalesAmount()));
            return response;
        }).toList();
    }

    @Override
    public List<DrillDownNodeResponse> drillDown(DrillDownRequest request) {
        AnalyticsFilterRequest filter = request.getFilter();
        if (filter == null) {
            filter = defaultFilter();
        }
        AnalyticsQueryParam param = toParam(filter, filter.getStartDate(), filter.getEndDate());
        param.setLevel(request.getLevel().toUpperCase());
        param.setParentRegion(request.getParentRegion());
        return analyticsMapper.queryDrillDown(param).stream().map(row -> {
            DrillDownNodeResponse response = new DrillDownNodeResponse();
            response.setName(row.getRegionName());
            response.setValue(defaultDecimal(row.getSalesAmount()));
            return response;
        }).toList();
    }

    @Override
    public BigDecimal resolveMetricValue(String metricCode, AnalyticsFilterRequest filterRequest) {
        AnalyticsSummaryResponse summary = summary(filterRequest);
        return switch (metricCode) {
            case "SALES_MOM_CHANGE" -> summary.getSalesMomChangeRate();
            case "SALES_TOTAL" -> summary.getSalesTotal();
            case "REPURCHASE_RATE" -> summary.getRepurchaseRate();
            case "ORDER_CONVERSION_RATE" -> summary.getOrderConversionRate();
            case "AVERAGE_ORDER_VALUE" -> summary.getAverageOrderValue();
            default -> BigDecimal.ZERO;
        };
    }

    private AnalyticsFilterRequest defaultFilter() {
        AnalyticsFilterRequest request = new AnalyticsFilterRequest();
        request.setStartDate(LocalDate.now().minusDays(30));
        request.setEndDate(LocalDate.now());
        return request;
    }

    private AnalyticsQueryParam toParam(AnalyticsFilterRequest request, LocalDate startDate, LocalDate endDate) {
        AnalyticsQueryParam param = new AnalyticsQueryParam();
        param.setStartTime(startDate.atStartOfDay());
        param.setEndTime(endDate.plusDays(1).atStartOfDay());
        param.setGranularity((request.getGranularity() == null ? "DAY" : request.getGranularity().name()));
        param.setProvinces(request.getProvinces());
        param.setCities(request.getCities());
        param.setDistricts(request.getDistricts());
        param.setPaymentStatuses(request.getPaymentStatuses());
        param.setOrderTypes(request.getOrderTypes());
        param.setUserTags(request.getUserTags());
        param.setSpendingTiers(request.getSpendingTiers());
        return param;
    }

    private BigDecimal growthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator, int scale) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, scale, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long defaultLong(Long value) {
        return Objects.requireNonNullElse(value, 0L);
    }
}
