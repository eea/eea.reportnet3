package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.kie.api.KieBase;

/**
 * The interface Validator. Functional Interface to perform validations commands coming from Kafka
 */

@FunctionalInterface
public interface Validator {

  /**
   * Perform validation.
   *
   * @param eeaEventVO the eea event vo
   * @param datasetId the dataset id
   * @param kieBase the kie base
   */
  void performValidation(EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase)
      throws EEAException;
}
