package org.eea.dataset.service;

import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.Task;
import org.eea.dataset.persistence.metabase.repository.TaskRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.DATASET_FORMAT_NAME;
import static org.eea.utils.LiteralConstants.PARQUET_RECORD_ID_COLUMN_HEADER;
import static org.eea.utils.LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH;
import static org.eea.utils.LiteralConstants.S3_VALIDATION;
import static org.eea.utils.LiteralConstants.S3_VALIDATION_TABLE_PATH;

@Service
public class ReleasePrecheckService {

  private static final Logger LOG = LoggerFactory.getLogger(ReleasePrecheckService.class);

  @Autowired
  private JobControllerZuul jobControllerZuul;

  @Autowired
  private JobProcessControllerZuul jobProcessControllerZuul;

  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  @Autowired
  private ProcessControllerZuul processControllerZuul;

  @Autowired
  private ValidationRepository validationRepository;

  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  @Qualifier("dremioJdbcTemplate")
  private JdbcTemplate dremioJdbcTemplate;

  @Autowired
  private S3Helper s3Helper;

  @Autowired
  private S3Service s3Service;

  // Default priority used when the first real RELEASE process is created.
  private int defaultReleaseProcessPriority = 20;

  /**
   * Checks whether a queued RELEASE job is allowed to continue.
   *
   * This method contains the blocker and canceled-task checks that used to live in the
   * old CheckBlockersDataSnapshotCommand. The main difference now is that the RELEASE
   * job already exists and is about to start, so we do the checks here just before the
   * actual release execution begins.
   *
   * Corresponding ticket #297462.
   */
  public void precheckOrThrow(Long releaseJobId) {
    JobVO releaseJob = jobControllerZuul.findJobById(releaseJobId);
    Map<String, Object> parameters = releaseJob.getParameters();

    // Parameters are taken directly from the queued RELEASE job that was created after
    // VALIDATION_RELEASE_FINISHED_EVENT.
    // Cast in case of id deserialization.
    Long dataflowId = Long.valueOf((Integer) parameters.get("dataflowId"));
    Long dataProviderId = Long.valueOf((Integer) parameters.get("dataProviderId"));
    List<Long> datasets = ((List<?>) parameters.get("datasetId")).stream()
        .map(obj -> Long.valueOf(String.valueOf(obj)))
        .collect(Collectors.toList());
    Long validationJobId = parameters.containsKey("validationJobId") ? Long.valueOf((Integer) parameters.get("validationJobId")): null;

    // For bigData dataflows we cannot use the normal validationRepository check because
    // blocker information lives in parquet validation output and must be read through Dremio.
    final boolean isBigData = dataFlowControllerZuul.isBigDataflow(dataflowId);
    LOG.info("Release precheck jobId={} dataflowId={} providerId={} datasets={}", releaseJobId, dataflowId, dataProviderId, datasets);
    boolean haveBlockers = false;
    for (Long datasetId : datasets) {
      LOG.info("Checking blockers for datasetId={} tenant={}", datasetId, TenantResolver.getTenantName());
      if (isBigData) {
        if (hasBigDataBlockers(dataflowId, dataProviderId, datasetId)) {
          haveBlockers = true;
          break;
        }
      } else {
        // For non-bigdata, validationRepository reads from the dataset_id schema.
        setTenant(datasetId);
        if (validationRepository.existsByLevelError(ErrorTypeEnum.BLOCKER)) {
          LOG.info("existsByLevelError(BLOCKER) for datasetId={} -> {}", datasetId, validationRepository.existsByLevelError(ErrorTypeEnum.BLOCKER));
          haveBlockers = true;
          break;
        }
      }
    }

    // If at least one dataset has a BLOCKER, the RELEASE must stop immediately.
    if (haveBlockers) {
      throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
          "One or more datasets have blockers errors, Release aborted");
    }

    // After blockers, inspect the validation job that preceded this RELEASE.
    if (validationJobId != null) {
      List<String> processIds = jobProcessControllerZuul.findProcessesByJobId(validationJobId);
      for (String processId : processIds) {
        //check if there were canceled tasks that were related to blockers or canceled tasks without a rule id. If the list is not empty we need to fail the release.
        List<Task> canceledBlockerTasks = taskRepository.findAllByProcessIdAndStatusAndLevelErrorBlocker(
            processId, ProcessStatusEnum.CANCELED.toString());
        if (canceledBlockerTasks != null && canceledBlockerTasks.size() > 0) {
          LOG.info("Found canceled tasks with blockers for validationJobId {} and processId {}", validationJobId, processId);
          jobControllerZuul.updateJobInfo(releaseJobId, JobInfoEnum.ERROR_RELEASE_CANCELED_BLOCKERS, null);
          throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
              "Release aborted: canceled validation tasks with blocker errors found");
        }
      }

