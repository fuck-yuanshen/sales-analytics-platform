package com.example.salesanalysis.service.impl;

import com.example.salesanalysis.dto.*;
import com.example.salesanalysis.service.AnalyticsService;
import com.example.salesanalysis.service.ReportService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AnalyticsService analyticsService;

    @Override
    public byte[] exportReport(ReportExportRequest request) {
        String format = request.getFormat().toLowerCase();
        return switch (format) {
            case "excel", "xlsx" -> exportExcel(request.getFilter());
            case "pdf" -> exportPdf(request.getFilter());
            default -> throw new IllegalArgumentException("不支持的导出格式: " + format);
        };
    }

    private byte[] exportExcel(AnalyticsFilterRequest filter) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            AnalyticsSummaryResponse summary = analyticsService.summary(filter);
            List<TrendPointResponse> trend = analyticsService.trend(filter);
            List<TopProductResponse> topProducts = analyticsService.topProducts(filter);

            Sheet summarySheet = workbook.createSheet("汇总指标");
            createSummaryRows(summarySheet, summary);

            Sheet trendSheet = workbook.createSheet("趋势分析");
            createTrendRows(trendSheet, trend);

            Sheet topSheet = workbook.createSheet("商品排行");
            createTopProductRows(topSheet, topProducts);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Excel 导出失败: " + ex.getMessage(), ex);
        }
    }

    private byte[] exportPdf(AnalyticsFilterRequest filter) {
        AnalyticsSummaryResponse summary = analyticsService.summary(filter);
        List<TrendPointResponse> trend = analyticsService.trend(filter);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("销售分析报告"));
            document.add(new Paragraph("销售总额: " + value(summary.getSalesTotal())));
            document.add(new Paragraph("环比变化(%): " + value(summary.getSalesMomChangeRate())));
            document.add(new Paragraph("同比变化(%): " + value(summary.getSalesYoyChangeRate())));
            document.add(new Paragraph("销量: " + summary.getSalesVolume()));
            document.add(new Paragraph("客单价: " + value(summary.getAverageOrderValue())));
            document.add(new Paragraph("复购率(%): " + value(summary.getRepurchaseRate())));
            document.add(new Paragraph("订单转化率(%): " + value(summary.getOrderConversionRate())));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("趋势明细:"));
            for (TrendPointResponse point : trend) {
                document.add(new Paragraph(point.getPeriod() + " => 销售额=" + value(point.getSalesAmount()) + ", 销量=" + point.getSalesQuantity()));
            }
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("PDF 导出失败: " + ex.getMessage(), ex);
        }
    }

    private void createSummaryRows(Sheet sheet, AnalyticsSummaryResponse summary) {
        String[][] rows = {
                {"销售总额", value(summary.getSalesTotal())},
                {"环比变化(%)", value(summary.getSalesMomChangeRate())},
                {"同比变化(%)", value(summary.getSalesYoyChangeRate())},
                {"销量", String.valueOf(summary.getSalesVolume())},
                {"客单价", value(summary.getAverageOrderValue())},
                {"复购率(%)", value(summary.getRepurchaseRate())},
                {"订单转化率(%)", value(summary.getOrderConversionRate())}
        };
        for (int i = 0; i < rows.length; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(rows[i][0]);
            row.createCell(1).setCellValue(rows[i][1]);
        }
    }

    private void createTrendRows(Sheet sheet, List<TrendPointResponse> trend) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("周期");
        header.createCell(1).setCellValue("销售额");
        header.createCell(2).setCellValue("销量");
        for (int i = 0; i < trend.size(); i++) {
            TrendPointResponse point = trend.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(point.getPeriod());
            row.createCell(1).setCellValue(value(point.getSalesAmount()));
            row.createCell(2).setCellValue(point.getSalesQuantity());
        }
    }

    private void createTopProductRows(Sheet sheet, List<TopProductResponse> topProducts) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("商品名称");
        header.createCell(1).setCellValue("销量");
        header.createCell(2).setCellValue("销售额");
        for (int i = 0; i < topProducts.size(); i++) {
            TopProductResponse top = topProducts.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(top.getProductName());
            row.createCell(1).setCellValue(top.getQuantity());
            row.createCell(2).setCellValue(value(top.getSalesAmount()));
        }
    }

    private String value(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }
}

