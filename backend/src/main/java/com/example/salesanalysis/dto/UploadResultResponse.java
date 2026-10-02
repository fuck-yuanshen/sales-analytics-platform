package com.example.salesanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResultResponse {
    private int successCount;
    private int failedCount;
    private String message;
}
