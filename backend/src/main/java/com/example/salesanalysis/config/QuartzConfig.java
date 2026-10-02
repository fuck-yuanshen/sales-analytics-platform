package com.example.salesanalysis.config;

import com.example.salesanalysis.quartz.AlertMonitorJob;
import com.example.salesanalysis.quartz.DataSyncJob;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail dataSyncJobDetail() {
        return newJob(DataSyncJob.class)
                .withIdentity("dataSyncJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger dataSyncTrigger() {
        return newTrigger()
                .forJob(dataSyncJobDetail())
                .withIdentity("dataSyncTrigger")
                .withSchedule(cronSchedule("0 0 * * * ?"))
                .build();
    }

    @Bean
    public JobDetail alertMonitorJobDetail() {
        return newJob(AlertMonitorJob.class)
                .withIdentity("alertMonitorJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger alertMonitorTrigger() {
        return newTrigger()
                .forJob(alertMonitorJobDetail())
                .withIdentity("alertMonitorTrigger")
                .withSchedule(cronSchedule("0 */30 * * * ?"))
                .build();
    }
}
