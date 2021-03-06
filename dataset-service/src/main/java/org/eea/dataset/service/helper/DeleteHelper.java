package org.eea.dataset.service.helper;

import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The Class FileTreatmentHelper.
 */
@Component
public class DeleteHelper {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
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
   */
  @Async
  public void executeDeleteTableProcess(final Long datasetId, String tableSchemaId) {
    LOG.info("Deleting table {} from dataset {}", tableSchemaId, datasetId);
    datasetService.deleteTableBySchema(tableSchemaId, datasetId);

    EventType eventType = DatasetTypeEnum.REPORTING.equals(datasetService.getDatasetType(datasetId))
        ? EventType.DELETE_TABLE_COMPLETED_EVENT
        : EventType.DELETE_TABLE_SCHEMA_COMPLETED_EVENT;

    // Release the lock manually
    Map<String, Object> deleteImportTable = new HashMap<>();
    deleteImportTable.put(LiteralConstants.SIGNATURE, LockSignature.DELETE_IMPORT_TABLE.getValue());
    deleteImportTable.put(LiteralConstants.DATASETID, datasetId);
    deleteImportTable.put(LiteralConstants.TABLESCHEMAID, tableSchemaId);
    lockService.removeLockByCriteria(deleteImportTable);

    // after the table has been deleted, an event is sent to notify it
    Map<String, Object> value = new HashMap<>();
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(datasetId)
        .tableSchemaId(tableSchemaId).build();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error releasing notification: {}", e.getMessage(), e);
    }
  }


  /**
   * Execute delete dataset process.
   *
   * @param datasetId the dataset id
   */
  @Async
  public void executeDeleteDatasetProcess(final Long datasetId) {
    LOG.info("Deleting data from dataset {}", datasetId);
    datasetService.deleteImportData(datasetId);

    // Release the lock manually
    Map<String, Object> deleteDatasetValues = new HashMap<>();
    deleteDatasetValues.put(LiteralConstants.SIGNATURE,
        LockSignature.DELETE_DATASET_VALUES.getValue());
    deleteDatasetValues.put(LiteralConstants.DATASETID, datasetId);
    lockService.removeLockByCriteria(deleteDatasetValues);

    // after the dataset values have been deleted, an event is sent to notify it
    Map<String, Object> value = new HashMap<>();
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(datasetId)
        .build();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DELETE_DATASET_DATA_COMPLETED_EVENT,
          value, notificationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error releasing notification: {}", e.getMessage());
    }
  }


  /**
   * Execute delete import data async before replacing.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   * @param operation the operation
   */
  @Async
  public void executeDeleteImportDataAsyncBeforeReplacing(Long datasetId, Long integrationId,
      IntegrationOperationTypeEnum operation) {
    datasetService.deleteImportData(datasetId);
    LOG.info("All data value deleted from datasetId {}. Next step call the FME by the kafka event",
        datasetId);
    // Send the kafka event after deleting to call FME
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    value.put(LiteralConstants.INTEGRATION_ID, integrationId);
    value.put(LiteralConstants.OPERATION, operation);
    kafkaSenderUtils.releaseKafkaEvent(EventType.DATA_DELETE_TO_REPLACE_COMPLETED_EVENT, value);
  }

}
