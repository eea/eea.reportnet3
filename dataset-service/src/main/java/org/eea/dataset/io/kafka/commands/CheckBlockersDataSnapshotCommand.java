package org.eea.dataset.io.kafka.commands;

import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobHistoryController.JobHistoryControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
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

  /** The kafka sender utils. */
  @Lazy
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset snapshot service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /** The dataflow controller zuul */
  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  /** The job controller zuul */
  @Autowired
  private JobControllerZuul jobControllerZuul;

  /** The job history controller zuul */
  @Autowired
  private JobHistoryControllerZuul jobHistoryControllerZuul;

  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  @Autowired
  private AdminUserAuthorization adminUserAuthorization;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CheckBlockersDataSnapshotCommand.class);

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
      Boolean silentRelease = false;
      if (validationJobId!=null) {
        valJobVo = jobControllerZuul.findJobById(validationJobId);
        TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
        adminUserAuthorization.setAdminSecurityContextAuthenticationWithJobUserRoles(tokenVo, valJobVo);

        Map<String, Object> parameters = valJobVo.getParameters();
        if(parameters.containsKey("silentRelease")){
          silentRelease = (Boolean) parameters.get("silentRelease");
        }
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
      DataFlowVO dataflow = null;
      try{
        dataflow = dataFlowControllerZuul.getMetabaseById(dataset.getDataflowId());
        dataflowName = dataflow.getName();
      }
      catch (Exception e) {
        LOG.error("Error when trying to receive dataflow for dataflowId {} ", dataset.getDataflowId(), e);
      }


      String userId = valJobVo!=null ? (String) valJobVo.getParameters().get("userId") : null;
      Timestamp ts = new Timestamp(System.currentTimeMillis());
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("dataflowId", dataset.getDataflowId());
      parameters.put("dataProviderId", dataset.getDataProviderId());
      parameters.put("userId", userId);
      parameters.put("datasetId", datasets);
      parameters.put("silentRelease", silentRelease);
      parameters.put("validate", false);
      if(validationJobId != null){
        parameters.put("validationJobId", validationJobId);
      }

      JobVO releaseJob = new JobVO(null, JobTypeEnum.RELEASE, JobStatusEnum.QUEUED, ts, ts, parameters, user, true, dataset.getDataflowId(), dataset.getDataProviderId(), null, null, dataflowName, null, null, null, null);

      waitForValidationJobIfInProgress(validationJobId, 2000);

      JobStatusEnum statusToInsert = jobControllerZuul.checkEligibilityOfJob(JobTypeEnum.RELEASE.toString(), true, dataset.getDataflowId(), dataset.getDataProviderId(), datasets, validationJobId);
      if (statusToInsert == JobStatusEnum.REFUSED) {
        releaseJob.setJobStatus(JobStatusEnum.REFUSED);
        addReleaseJob(user, dataset, releaseJob, statusToInsert);
        datasetSnapshotService.releaseLocksRelatedToRelease(dataset.getDataflowId(), dataset.getDataProviderId());
        if(!silentRelease) {
          //send Refused notification
          Map<String, Object> value = new HashMap<>();
          value.put(LiteralConstants.USER, user);
          value.put("release_job_id", releaseJob.getId());
          kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_REFUSED_EVENT, value,
              NotificationVO.builder().user(user).dataflowId(dataset.getDataflowId()).providerId(dataset.getDataProviderId())
                  .error("There is another job with status QUEUED or IN_PROGRESS for dataflowId " + dataset.getDataflowId() + " and providerId " + dataset.getDataProviderId()).build());
          return;
        }
        else{
          LOG.info("Sending SILENT_RELEASE_FAILED_EVENT event for jobId {}", releaseJob.getId());
          //this event will not produce any notifications to the user because frontend will never show it in the user notifications
          Map<String, Object> value = new HashMap<>();
          value.put(LiteralConstants.USER, user);
          value.put("release_job_id", releaseJob.getId());
          kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.SILENT_RELEASE_FAILED_EVENT, value,
              NotificationVO.builder().user(user).dataflowId(dataset.getDataflowId()).providerId(dataset.getDataProviderId())
                  .error("There is another job with status QUEUED or IN_PROGRESS for dataflowId " + dataset.getDataflowId() + " and providerId " + dataset.getDataProviderId()).build());
          return;
        }
      }
      releaseJob = addReleaseJob(user, dataset, releaseJob, statusToInsert);


    } catch (Exception e) {
      LOG.error("Unexpected error! Error executing event {}. Message: {}", eeaEventVO, e.getMessage());
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
   * Waits briefly if the given validation job is still in progress,
   * to avoid race conditions when triggering the release process.
   *
   * @param validationJobId the ID of the validation job to check
   * @param waitMillis the time to wait in milliseconds if the job is still running
   */
  private void waitForValidationJobIfInProgress(Long validationJobId, long waitMillis) {
    if (validationJobId == null) {
      return;
    }

    try {
      JobVO validationJob = jobControllerZuul.findJobById(validationJobId);
      if (validationJob != null && JobStatusEnum.IN_PROGRESS.equals(validationJob.getJobStatus())) {
        LOG.info("Validation job {} still in progress. Sleeping {} ms before release eligibility check.",
            validationJobId, waitMillis);
        Thread.sleep(waitMillis);
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      LOG.warn("Sleep interrupted while waiting for validation job {} to finish", validationJobId);
    } catch (Exception e) {
      LOG.warn("Error checking validation job {} status before release: {}", validationJobId, e.getMessage());
    }
  }


}
