package com.example.salesanalysis.quartz;

import com.example.salesanalysis.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertMonitorJob implements Job {

    private final AlertService alertService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int count = alertService.evaluateRules();
        log.info("预警监控任务执行结束，触发事件数: {}", count);
    }
}

