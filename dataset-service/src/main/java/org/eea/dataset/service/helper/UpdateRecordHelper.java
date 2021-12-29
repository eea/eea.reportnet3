package org.eea.dataset.service.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
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

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;


  /** The field batch size. */
  @Value("${dataset.propagation.fieldBatchSize}")
  private int fieldBatchSize;


  /**
   * Instantiates a new file loader helper.
   */
  public UpdateRecordHelper() {
    super();
  }


  /**
   * Execute update process.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param updateCascadePK the update cascade PK
   * @throws EEAException the EEA exception
   */
  public void executeUpdateProcess(final Long datasetId, List<RecordVO> records,
      boolean updateCascadePK) throws EEAException {
    datasetService.updateRecords(datasetId, records, updateCascadePK);
    LOG.info("Records have been modified");
    // after the records have been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.RECORD_UPDATED_COMPLETED_EVENT, datasetId);
  }

  /**
   * Execute create process.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param tableSchemaId the id table schema
   * @throws EEAException the EEA exception
   */
  public void executeCreateProcess(final Long datasetId, List<RecordVO> records,
      String tableSchemaId) throws EEAException {
    datasetService.insertRecords(datasetId, records, tableSchemaId);
    LOG.info("Records have been created");
    // after the records have been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.RECORD_CREATED_COMPLETED_EVENT, datasetId);
  }

  /**
   * Execute multi create process.
   *
   * @param datasetId the dataset id
   * @param tableRecords the table records
   * @throws EEAException the EEA exception
   */
  public void executeMultiCreateProcess(final Long datasetId, List<TableVO> tableRecords)
      throws EEAException {
    for (TableVO tableVO : tableRecords) {
      datasetService.insertRecords(datasetId, tableVO.getRecords(), tableVO.getIdTableSchema());
    }
    LOG.info("Records have been created");
    // after the records have been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.RECORD_CREATED_COMPLETED_EVENT, datasetId);
  }

  /**
   * Execute delete process.
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   * @param deleteCascadePK the delete cascade PK
   * @throws EEAException the EEA exception
   */
  public void executeDeleteProcess(Long datasetId, String recordId, boolean deleteCascadePK)
      throws EEAException {
    datasetService.deleteRecord(datasetId, recordId, deleteCascadePK);
    LOG.info("Records have been deleted");
    // after the records have been deleted, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.RECORD_DELETED_COMPLETED_EVENT, datasetId);
  }


  /**
   * Execute field update process.
   *
   * @param datasetId the dataset id
   * @param field the field
   * @param updateCascadePK the update cascade PK
   * @throws EEAException the EEA exception
   */
  public void executeFieldUpdateProcess(Long datasetId, FieldVO field, boolean updateCascadePK)
      throws EEAException {
    datasetService.updateField(datasetId, field, updateCascadePK);
    LOG.info("Field is modified");
    // now the view is not updated, update the check to false
    datasetService.updateCheckView(datasetId, false);
    // after the field has been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.FIELD_UPDATED_COMPLETED_EVENT, datasetId);
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
      Integer numPag, String uuId, String idFieldSchema, DataType typeField) {

    if (fieldBatchSize != 0) {
      Set<Integer> pages = new HashSet<>();
      pages.add(0);
      for (int numPage = 0; sizeRecords >= 0; sizeRecords = sizeRecords - fieldBatchSize) {
        numPage++;
        pages.add(numPage);
      }
      releaseFieldPropagation(datasetId, uuId, pages, idTableSchema, idFieldSchema, typeField);
    }
  }

  /**
   * Release field propagation.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   * @param pages the pages
   * @param idTableSchema the id table schema
   * @param idFieldSchema the id field schema
   * @param typeField the type field
   */
  public void releaseFieldPropagation(final Long datasetId, final String uuid, Set<Integer> pages,
      String idTableSchema, String idFieldSchema, DataType typeField) {

    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("idTableSchema", idTableSchema);
    value.put("pages", pages);
    value.put("idFieldSchema", idFieldSchema);
    value.put("typeField", typeField);
    value.put("uuId", uuid);
    value.put("numPag", 0);

    releaseKafkaEvent(EventType.COMMAND_EXECUTE_NEW_DESIGN_FIELD_PROPAGATION, value);



  }


}
