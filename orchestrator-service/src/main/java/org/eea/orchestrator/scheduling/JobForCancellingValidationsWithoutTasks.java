package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class JobForCancellingValidationsWithoutTasks {

    @Value(value = "${scheduling.inProgress.validation.process.without.task.max.time}")
    private long maxTimeInMinutesForInProgressValidationWithoutTasks;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForCancellingValidationsWithoutTasks.class);

    @Autowired
    private ProcessControllerZuul processControllerZuul;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> cancelInProgressValidationsWithoutTasks(),
                new CronTrigger("0 0 * * * *"));
    }

    /**
     * The job runs every hour. It finds validation processes that have status=IN_PROGRESS for more than maxTimeInMinutesForInProgressValidationWithoutTasks
     * and have no tasks created and sets their status to CANCELED
     */
    public void cancelInProgressValidationsWithoutTasks() {
        try {
            List<ProcessVO> processesInProgress = processControllerZuul.listInProgressValidationProcessesThatExceedTime(maxTimeInMinutesForInProgressValidationWithoutTasks);
            if (processesInProgress.size() > 0) {
                LOG.info("Cancelling processes " + processesInProgress);
                processesInProgress.stream().forEach(processVO -> {
                    try {
                        LOG.info("Updating validation process to status CANCELLED for processId", processVO.getProcessId());
                        processControllerZuul.updateProcess(processVO.getDatasetId(), processVO.getDataflowId(),
                                ProcessStatusEnum.CANCELED, ProcessTypeEnum.VALIDATION, processVO.getProcessId(),
                                SecurityContextHolder.getContext().getAuthentication().getName(), processVO.getPriority(), processVO.isReleased());
                        LOG.info("Updated validation process to status CANCELLED for processId", processVO.getProcessId());

                    } catch (Exception e) {
                        LOG.error("Error while running scheduled task cancelInProgressValidationsWithoutTasks for processId " + processVO.getProcessId());
                    }
                });
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled task cancelInProgressValidationsWithoutTasks " + e.getMessage());
        }
    }
}


















