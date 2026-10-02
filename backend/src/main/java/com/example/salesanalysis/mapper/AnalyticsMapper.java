package com.example.salesanalysis.mapper;

import com.example.salesanalysis.dto.*;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AnalyticsMapper {

    BigDecimal sumSales(AnalyticsQueryParam param);

    Long sumQuantity(AnalyticsQueryParam param);

    Long countEffectiveOrders(AnalyticsQueryParam param);

    Long countPlacedOrders(AnalyticsQueryParam param);

    Long countPaidOrders(AnalyticsQueryParam param);

    Long countPaidUsers(AnalyticsQueryParam param);

    Long countRepurchaseUsers(AnalyticsQueryParam param);

    List<TrendPointRow> queryTrend(AnalyticsQueryParam param);

    List<TopProductRow> queryTopProducts(AnalyticsQueryParam param);

    List<LabelCountRow> queryUserGenderDistribution(AnalyticsQueryParam param);

    List<LabelCountRow> queryUserTagDistribution(AnalyticsQueryParam param);

    List<RegionSalesRow> queryGeoDistribution(AnalyticsQueryParam param);

    List<RegionSalesRow> queryDrillDown(AnalyticsQueryParam param);
}
