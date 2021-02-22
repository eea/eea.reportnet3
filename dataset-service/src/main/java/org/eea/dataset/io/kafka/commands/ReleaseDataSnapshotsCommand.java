package org.eea.dataset.io.kafka.commands;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    Long nextData = datasetMetabaseService.getLastDatasetValidationForRelease(datasetId);
    if (null != nextData) {
      CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
      createSnapshotVO.setReleased(true);
      createSnapshotVO.setAutomatic(Boolean.TRUE);
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      Date ahora = new Date();
      SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      createSnapshotVO.setDescription("Release " + formateador.format(ahora));
      datasetSnapshotService.addSnapshot(nextData, createSnapshotVO, null);
    } else {
      DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);


      // now when all finish we create the file to save the data to public export
      DataFlowVO dataflowVO = dataflowControllerZuul.findById(dataset.getDataflowId());
      if (dataflowVO.isShowPublicInfo()) {
        try {
          datasetService.savePublicFiles(dataflowVO.getId(), dataset.getDataProviderId());
        } catch (IOException e) {
          LOG_ERROR.error("Folder not created in dataflow {} with dataprovider {} message {}",
              dataset.getDataflowId(), dataset.getDataProviderId(), e.getMessage(), e);
        }
      }

      // At this point the process of releasing all the datasets has been finished so we unlock
      // everything involved
      datasetSnapshotService.releaseLocksRelatedToRelease(dataset.getDataflowId(),
          dataset.getDataProviderId());

      LOG.info("Releasing datasets process ends. DataflowId: {} DataProviderId: {}",
          dataset.getDataflowId(), dataset.getDataProviderId());
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_COMPLETED_EVENT, null,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .dataflowId(dataset.getDataflowId()).dataflowName(dataflowVO.getName())
              .providerId(dataset.getDataProviderId()).build());
    }

  }
}
