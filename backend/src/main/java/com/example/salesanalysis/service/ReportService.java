package com.example.salesanalysis.service;

import com.example.salesanalysis.dto.ReportExportRequest;

public interface ReportService {

    byte[] exportReport(ReportExportRequest request);
}
