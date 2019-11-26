package org.eea.dataset.service.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
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
public class FileTreatmentHelper {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FileTreatmentHelper.class);

  /**
   * The kafka sender helper.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;


  /**
   * The dataset service.
   */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /**
   * The data set mapper.
   */
  @Autowired
  private DataSetMapper dataSetMapper;

  @Autowired
  private LockService lockService;

  /**
   * Instantiates a new file loader helper.
   */
  public FileTreatmentHelper() {
    super();
  }

  /**
   * Execute file process.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the input stream
   * @param tableSchemaId the id table schema
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   */
  @Async
  public void executeFileProcess(final Long datasetId, final String fileName, final InputStream is,
      String tableSchemaId, String user) throws EEAException, IOException, InterruptedException {
    LOG.info("Processing file");
    DataSetVO datasetVO = datasetService.processFile(datasetId, fileName, is, tableSchemaId);

    // map the VO to the entity
    datasetVO.setId(datasetId);
    final DatasetValue dataset = dataSetMapper.classToEntity(datasetVO);
    if (dataset == null) {
      throw new IOException("Error mapping file");
    }

    // **********************************************************************************
    // **********************************************************************************

    // Save empty table
    List<RecordValue> allRecords = dataset.getTableValues().get(0).getRecords();
    dataset.getTableValues().get(0).setRecords(new ArrayList<>());

    // Check if the table with idTableSchema has been populated already
    Long oldTableId = datasetService.findTableIdByTableSchema(datasetId, tableSchemaId);
    fillTableId(tableSchemaId, dataset.getTableValues(), oldTableId);

    if (null == oldTableId) {
      datasetService.saveTable(datasetId, dataset.getTableValues().get(0));
    }

    List<List<RecordValue>> listaGeneral = getListOfRecords(allRecords);

    listaGeneral.parallelStream().forEach(value -> {
      datasetService.saveAllRecords(datasetId, value);
    });

    LOG.info("File processed and saved into DB");

    // Release the lock manually
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.LOAD_TABLE.getValue());
    criteria.add(datasetId);
    criteria.add(tableSchemaId);
    lockService.removeLockByCriteria(criteria);

    // after the dataset has been saved, an event is sent to notify it
    Map<String, Object> notification = new HashMap<>();
    notification.put("user", user);
    notification.put("datasetId", datasetId);
    notification.put("tableSchemaId", tableSchemaId);
    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("notification", notification);
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
    kafkaSenderUtils.releaseKafkaEvent(EventType.LOAD_DATA_COMPLETED_EVENT, value);
  }

  /**
   * Fill table id.
   *
   * @param idTableSchema the id table schema
   * @param listTableValues the list table values
   * @param oldTableId the old table id
   */
  private void fillTableId(final String idTableSchema, final List<TableValue> listTableValues,
      Long oldTableId) {
    if (oldTableId != null) {
      listTableValues.stream()
          .filter(tableValue -> tableValue.getIdTableSchema().equals(idTableSchema))
          .forEach(tableValue -> tableValue.setId(oldTableId));
    }
  }

  /**
   * Gets the list of records.
   *
   * @param allRecords the all records
   *
   * @return the list of records
   */
  private List<List<RecordValue>> getListOfRecords(List<RecordValue> allRecords) {
    List<List<RecordValue>> generalList = new ArrayList<>();
    // lists size
    final int BATCH = 1000;

    // dividing the number of records in different lists
    int nLists = (int) Math.ceil(allRecords.size() / (double) BATCH);
    if (nLists > 1) {
      for (int i = 0; i < (nLists - 1); i++) {
        generalList.add(new ArrayList<>(allRecords.subList(BATCH * i, BATCH * (i + 1))));
      }
    }
    generalList.add(new ArrayList<>(allRecords.subList(BATCH * (nLists - 1), allRecords.size())));

    return generalList;
  }
}
