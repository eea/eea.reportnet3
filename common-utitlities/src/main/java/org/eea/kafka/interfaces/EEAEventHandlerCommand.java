package org.eea.kafka.interfaces;

import java.io.IOException;
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
   * @throws IOException
   */
  void execute(EEAEventVO eeaEventVO) throws EEAException;

}