      // If none canceled tasks with blocker errors were found check for any canceled tasks and write a warning to Release Job.
      Task jobHasCanceledTask = taskRepository.findFirstByProcessIdInAndStatus(processIds, ProcessStatusEnum.CANCELED);
      if (jobHasCanceledTask != null) {
        LOG.info("Found canceled task(s) without blockers for validationJobId {}", validationJobId);
        jobControllerZuul.updateJobInfo(releaseJobId, JobInfoEnum.WARNING_HAS_CANCELED_VALIDATION_TASKS, null);
      }
    }
  }

  /**
   * Starts the real queued RELEASE job.
   *
   * This method intentionally stays close to the old CheckBlockersDataSnapshotCommand logic:
   * - sort datasets
   * - pick the first dataset
   * - clear release-related locks
   * - create the first RELEASE process
   * - create the job_process relation
   * - start the first snapshot
   *
   * After this first dataset starts, the existing one-by-one release flow continues through
   * ReleaseDataSnapshotsCommand as before.
   */
  public void startQueuedReleaseJob(Long jobId) throws EEAException {
    JobVO releaseJob = jobControllerZuul.findJobById(jobId);
    Map<String, Object> parameters = releaseJob.getParameters();
    List<Long> datasets = ((List<?>) parameters.get("datasetId")).stream()
        .map(obj -> Long.valueOf(String.valueOf(obj)))
        .collect(Collectors.toList());

    Collections.sort(datasets);

    // The first dataset is used to kick off the actual RELEASE process.
    Long firstDatasetId = datasets.get(0);
    DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(firstDatasetId);
    String user = releaseJob.getCreatorUsername() != null
        ? releaseJob.getCreatorUsername()
        : SecurityContextHolder.getContext().getAuthentication().getName();

    // Keep the effective user in thread context because deeper layers already rely on it.
    ThreadPropertiesManager.setVariable("user", user);

    // Validation-for-release leaves locks that are useful during validation but must be removed
    // before the real RELEASE starts, otherwise the release kickoff is blocked.
    datasetSnapshotService.releaseLocksRelatedToRelease(dataset.getDataflowId(), dataset.getDataProviderId());

    LOG.info(
        "Releasing datasets process continues. At this point, the datasets from the dataflowId {}, dataProviderId {} and jobId {} have no blockers",
        dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId());

    LOG.info("Creating the first release process for dataflowId {}, dataProviderId {}, jobId {}",
        dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId());

    // Create the first RELEASE process in recordstore.
    String processId = UUID.randomUUID().toString();
    Boolean isProcessCreated = processControllerZuul.updateProcess(
        firstDatasetId,
        dataset.getDataflowId(),
        ProcessStatusEnum.IN_PROGRESS,
        ProcessTypeEnum.RELEASE,
        processId,
        user,
        defaultReleaseProcessPriority,
        true);

    LOG.info("Created the first release process for dataflowId {}, dataProviderId {}, jobId {} and processId {} dataset id {} success: {}",
        dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId(), processId, firstDatasetId, isProcessCreated);

    // Build the snapshot payload exactly like the old release kickoff logic did.
    CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
    createSnapshotVO.setReleased(true);
    createSnapshotVO.setAutomatic(Boolean.TRUE);

    // Description is stored as CET text, while the release date sent downstream is UTC.
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date dateRelease = new Date();
    dateFormatter.setTimeZone(TimeZone.getTimeZone(LiteralConstants.EUROPE_ZONE_ID));
    createSnapshotVO.setDescription("Release " + dateFormatter.format(dateRelease) + " CET");
    dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

    LOG.info("Creating jobProcess for dataflowId {}, dataProviderId {}, jobId {} and release processId {}",
        dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId(), processId);

    // Persist the link between the RELEASE job and the first process.
    JobProcessVO jobProcessVO = new JobProcessVO(null, releaseJob.getId(), processId);
    jobProcessControllerZuul.save(jobProcessVO);

    LOG.info("Created jobProcess for dataflowId {}, dataProviderId {}, jobId {} and release processId {}",
        dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId(), processId);

    // Start the first actual snapshot creation. From here the standard one-by-one release
    // continuation takes over.
    datasetSnapshotService.addSnapshot(
        firstDatasetId,
        createSnapshotVO,
        null,
        dateFormatter.format(dateRelease),
        false,
        processId
    );
  }

  /**
   * Big-data blocker check.
   *
   * For big-data dataflows, blockers are stored in parquet validation output instead of the
   * normal validation schema, so they must be queried through Dremio.
   */
  private boolean hasBigDataBlockers(Long dataflowId, Long providerId, Long datasetId) {
    S3PathResolver s3PathResolver = new S3PathResolver(dataflowId, providerId, datasetId, S3_VALIDATION);
    if (!s3Helper.checkFolderExist(s3PathResolver, S3_VALIDATION_TABLE_PATH)) {
      return false;
    }
    StringBuilder blockersQueryBuilder = new StringBuilder();
    blockersQueryBuilder.append("select ")
        .append(PARQUET_RECORD_ID_COLUMN_HEADER)
        .append(" from ")
        .append(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH))
        .append(" where validation_level='BLOCKER' limit 1");
    SqlRowSet blockersRowSet = dremioJdbcTemplate.queryForRowSet(blockersQueryBuilder.toString());
    return blockersRowSet.next();
  }

  /**
   * Switches tenant to the dataset schema so validationRepository points to the correct
   * dataset_<id> validation tables.
   */
  private void setTenant(Long idDataset) {
    TenantResolver.setTenantName(String.format(DATASET_FORMAT_NAME, idDataset));
  }
}
