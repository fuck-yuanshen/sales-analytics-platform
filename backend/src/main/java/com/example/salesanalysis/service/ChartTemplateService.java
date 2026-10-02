package com.example.salesanalysis.service;

import com.example.salesanalysis.domain.ChartTemplate;
import com.example.salesanalysis.dto.ChartRecommendRequest;
import com.example.salesanalysis.dto.ChartRecommendResponse;
import com.example.salesanalysis.dto.ChartTemplateRequest;

import java.util.List;

public interface ChartTemplateService {

    List<ChartTemplate> listTemplates();

    ChartTemplate getTemplate(Long id);

    ChartTemplate saveTemplate(ChartTemplateRequest request);

    ChartTemplate updateTemplate(Long id, ChartTemplateRequest request);

    void deleteTemplate(Long id);

    ChartRecommendResponse recommend(ChartRecommendRequest request);
}
