package org.eea.dataset.io.kafka.commands;

import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.utils.ProcessUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobHistoryController.JobHistoryControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class CheckBlockersDataSnapshotCommand extends AbstractEEAEventHandlerCommand {


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

  /** The process utils */
  @Autowired
  private ProcessUtils processUtils;

  /** The process controller zuul */
  @Autowired
  private ProcessControllerZuul processControllerZuul;

  /** The job controller zuul */
  @Autowired
  private JobControllerZuul jobControllerZuul;

  /** The job history controller zuul */
  @Autowired
  private JobHistoryControllerZuul jobHistoryControllerZuul;

  /** The job process controller zuul */
  @Autowired
  private JobProcessControllerZuul jobProcessControllerZuul;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CheckBlockersDataSnapshotCommand.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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

      LOG.info("The user on CheckBlockersDataSnapshotCommand.execute is {} and datasetId {}",
              SecurityContextHolder.getContext().getAuthentication().getName(), datasetId);

      // with one id we take all the datasets with the same dataProviderId and dataflowId
      DataSetMetabase dataset =
              dataSetMetabaseRepository.findById(datasetId).orElse(new DataSetMetabase());
      List<Long> datasets = dataSetMetabaseRepository.getDatasetIdsByDataflowIdAndDataProviderId(
              dataset.getDataflowId(), dataset.getDataProviderId());
      Collections.sort(datasets);

      Timestamp ts = new Timestamp(System.currentTimeMillis());
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("dataflowId", dataset.getDataflowId());
      parameters.put("dataProviderId", dataset.getDataProviderId());
      JobVO releaseJob = new JobVO(null, JobTypeEnum.RELEASE, JobStatusEnum.QUEUED, ts, ts, parameters, SecurityContextHolder.getContext().getAuthentication().getName(),true, dataset.getDataflowId(), dataset.getDataProviderId(), null);

      JobStatusEnum statusToInsert = jobControllerZuul.checkEligibilityOfJob(JobTypeEnum.VALIDATION.toString(), true, dataset.getDataflowId(), dataset.getDataProviderId(), datasets);
      if (statusToInsert == JobStatusEnum.REFUSED) {
        return;
      }

      LOG.info("Adding release job for dataflowId {}, dataProviderId {} and creator {} with status {}", dataset.getDataflowId(), dataset.getDataProviderId(), SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
      releaseJob = jobControllerZuul.save(releaseJob);
      jobHistoryControllerZuul.save(releaseJob);
      LOG.info("Added release job for dataflowId {}, dataProviderId {} and creator {} with status {} and jobId {}", dataset.getDataflowId(), dataset.getDataProviderId(), SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert, releaseJob.getId());

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
                          .user(SecurityContextHolder.getContext().getAuthentication().getName())
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

        jobControllerZuul.updateJobStatus(releaseJob.getId(), JobStatusEnum.IN_PROGRESS);

        LOG.info("Creating release process for dataflowId {}, dataProviderId {}, jobId {}", dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId());
        String processId = UUID.randomUUID().toString();
        ProcessVO processVO = processUtils.createProcessVOForRelease(dataset.getDataflowId(), datasets.get(0), processId);
        processVO = processControllerZuul.saveProcess(processVO);
        LOG.info("Created release process for dataflowId {}, dataProviderId {}, jobId {} and processId {}", dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId(), processVO.getProcessId());

        CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
        createSnapshotVO.setReleased(true);
        createSnapshotVO.setAutomatic(Boolean.TRUE);
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
        Date ahora = new Date();
        SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        createSnapshotVO.setDescription("Release " + formateador.format(ahora) + " CET");
        Date dateRelease = java.sql.Timestamp.valueOf(LocalDateTime.now());

        LOG.info("Creating jobProcess for dataflowId {}, dataProviderId {}, jobId {} and release processId {}", dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId(), processVO.getProcessId());
        JobProcessVO jobProcessVO = new JobProcessVO(null, releaseJob.getId(), processVO.getProcessId());
        jobProcessControllerZuul.save(jobProcessVO);
        LOG.info("Created jobProcess for dataflowId {}, dataProviderId {}, jobId {} and release processId {}", dataset.getDataflowId(), dataset.getDataProviderId(), releaseJob.getId(), processVO.getProcessId());

        LOG.info("Updating release process for dataflowId {}, dataProviderId {}, dataset {}, jobId {} and release processId {} to status IN_PROGRESS", dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), releaseJob.getId(), processVO.getProcessId());
        processVO.setStatus(ProcessStatusEnum.IN_PROGRESS.toString());
        processVO.setProcessStartingDate(new Date());
        processControllerZuul.saveProcess(processVO);
        LOG.info("Created release process for dataflowId {}, dataProviderId {}, dataset {}, jobId {} and release processId {} to status IN_PROGRESS", dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), releaseJob.getId(), processVO.getProcessId());

        datasetSnapshotService.addSnapshot(datasets.get(0), createSnapshotVO, null,
                dateRelease.toString(), false, processId);
      }
    } catch (Exception e) {
      LOG_ERROR.error("Unexpected error! Error executing event {}. Message: {}", eeaEventVO, e.getMessage());
      throw e;
    }
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
