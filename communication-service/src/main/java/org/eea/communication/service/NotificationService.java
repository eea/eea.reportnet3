package org.eea.communication.service;

import org.eea.kafka.domain.EEAEventVO;

public interface NotificationService {

  /**
   * Process the event to get the message and a user to send it. EEAEventVO should has set keys
   * "userId" and "message" on data property.
   *
   * @param eeaEventVO An event to send to a user.
   * @return true, if successful
   */
  public boolean processAndSendEvent(EEAEventVO eeaEventVO);
}
