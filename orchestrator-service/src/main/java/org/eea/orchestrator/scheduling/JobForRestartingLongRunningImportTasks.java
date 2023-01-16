package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.validation.TaskVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JobForRestartingLongRunningImportTasks {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForRestartingLongRunningImportTasks.class);

    @Value(value = "${scheduling.inProgress.import.task.max.hours}")
    private long maxHoursForInProgressImportTasks;

    @Autowired
    private RecordStoreControllerZuul recordStoreControllerZuul;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> restartLongRunningImportTasks(),
                new CronTrigger("0 */2 * * * *"));
    }

    /**
     * The job runs every 2 minutes. It finds tasks that have been stuck in status IN_PROGRESS for more than a number of hours
     * and changes their status to IN_QUEUE so that they are picked up by the ImportFileTasksScheduler.scheduledConsumer() method
     */
    //For now this scheduled task will not do anything but log the cases because this use case might already be resolved
    public void restartLongRunningImportTasks() {
        try {
            List<TaskVO> tasks = recordStoreControllerZuul.findImportTasksInProgress();
            String tasksWithEmptyStartingDates = "";
            String tasksWithMaxDuration = "";
            for(TaskVO task : tasks){
                if(task.getStartingDate() == null) {
                    tasksWithEmptyStartingDates += task.getId().toString() + " ";
                    //recordStoreControllerZuul.restartTask(task.getId());
                }
                Long durationInMs = new Date().getTime() - task.getStartingDate().getTime();
                long durationInHours = TimeUnit.MILLISECONDS.toHours(durationInMs);
                if(durationInHours > maxHoursForInProgressImportTasks) {
                    tasksWithMaxDuration += task.getId().toString() + " ";
                    //recordStoreControllerZuul.restartTask(task.getId());
                }
            }
            if(tasksWithEmptyStartingDates.length() > 0 ){
                LOG.info("Found tasks that are in status IN_PROGRESS but their starting date is empty. The tasks ids are: {}", tasksWithEmptyStartingDates);
            }
            if(tasksWithMaxDuration.length() > 0 ){
                LOG.info("Found tasks that are in status IN_PROGRESS for more than {} hours. The tasks ids are: {}", maxHoursForInProgressImportTasks, tasksWithEmptyStartingDates);
            }

        } catch (Exception e) {
            LOG.error("Unexpected error! Error while running scheduled task restartLongRunningImportTasks. Message: {}", e.getMessage());
        }
    }
}
