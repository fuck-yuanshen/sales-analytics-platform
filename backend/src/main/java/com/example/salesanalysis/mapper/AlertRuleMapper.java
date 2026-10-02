package com.example.salesanalysis.mapper;

import com.example.salesanalysis.domain.AlertRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AlertRuleMapper {

    @Select("SELECT * FROM alert_rules ORDER BY id DESC")
    List<AlertRule> findAll();

    @Select("SELECT * FROM alert_rules WHERE enabled = 1 ORDER BY id DESC")
    List<AlertRule> findEnabledRules();

    @Insert("""
        INSERT INTO alert_rules(rule_name, metric_code, comparator, threshold, enabled, description, created_at, updated_at)
        VALUES(#{ruleName}, #{metricCode}, #{comparator}, #{threshold}, #{enabled}, #{description}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AlertRule rule);

    @Update("""
        UPDATE alert_rules
        SET rule_name = #{ruleName}, metric_code = #{metricCode}, comparator = #{comparator}, threshold = #{threshold},
            enabled = #{enabled}, description = #{description}, updated_at = NOW()
        WHERE id = #{id}
        """)
    int update(AlertRule rule);

    @Delete("DELETE FROM alert_rules WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
