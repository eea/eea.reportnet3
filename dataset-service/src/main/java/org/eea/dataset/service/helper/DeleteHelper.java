package org.eea.dataset.service.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * The Class FileTreatmentHelper.
 */
@Component
public class DeleteHelper {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DeleteHelper.class);

  /** The kafka sender helper. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;


  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /**
   * Instantiates a new file loader helper.
   */
  public DeleteHelper() {
    super();
  }



  /**
   * Execute delete table process.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @throws EEAException the EEA exception
   */
  @Async
  public void executeDeleteTableProcess(final Long datasetId, String tableSchemaId)
      throws EEAException {
    LOG.info("Deleting table {} from dataset {}", tableSchemaId, datasetId);
    datasetService.deleteTableBySchema(tableSchemaId, datasetId);

    EventType eventType =
        datasetService.isReportingDataset(datasetId) ? EventType.DELETE_TABLE_COMPLETED_EVENT
            : EventType.DELETE_TABLE_SCHEMA_COMPLETED_EVENT;

    // Release the lock manually
    List<Object> criteria = new ArrayList<>();
    criteria.add(tableSchemaId);
    criteria.add(LockSignature.DELETE_IMPORT_TABLE.getValue());
    criteria.add(datasetId);
    lockService.removeLockByCriteria(criteria);

    // after the table has been deleted, an event is sent to notify it
    Map<String, Object> value = new HashMap<>();
    NotificationVO notificationVO =
        NotificationVO.builder().user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
            .datasetId(datasetId).tableSchemaId(tableSchemaId).build();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
    kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
  }


  /**
   * Execute delete dataset process.
   *
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  @Async
  public void executeDeleteDatasetProcess(final Long datasetId) throws EEAException {
    LOG.info("Deleting data from dataset {}", datasetId);
    datasetService.deleteImportData(datasetId);

    // Release the lock manually
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.DELETE_DATASET_VALUES.getValue());
    criteria.add(datasetId);
    lockService.removeLockByCriteria(criteria);

    // after the dataset values have been deleted, an event is sent to notify it
    Map<String, Object> value = new HashMap<>();
    NotificationVO notificationVO =
        NotificationVO.builder().user(String.valueOf(ThreadPropertiesManager.getVariable("user")))
            .datasetId(datasetId).build();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETE_DATASET_DATA_COMPLETED_EVENT,
        value, notificationVO);
  }

}
