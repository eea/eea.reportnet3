package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.orchestrator.service.JobProcessService;
import org.eea.orchestrator.service.JobService;
import org.eea.utils.LiteralConstants;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JobForCancellingValidationsAndReleasesWithoutTasks {

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
    private static final Logger LOG = LoggerFactory.getLogger(JobForCancellingValidationsAndReleasesWithoutTasks.class);

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
    @Autowired
    private KafkaSenderUtils kafkaSenderUtils;
    @Autowired
    private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> cancelInProgressValidationsAndReleasesWithoutTasks(),
                new CronTrigger("0 0 * * * *"));
    }

    /**
     * The job runs every hour. It finds validation processes that have status=IN_PROGRESS for more than maxTimeInMinutesForInProgressValidationWithoutTasks
     * and have no tasks created and sets their status to CANCELED
     */
    public void cancelInProgressValidationsAndReleasesWithoutTasks() {
        try {
            LOG.info("Running scheduled job cancelInProgressValidationsWithoutTasks");
            List<ProcessVO> processesInProgress = processControllerZuul.listProcessesThatExceedTime(Arrays.asList(ProcessTypeEnum.VALIDATION.toString(),ProcessTypeEnum.RELEASE.toString()), ProcessStatusEnum.IN_PROGRESS.toString(), maxTimeInMinutesForInProgressValidationWithoutTasks);
            if (processesInProgress.size() > 0) {
                for (ProcessVO processVO : processesInProgress) {
                    try {
                        List<BigInteger> tasks = validationControllerZuul.findTasksByProcessId(processVO.getProcessId());
                        if (tasks.size()==0) {
                            Long jobId = jobProcessService.findJobIdByProcessId(processVO.getProcessId());
                            JobVO jobVO = jobService.findById(jobId);
                            if (jobVO.getJobStatus().equals(JobStatusEnum.CANCELED)) {
                                continue;
                            }
                            LOG.info("Cancelling process {}", processVO.getProcessId());
                            LOG.info("Updating validation process to status CANCELED for processId {}", processVO.getProcessId());
                            processControllerZuul.updateProcess(processVO.getDatasetId(), processVO.getDataflowId(),
                                    ProcessStatusEnum.CANCELED, ProcessTypeEnum.valueOf(processVO.getProcessType()), processVO.getProcessId(),
                                    processVO.getUser(), processVO.getPriority(), processVO.isReleased());
                            LOG.info("Updated validation process to status CANCELED for processId {}", processVO.getProcessId());
                            TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            validationControllerZuul.deleteLocksToReleaseProcess(processVO.getDatasetId());
                            LOG.info("Locks removed for canceled process {}, datasetId {}", processVO.getProcessId(), processVO.getDatasetId());
                            if (jobVO.isRelease()) {
                                List<String> jobProcesses = jobProcessService.findProcessesByJobId(jobId);
                                jobProcesses.remove(processVO.getProcessId());
                                for (String processId : jobProcesses) {
                                    ProcessVO process = processControllerZuul.findById(processId);
                                    if (!process.getStatus().equals(ProcessStatusEnum.FINISHED.toString()) && !process.getStatus().equals(ProcessStatusEnum.CANCELED.toString())) {
                                        LOG.info("Cancelling process {}", processVO.getProcessId());
                                        LOG.info("Cancelling running validation tasks for process {}", process.getProcessId());
                                        validationControllerZuul.cancelRunningProcessTasks(process.getProcessId());
                                        LOG.info("Cancelled running validation tasks for process {}", process.getProcessId());
                                        LOG.info("Updating validation process to status CANCELED for processId {}", processId);
                                        processControllerZuul.updateProcess(process.getDatasetId(), process.getDataflowId(),
                                                ProcessStatusEnum.CANCELED, ProcessTypeEnum.valueOf(processVO.getProcessType()), process.getProcessId(),
                                                process.getUser(), process.getPriority(), process.isReleased());
                                        LOG.info("Updated validation process to status CANCELED for processId {}", processId);
                                    }
                                }
                                dataSetSnapshotControllerZuul.releaseLocksFromReleaseDatasets(jobVO.getDataflowId(), jobVO.getProviderId());
                            }
                            if (jobId!=null) {
                                jobService.updateJobStatus(jobId, JobStatusEnum.CANCELED);
                            }
                            LOG.info("Job canceled for canceled process {}, datasetId {}", processVO.getProcessId(), processVO.getDatasetId());
                            Map<String, Object> value = new HashMap<>();
                            String user = jobVO.getCreatorUsername();
                            value.put(LiteralConstants.USER, user);
                            if (jobVO.getJobType().equals(JobTypeEnum.VALIDATION) && !jobVO.isRelease()) {
                                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATION_CANCELED_EVENT, value,
                                        NotificationVO.builder().datasetId(jobVO.getDatasetId()).user(user).error("No tasks created").build());
                            } else {
                                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_CANCELED_EVENT, value,
                                        NotificationVO.builder().dataflowId(jobVO.getDataflowId()).providerId(jobVO.getProviderId()).user(user).error("No tasks created").build());
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error while running scheduled task cancelInProgressValidationsWithoutTasks for processId {}, {}", processVO.getProcessId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled task cancelInProgressValidationsWithoutTasks " + e.getMessage());
        }
    }
}


















