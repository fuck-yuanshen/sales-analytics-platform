package com.example.salesanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportExportRequest {
    @NotBlank(message = "导出格式不能为空")
    private String format;

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @NotNull(message = "筛选条件不能为空")
    private AnalyticsFilterRequest filter;
}

