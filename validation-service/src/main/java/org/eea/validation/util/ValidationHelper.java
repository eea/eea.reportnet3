package org.eea.validation.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.service.ValidationService;
import org.kie.api.KieBase;
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

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /**
   * The validation service.
   */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /**
   * The processes map.
   */
  private ConcurrentHashMap<String, Integer> processesMap;

  /** The drools active sessions. */
  private Map<String, KieBase> droolsActiveSessions;

  /**
   * The field batch size.
   */
  @Value("${validation.fieldBatchSize}")
  private int fieldBatchSize;

  /**
   * The record batch size.
   */
  @Value("${validation.recordBatchSize}")
  private int recordBatchSize;

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;


  /**
   * Instantiates a new file loader helper.
   */
  public ValidationHelper() {
    super();
    processesMap = new ConcurrentHashMap<>();
    droolsActiveSessions = new ConcurrentHashMap<>();
  }

  /**
   * Gets kie base for given process.
   *
   * @param processId the process id
   * @param datasetId the dataset id
   *
   * @return the kie base
   *
   * @throws EEAException the eea exception
   */
  public KieBase getKieBase(String processId, Long datasetId) throws EEAException {
    KieBase kieBase = null;
    synchronized (droolsActiveSessions) {
      if (!droolsActiveSessions.containsKey(processId)) {
        LOG.info("KieBase created for process {}", processId);
        droolsActiveSessions.put(processId, validationService.loadRulesKnowledgeBase(datasetId));
      }
      kieBase = droolsActiveSessions.get(processId);
    }
    return kieBase;
  }

  /**
   * Remove kie base after given process finishes.
   *
   * @param processId the process id
   */
  public void removeKieBase(String processId) {
    synchronized (droolsActiveSessions) {
      if (droolsActiveSessions.containsKey(processId)) {
        LOG.info("KieBase removed for process {}", processId);
        droolsActiveSessions.remove(processId);

      }
    }
  }

  /**
   * Execute validation. The lock would be released on ValidationHelper.checkFinishedValidations(..)
   *
   * @param datasetId the dataset id
   * @param uuId the uu id
   * @throws EEAException the EEA exception
   */
  @Async
  @LockMethod(removeWhenFinish = false, isController = false)
  public void executeValidation(@LockCriteria(name = "datasetId") final Long datasetId,
      String uuId) {
    synchronized (processesMap) {
      processesMap.put(uuId, 0);
    }
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
   */
  private void releaseFieldsValidation(Long datasetId, String uuId) {
    Integer totalFields = validationService.countFieldsDataset(datasetId);
    if (fieldBatchSize != 0) {
      for (int i = 0; totalFields >= 0; totalFields = totalFields - fieldBatchSize) {
        releaseFieldValidation(datasetId, uuId, i);
        i++;
      }
    }
  }

  /**
   * Release records validation.
   *
   * @param datasetId the dataset id
   * @param uuId the uu id
   */
  private void releaseRecordsValidation(Long datasetId, String uuId) {
    Integer totalRecords = validationService.countRecordsDataset(datasetId);
    if (recordBatchSize != 0) {
      for (int i = 0; totalRecords >= 0; totalRecords = totalRecords - recordBatchSize) {
        releaseRecordValidation(datasetId, uuId, i);
        i++;
      }
    }
  }


  /**
   * Release table validation.
   *
   * @param datasetId the dataset id
   * @param uuId the uu id
   */
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
    synchronized (processesMap) {
      processesMap.merge(uuid, 1, Integer::sum);
    }
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATE_DATASET, value);
  }

  /**
   * Release table validation.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   * @param Tablenum the tablenum
   */
  public void releaseTableValidation(final Long datasetId, final String uuid, Long Tablenum) {
    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("uuid", uuid);
    value.put("idTable", Tablenum);
    synchronized (processesMap) {
      processesMap.merge(uuid, 1, Integer::sum);
    }

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
    synchronized (processesMap) {
      processesMap.merge(uuid, 1, Integer::sum);
    }
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
    synchronized (processesMap) {
      processesMap.merge(uuid, 1, Integer::sum);
    }
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATE_FIELD, value);
  }

  /**
   * Check finished validations.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   *
   * @throws EEAException the EEA exception
   */

  public void checkFinishedValidations(final Long datasetId, final String uuid)
      throws EEAException {
    if (processesMap.get(uuid) == 0) {
      // Release the lock manually
      List<Object> criteria1 = new ArrayList<>();
      List<Object> criteria2 = new ArrayList<>();
      criteria1.add(LockSignature.EXECUTE_VALIDATION.getValue());
      criteria1.add(datasetId);
      criteria2.add(LockSignature.FORCE_EXECUTE_VALIDATION.getValue());
      criteria2.add(datasetId);
      lockService.removeLockByCriteria(criteria1);
      lockService.removeLockByCriteria(criteria2);

      // after last dataset validations have been saved, an event is sent to notify it
      Map<String, Object> value = new HashMap<>();
      value.put("dataset_id", datasetId);
      value.put("uuid", uuid);
      this.removeKieBase(uuid);
      kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_CLEAN_KYEBASE, value);
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATION_FINISHED_EVENT, value,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .datasetId(datasetId).build());
    }
  }
}
