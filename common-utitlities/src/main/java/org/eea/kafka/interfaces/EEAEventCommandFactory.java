package org.eea.kafka.interfaces;

import org.eea.kafka.domain.EEAEventVO;

/**
 * A factory for creating ICommand objects.
 */
public interface EEAEventCommandFactory {

  /**
   * EEA event.
   *
   * @param message the message
   * @return the event command
   */
  EEAEventHandlerCommand getEventCommand(EEAEventVO message);
}
