package org.eea.communication.io.kafka.command;

import java.util.HashMap;
import java.util.Map;
import org.eea.communication.service.NotificationService;
import org.eea.kafka.commands.DefaultEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class SendNotificationCommand.
 */
@Component
public class SendNotificationCommand extends DefaultEventHandlerCommand {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The notification service. */
  @Autowired
  private NotificationService notificationService;

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    if (eeaEventVO.getData().containsKey("notification")) {
      Object object = eeaEventVO.getData().get("notification");
      if (object instanceof Map) {
        Map<String, Object> notification = (HashMap<String, Object>) object;
        String user = (String) notification.remove("user");
        if (notificationService.send(user, eeaEventVO.getEventType(), notification)) {
          return;
        }
      }
      LOG_ERROR.error("Error sending notification: {}", eeaEventVO);
    }
  }
}

