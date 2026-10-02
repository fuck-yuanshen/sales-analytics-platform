package com.example.salesanalysis.controller;

import com.example.salesanalysis.domain.AlertEvent;
import com.example.salesanalysis.domain.AlertRule;
import com.example.salesanalysis.dto.AlertRuleRequest;
import com.example.salesanalysis.dto.ApiResponse;
import com.example.salesanalysis.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/rules")
    public ApiResponse<List<AlertRule>> listRules() {
        return ApiResponse.ok(alertService.listRules());
    }

    @PostMapping("/rules")
    public ApiResponse<AlertRule> createRule(@RequestBody @Valid AlertRuleRequest request) {
        return ApiResponse.ok(alertService.createRule(request));
    }

    @PutMapping("/rules/{id}")
    public ApiResponse<AlertRule> updateRule(@PathVariable Long id, @RequestBody @Valid AlertRuleRequest request) {
        return ApiResponse.ok(alertService.updateRule(id, request));
    }

    @DeleteMapping("/rules/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        alertService.deleteRule(id);
        return ApiResponse.ok("删除成功", null);
    }

    @GetMapping("/events")
    public ApiResponse<List<AlertEvent>> listEvents() {
        return ApiResponse.ok(alertService.listEvents());
    }

    @PostMapping("/evaluate")
    public ApiResponse<Integer> evaluate() {
        return ApiResponse.ok(alertService.evaluateRules());
    }
}

