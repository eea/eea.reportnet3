package org.eea.communication.io.kafka.command;

import java.util.Map;
import org.eea.communication.service.NotificationService;
import org.eea.kafka.commands.DefaultEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class CreateConnectionCommand.
 */
@Component
public class SendNotificationCommand extends DefaultEventHandlerCommand {


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
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    if (eeaEventVO.getData().get("user") != null
        && eeaEventVO.getData().get("notification") != null) {
      String user = (String) eeaEventVO.getData().get("user");
      Object object = eeaEventVO.getData().get("notification");
      if (object instanceof Map) {
        Map<?, ?> notification = (Map<?, ?>) object;
        if (!notificationService.send(user, notification)) {
          LOG.error("Error sending notification: {}", notification);
        }
      }
    }
  }
}


