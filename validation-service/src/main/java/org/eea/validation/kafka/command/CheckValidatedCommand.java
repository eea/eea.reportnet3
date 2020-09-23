package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.validation.util.ValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

/**
 * The Class CheckValidatedCommand.
 */
public abstract class CheckValidatedCommand extends AbstractEEAEventHandlerCommand {

  /**
   * The validation helper.
   */
  @Autowired
  private ValidationHelper validationHelper;

  /**
   * Perform action.
   *
   * @param eeaEventVO the eea event VO
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void execute(final EEAEventVO eeaEventVO) throws EEAException {

    final String processId = (String) eeaEventVO.getData().get("uuid");
    if (validationHelper.isProcessCoordinator(processId)) {
      final Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataset_id")));
      validationHelper.reducePendingTasks(datasetId, processId);
    }

  }

}
