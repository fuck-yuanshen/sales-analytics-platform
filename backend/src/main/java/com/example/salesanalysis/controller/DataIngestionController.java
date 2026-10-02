package com.example.salesanalysis.controller;

import com.example.salesanalysis.domain.BackupRecord;
import com.example.salesanalysis.dto.*;
import com.example.salesanalysis.service.DataIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/data-ingestion")
@RequiredArgsConstructor
public class DataIngestionController {

    private final DataIngestionService dataIngestionService;

    @PostMapping("/upload")
    public ApiResponse<UploadResultResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.ok(dataIngestionService.importOfflineFile(file));
    }

    @PostMapping("/sync")
    public ApiResponse<String> sync(@RequestBody @Valid SyncRequest request) {
        return ApiResponse.ok(dataIngestionService.syncFromExternal(request));
    }

    @PostMapping("/sync/scheduled")
    public ApiResponse<String> runScheduledSync() {
        return ApiResponse.ok(dataIngestionService.runScheduledSync());
    }

    @PostMapping("/archive")
    public ApiResponse<Integer> archive() {
        return ApiResponse.ok("归档完成", dataIngestionService.archiveExpiredData());
    }

    @PostMapping("/backup")
    public ApiResponse<BackupRecord> backup(@RequestBody(required = false) BackupRequest request) throws IOException {
        String comment = request == null ? null : request.getComment();
        return ApiResponse.ok(dataIngestionService.backupData(comment));
    }

    @PostMapping("/restore/{backupId}")
    public ApiResponse<Void> restore(@PathVariable Long backupId) throws IOException {
        dataIngestionService.restoreBackup(backupId);
        return ApiResponse.ok("恢复完成", null);
    }

    @GetMapping("/backups")
    public ApiResponse<List<BackupRecord>> listBackups() {
        return ApiResponse.ok(dataIngestionService.listBackups());
    }

    @PutMapping("/retention")
    public ApiResponse<Void> updateRetention(@RequestBody @Valid RetentionPolicyRequest request) {
        dataIngestionService.updateRetentionDays(request.getRetentionDays());
        return ApiResponse.ok("保留策略已更新", null);
    }

    @GetMapping("/retention")
    public ApiResponse<Integer> getRetention() {
        return ApiResponse.ok(dataIngestionService.getRetentionDays());
    }

    @PutMapping("/sync-frequency")
    public ApiResponse<Void> updateSyncFrequency(@RequestBody @Valid SyncFrequencyRequest request) {
        dataIngestionService.updateSyncFrequency(request.getFrequency());
        return ApiResponse.ok("同步频率已更新", null);
    }

    @GetMapping("/sync-frequency")
    public ApiResponse<String> getSyncFrequency() {
        return ApiResponse.ok(dataIngestionService.getSyncFrequency());
    }
}

