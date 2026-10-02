package com.example.salesanalysis.mapper;

import com.example.salesanalysis.domain.AlertEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AlertEventMapper {

    @Insert("""
        INSERT INTO alert_events(rule_id, metric_code, metric_value, period_label, severity, message, triggered_at)
        VALUES(#{ruleId}, #{metricCode}, #{metricValue}, #{periodLabel}, #{severity}, #{message}, NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AlertEvent event);

    @Select("SELECT * FROM alert_events ORDER BY id DESC LIMIT 200")
    List<AlertEvent> findLatest();
}
