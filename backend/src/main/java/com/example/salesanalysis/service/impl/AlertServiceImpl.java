package com.example.salesanalysis.service.impl;

import com.example.salesanalysis.domain.AlertEvent;
import com.example.salesanalysis.domain.AlertRule;
import com.example.salesanalysis.dto.AlertRuleRequest;
import com.example.salesanalysis.dto.AnalyticsFilterRequest;
import com.example.salesanalysis.mapper.AlertEventMapper;
import com.example.salesanalysis.mapper.AlertRuleMapper;
import com.example.salesanalysis.service.AlertService;
import com.example.salesanalysis.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertEventMapper alertEventMapper;
    private final AnalyticsService analyticsService;

    @Override
    public List<AlertRule> listRules() {
        return alertRuleMapper.findAll();
    }

    @Override
    @Transactional
    public AlertRule createRule(AlertRuleRequest request) {
        AlertRule rule = toEntity(request);
        if (rule.getEnabled() == null) {
            rule.setEnabled(Boolean.TRUE);
        }
        alertRuleMapper.insert(rule);
        return rule;
    }

    @Override
    @Transactional
    public AlertRule updateRule(Long id, AlertRuleRequest request) {
        AlertRule rule = toEntity(request);
        rule.setId(id);
        if (rule.getEnabled() == null) {
            rule.setEnabled(Boolean.TRUE);
        }
        alertRuleMapper.update(rule);
        return rule;
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        alertRuleMapper.deleteById(id);
    }

    @Override
    public List<AlertEvent> listEvents() {
        return alertEventMapper.findLatest();
    }

    @Override
    @Transactional
    public int evaluateRules() {
        List<AlertRule> rules = alertRuleMapper.findEnabledRules();
        AnalyticsFilterRequest filter = new AnalyticsFilterRequest();
        filter.setStartDate(LocalDate.now().minusDays(7));
        filter.setEndDate(LocalDate.now());

        int triggeredCount = 0;
        for (AlertRule rule : rules) {
            BigDecimal value = analyticsService.resolveMetricValue(rule.getMetricCode(), filter);
            if (match(rule.getComparator(), value, rule.getThreshold())) {
                AlertEvent event = new AlertEvent();
                event.setRuleId(rule.getId());
                event.setMetricCode(rule.getMetricCode());
                event.setMetricValue(value);
                event.setPeriodLabel("最近7天");
                event.setSeverity(resolveSeverity(value, rule.getThreshold()));
                event.setMessage(buildMessage(rule, value));
                alertEventMapper.insert(event);
                triggeredCount++;
            }
        }
        return triggeredCount;
    }

    private AlertRule toEntity(AlertRuleRequest request) {
        AlertRule rule = new AlertRule();
        rule.setRuleName(request.getRuleName());
        rule.setMetricCode(request.getMetricCode());
        rule.setComparator(request.getComparator());
        rule.setThreshold(request.getThreshold());
        rule.setEnabled(request.getEnabled());
        rule.setDescription(request.getDescription());
        return rule;
    }

    private boolean match(String comparator, BigDecimal value, BigDecimal threshold) {
        if (value == null || threshold == null) {
            return false;
        }
        return switch (comparator) {
            case "GREATER_THAN" -> value.compareTo(threshold) > 0;
            case "GREATER_EQUAL" -> value.compareTo(threshold) >= 0;
            case "LESS_THAN" -> value.compareTo(threshold) < 0;
            case "LESS_EQUAL" -> value.compareTo(threshold) <= 0;
            default -> false;
        };
    }

    private String resolveSeverity(BigDecimal value, BigDecimal threshold) {
        BigDecimal delta = value.subtract(threshold).abs();
        if (delta.compareTo(BigDecimal.valueOf(30)) > 0) {
            return "高";
        }
        if (delta.compareTo(BigDecimal.valueOf(10)) > 0) {
            return "中";
        }
        return "低";
    }

    private String buildMessage(AlertRule rule, BigDecimal value) {
        String description = rule.getDescription() == null ? "" : rule.getDescription() + " | ";
        return description + "规则[" + rule.getRuleName() + "]已触发，指标=" + rule.getMetricCode() + "，当前值=" + value;
    }
}

