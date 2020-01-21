package org.eea.kafka.commands;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;

/**
 * The Class AbstracEEAEventHandlerCommand.
 */
public abstract class AbstractEEAEventHandlerCommand implements EEAEventHandlerCommand {

  /**
   * Gets the event type on which the command will act.
   *
   * @return the event type
   */
  public abstract EventType getEventType();

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public abstract void execute(EEAEventVO eeaEventVO) throws EEAException;
}
