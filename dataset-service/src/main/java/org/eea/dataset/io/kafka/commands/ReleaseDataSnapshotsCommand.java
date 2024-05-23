package org.eea.dataset.io.kafka.commands;

import io.jsonwebtoken.lang.Collections;
import org.eea.dataset.persistence.metabase.domain.ChangesEUDataset;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.repository.ChangesEUDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.collaboration.CollaborationController.CollaborationControllerZuul;
import org.eea.interfaces.controller.communication.EmailController.EmailControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.communication.EmailVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class ReleaseDataSnapshotsCommand extends AbstractEEAEventHandlerCommand {

  /** The dataset metabase controller. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset snapshot service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The email controller zuul. */
  @Autowired
  private EmailControllerZuul emailControllerZuul;

  /** The user management controller zuul. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZuul;

  /** The collaboration controller zuul. */
  @Autowired
  private CollaborationControllerZuul collaborationControllerZuul;

  /** The file treatment helper. */
  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  /** The changes EU dataset repository. */
  @Autowired
  private ChangesEUDatasetRepository changesEUDatasetRepository;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The data collection repository. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  @Autowired
  private ProcessControllerZuul processControllerZuul;

  /** The job process controller zuul */
  @Autowired
  private JobProcessControllerZuul jobProcessControllerZuul;

  /** The job controller zuul */
  @Autowired
  private JobControllerZuul jobControllerZuul;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ReleaseDataSnapshotsCommand.class);

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
    return EventType.RELEASE_ONEBYONE_COMPLETED_EVENT;
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
      String dateRelease = String.valueOf(eeaEventVO.getData().get("dateRelease"));
      String processId = String.valueOf(eeaEventVO.getData().get("process_id"));
      Long jobId = null;
      ProcessVO processVO = null;
      Boolean silentRelease = false;

      if (processId!=null) {
        jobId = jobProcessControllerZuul.findJobIdByProcessId(processId);
        processVO = processControllerZuul.findById(processId);

        if(jobId != null){
          JobVO jobVO = jobControllerZuul.findJobById(jobId);
          if(jobVO != null) {
            if (!jobVO.getJobStatus().equals(JobStatusEnum.IN_PROGRESS)) {
              return;
            } else {
              Map<String, Object> parameters = jobVO.getParameters();
              if (parameters.containsKey("silentRelease")) {
                silentRelease = (Boolean) parameters.get("silentRelease");
              }
            }
          }
        }
      }

      String user = processVO!=null ? processVO.getUser() : SecurityContextHolder.getContext().getAuthentication().getName();

      Long nextData = datasetMetabaseService.getLastDatasetForRelease(datasetId);
      DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);

      // Fill the table changes to eu_dataset and put there are changes in the DC data
      ChangesEUDataset providerRelease = new ChangesEUDataset();
      Optional<DataCollection> dc =
          dataCollectionRepository.findFirstByDatasetSchema(dataset.getDatasetSchema());
      if (dc.isPresent()) {
        providerRelease.setDatacollection(dc.get().getId());
      }
      DataProviderVO provider =
          representativeControllerZuul.findDataProviderById(dataset.getDataProviderId());
      providerRelease.setProvider(provider.getCode());
      changesEUDatasetRepository.saveAndFlush(providerRelease);

      if (null != nextData) {
        CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
        createSnapshotVO.setReleased(true);
        createSnapshotVO.setAutomatic(Boolean.TRUE);

        //force date description to CET
        DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter cetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("CET"));
        String cetReleaseDate = LocalDateTime.parse(dateRelease, utcFormatter).atZone(ZoneOffset.UTC).format(cetFormatter);
        createSnapshotVO.setDescription("Release " + cetReleaseDate + " CET");

        LOG.info("Creating release process for dataflowId {}, dataProviderId {} dataset {}, jobId {}", dataset.getDataflowId(), dataset.getDataProviderId(), nextData, jobId);
        String nextProcessId = UUID.randomUUID().toString();
        processControllerZuul.updateProcess(nextData, dataset.getDataflowId(),
                ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.RELEASE, nextProcessId,
                user, defaultReleaseProcessPriority, true);
        LOG.info("Created release process with processId {} for dataflowId {}, dataProviderId {} dataset {}, jobId {}", nextProcessId, dataset.getDataflowId(), dataset.getDataProviderId(), nextData, jobId);

        if (jobId!=null) {
          LOG.info("Creating jobProcess for dataflowId {}, dataProviderId {}, jobId {} and release processId {}", dataset.getDataflowId(), dataset.getDataProviderId(), jobId, nextProcessId);
          JobProcessVO jobProcessVO = new JobProcessVO(null, jobId, nextProcessId);
          jobProcessControllerZuul.save(jobProcessVO);
          LOG.info("Created jobProcess for dataflowId {}, dataProviderId {}, jobId {} and release processId {}", dataset.getDataflowId(), dataset.getDataProviderId(), jobId, nextProcessId);
        }

        LOG.info("Updating release process for dataflowId {}, dataProviderId {}, dataset {}, jobId {} and release processId {} to status IN_PROGRESS", dataset.getDataflowId(), dataset.getDataProviderId(), jobId, nextData, nextProcessId);
        processControllerZuul.updateProcess(nextData, dataset.getDataflowId(),
                ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.RELEASE, nextProcessId,
                user, defaultReleaseProcessPriority, true);
        LOG.info("Updated release process for dataflowId {}, dataProviderId {}, dataset {}, jobId {} and release processId {} to status IN_PROGRESS", dataset.getDataflowId(), dataset.getDataProviderId(), jobId, nextData, nextProcessId);

        datasetSnapshotService.addSnapshot(nextData, createSnapshotVO, null, dateRelease, false, nextProcessId);
      } else {

        // now when all finish we create the file to save the data to public export
        DataFlowVO dataflowVO = dataflowControllerZuul.getMetabaseById(dataset.getDataflowId());
        if (dataflowVO.isShowPublicInfo()) {
          try {
            fileTreatmentHelper.savePublicFiles(dataflowVO.getId(), dataset.getDataProviderId());
          } catch (IOException e) {
            LOG.error("Folder not created in dataflow {} with dataprovider {} and datasetId {} message {}",
                dataset.getDataflowId(), dataset.getDataProviderId(), datasetId, e.getMessage(), e);
          } catch (Exception e) {
            LOG.error("Unexpected error! Error creating folder for dataflow {} with dataprovider {}. Message {}",
                    dataset.getDataflowId(), dataset.getDataProviderId(), e.getMessage());
          }
        }

        // At this point the process of releasing all the datasets has been finished so we unlock
        // everything involved
        datasetSnapshotService.releaseLocksRelatedToRelease(dataset.getDataflowId(),
            dataset.getDataProviderId());

        if(!silentRelease) {
          // Send email to requesters
          sendMail(dateRelease, dataset, dataflowVO);
        }

        LOG.info("Releasing datasets process ends. DataflowId: {} DataProviderId: {} DatasetId: {}, JobId: {}",
            dataset.getDataflowId(), dataset.getDataProviderId(), datasetId, jobId);
        List<Long> datasetMetabaseListIds =
            datasetMetabaseService.getDatasetIdsByDataflowIdAndDataProviderId(dataflowVO.getId(),
                dataset.getDataProviderId());


        if(!silentRelease) {
          // we send different notification if we have more than one dataset or have only one to redirect
          if (!Collections.isEmpty(datasetMetabaseListIds) && datasetMetabaseListIds.size() > 1) {
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_PROVIDER_COMPLETED_EVENT,
                    null,
                    NotificationVO.builder()
                            .user(user)
                            .dataflowId(dataset.getDataflowId()).dataflowName(dataflowVO.getName())
                            .providerId(dataset.getDataProviderId()).build());


          } else {
            kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_COMPLETED_EVENT, null,
                    NotificationVO.builder()
                            .user(user)
                            .dataflowId(dataset.getDataflowId()).dataflowName(dataflowVO.getName())
                            .providerId(dataset.getDataProviderId()).build());
          }

          // send feedback message
          String country = dataset.getDataSetName();
          String dataflowName = dataflowVO.getName();

          MessageVO messageVO = new MessageVO();
          messageVO.setProviderId(dataset.getDataProviderId());
          messageVO.setContent(country + " released " + dataflowName + " successfully");
          messageVO.setAutomatic(true);
          collaborationControllerZuul.createMessage(dataflowVO.getId(), messageVO, user, jobId);
          LOG.info("Automatic feedback message created of dataflow {}, datasetId {} and jobId {}. Message: {}", dataflowVO.getId(), datasetId, jobId,
                  messageVO.getContent());
        }
      }
    } catch (Exception e) {
      LOG.error("Unexpected error! Error executing event {}. Message: {}", eeaEventVO, e.getMessage());
      throw new EEAException(e.getMessage());
    }
  }


  /**
   * Send mail.
   *
   * @param dateRelease the date release
   * @param dataset the dataset
   * @param dataflowVO the dataflow VO
   */
  private void sendMail(String dateRelease, DataSetMetabaseVO dataset, DataFlowVO dataflowVO) {
    try {
      // get custodian and stewards emails
      List<UserRepresentationVO> custodians = userManagementControllerZuul
              .getUsersByGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(dataflowVO.getId()));
      List<UserRepresentationVO> stewards = userManagementControllerZuul
              .getUsersByGroup(ResourceGroupEnum.DATAFLOW_STEWARD.getGroupName(dataflowVO.getId()));
      List<UserRepresentationVO> observers = userManagementControllerZuul
              .getUsersByGroup(ResourceGroupEnum.DATAFLOW_OBSERVER.getGroupName(dataflowVO.getId()));
      List<UserRepresentationVO> custodianSupport = userManagementControllerZuul.getUsersByGroup(
              ResourceGroupEnum.DATAFLOW_STEWARD_SUPPORT.getGroupName(dataflowVO.getId()));
      List<String> emails = new ArrayList<>();
      if (null != custodians) {
        custodians.stream().forEach(custodian -> emails.add(custodian.getEmail()));
      }
      if (null != stewards) {
        stewards.stream().forEach(steward -> emails.add(steward.getEmail()));
      }
      if (null != observers) {
        observers.stream().forEach(observer -> emails.add(observer.getEmail()));
      }
      if (null != custodianSupport) {
        custodianSupport.stream().forEach(support -> emails.add(support.getEmail()));
      }

      EmailVO emailVO = new EmailVO();
      emailVO.setBbc(emails);
      emailVO.setSubject(String.format(LiteralConstants.RELEASESUBJECT, dataset.getDataSetName(),
              dataflowVO.getName()));
      emailVO.setText(String.format(LiteralConstants.RELEASEMESSAGE, dataset.getDataSetName(),
              dataflowVO.getName(), dateRelease));
      emailControllerZuul.sendMessage(emailVO);
    } catch (Exception e) {
      Long dataflowId = (dataflowVO != null) ? dataflowVO.getId() : null;
      Long datasetId = (dataflowVO != null) ? dataset.getId() : null;
      LOG.error("Unexpected error! Error sending release mail for dataflowId {} and datasetId {}. Message: {}", dataflowId, datasetId, e.getMessage());
    }
  }
}
