package org.eea.communication.io.kafka;

import org.eea.communication.service.NotificationService;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.handler.EEAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class EventHandler.
 */
@Service
public class EventHandler implements EEAEventHandler {

  /** The notification service. */
  @Autowired
  private NotificationService notificationService;

  /** The logger. */
  private final Logger logger = LoggerFactory.getLogger(EventHandler.class);

  /**
   * Gets the type.
   *
   * @return the type
   */
  @Override
  public Class<EEAEventVO> getType() {
    return EEAEventVO.class;
  }

  /**
   * Filter WEBSOCKET_NOTIFICATION events to be sent to the user.
   *
   * @param eeaEventVO The event to be sent.
   */
  @Override
  public void processMessage(final EEAEventVO eeaEventVO) {

    if (eeaEventVO != null && eeaEventVO.getEventType().equals(EventType.WEBSOCKET_NOTIFICATION)
        && eeaEventVO.getData() != null) {

      String userId = (String) eeaEventVO.getData().get("userId");
      String message = (String) eeaEventVO.getData().get("message");

      if (!notificationService.send(userId, message)) {
        logger.error("Error sending notification: {}", eeaEventVO);
      }
    }
  }
}
