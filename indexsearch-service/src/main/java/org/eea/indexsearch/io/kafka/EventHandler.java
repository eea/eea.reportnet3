package org.eea.indexsearch.io.kafka;

import java.io.IOException;
import org.eea.indexsearch.io.kafka.interfaces.CommandEventFactory;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Event handler.
 */
@Service
public class EventHandler implements EEAEventHandler {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EventHandler.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  @Autowired
  private CommandEventFactory commandEventFactory;

  /**
   * Gets the type.
   *
   * @return the type
   */
  @Override
  public Class<EEAEventVO> getType() {
    return EEAEventVO.class;
  }


  @Override
  public void processMessage(EEAEventVO message) {

    try {
      commandEventFactory.getEventCommand(message).execute(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
