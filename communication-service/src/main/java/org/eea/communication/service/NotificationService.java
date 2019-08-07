package org.eea.communication.service;

import org.eea.kafka.domain.EEAEventVO;

public interface NotificationService {

  boolean sendNotification(EEAEventVO eeaEventVO);
}
