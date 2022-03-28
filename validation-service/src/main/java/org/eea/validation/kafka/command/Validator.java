package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.kie.api.KieBase;

/**
 * The Interface Validator.
 */
@FunctionalInterface
public interface Validator {

  /**
   * Perform validation.
   *
   * @param eeaEventVO the eea event VO
   * @param datasetId the dataset id
   * @param kieBase the kie base
   * @param taskId the task id
   * @throws EEAException the EEA exception
   */
  void performValidation(EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase, Long taskId)
      throws EEAException;
}
