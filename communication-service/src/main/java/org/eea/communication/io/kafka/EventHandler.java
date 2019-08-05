package org.eea.communication.io.kafka;

import org.eea.communication.controller.NotificationController;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventHandler implements EEAEventHandler {

  @Autowired
  NotificationController notificationController;

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Override
  public Class<EEAEventVO> getType() {
    return EEAEventVO.class;
  }

  @Override
  public void processMessage(final EEAEventVO eeaEventVO) {
    try {
      notificationController.sendNotification(eeaEventVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error sending notification: {}", e.getMessage());
    }
  }
}
