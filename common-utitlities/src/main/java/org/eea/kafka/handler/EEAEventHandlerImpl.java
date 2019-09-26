package org.eea.kafka.handler;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.EEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.factory.EEAEventCommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EEAEventHandlerImpl implements EEAEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(EEAEventHandlerImpl.class);

  @Autowired
  private EEAEventCommandFactory eeaEentCommandFactory;

  @Override
  public Class<EEAEventVO> getType() {
    return EEAEventVO.class;
  }

  @Override
  public void processMessage(EEAEventVO message) throws EEAException {
    EEAEventHandlerCommand command = eeaEentCommandFactory.getEventCommand(message);
    if (null != command) {
      command.execute(message);
    }



  }

}
