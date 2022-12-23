package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.orchestrator.service.JobProcessService;
import org.eea.orchestrator.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.List;

@Component
public class JobForCancellingValidationsWithoutTasks {

    @Value(value = "${scheduling.inProgress.validation.process.without.task.max.time}")
    private long maxTimeInMinutesForInProgressValidationWithoutTasks;

    /**
     * The admin user.
     */
    @Value("${eea.keycloak.admin.user}")
    private String adminUser;

    /**
     * The admin pass.
     */
    @Value("${eea.keycloak.admin.password}")
    private String adminPass;

    private static final String BEARER = "Bearer ";

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForCancellingValidationsWithoutTasks.class);

    @Autowired
    private ProcessControllerZuul processControllerZuul;
    @Autowired
    private ValidationControllerZuul validationControllerZuul;
    @Autowired
    private JobProcessService jobProcessService;
    @Autowired
    private JobService jobService;
    @Autowired
    private UserManagementControllerZull userManagementControllerZull;

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
            LOG.info("Running scheduled job cancelInProgressValidationsWithoutTasks");
            List<ProcessVO> processesInProgress = processControllerZuul.listInProgressValidationProcessesThatExceedTime(maxTimeInMinutesForInProgressValidationWithoutTasks);
            if (processesInProgress.size() > 0) {
                processesInProgress.stream().forEach(processVO -> {
                    try {
                        List<BigInteger> tasks = validationControllerZuul.findTasksByProcessId(processVO.getProcessId());
                        if (tasks.size()==0) {
                            LOG.info("Cancelling processe " + processVO);
                            LOG.info("Updating validation process to status CANCELLED for processId", processVO.getProcessId());
                            processControllerZuul.updateProcess(processVO.getDatasetId(), processVO.getDataflowId(),
                                    ProcessStatusEnum.CANCELED, ProcessTypeEnum.VALIDATION, processVO.getProcessId(),
                                    processVO.getUser(), processVO.getPriority(), processVO.isReleased());
                            LOG.info("Updated validation process to status CANCELLED for processId", processVO.getProcessId());
                            TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            validationControllerZuul.deleteLocksToReleaseProcess(processVO.getDatasetId());
                            LOG.info("Locks removed for canceled process {}, datasetId {}", processVO.getProcessId(), processVO.getDatasetId());
                            Long jobId = jobProcessService.findJobIdByProcessId(processVO.getProcessId());
                            if (jobId!=null) {
                                jobService.updateJobStatus(jobId, JobStatusEnum.CANCELED);
                            }
                            LOG.info("Job cancelled for canceled process {}, datasetId {}", processVO.getProcessId(), processVO.getDatasetId());
                        }
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


















