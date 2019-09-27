package org.eea.validation.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.service.ValidationService;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

  /**
   * The validation service.
   */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /** The processes map. */
  private ConcurrentHashMap<String, Integer> processesMap;

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
    LOG.info("Deleting all Validations");
    validationService.deleteAllValidation(datasetId);
    LOG.info("Load Rules");
    KieBase kieBase = validationService.loadRulesKnowledgeBase(datasetId);

    LOG.info("Validating Dataset");
    releaseDatasetValidation(datasetId, uuId, kieBase);
    LOG.info("Validating Tables");
    releaseTableValidation(datasetId, uuId, kieBase);
    LOG.info("Validating Records");
    releaseRecordValidation(datasetId, uuId, kieBase);
    LOG.info("Validating Fields");
    releaseFieldValidation(datasetId, uuId, kieBase);

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
   * @param kieBase the kie base
   */
  public void releaseDatasetValidation(final Long datasetId, final String uuid,
      final KieBase kieBase) {
    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("uuid", uuid);
    value.put("kieBase", kieBase);
    processesMap.merge(uuid, 1, Integer::sum);
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATE_DATASET, value);
  }

  /**
   * Release table validation.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   * @param kieBase the kie base
   */
  public void releaseTableValidation(final Long datasetId, final String uuid,
      final KieBase kieBase) {
    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("uuid", uuid);
    value.put("kieBase", kieBase);
    processesMap.merge(uuid, 1, Integer::sum);
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATE_TABLE, value);
  }

  /**
   * Release record validation.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   * @param kieBase the kie base
   */
  public void releaseRecordValidation(final Long datasetId, final String uuid,
      final KieBase kieBase) {
    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("uuid", uuid);
    value.put("kieBase", kieBase);
    processesMap.merge(uuid, 1, Integer::sum);
    kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATE_RECORD, value);
  }

  /**
   * Release field validation.
   *
   * @param datasetId the dataset id
   * @param uuid the uuid
   * @param kieBase the kie base
   */
  public void releaseFieldValidation(final Long datasetId, final String uuid,
      final KieBase kieBase) {
    Map<String, Object> value = new HashMap<>();
    value.put("dataset_id", datasetId);
    value.put("uuid", uuid);
    value.put("kieBase", kieBase);
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

  public void checkFinishedValidations(final Long datasetId, final String uuid,
      final KieBase kieBase) throws EEAException {
    if (processesMap.get(uuid) == 0) {
      LOG.info("scaling errors");
      validationService.errorScale(datasetId, kieBase);
      // after the dataset has been saved, an event is sent to notify it
      processesMap.remove(uuid);
      kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.VALIDATION_FINISHED_EVENT, datasetId);
    }
  }

}
