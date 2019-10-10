package org.eea.communication.io.kafka.command;

import org.eea.communication.service.NotificationService;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class CreateConnectionCommand.
 */
@Component
public class SendNotificationCommand extends AbstractEEAEventHandlerCommand {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The notification service.
   */
  @Autowired
  private NotificationService notificationService;

  /**
   * The logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SendNotificationCommand.class);

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.WEBSOCKET_NOTIFICATION;
  }


  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {

    String userId = (String) eeaEventVO.getData().get("userId");
    String message = (String) eeaEventVO.getData().get("message");

    if (!notificationService.send(userId, message)) {
      LOG_ERROR.error("Error sending notification: {}", eeaEventVO);
    }

  }


}



