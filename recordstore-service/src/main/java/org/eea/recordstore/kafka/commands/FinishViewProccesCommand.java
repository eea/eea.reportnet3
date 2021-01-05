package org.eea.recordstore.kafka.commands;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.springframework.stereotype.Component;

@Component
public class FinishViewProccesCommand extends AbstractEEAEventHandlerCommand {

  @Override
  public EventType getEventType() {
    return EventType.FINISH_VIEW_PROCCES_EVENT;
  }

  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    // TODO Auto-generated method stub

  }

}
