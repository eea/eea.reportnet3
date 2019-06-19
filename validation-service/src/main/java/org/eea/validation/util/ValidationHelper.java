package org.eea.validation.util;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
  public void executeValidation(final Long datasetId) throws EEAException {
    LOG.info("Validating dataset");
    validationService.validateDataSetData(datasetId);
    // after the dataset has been saved, an event is sent to notify it
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.VALIDATION_FINISHED_EVENT, datasetId);
  }

}
