package org.eea.orchestrator.scheduling;

import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.controller.recordstore.ProcessController;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.metabase.TaskType;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.validation.TaskVO;
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
import java.util.*;

@Component
public class JobForFinalizingInProgressImportJobsWithFinishedOrCanceledTasks {

    @Value(value = "${scheduling.inProgress.import.job.completed.task.max.time}")
    private long maxTimeInMinutesForCompletedTasksOfInProgressImportJobs;

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
    private DatasetController.DataSetControllerZuul dataSetControllerZuul;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForFinalizingInProgressImportJobsWithFinishedOrCanceledTasks.class);

    @Autowired
    private JobProcessService jobProcessService;
    @Autowired
    private JobService jobService;
    @Autowired
    private ValidationController.ValidationControllerZuul validationControllerZuul;

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
        scheduler.schedule(() -> finalizeInProgressImportJobsWithCompletedTasks(),
                new CronTrigger("0 */15 * * * *"));
    }

    /**
     * The job runs every hour. It finds in_progress import jobs that have all their tasks finished or canceled and the latest completed task
     * has had the status for more than maxTimeInMinutesForCompletedTasksOfInProgressImportJobs minutes
     */
    public void finalizeInProgressImportJobsWithCompletedTasks() {
        try {
            LOG.info("Running scheduled job finalizeInProgressImportJobsWithCompletedTasks");
            List<JobVO> jobs = jobService.findByStatusAndJobType(JobStatusEnum.IN_PROGRESS, JobTypeEnum.IMPORT);
            for (JobVO jobVO : jobs) {
                try {
                    List<String> processes = jobProcessService.findProcessesByJobId(jobVO.getId());
                    String processId = processes.get(0);
                    ProcessStatusEnum statusOfProcessAndTasks = getProcessAndTasksStatus(processId);
                    TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    if (statusOfProcessAndTasks == ProcessStatusEnum.FINISHED) {
                        LOG.info("Finalizing stuck finished import job {}", jobVO.getId());
                        ProcessVO process = processControllerZuul.findById(processId);
                        if (process.getStatus().equals(ProcessStatusEnum.IN_PROGRESS.toString())) {
                            processControllerZuul.updateProcess(process.getDatasetId(), process.getDataflowId(), ProcessStatusEnum.FINISHED,
                                    ProcessTypeEnum.IMPORT, processId, process.getUser(), 0, null);
                        }
                        jobService.updateJobStatus(jobVO.getId(), JobStatusEnum.FINISHED);
                        //remove locks
                        dataSetControllerZuul.deleteLocksToImportProcess(jobVO.getDatasetId());
                        LOG.info("Locks removed for finished import job {}, datasetId {}", jobVO.getId(), jobVO.getDatasetId());

                        releaseSuccessNotification(jobVO);
                    }
                    else if(statusOfProcessAndTasks == ProcessStatusEnum.CANCELED){
                        LOG.info("Finalizing stuck canceled import job {}", jobVO.getId());
                        ProcessVO process = processControllerZuul.findById(processId);
                        if (process.getStatus().equals(ProcessStatusEnum.IN_PROGRESS.toString())) {
                            processControllerZuul.updateProcess(process.getDatasetId(), process.getDataflowId(), ProcessStatusEnum.CANCELED,
                                    ProcessTypeEnum.IMPORT, processId, process.getUser(), 0, null);
                        }
                        jobService.updateJobStatus(jobVO.getId(), JobStatusEnum.CANCELED);
                        //remove locks
                        dataSetControllerZuul.deleteLocksToImportProcess(jobVO.getDatasetId());
                        LOG.info("Locks removed for canceled import job {}, datasetId {}", jobVO.getId(), jobVO.getDatasetId());

                        releaseCancelNotification(jobVO);
                    }
                } catch (Exception er) {
                    LOG.error("Error while running scheduled job finalizeInProgressImportJobsWithCompletedTasks for job {}, {}", jobVO.getId(), er.getMessage());
                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled job finalizeInProgressImportJobsWithCompletedTasks ", e);
        }
    }

    ProcessStatusEnum getProcessAndTasksStatus(String processId) {
        Integer unfinishedTasks = validationControllerZuul.findTasksCountByProcessIdAndStatusIn(processId, Arrays.asList(ProcessStatusEnum.IN_QUEUE.toString(), ProcessStatusEnum.IN_PROGRESS.toString()));
        //check if all tasks have finished
        if (unfinishedTasks > 0) {
            return ProcessStatusEnum.IN_PROGRESS;
        } else {
            //if all tasks have finished, check if the latest finished task is in finished state for more than maxTimeInMinutesForCompletedTasksOfInProgressImportJobs minutes
            TaskVO finishedTask = validationControllerZuul.getTaskThatExceedsTimeByStatusesAndType(processId, maxTimeInMinutesForCompletedTasksOfInProgressImportJobs, new HashSet<>(Arrays.asList(ProcessStatusEnum.FINISHED.toString(), ProcessStatusEnum.CANCELED.toString())), TaskType.IMPORT_TASK);
            if (finishedTask == null) {
                return ProcessStatusEnum.IN_PROGRESS;
            }
            else{
                return finishedTask.getStatus();
            }
        }
    }

    private void releaseSuccessNotification(JobVO job){
        DatasetTypeEnum type = dataSetControllerZuul.getDatasetType(job.getDatasetId());
        Map<String, Object> value = new HashMap<>();
        String user = job.getCreatorUsername();
        value.put(LiteralConstants.USER, user);
        value.put(LiteralConstants.DATASET_ID, job.getDatasetId());
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

        EventType eventType = DatasetTypeEnum.REPORTING.equals(type) || DatasetTypeEnum.TEST.equals(type)
                ? EventType.IMPORT_REPORTING_COMPLETED_EVENT
                : EventType.IMPORT_DESIGN_COMPLETED_EVENT;


        try {
            kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value,
                    NotificationVO.builder().datasetId(job.getDatasetId()).dataflowId(job.getDataflowId()).tableSchemaId(tableSchemaId).fileName(fileName)
                            .dataflowName(dataflowName).datasetName(datasetName).tableSchemaName(tableSchemaName)
                            .user(user).build());
        } catch (EEAException e) {
            LOG.error("Error while releasing {} notification for jobId {} and datasetId {} ", eventType.getKey(), job.getId(), job.getDatasetId(), e);
        }
    }

    private void releaseCancelNotification(JobVO job){

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
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_CANCELED_EVENT, value,
                    NotificationVO.builder().datasetId(job.getDatasetId()).dataflowId(job.getDataflowId()).tableSchemaId(tableSchemaId).fileName(fileName)
                            .dataflowName(dataflowName).datasetName(datasetName).tableSchemaName(tableSchemaName)
                            .user(user).error("Tasks have been canceled").build());
        } catch (EEAException e) {
            LOG.error("Error while releasing IMPORT_CANCELED_EVENT notification for jobId {} and datasetId {} ", job.getId(), job.getDatasetId(), e);
        }
    }
}
