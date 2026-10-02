package com.example.salesanalysis.controller;

import com.example.salesanalysis.dto.ApiResponse;
import com.example.salesanalysis.dto.ReportExportRequest;
import com.example.salesanalysis.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody @Valid ReportExportRequest request) {
        byte[] data = reportService.exportReport(request);
        String format = request.getFormat().equalsIgnoreCase("pdf") ? "pdf" : "xlsx";
        String contentType = request.getFormat().equalsIgnoreCase("pdf")
                ? MediaType.APPLICATION_PDF_VALUE
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + request.getFileName() + "." + format + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.ok("报表模块运行正常");
    }
}

