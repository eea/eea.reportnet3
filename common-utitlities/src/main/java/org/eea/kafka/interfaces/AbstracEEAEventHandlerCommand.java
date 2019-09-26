package org.eea.kafka.interfaces;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;

public abstract class AbstracEEAEventHandlerCommand implements EEAEventHandlerCommand {

  public abstract EventType getEventType();

  @Override
  public abstract void execute(EEAEventVO eeaEventVO) throws EEAException;

}
