package com.example.salesanalysis.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BackupRecord {
    private Long id;
    private String fileName;
    private String filePath;
    private LocalDateTime backupAt;
    private String comment;
}
