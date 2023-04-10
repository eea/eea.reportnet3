package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.validation.TaskVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class JobForHandlingQueuedReleaseTasks {

    private static final Logger LOG = LoggerFactory.getLogger(JobForHandlingQueuedReleaseTasks.class);

    @Autowired
    private RecordStoreControllerZuul recordStoreControllerZuul;
    @Autowired
    private ProcessControllerZuul processControllerZuul;
    @Autowired
    private ValidationControllerZuul validationControllerZuul;
    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> handleQueuedReleaseTasks(),
                new CronTrigger("0 0 * * * *"));
    }

    /**
     * The job runs every hour. It finds in_progress release processes with queued jobs and for every process if there are no in_progress tasks, then it sets the first queued task of the process
     * to status=IN_PROGRESS and its date_start equal to the create_date, so that the cron job JobForRestartingReleaseTasks will grab it and execute it.
     */
    public void handleQueuedReleaseTasks() {
        try {
            //Get in_progress release processes that have queued tasks
            List<String> processes = processControllerZuul.findProcessIdsByTypeAndStatusAndTaskStatus(ProcessTypeEnum.RELEASE.toString(), ProcessStatusEnum.IN_PROGRESS.toString(), ProcessStatusEnum.IN_QUEUE.toString());

            if (processes.size() > 0) {
                processes.forEach(process -> {
                    try {
                        Integer inProgressTasks = validationControllerZuul.findTasksCountByProcessIdAndStatusIn(process, Arrays.asList(ProcessStatusEnum.IN_PROGRESS.toString()));
                        if (inProgressTasks == 0) {
                            List<TaskVO> tasks = dataSetControllerZuul.findTasksByProcessIdAndStatusIn(process, Arrays.asList(ProcessStatusEnum.IN_QUEUE));
                            tasks.sort(Comparator.comparing(t -> t.getId()));
                            TaskVO taskToUpdate = tasks.get(0);
                            if (taskToUpdate.getStatus().equals(ProcessStatusEnum.IN_QUEUE)) {
                                taskToUpdate.setStatus(ProcessStatusEnum.IN_PROGRESS);
                                taskToUpdate.setStartingDate(taskToUpdate.getCreateDate());
                                recordStoreControllerZuul.saveTask(taskToUpdate);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error handling queued release task for process {}", process, e);
                    }
                });
            }
        } catch (Exception e) {
            LOG.error("Error while handling queued release tasks " + e);
        }
    }
}




















