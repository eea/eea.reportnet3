package org.eea.validation.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationHelper.
 */
@Component
public class ValidationHelper {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationHelper.class);

  /**
   * The kafka sender utils.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  @Autowired
  private TableRepository tableRepository;

  /**
   * The validation service.
   */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /** The processes map. */
  private ConcurrentHashMap<String, Integer> processesMap;

  /** The field batch size. */
  @Value("${validation.fieldBatchSize}")
  private int fieldBatchSize;

  /** The record batch size. */
  @Value("${validation.recordBatchSize}")
  private int recordBatchSize;

  /**
   * Instantiates a new file loader helper.
   */
  public ValidationHelper() {
    super();
    processesMap = new ConcurrentHashMap<>();
  }

  /**
   * Execute file process.
   *
   * @param datasetId the dataset id
   * @param uuId the main process id
   * @throws EEAException the EEA exception
   */
  @Async
  public void executeValidation(final Long datasetId, String uuId) throws EEAException {
    processesMap.put(uuId, 0);
    TenantResolver.setTenantName("dataset_" + datasetId);
    LOG.info("Deleting all Validations");
    validationService.deleteAllValidation(datasetId);

    LOG.info("Validating Dataset");
    releaseDatasetValidation(datasetId, uuId);
    LOG.info("Validating Tables");
    releaseTableValidation(datasetId, uuId);
    LOG.info("Validating Records");
    releaseRecordsValidation(datasetId, uuId);
    LOG.info("Validating Fields");
    releaseFieldsValidation(datasetId, uuId);

  }

  /**
   * Release fields validation.
   *
   * @param datasetId the dataset id
   * @param uuId the uu id
   * @param kieBase the kie base
   */
  private void releaseFieldsValidation(Long datasetId, String uuId) {
    Integer totalFields = validationService.countFieldsDataset(datasetId);
    for (int i = 0; totalFields >= 0; totalFields = totalFields - fieldBatchSize) {
      releaseFieldValidation(datasetId, uuId, i);
      i++;
    }
  }

  /**
   * Release records validation.
   *
   * @param datasetId the dataset id
   * @param uuId the uu id
   * @param kieBase the kie base
   */
  private void releaseRecordsValidation(Long datasetId, String uuId) {
    Integer totalRecords = validationService.countRecordsDataset(datasetId);
    for (int i = 0; totalRecords >= 0; totalRecords = totalRecords - recordBatchSize) {
      releaseRecordValidation(datasetId, uuId, i);
      i++;
    }
  }


  private void releaseTableValidation(Long datasetId, String uuId) {
    TenantResolver.setTenantName("dataset_" + datasetId);
    Integer totalTables = tableRepository.findAllTables().size();
    List<TableValue> tableList = tableRepository.findAll();
    for (int i = 0; totalTables > 0; totalTables = totalTables - 1) {
      Long idTable = tableList.get(i).getId();
      releaseTableValidation(datasetId, uuId, idTable);
      i++;
    }
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
   * Release dataset validation.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   */
  public void releaseDatasetValidation(final Long datasetId, final String uuid) {
    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("uuid", uuid);
    processesMap.merge(uuid, 1, Integer::sum);
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATE_DATASET, value);
  }

  /**
   * Release table validation.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   */
  public void releaseTableValidation(final Long datasetId, final String uuid, Long Tablenum) {
    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("uuid", uuid);
    value.put("idTable", Tablenum);
    processesMap.merge(uuid, 1, Integer::sum);
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATE_TABLE, value);
  }

  /**
   * Release record validation.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   * @param numPag the numPag
   */
  public void releaseRecordValidation(final Long datasetId, final String uuid, int numPag) {
    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("uuid", uuid);
    value.put("numPag", numPag);
    processesMap.merge(uuid, 1, Integer::sum);
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATE_RECORD, value);
  }

  /**
   * Release field validation.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   * @param numPag the numPag
   */
  public void releaseFieldValidation(final Long datasetId, final String uuid, int numPag) {
    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("uuid", uuid);
    value.put("numPag", numPag);
    processesMap.merge(uuid, 1, Integer::sum);
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATE_FIELD, value);
  }

  /**
   * Check finished validations.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   * @param kieBase the kie base
   * @throws EEAException the EEA exception
   */

  public void checkFinishedValidations(final Long datasetId, final String uuid)
      throws EEAException {
    if (processesMap.get(uuid) == 0) {
      LOG.info("scaling errors");
      validationService.errorScale(datasetId);
      // after the dataset has been saved, an event is sent to notify it
      processesMap.remove(uuid);
      kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.VALIDATION_FINISHED_EVENT, datasetId);
    }
  }

}
