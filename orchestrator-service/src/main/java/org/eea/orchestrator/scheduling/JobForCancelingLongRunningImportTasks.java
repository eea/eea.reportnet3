package org.eea.orchestrator.scheduling;

import org.apache.tomcat.jni.Proc;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.orchestrator.service.JobProcessService;
import org.eea.orchestrator.service.JobService;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class JobForCancelingLongRunningImportTasks {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForCancelingLongRunningImportTasks.class);

    /* The maximum time in milliseconds for which an import task can be in progress */
    @Value(value = "${scheduling.inProgress.import.task.max.ms.fail}")
    private long maxTimeForInProgressImportTasks;

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


    @Autowired
    private JobProcessService jobProcessService;
    @Autowired
    private JobService jobService;

    @Autowired
    private ValidationControllerZuul validationControllerZuul;

    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;

    @Autowired
    private ProcessController.ProcessControllerZuul processControllerZuul;

    @Autowired
    private UserManagementController.UserManagementControllerZull userManagementControllerZull;

    @Autowired
    private KafkaSenderUtils kafkaSenderUtils;

    @Autowired
    private DatasetSchemaController.DatasetSchemaControllerZuul datasetSchemaControllerZuul;

    /** The dataflow controller zuul. */
    @Autowired
    private DataFlowController.DataFlowControllerZuul dataflowControllerZuul;

    @Autowired
    private DatasetMetabaseController.DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;


    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> cancelLongRunningImportTasks(),
                new CronTrigger("0 */15 * * * *"));
    }

    /**
     * The job runs every hour. It finds import jobs that have tasks with status=IN_PROGRESS for more than maxTimeForInProgressImportTasks
     * and changes the status of the tasks and processes to CANCELED and the job status to FAILED. Then it removes the locks
     */
    public void cancelLongRunningImportTasks() {
        try {
            LOG.info("Running scheduled job cancelLongRunningImportTasks");

            List<JobVO> longRunningJobs = jobService.getJobsByTypeAndStatus(JobTypeEnum.IMPORT, JobStatusEnum.IN_PROGRESS);
            for (JobVO job: longRunningJobs){
                Boolean longRunningTasksExist = false;
                //get job processes
                List<String> processIds = jobProcessService.findProcessesByJobId(job.getId());
                for(String processId: processIds){
                    longRunningTasksExist = validationControllerZuul.findIfTasksExistByProcessIdAndStatusAndDuration(processId, ProcessStatusEnum.IN_PROGRESS, maxTimeForInProgressImportTasks);
                    if(longRunningTasksExist) {
                        //update tasks status
                        validationControllerZuul.updateTaskStatusByProcessIdAndCurrentStatuses(processId, ProcessStatusEnum.CANCELED, new HashSet<>(Arrays.asList(ProcessStatusEnum.IN_QUEUE.toString(), ProcessStatusEnum.IN_PROGRESS.toString())));

                        ProcessVO processVO = processControllerZuul.findById(processId);
                        //update process status
                        processControllerZuul.updateProcess(processVO.getDatasetId(), processVO.getDataflowId(),
                                ProcessStatusEnum.CANCELED, ProcessTypeEnum.IMPORT, processVO.getProcessId(),
                                processVO.getUser(), processVO.getPriority(), processVO.isReleased());
                        LOG.info("Updated import tasks and process to status CANCELED for jobId {} and processId {}", job.getId(), processVO.getProcessId());
                    }
                }
                if(longRunningTasksExist){
                    //update job status
                    jobService.updateJobStatus(job.getId(), JobStatusEnum.FAILED);
                    LOG.info("Updated import job to status FAILED for jobId {}", job.getId());

                    //remove locks
                    TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    dataSetControllerZuul.deleteLocksToImportProcess(job.getDatasetId());
                    LOG.info("Locks removed for failed job {}, datasetId {}", job.getId(), job.getDatasetId());

                    sendKafkaNotification(job);

                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled task cancelLongRunningImportTasks ", e);
        }
    }

    private void sendKafkaNotification(JobVO job){
        Map<String, Object> value = new HashMap<>();
        String user = job.getCreatorUsername();
        value.put(LiteralConstants.USER, user);
        Map<String, Object> insertedParameters = job.getParameters();
        String fileName = (String) insertedParameters.get("fileName");
        String tableSchemaName = null;
        String tableSchemaId = null;
        String dataflowName = job.getDataflowName();
        String datasetName = job.getDatasetName();

        if(dataflowName == null) {
            try {
                dataflowName = dataflowControllerZuul.findDataflowNameById(job.getDataflowId());
            } catch (Exception e) {
                LOG.error("Error when trying to receive dataflow name for dataflowId {} ", job.getDataflowId(), e);
            }
        }

        if(datasetName == null) {
            try {
                datasetName = dataSetMetabaseControllerZuul.findDatasetNameById(job.getDatasetId());
            } catch (Exception e) {
                LOG.error("Error when trying to receive dataset name for datasetId {} ", job.getDatasetId(), e);
            }
        }

        String datasetSchemaId = null;
        try {
            datasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(job.getDatasetId());
        } catch (Exception e) {
            LOG.error("Error when trying to receive dataset schema id for datasetId {} ", job.getDatasetId(), e);
        }

        if(insertedParameters.get("tableSchemaId") != null) {
            tableSchemaId = (String) insertedParameters.get("tableSchemaId");
            if (tableSchemaId != null && datasetSchemaId != null) {
                try {
                    tableSchemaName = datasetSchemaControllerZuul.getTableSchemaName(datasetSchemaId, tableSchemaId);
                } catch (Exception e) {
                    LOG.error("Error when trying to receive table schema name for tableSchemaId {} datasetId {} and datasetSchemaId {} ", tableSchemaId, job.getDatasetId(), datasetSchemaId, e);
                }
            }
        }

        try {
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.LONG_RUNNING_IMPORT_FAILED_EVENT, value,
                    NotificationVO.builder().datasetId(job.getDatasetId()).dataflowId(job.getDataflowId()).tableSchemaId(tableSchemaId).fileName(fileName)
                            .dataflowName(dataflowName).datasetName(datasetName).tableSchemaName(tableSchemaName)
                            .user(user).error("Long running import tasks").build());
        } catch (EEAException e) {
            LOG.error("Error while releasing LONG_RUNNING_IMPORT_FAILED_EVENT notification for jobId {} and datasetId {} ", job.getId(), job.getDatasetId(), e);
        }
    }
}
