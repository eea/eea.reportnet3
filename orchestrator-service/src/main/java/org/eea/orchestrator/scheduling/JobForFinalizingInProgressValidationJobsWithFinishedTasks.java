package org.eea.orchestrator.scheduling;

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
public class JobForFinalizingInProgressValidationJobsWithFinishedTasks {

    @Value(value = "${scheduling.inProgress.validation.job.max.time}")
    private long maxTimeInMinutesForInProgressValidationJobs;

    @Value(value = "${scheduling.inProgress.validation.job.finished.task.max.time}")
    private long maxTimeInMinutesForFinishedTasksOfInProgressValidationJobs;

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
    private static final Logger LOG = LoggerFactory.getLogger(JobForFinalizingInProgressValidationJobsWithFinishedTasks.class);

    @Autowired
    private JobProcessService jobProcessService;
    @Autowired
    private JobService jobService;
    @Autowired
    private ValidationControllerZuul validationControllerZuul;
    @Autowired
    private KafkaSenderUtils kafkaSenderUtils;
    @Autowired
    private ProcessControllerZuul processControllerZuul;
    @Autowired
    private UserManagementControllerZull userManagementControllerZull;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> finalizeInProgressValidationJobsWithoutTasks(),
                new CronTrigger("0 */30 * * * *"));
    }

    /**
     * The job runs every 30 minutes. It finds jobs that have status=IN_PROGRESS for more than maxTimeInMinutesForInProgressValidationJobs, have all their tasks finished
     * and the latest finished task is in finished status for more than maxTimeInMinutesForFinishedTasksOfInProgressValidationJobs minutes
     */
    public void finalizeInProgressValidationJobsWithoutTasks() {
        try {
            LOG.info("Running scheduled job finalizeInProgressValidationJobsWithoutTasks");
            List<JobVO> jobs = jobService.findJobsThatExceedTimeWithSpecificTypeAndStatus(JobTypeEnum.VALIDATION.toString(), JobStatusEnum.IN_PROGRESS.toString(), maxTimeInMinutesForInProgressValidationJobs);
            TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            for (JobVO jobVO : jobs) {
                try {
                    List<String> processes = jobProcessService.findProcessesByJobId(jobVO.getId());
                    if (jobVO.isRelease()) {
                        //validation with release true
                        boolean finished = true;
                        String uuid = null, user = null, queuedProcess = null;
                        Long datasetId = null;
                        for (String processId : processes) {
                            ProcessVO process = processControllerZuul.findById(processId);
                            if (process.getStatus().equals(ProcessStatusEnum.IN_QUEUE.toString())) {
                                queuedProcess = processId;
                                continue;
                            }
                            finished = isProcessFinished(processId);
                            if (!finished) break;
                            else {
                                if (process.getStatus().equals(ProcessStatusEnum.IN_PROGRESS.toString())) {
                                    processControllerZuul.updateProcess(process.getDatasetId(), process.getDataflowId(), ProcessStatusEnum.FINISHED,
                                            ProcessTypeEnum.VALIDATION, processId, process.getUser(), 0, null);
                                }
                            }
                            uuid = processId;
                            user = process.getUser();
                            datasetId = process.getDatasetId();
                        }
                        if (finished && queuedProcess == null) {
                            //all processes of the provider datasets are finished, as all tasks are finished
                            LOG.info("Finalizing stuck validation job {}", jobVO.getId());
                            Map<String, Object> value = createValue(jobVO.getId(), uuid, user, datasetId);
                            jobService.updateJobStatus(jobVO.getId(), JobStatusEnum.FINISHED);
                            kafkaSenderUtils.releaseKafkaEvent(EventType.VALIDATION_RELEASE_FINISHED_EVENT,
                                    value);
                        } else if (queuedProcess != null) {
                            //a process for one of the provider datasets is stuck in state IN_QUEUE, so execute validation for that process
                            ProcessVO process = processControllerZuul.findById(queuedProcess);
                            validationControllerZuul.executeValidation(process.getDatasetId(), process.getProcessId(), true, true);
                        }
                    } else {
                        //validation with release false
                        String processId = processes.get(0);
                        if (isProcessFinished(processId)) {
                            LOG.info("Finalizing stuck validation job {}", jobVO.getId());
                            ProcessVO process = processControllerZuul.findById(processId);
                            if (process.getStatus().equals(ProcessStatusEnum.IN_PROGRESS.toString())) {
                                processControllerZuul.updateProcess(process.getDatasetId(), process.getDataflowId(), ProcessStatusEnum.FINISHED,
                                        ProcessTypeEnum.VALIDATION, processId, process.getUser(), 0, null);
                            }
                            String uuid = process.getProcessId();
                            String user = process.getUser();
                            Long datasetId = process.getDatasetId();
                            Map<String, Object> value = createValue(jobVO.getId(), uuid, user, datasetId);
                            validationControllerZuul.deleteLocksToReleaseProcess(jobVO.getDatasetId());
                            jobService.updateJobStatus(jobVO.getId(), JobStatusEnum.FINISHED);
                            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATION_FINISHED_EVENT,
                                    value,
                                    NotificationVO.builder().user(user).datasetId(datasetId).build());
                        }
                    }
                } catch (Exception er) {
                    LOG.error("Error while running scheduled job finalizeInProgressValidationJobsWithoutTasks for job {}, {}", jobVO.getId(), er.getMessage());
                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled job finalizeInProgressValidationJobsWithoutTasks " + e.getMessage());
        }
    }

    private Map<String, Object> createValue(Long jobId, String uuid, String user, Long datasetId) {
        Map<String, Object> value = new HashMap<>();
        value.put("uuid", uuid);
        value.put("user", user);
        value.put("validation_job_id", jobId);
        value.put(LiteralConstants.DATASET_ID, datasetId);
        return value;
    }

    boolean isProcessFinished(String processId) {
        boolean finished = true;
        Integer unfinishedTasks = validationControllerZuul.findTasksCountByProcessIdAndStatusIn(processId, Arrays.asList(ProcessStatusEnum.IN_QUEUE.toString(), ProcessStatusEnum.IN_PROGRESS.toString()));
        //check if all tasks have finished
        if (unfinishedTasks > 0) {
            finished = false;
        } else {
            //if all tasks have finished, check if the latest finished task is in finished state for more than maxTimeInMinutesForFinishedTasksOfInProgressValidationJobs minutes
            BigInteger finishedTask = validationControllerZuul.getFinishedValidationTaskThatExceedsTime(processId, maxTimeInMinutesForFinishedTasksOfInProgressValidationJobs);
            if (finishedTask == null) {
                finished = false;
            }
        }
        return finished;
    }
}