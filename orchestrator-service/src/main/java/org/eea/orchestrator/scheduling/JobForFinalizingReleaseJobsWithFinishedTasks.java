package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.collaboration.CollaborationController;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataset.DatasetStatusMessageVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
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
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Component
public class JobForFinalizingReleaseJobsWithFinishedTasks {

    @Value(value = "${scheduling.inProgress.release.job.finished.process.max.time}")
    private long maxTimeInMinutesForFinishedProcessesOfInProgressReleaseJobs;

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
    private static final Logger LOG = LoggerFactory.getLogger(JobForFinalizingReleaseJobsWithFinishedTasks.class);

    @Autowired
    private JobProcessService jobProcessService;
    @Autowired
    private JobService jobService;
    @Autowired
    private DatasetSnapshotController datasetSnapshotController;
    @Autowired
    private KafkaSenderUtils kafkaSenderUtils;
    @Autowired
    private ProcessControllerZuul processControllerZuul;
    @Autowired
    private UserManagementControllerZull userManagementControllerZull;
    @Autowired
    private DatasetMetabaseController datasetMetabaseController;
    @Autowired
    private DataFlowController dataFlowController;
    @Autowired
    private CollaborationController collaborationControllerZuul;
    @Autowired
    private DatasetMetabaseController datasetMetabaseControllerZull;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(this::finalizeInProgressReleaseJobsWithFinishedTasks,
                new CronTrigger("0 */30 * * * *"));
    }

    /**
     * Finalizes in-progress release jobs.
     *
     * If all processes and tasks finish, and the most recently finished process
     * has remained in FINISHED state longer than the configured wait time,
     * the job is marked as FINISHED.
     *
     * If the number of created processes never reaches the expected dataset count
     * after the wait time has passed, the job is marked as FAILED.
     */
    public void finalizeInProgressReleaseJobsWithFinishedTasks() {
        try {
            LOG.info("Running scheduled job finalizeInProgressReleaseJobsWithFinishedProcessesAndTasks");
            List<JobVO> jobs = jobService.findByStatusAndJobType(JobStatusEnum.IN_PROGRESS, JobTypeEnum.RELEASE);
            TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            jobLoop:
            for (JobVO jobVO : jobs) {
                Long providerId = jobVO.getProviderId();
                Long dataflowId = jobVO.getDataflowId();
                DataFlowVO dataflow = dataFlowController.getMetabaseById(dataflowId);
                boolean isSilentRelease = Boolean.TRUE.equals(jobVO.getParameters().get("silentRelease"));

                List<String> processIds = jobProcessService.findProcessesByJobId(jobVO.getId());

                boolean allFinished = true;

                List<Long> datasetIds = datasetMetabaseControllerZull
                        .getDatasetIdsByDataflowIdAndDataProviderId(dataflowId, providerId);

                if (checkAndFailIncompleteReleaseJob(jobVO, datasetIds, processIds, isSilentRelease)) {
                    continue; // fail job and exit
                }

                if (!(processIds.size() == datasetIds.size())){
                    continue; // not all processes created for each dataset Release cannot finish
                }

                for (String processId : processIds) {
                    ProcessVO process = processControllerZuul.findById(processId);

                    List<TaskVO> tasks = processControllerZuul.findTasksByProcessId(processId);
                    boolean everyTaskFinished =
                            tasks.stream().allMatch(taskVO -> taskVO.getStatus() == ProcessStatusEnum.FINISHED);

                    if (!tasks.isEmpty() && everyTaskFinished
                            && !ProcessStatusEnum.FINISHED.toString().equals(process.getStatus())
                            && !ProcessStatusEnum.CANCELED.toString().equals(process.getStatus())) {
                        LOG.info("All tasks finished for processId={}, updating process to FINISHED", processId);
                        processControllerZuul.updateProcess(
                                process.getDatasetId(),
                                process.getDataflowId(),
                                ProcessStatusEnum.FINISHED,
                                ProcessTypeEnum.valueOf(process.getProcessType()),
                                process.getProcessId(),
                                process.getUser(),
                                process.getPriority(),
                                process.isReleased()
                        );
                        process.setStatus(ProcessStatusEnum.FINISHED.toString());
                        process.setProcessFinishingDate(new Date());
                    }

                    if (!ProcessStatusEnum.FINISHED.toString().equals(process.getStatus())) {
                        allFinished = false;
                        break;
                    }

                    if (process.getProcessFinishingDate() != null) {
                        long minutesSinceFinish = Duration.between(process.getProcessFinishingDate().toInstant(), Instant.now()).toMinutes();

                        if (minutesSinceFinish < maxTimeInMinutesForFinishedProcessesOfInProgressReleaseJobs) {
                            allFinished = false;
                            break;
                        }
                    }

                    if (!everyTaskFinished) {
                        allFinished = false;
                        break;
                    }
                }

                if (!allFinished) {
                    // At least one process or task is still running.
                    continue;
                }

                // Remove locks.
                datasetSnapshotController.releaseLocksFromReleaseDatasets(dataflowId, providerId);

                // Check that for the datasets released column is false.
                List<ReportingDatasetVO> datasets =
                        datasetMetabaseController.findReportingDataSetIdByDataflowIdAndProviderId(dataflowId, providerId);

                for (ReportingDatasetVO dataset : datasets) {

                    // Set to false if dataset is not released yet.
                    if (dataset.getReleasing()) {
                        dataset.setReleasing(false);
                        datasetMetabaseControllerZull.updateReportingDatasetMetabase(dataset);
                    }

                    // Check Snapshot entries only if not silent release.
                    if (!isSilentRelease) {
                        // Get last snapshot from reporting dataset.
                        SnapshotVO lastSnapshot = Collections.max(
                                datasetSnapshotController.getSnapshotByDatasetId(dataset.getId()),
                                Comparator.comparingLong(SnapshotVO::getId));

                        // Stops the process either field is has wrong values.
                        if (!lastSnapshot.getRelease() || lastSnapshot.getDateReleased() == null) {
                            LOG.error("Snapshot pre-condition error. Release failed for jobId {} and snapshotId {} of datasetId {}",jobVO.getId(), lastSnapshot.getId(), dataset.getId());
                            continue jobLoop;
                        }
                    }

                    // Change Job status to FINISHED.
                    jobService.updateJobStatus(jobVO.getId(), JobStatusEnum.FINISHED);
                    String dataflowName = dataflow.getName();

                    // Send emails and notifications only if not silent release.
                    if (!isSilentRelease) {
                        // Create feedback message for eash dataset.
                        String country = dataset.getDataSetName();

                        MessageVO messageVO = new MessageVO();
                        messageVO.setProviderId(providerId);
                        messageVO.setContent(country + " released " + dataflowName + " successfully");
                        messageVO.setAutomatic(true);
                        boolean sendEmail = false;

                        collaborationControllerZuul.createMessage(dataflowId, messageVO, jobVO.getCreatorUsername(), jobVO.getId(), sendEmail);

                        LOG.info("Automatic feedback message created of dataflow {}, datasetId {}, jobId {}, Message: {}, User: {}",
                                dataflow.getId(), dataset.getId(), jobVO.getId(), messageVO.getContent(), jobVO.getCreatorUsername());

                        // Notification send to reporting user for dataflow release job completion.
                        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_COMPLETED_EVENT, null,
                                NotificationVO.builder()
                                        .user(jobVO.getCreatorUsername())
                                        .dataflowId(dataflowId).dataflowName(dataflowName)
                                        .providerId(providerId).build());
                    }
                    else{
                        LOG.info("Sending SILENT_RELEASE_COMPLETED_EVENT event for jobId {}", jobVO.getId());
                        //this event will not produce any notifications to the user because frontend will never show it in the user notifications
                        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.SILENT_RELEASE_COMPLETED_EVENT, null,
                                NotificationVO.builder()
                                        .user(jobVO.getCreatorUsername())
                                        .dataflowId(dataflowId).dataflowName(dataflowName)
                                        .providerId(providerId).build());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled job finalizeInProgressReleaseJobsWithFinishedProcessesAndTasks ", e);
        }
    }

    /**
     * Checks whether a release job has missing or failed processes.
     * If inconsistency is found, the job is marked as FAILED and locks are released.
     *
     * @param jobVO the release job to check
     * @param datasetIds all dataset IDs for this provider/dataflow
     * @param processIds all process IDs linked to the job
     * @return true if the job was marked as FAILED (and should be skipped), false otherwise
     */
    private boolean checkAndFailIncompleteReleaseJob(JobVO jobVO, List<Long> datasetIds, List<String> processIds, Boolean isSilentRelease) throws Exception {
        Long dataflowId = jobVO.getDataflowId();
        Long providerId = jobVO.getProviderId();
        String user = jobVO.getCreatorUsername();

        if (!(processIds.size() == datasetIds.size())) {
            // 1) Find latest FINISHED process finishing date
            Optional<Date> latestFinished = processIds.stream()
                    .map(pid -> processControllerZuul.findById(pid))
                    .filter(p -> p.getProcessFinishingDate() != null)
                    .map(ProcessVO::getProcessFinishingDate)
                    .max(Date::compareTo);

            long minutesSinceReferenceTime = 0;

            if (latestFinished.isPresent()) {
                minutesSinceReferenceTime = Duration.between(
                        latestFinished.get().toInstant(),
                        Instant.now()
                ).toMinutes();
            } else {
                // 2) No FINISHED processes fallback to earliest process START date
                Optional<Date> earliestStart = processIds.stream()
                        .map(pid -> processControllerZuul.findById(pid))
                        .filter(p -> p.getProcessStartingDate() != null)
                        .map(ProcessVO::getProcessStartingDate)
                        .min(Date::compareTo);

                if (earliestStart.isPresent()) {
                    minutesSinceReferenceTime = Duration.between(
                            earliestStart.get().toInstant(),
                            Instant.now()
                    ).toMinutes();
                }
            }

            if (minutesSinceReferenceTime < maxTimeInMinutesForFinishedProcessesOfInProgressReleaseJobs) {
                return false;  // still waiting
            }

            LOG.error("Release job {} for dataflowId {} and providerId {} is missing processes. Expected {}, found {}. Marking job as FAILED.",
                    jobVO.getId(), dataflowId, providerId,
                    datasetIds.size(),
                    processIds.size());

            //Set the status of all the datasets of the failed release back to PENDING
            for (Long datasetId : datasetIds) {
                final DatasetStatusMessageVO datasetStatusMessageVO = new DatasetStatusMessageVO();
                datasetStatusMessageVO.setDatasetId(datasetId);
                datasetStatusMessageVO.setDataflowId(dataflowId);
                datasetStatusMessageVO.setStatus(DatasetStatusEnum.PENDING);
                datasetMetabaseController.updateDatasetStatus(datasetStatusMessageVO);
            }

            datasetSnapshotController.releaseLocksFromReleaseDatasets(dataflowId, providerId);
            jobService.updateJobStatus(jobVO.getId(), JobStatusEnum.FAILED);
            jobService.updateJobInfo(jobVO.getId(), JobInfoEnum.ERROR_RELEASE_PARTIALLY_COMPLETED, null, true);
            datasetSnapshotController.rollBackSnapshotRecord(jobVO.getId(), dataflowId, providerId);

            if(isSilentRelease){
                LOG.info("Sending SILENT_RELEASE_FAILED_EVENT event for jobId {}", jobVO.getId());
                //this event will not produce any notifications to the user because frontend will never show it in the user notifications
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.SILENT_RELEASE_FAILED_EVENT, null, NotificationVO.builder().dataflowId(dataflowId).providerId(providerId).user(user)
                                .error("Incomplete release: Not all datasets were able to release").jobId(jobVO.getId()).build());
            } else
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_CANCELED_EVENT, null, NotificationVO.builder().dataflowId(dataflowId).providerId(providerId)
                                .user(user).error("Incomplete release: Not all datasets were able to release").jobId(jobVO.getId()).build());

            return true;
        }
        return false;
    }
}
