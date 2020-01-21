package org.eea.kafka.commands;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;

/**
 * The Interface ICommand. Command Interface which will be implemented by the exact commands.
 *
 */
public interface EEAEventHandlerCommand {

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  void execute(EEAEventVO eeaEventVO) throws EEAException;
}
