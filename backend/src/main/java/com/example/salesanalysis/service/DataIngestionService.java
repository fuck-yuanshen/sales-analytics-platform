package com.example.salesanalysis.service;

import com.example.salesanalysis.domain.BackupRecord;
import com.example.salesanalysis.dto.SyncRequest;
import com.example.salesanalysis.dto.UploadResultResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DataIngestionService {

    UploadResultResponse importOfflineFile(MultipartFile file) throws IOException;

    String syncFromExternal(SyncRequest request);

    String runScheduledSync();

    String runAutoScheduledSync();

    int archiveExpiredData();

    BackupRecord backupData(String comment) throws IOException;

    void restoreBackup(Long backupId) throws IOException;

    List<BackupRecord> listBackups();

    void updateRetentionDays(Integer retentionDays);

    Integer getRetentionDays();

    void updateSyncFrequency(String frequency);

    String getSyncFrequency();
}
