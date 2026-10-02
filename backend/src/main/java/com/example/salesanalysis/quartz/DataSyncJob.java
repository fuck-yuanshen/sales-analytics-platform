package com.example.salesanalysis.quartz;

import com.example.salesanalysis.service.DataIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSyncJob implements Job {

    private final DataIngestionService dataIngestionService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String message = dataIngestionService.runAutoScheduledSync();
        log.info("数据同步任务执行结束: {}", message);
    }
}

