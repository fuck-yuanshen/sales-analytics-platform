package com.example.salesanalysis.service;

import com.example.salesanalysis.domain.AlertEvent;
import com.example.salesanalysis.domain.AlertRule;
import com.example.salesanalysis.dto.AlertRuleRequest;

import java.util.List;

public interface AlertService {

    List<AlertRule> listRules();

    AlertRule createRule(AlertRuleRequest request);

    AlertRule updateRule(Long id, AlertRuleRequest request);

    void deleteRule(Long id);

    List<AlertEvent> listEvents();

    int evaluateRules();
}
