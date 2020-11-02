package org.eea.dataset.io.kafka.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class ReleaseDataSnapshotsCommand extends AbstractEEAEventHandlerCommand {


  /** The dataset metabase controller. */
  @Autowired
  private DatasetMetabaseController datasetMetabaseController;

  /** The dataset snapshot controller. */
  @Autowired
  private DatasetSnapshotController datasetSnapshotController;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;


  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

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

    Long nextData = datasetMetabaseController.lastDatasetValidationForReleasingById(datasetId);
    if (null != nextData) {
      CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
      createSnapshotVO.setReleased(true);
      createSnapshotVO.setAutomatic(Boolean.TRUE);
      Date ahora = new Date();
      SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      createSnapshotVO.setDescription("Release " + formateador.format(ahora));
      datasetSnapshotController.createSnapshot(nextData, createSnapshotVO);
    } else {
      DataSetMetabase dataset = dataSetMetabaseRepository.findById(datasetId).get();
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_COMPLETED_EVENT, null,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .dataflowId(dataset.getDataflowId()).providerId(dataset.getDataProviderId()).build());
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
