package org.eea.validation.util;

import org.eea.exception.EEAException;
import org.eea.helper.KafkaSenderHelper;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.service.ValidationService;

/**
 * The Class ValidationHelper.
 */
public class ValidationHelper extends KafkaSenderHelper {

  /**
   * Instantiates a new file loader helper.
   */
  public ValidationHelper() {
    super();
  }

  /**
   * Execute file process.
   *
   * @param kafkaSender the kafka sender
   * @param validationService the validation service
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  public static void executeValidation(final KafkaSender kafkaSender,
      final ValidationService validationService, final Long datasetId) throws EEAException {
    validationService.deleteAllValidation(datasetId);

    validationService.validateDataSetData(datasetId);
    // after the dataset has been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(kafkaSender, EventType.VALIDATION_FINISHED_EVENT, datasetId);
  }

}
