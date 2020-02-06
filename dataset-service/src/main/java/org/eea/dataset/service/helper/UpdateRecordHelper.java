package org.eea.dataset.service.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * The Class UpdateRecordHelper.
 */
@Component
public class UpdateRecordHelper extends KafkaSenderUtils {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UpdateRecordHelper.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The processes map. */
  private ConcurrentHashMap<String, Integer> processesMap;

  /** The field batch size. */
  @Value("${dataset.propagation.fieldBatchSize}")
  private int fieldBatchSize;


  /**
   * Instantiates a new file loader helper.
   */
  public UpdateRecordHelper() {
    super();
    processesMap = new ConcurrentHashMap<>();
  }


  /**
   * Execute update process.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @throws EEAException the EEA exception
   */
  public void executeUpdateProcess(final Long datasetId, List<RecordVO> records)
      throws EEAException {
    datasetService.updateRecords(datasetId, records);
    LOG.info("Records have been modified");
    // after the records have been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.RECORD_UPDATED_COMPLETED_EVENT, datasetId);
  }

  /**
   * Execute create process.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param idTableSchema the id table schema
   * @throws EEAException the EEA exception
   */
  public void executeCreateProcess(final Long datasetId, List<RecordVO> records,
      String idTableSchema) throws EEAException {
    datasetService.createRecords(datasetId, records, idTableSchema);
    LOG.info("Records have been created");
    // after the records have been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.RECORD_CREATED_COMPLETED_EVENT, datasetId);
  }


  /**
   * Execute delete process.
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   * @throws EEAException the EEA exception
   */
  public void executeDeleteProcess(Long datasetId, String recordId) throws EEAException {
    datasetService.deleteRecord(datasetId, recordId);
    LOG.info("Records have been deleted");
    // after the records have been deleted, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.RECORD_DELETED_COMPLETED_EVENT, datasetId);
  }


  /**
   * Execute field update process.
   *
   * @param datasetId the dataset id
   * @param field the field
   * @throws EEAException the EEA exception
   */
  public void executeFieldUpdateProcess(Long datasetId, FieldVO field) throws EEAException {
    datasetService.updateField(datasetId, field);
    LOG.info("Field is modified");
    // after the field has been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.FIELD_UPDATED_COMPLETED_EVENT, datasetId);
  }

  /**
   * Gets the processes map.
   *
   * @return the processes map
   */
  public ConcurrentHashMap<String, Integer> getProcessesMap() {
    return processesMap;
  }



  /**
   * Propagate new field design.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param sizeRecords the size records
   * @param numPag the num pag
   * @param uuId the uu id
   * @param idFieldSchema the id field schema
   * @param typeField the type field
   */
  @Async
  public void propagateNewFieldDesign(Long datasetId, String idTableSchema, Integer sizeRecords,
      Integer numPag, String uuId, String idFieldSchema, TypeData typeField) {

    synchronized (processesMap) {
      processesMap.put(uuId, 0);
    }

    if (fieldBatchSize != 0) {
      for (int numPage = 0; sizeRecords >= 0; sizeRecords = sizeRecords - fieldBatchSize) {
        releaseFieldPropagation(datasetId, uuId, numPage, idTableSchema, idFieldSchema, typeField);
        numPage++;
      }
    }



  }

  /**
   * Release field propagation.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   * @param numPag the num pag
   * @param idTableSchema the id table schema
   * @param idFieldSchema the id field schema
   * @param typeField the type field
   */
  public void releaseFieldPropagation(final Long datasetId, final String uuid, int numPag,
      String idTableSchema, String idFieldSchema, TypeData typeField) {

    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("idTableSchema", idTableSchema);
    value.put("numPag", numPag);
    value.put("idFieldSchema", idFieldSchema);
    value.put("typeField", typeField);
    value.put("uuId", uuid);
    synchronized (processesMap) {
      processesMap.merge(uuid, 1, Integer::sum);
    }
    releaseKafkaEvent(EventType.COMMAND_EXECUTE_NEW_DESIGN_FIELD_PROPAGATION, value);

  }



}
