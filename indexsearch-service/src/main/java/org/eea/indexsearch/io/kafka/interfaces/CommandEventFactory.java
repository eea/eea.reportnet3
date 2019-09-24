package org.eea.indexsearch.io.kafka.interfaces;

import org.eea.kafka.domain.EEAEventVO;

/**
 * A factory for creating ICommand objects.
 */
public interface CommandEventFactory {

  /**
   * EEA event.
   *
   * @param message the message
   * @return the i command
   */
  EventCommand getEventCommand(EEAEventVO message);
}
