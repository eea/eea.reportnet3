package org.eea.validation.util;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.service.ValidationService;
import org.kie.api.runtime.KieSession;
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

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The validation service. */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationHelper.class);

  /**
   * Instantiates a new file loader helper.
   */
  public ValidationHelper() {
    super();
  }

  /**
   * Execute file process.
   *
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  @Async
  public void executeValidation(final Long datasetId) throws EEAException {
    LOG.info("Deleting all Validations");
    validationService.deleteAllValidation(datasetId);
    LOG.info("Load Rules");
    KieSession session = validationService.loadRulesKnowledgeBase(datasetId);
    LOG.info("Validating Fields");
    System.err.println(System.currentTimeMillis());
    Long timer = System.currentTimeMillis();
    validationService.validateFields(datasetId, session);
    System.err.println(System.currentTimeMillis() - timer);
    timer = System.currentTimeMillis();
    LOG.info("Validating Records");
    validationService.validateRecord(datasetId, session);
    System.err.println(System.currentTimeMillis() - timer);
    timer = System.currentTimeMillis();
    LOG.info("Validating Tables");
    // validationService.validateTable(datasetId, session);
    System.err.println(System.currentTimeMillis() - timer);
    timer = System.currentTimeMillis();
    LOG.info("Validating Dataset");
    // validationService.validateDataSet(datasetId, session);
    System.err.println(System.currentTimeMillis() - timer);
    // after the dataset has been saved, an event is sent to notify it
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.VALIDATION_FINISHED_EVENT, datasetId);
  }

}
