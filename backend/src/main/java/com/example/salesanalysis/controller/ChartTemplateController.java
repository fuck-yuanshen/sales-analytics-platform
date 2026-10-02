package com.example.salesanalysis.controller;

import com.example.salesanalysis.domain.ChartTemplate;
import com.example.salesanalysis.dto.*;
import com.example.salesanalysis.service.ChartTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chart-templates")
@RequiredArgsConstructor
public class ChartTemplateController {

    private final ChartTemplateService chartTemplateService;

    @GetMapping
    public ApiResponse<List<ChartTemplate>> listTemplates() {
        return ApiResponse.ok(chartTemplateService.listTemplates());
    }

    @GetMapping("/{id}")
    public ApiResponse<ChartTemplate> getTemplate(@PathVariable Long id) {
        return ApiResponse.ok(chartTemplateService.getTemplate(id));
    }

    @PostMapping
    public ApiResponse<ChartTemplate> createTemplate(@RequestBody @Valid ChartTemplateRequest request) {
        return ApiResponse.ok(chartTemplateService.saveTemplate(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ChartTemplate> updateTemplate(@PathVariable Long id, @RequestBody @Valid ChartTemplateRequest request) {
        return ApiResponse.ok(chartTemplateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        chartTemplateService.deleteTemplate(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PostMapping("/recommend")
    public ApiResponse<ChartRecommendResponse> recommend(@RequestBody @Valid ChartRecommendRequest request) {
        return ApiResponse.ok(chartTemplateService.recommend(request));
    }
}

