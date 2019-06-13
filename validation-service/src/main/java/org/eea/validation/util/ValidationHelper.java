package org.eea.validation.util;

import org.eea.helper.Helper;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.service.ValidationService;

/**
 * The Class ValidationHelper.
 */
public class ValidationHelper extends Helper {

  /**
   * Instantiates a new file loader helper.
   */
  public ValidationHelper() {}

  /**
   * Execute file process.
   *
   * @param kafkaSender the kafka sender
   * @param validationService the validation service
   * @param datasetId the dataset id
   */
  public static void executeValidation(final KafkaSender kafkaSender,
      final ValidationService validationService, final Long datasetId) {
    validationService.deleteAllValidation(datasetId);

    validationService.validateDataSetData(datasetId);
    // after the dataset has been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(kafkaSender, EventType.VALIDATION_FINISHED_EVENT, datasetId);
  }

}
