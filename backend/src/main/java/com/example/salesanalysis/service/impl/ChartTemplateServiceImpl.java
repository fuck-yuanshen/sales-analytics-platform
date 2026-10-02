package com.example.salesanalysis.service.impl;

import com.example.salesanalysis.domain.ChartTemplate;
import com.example.salesanalysis.dto.ChartRecommendRequest;
import com.example.salesanalysis.dto.ChartRecommendResponse;
import com.example.salesanalysis.dto.ChartTemplateRequest;
import com.example.salesanalysis.mapper.ChartTemplateMapper;
import com.example.salesanalysis.service.ChartTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChartTemplateServiceImpl implements ChartTemplateService {

    private final ChartTemplateMapper chartTemplateMapper;

    @Override
    public List<ChartTemplate> listTemplates() {
        return chartTemplateMapper.findAll();
    }

    @Override
    public ChartTemplate getTemplate(Long id) {
        return chartTemplateMapper.findById(id);
    }

    @Override
    @Transactional
    public ChartTemplate saveTemplate(ChartTemplateRequest request) {
        ChartTemplate template = toEntity(request);
        chartTemplateMapper.insert(template);
        return template;
    }

    @Override
    @Transactional
    public ChartTemplate updateTemplate(Long id, ChartTemplateRequest request) {
        ChartTemplate template = toEntity(request);
        template.setId(id);
        chartTemplateMapper.update(template);
        return template;
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        chartTemplateMapper.deleteById(id);
    }

    @Override
    public ChartRecommendResponse recommend(ChartRecommendRequest request) {
        String dataType = request.getDataType().toUpperCase();
        return switch (dataType) {
            case "TREND" -> new ChartRecommendResponse("LINE", "趋势类数据适合折线图或面积图");
            case "RANK" -> new ChartRecommendResponse("BAR", "排行类对比更适合柱状图");
            case "PROPORTION" -> new ChartRecommendResponse("PIE", "占比关系适合饼图或环形图");
            case "GEO" -> new ChartRecommendResponse("MAP", "区域数据适合地图或热力可视化");
            default -> new ChartRecommendResponse("TABLE", "未知数据类型时建议先使用表格展示");
        };
    }

    private ChartTemplate toEntity(ChartTemplateRequest request) {
        ChartTemplate template = new ChartTemplate();
        template.setTemplateName(request.getTemplateName());
        template.setChartType(request.getChartType());
        template.setTitle(request.getTitle());
        template.setColorScheme(request.getColorScheme());
        template.setAxisConfig(request.getAxisConfig());
        template.setDataFormat(request.getDataFormat());
        template.setCreatedBy(request.getCreatedBy());
        return template;
    }
}

