package org.eea.orchestrator.scheduling;

import org.eea.orchestrator.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class JobForCleanupOfFinishedJobs {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForCleanupOfFinishedJobs.class);

    @Autowired
    private JobService jobService;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> cleanupFinishedJobs(),
                new CronTrigger("0 0 0 * * *"));
    }

    /**
     * The job runs every day. It finds jobs that have status FINISHED, REFUSED, CANCELED, CANCELED_BY_ADMIN or FAILED for more than a day
     * and removes them from the JOBS table
     */
    public void cleanupFinishedJobs() {
        LOG.info("Running scheduled task cleanupFinishedJobs");
        try {
            jobService.deleteFinishedJobsBasedOnDuration();
        } catch (Exception e) {
            LOG.error("Unexpected error! Error while running scheduled task cleanupFinishedJobs. Message: ", e);
        }
    }
}
