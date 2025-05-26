package org.eea.orchestrator.scheduling;

import org.apache.commons.lang3.BooleanUtils;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.validation.TaskVO;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    //For now this scheduled task will not do anything but log the cases because this use case might already be resolved
    public void restartLongRunningImportJobs() {
        try {
            List<JobVO> longRunningQueuedJobs = jobService.getJobsByTypeAndStatus(JobTypeEnum.IMPORT, JobStatusEnum.IN_PROGRESS);
            for (JobVO job: longRunningQueuedJobs){
                Long durationOfJob = new Timestamp(System.currentTimeMillis()).getTime() - job.getDateStatusChanged().getTime();
                if(durationOfJob > maxTimeForInProgressImportJobs){
                    Boolean jobRestarted = jobService.restartImportJob(job.getId(), false);
                    LOG.info("When restarting import jobId {} jobRestarted={}", job.getId(), jobRestarted);
                    if(!jobRestarted){
                        //fail job
                    }
                }
            }
            /*
            big data/citus

            if replaceData = true

           call again the endpoint importBigFileData


check what happens with restart task

make sure we don't end up with loop of restarting job
make sure other scheduled tasks don't interfere with this one
if restarted add parameter restarts and increase it
if restarts != null && restarts >0 fail the job
             */











/*
            List<TaskVO> tasks = recordStoreControllerZuul.findImportTasksInProgress();
            String tasksWithEmptyStartingDates = "";
            String tasksWithMaxDuration = "";
            for(TaskVO task : tasks){
                if(task.getStartingDate() == null) {
                    tasksWithEmptyStartingDates += task.getId().toString() + " ";
                    //recordStoreControllerZuul.restartTask(task.getId());
                }
                Long durationInMs = new Date().getTime() - task.getStartingDate().getTime();
                if(durationInMs > maxTimeForInProgressImportTasks) {
                    tasksWithMaxDuration += task.getId().toString() + " ";
                    //recordStoreControllerZuul.restartTask(task.getId());
                }
            }
            if(tasksWithEmptyStartingDates.length() > 0 ){
                LOG.info("Found tasks that are in status IN_PROGRESS but their starting date is empty. The tasks ids are: {}", tasksWithEmptyStartingDates);
            }
            if(tasksWithMaxDuration.length() > 0 ){
                LOG.info("Found tasks that are in status IN_PROGRESS for more than {} ms. The tasks ids are: {}", maxTimeForInProgressImportTasks, tasksWithMaxDuration);
            }*/

        } catch (Exception e) {
            LOG.error("Unexpected error! Error while running scheduled task restartLongRunningImportTasks.", e);
        }
    }
}
