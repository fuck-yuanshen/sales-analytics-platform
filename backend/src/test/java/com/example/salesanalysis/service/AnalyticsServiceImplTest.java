package com.example.salesanalysis.service;

import com.example.salesanalysis.dto.AnalyticsFilterRequest;
import com.example.salesanalysis.dto.AnalyticsSummaryResponse;
import com.example.salesanalysis.dto.AnalyticsQueryParam;
import com.example.salesanalysis.mapper.AnalyticsMapper;
import com.example.salesanalysis.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private AnalyticsMapper analyticsMapper;

    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        Executor directExecutor = Runnable::run;
        analyticsService = new AnalyticsServiceImpl(analyticsMapper, directExecutor);
    }

    @Test
    void summaryShouldCalculateCoreMetrics() {
        when(analyticsMapper.sumSales(any(AnalyticsQueryParam.class))).thenReturn(new BigDecimal("1000"), new BigDecimal("800"), new BigDecimal("600"));
        when(analyticsMapper.sumQuantity(any(AnalyticsQueryParam.class))).thenReturn(30L);
        when(analyticsMapper.countEffectiveOrders(any(AnalyticsQueryParam.class))).thenReturn(10L);
        when(analyticsMapper.countPlacedOrders(any(AnalyticsQueryParam.class))).thenReturn(12L);
        when(analyticsMapper.countPaidOrders(any(AnalyticsQueryParam.class))).thenReturn(10L);
        when(analyticsMapper.countPaidUsers(any(AnalyticsQueryParam.class))).thenReturn(8L);
        when(analyticsMapper.countRepurchaseUsers(any(AnalyticsQueryParam.class))).thenReturn(2L);

        AnalyticsFilterRequest request = new AnalyticsFilterRequest();
        request.setStartDate(LocalDate.now().minusDays(7));
        request.setEndDate(LocalDate.now());

        AnalyticsSummaryResponse summary = analyticsService.summary(request);

        assertEquals(new BigDecimal("1000"), summary.getSalesTotal());
        assertEquals(new BigDecimal("25.00"), summary.getSalesMomChangeRate());
        assertEquals(new BigDecimal("66.67"), summary.getSalesYoyChangeRate());
        assertEquals(new BigDecimal("100.00"), summary.getAverageOrderValue());
        assertEquals(new BigDecimal("25.00"), summary.getRepurchaseRate());
        assertEquals(new BigDecimal("83.33"), summary.getOrderConversionRate());
    }
}
