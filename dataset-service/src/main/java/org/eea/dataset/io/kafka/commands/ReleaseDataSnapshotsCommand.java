package org.eea.dataset.io.kafka.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.EmailController.EmailControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.communication.EmailVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
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
import io.jsonwebtoken.lang.Collections;

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

  /** The dataset service. */
  @Autowired
  private DatasetService datasetService;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The email controller zuul. */
  @Autowired
  private EmailControllerZuul emailControllerZuul;

  /** The user management controller zuul. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZuul;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ReleaseDataSnapshotsCommand.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR =
      LoggerFactory.getLogger(ReleaseDataSnapshotsCommand.class);

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
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
    String dateRelease = String.valueOf(eeaEventVO.getData().get("dateRelease"));

    Long nextData = datasetMetabaseService.getLastDatasetValidationForRelease(datasetId);
    if (null != nextData) {
      CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
      createSnapshotVO.setReleased(true);
      createSnapshotVO.setAutomatic(Boolean.TRUE);
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      createSnapshotVO.setDescription("Release " + dateRelease + " CET");

      datasetSnapshotService.addSnapshot(nextData, createSnapshotVO, null, dateRelease, false);
    } else {
      DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);


      // now when all finish we create the file to save the data to public export
      DataFlowVO dataflowVO = dataflowControllerZuul.getMetabaseById(dataset.getDataflowId());
      if (dataflowVO.isShowPublicInfo()) {
        try {
          datasetService.savePublicFiles(dataflowVO.getId(), dataset.getDataProviderId());
        } catch (IOException e) {
          LOG_ERROR.error("Folder not created in dataflow {} with dataprovider {} message {}",
              dataset.getDataflowId(), dataset.getDataProviderId(), e.getMessage(), e);
        }
      }

      // Send email to requesters
      sendMail(dateRelease, dataset, dataflowVO);

      // At this point the process of releasing all the datasets has been finished so we unlock
      // everything involved
      datasetSnapshotService.releaseLocksRelatedToRelease(dataset.getDataflowId(),
          dataset.getDataProviderId());

      LOG.info("Releasing datasets process ends. DataflowId: {} DataProviderId: {}",
          dataset.getDataflowId(), dataset.getDataProviderId());


      List<Long> datasetMetabaseListIds =
          datasetMetabaseService.getDatasetIdsByDataflowIdAndDataProviderId(dataflowVO.getId(),
              dataset.getDataProviderId());

      // we send diferent notification if have morethan one dataset or have only one to redirect
      if (!Collections.isEmpty(datasetMetabaseListIds) && datasetMetabaseListIds.size() > 1) {
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_PROVIDER_COMPLETED_EVENT,
            null,
            NotificationVO.builder()
                .user(SecurityContextHolder.getContext().getAuthentication().getName())
                .dataflowId(dataset.getDataflowId()).dataflowName(dataflowVO.getName())
                .providerId(dataset.getDataProviderId()).build());
      } else {
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_COMPLETED_EVENT, null,
            NotificationVO.builder()
                .user(SecurityContextHolder.getContext().getAuthentication().getName())
                .dataflowId(dataset.getDataflowId()).dataflowName(dataflowVO.getName())
                .providerId(dataset.getDataProviderId()).build());
      }

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
    // get custodian and stewards emails
    List<UserRepresentationVO> custodians = userManagementControllerZuul
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(dataflowVO.getId()));
    List<UserRepresentationVO> stewards = userManagementControllerZuul
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_STEWARD.getGroupName(dataflowVO.getId()));
    List<UserRepresentationVO> observers = userManagementControllerZuul
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_OBSERVER.getGroupName(dataflowVO.getId()));
    List<UserRepresentationVO> custodianSupport = userManagementControllerZuul.getUsersByGroup(
        ResourceGroupEnum.DATAFLOW_CUSTODIAN_SUPPORT.getGroupName(dataflowVO.getId()));
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
  }
}
