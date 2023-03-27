package org.eea.dataset.io.kafka.commands;

import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobHistoryController.JobHistoryControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.security.authorization.AdminUserAuthorization;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class CheckBlockersDataSnapshotCommand extends AbstractEEAEventHandlerCommand {

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

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The validation repository. */
  @Autowired
  private ValidationRepository validationRepository;

  /** The kafka sender utils. */
  @Lazy
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset snapshot service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /** The process controller zuul */
  @Autowired
  private ProcessControllerZuul processControllerZuul;


  /** The dataflow controller zuul */
  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  /** The job controller zuul */
  @Autowired
  private JobControllerZuul jobControllerZuul;

  /** The job history controller zuul */
  @Autowired
  private JobHistoryControllerZuul jobHistoryControllerZuul;

  /** The job process controller zuul */
  @Autowired
  private JobProcessControllerZuul jobProcessControllerZuul;

  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  @Autowired
  private AdminUserAuthorization adminUserAuthorization;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CheckBlockersDataSnapshotCommand.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The default release process priority
   */
  private int defaultReleaseProcessPriority = 20;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.VALIDATION_RELEASE_FINISHED_EVENT;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {

    try {
      Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
      Long validationJobId = null;
      if (eeaEventVO.getData().get("validation_job_id")!=null) {
        validationJobId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("validation_job_id")));
      }
      JobVO valJobVo = null;
      List<LinkedHashMap<String, String>> authorities = new ArrayList<>();
      if (validationJobId!=null) {
        valJobVo = jobControllerZuul.findJobById(validationJobId);
        TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
        adminUserAuthorization.setAdminSecurityContextAuthenticationWithJobUserRoles(tokenVo, valJobVo);
      }
      String user = valJobVo!=null ? valJobVo.getCreatorUsername() : SecurityContextHolder.getContext().getAuthentication().getName();
      LOG.info("The user on CheckBlockersDataSnapshotCommand.execute is {} and datasetId {}", user, datasetId);

      // with one id we take all the datasets with the same dataProviderId and dataflowId
      DataSetMetabase dataset =
              dataSetMetabaseRepository.findById(datasetId).orElse(new DataSetMetabase());
      List<Long> datasets = dataSetMetabaseRepository.getDatasetIdsByDataflowIdAndDataProviderId(
              dataset.getDataflowId(), dataset.getDataProviderId());
      Collections.sort(datasets);

      String dataflowName = null;
      try{
        dataflowName = dataFlowControllerZuul.findDataflowNameById(dataset.getDataflowId());
      }
      catch (Exception e) {
        LOG.error("Error when trying to receive dataflow name for dataflowId {} ", dataset.getDataflowId(), e);
      }


      String userId = valJobVo!=null ? (String) valJobVo.getParameters().get("userId") : null;
      Timestamp ts = new Timestamp(System.currentTimeMillis());
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("dataflowId", dataset.getDataflowId());
      parameters.put("dataProviderId", dataset.getDataProviderId());
      parameters.put("userId", userId);
      parameters.put("datasetId", datasets);
      JobVO releaseJob = new JobVO(null, JobTypeEnum.RELEASE, JobStatusEnum.IN_PROGRESS, ts, ts, parameters, user,true, dataset.getDataflowId(), dataset.getDataProviderId(), null,null, dataflowName,null, null);

      JobStatusEnum statusToInsert = jobControllerZuul.checkEligibilityOfJob(JobTypeEnum.RELEASE.toString(), true, dataset.getDataflowId(), dataset.getDataProviderId(), datasets);
      if (statusToInsert == JobStatusEnum.REFUSED) {
        releaseJob.setJobStatus(JobStatusEnum.REFUSED);
        addReleaseJob(user, dataset, releaseJob, statusToInsert);
        datasetSnapshotService.releaseLocksRelatedToRelease(dataset.getDataflowId(), dataset.getDataProviderId());
        //send Refused notification
        Map<String, Object> value = new HashMap<>();
        value.put(LiteralConstants.USER, user);
        value.put("release_job_id", releaseJob.getId());
          kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_REFUSED_EVENT, value,
                  NotificationVO.builder().user(user).dataflowId(dataset.getDataflowId()).providerId(dataset.getDataProviderId())
                          .error("There is another job with status QUEUED or IN_PROGRESS for dataflowId " + dataset.getDataflowId() + " and providerId " + dataset.getDataProviderId()).build());
        return;
      }
      releaseJob = addReleaseJob(user, dataset, releaseJob, statusToInsert);

      // we check if one or more dataset have error, if have we create a notification and abort
      // process of releasing
      boolean haveBlockers = false;
      for (Long id : datasets) {
        setTenant(id);
        if (validationRepository.existsByLevelError(ErrorTypeEnum.BLOCKER)) {
          haveBlockers = true;
          // Release the locks
          datasetSnapshotService.releaseLocksRelatedToRelease(dataset.getDataflowId(),
                  dataset.getDataProviderId());
          LOG_ERROR.error(
                  "Error in the releasing process of the dataflowId {}, dataProviderId {} and jobId {}, the datasets have blocker errors",
                  dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId());

          releaseJob.setJobStatus(JobStatusEnum.FAILED);
          jobControllerZuul.updateJobStatus(releaseJob.getId(), JobStatusEnum.FAILED);

          kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_BLOCKERS_FAILED_EVENT, null,
                  NotificationVO.builder()
                          .user(user)
                          .datasetId(datasetId)
                          .error("One or more datasets have blockers errors, Release aborted")
                          .providerId(dataset.getDataProviderId()).build());
          break;
        }
      }
      // If none blocker errors has found, we have to release datasets one by one
      if (!haveBlockers) {
        LOG.info(
                "Releasing datasets process continues. At this point, the datasets from the dataflowId {}, dataProviderId {} and jobId {} have no blockers",
                dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId());

        LOG.info("Creating release process for dataflowId {}, dataProviderId {}, jobId {}", dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId());
        String processId = UUID.randomUUID().toString();
        processControllerZuul.updateProcess(datasets.get(0), dataset.getDataflowId(),
                ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.RELEASE, processId, user, defaultReleaseProcessPriority, true);
        LOG.info("Created release process for dataflowId {}, dataProviderId {}, jobId {} and processId {}", dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId(), processId);

        CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
        createSnapshotVO.setReleased(true);
        createSnapshotVO.setAutomatic(Boolean.TRUE);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //force date to CET
        Date dateRelease = java.sql.Timestamp.valueOf(LocalDateTime.now(ZoneId.of("CET")));
        createSnapshotVO.setDescription("Release " + dateFormatter.format(dateRelease) + " CET");

        LOG.info("Creating jobProcess for dataflowId {}, dataProviderId {}, jobId {} and release processId {}", dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId(), processId);
        JobProcessVO jobProcessVO = new JobProcessVO(null, releaseJob.getId(), processId);
        jobProcessControllerZuul.save(jobProcessVO);
        LOG.info("Created jobProcess for dataflowId {}, dataProviderId {}, jobId {} and release processId {}", dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId(), processId);

        LOG.info("Updating release process for dataflowId {}, dataProviderId {}, dataset {}, jobId {} and release processId {} to status IN_PROGRESS", dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), releaseJob.getId(), processId);
        processControllerZuul.updateProcess(datasets.get(0), dataset.getDataflowId(),
                ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.RELEASE, processId, user, defaultReleaseProcessPriority, true);
        LOG.info("Updated release process for dataflowId {}, dataProviderId {}, dataset {}, jobId {} and release processId {} to status IN_PROGRESS", dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), releaseJob.getId(), processId);

        datasetSnapshotService.addSnapshot(datasets.get(0), createSnapshotVO, null,
                dateRelease.toString(), false, processId);
      }
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Error executing event {}. Message: {}", eeaEventVO, e.getMessage());
      throw e;
    }
  }

  private JobVO addReleaseJob(String user, DataSetMetabase dataset, JobVO releaseJob, JobStatusEnum statusToInsert) {
    LOG.info("Adding release job for dataflowId {}, dataProviderId {} and creator {} with status {}", dataset.getDataflowId(), dataset.getDataProviderId(), user, statusToInsert);
    releaseJob = jobControllerZuul.save(releaseJob);
    jobHistoryControllerZuul.save(releaseJob);
    LOG.info("Added release job for dataflowId {}, dataProviderId {} and creator {} with status {} and jobId {}", dataset.getDataflowId(), dataset.getDataProviderId(), user, statusToInsert, releaseJob.getId());
    return releaseJob;
  }

  /**
   * Sets the tenant.
   *
   * @param idDataset the new tenant
   */
  private void setTenant(Long idDataset) {
    TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, idDataset));
  }

}
