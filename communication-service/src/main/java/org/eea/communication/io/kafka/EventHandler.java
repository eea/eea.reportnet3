package org.eea.communication.io.kafka;

import org.eea.communication.service.NotificationService;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventHandler implements EEAEventHandler {

  @Autowired
  NotificationService notificationService;

  private final Logger logger = LoggerFactory.getLogger(EventHandler.class);

  @Override
  public Class<EEAEventVO> getType() {
    return EEAEventVO.class;
  }

  @Override
  public void processMessage(final EEAEventVO eeaEventVO) {

    if (!notificationService.sendNotification(eeaEventVO)) {
      logger.error("Error sending notification: {}", eeaEventVO);
    }
  }
}
