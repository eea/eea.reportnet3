package org.eea.kafka.io;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * The type Kafka receiver.
 */
@Component
public class DefaultKafkaReceiver extends KafkaReceiver {


  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultKafkaReceiver.class);


  /**
   * Listen message.
   *
   * @param message the message
   * @throws EEAException
   */
  @Override
  @KafkaListener(topics = "DATA_REPORTING_TOPIC")
  public void listenMessage(final Message<EEAEventVO> message) throws EEAException {
    LOG.info("Received message {}", message.getPayload());
    if (null != handler) {
      handler.processMessage(message.getPayload());
    }

  }
}
