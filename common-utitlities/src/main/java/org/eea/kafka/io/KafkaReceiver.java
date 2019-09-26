package org.eea.kafka.io;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * The type Kafka receiver.
 */
@Component
public class KafkaReceiver {

  /**
   * The handler.
   */
  @Autowired(required = false)
  private EEAEventHandler handler;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(KafkaReceiver.class);


  /**
   * Listen message.
   *
   * @param message the message
   * @throws EEAException
   */
  @KafkaListener(topics = "DATA_REPORTING_TOPIC")
  public void listenMessage(final Message<EEAEventVO> message) throws EEAException {
    LOG.info("Received message {}", message.getPayload());
    if (null != handler) {
      handler.processMessage(message.getPayload());
    }

  }
}
