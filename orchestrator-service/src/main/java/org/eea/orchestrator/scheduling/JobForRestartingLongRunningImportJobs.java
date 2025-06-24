package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.orchestrator.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.List;

@Component
public class JobForRestartingLongRunningImportJobs {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForRestartingLongRunningImportJobs.class);

    @Value(value = "${scheduling.inProgress.import.task.max.ms.restart}")
    private long maxTimeForInProgressImportJobs;

    @Autowired
    private JobService jobService;

    @Autowired
    DataFlowControllerZuul dataFlowControllerZuul;


    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> restartLongRunningImportJobs(),
                new CronTrigger("0 0 * * * *"));
    }

    /**
     * The job runs every hour. It finds tasks that have been stuck in status IN_PROGRESS for more than a number of hours
     * and changes their status to IN_QUEUE so that they are picked up by the ImportFileTasksScheduler.scheduledConsumer() method
     */
    public void restartLongRunningImportJobs() {
       /* try {
            List<JobVO> longRunningJobs = jobService.getJobsByTypeAndStatus(JobTypeEnum.IMPORT, JobStatusEnum.IN_PROGRESS);
            for (JobVO job: longRunningJobs){
                Long durationOfJob = new Timestamp(System.currentTimeMillis()).getTime() - job.getDateStatusChanged().getTime();
                if(durationOfJob > maxTimeForInProgressImportJobs){
                    jobService.restartImportJob(job.getId(), false);
                }
            }
        } catch (Exception e) {
            LOG.error("Unexpected error! Error while running scheduled task restartLongRunningImportTasks.", e);
        }
        */
    }
}
