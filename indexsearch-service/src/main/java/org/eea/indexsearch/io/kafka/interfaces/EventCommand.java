package org.eea.indexsearch.io.kafka.interfaces;

import java.io.IOException;
import org.eea.kafka.domain.EEAEventVO;

/**
 * The Interface ICommand. Command Interface which will be implemented by the exact commands.
 * 
 */
public interface EventCommand {

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws IOException
   */
  public void execute(EEAEventVO eeaEventVO) throws IOException;

}
