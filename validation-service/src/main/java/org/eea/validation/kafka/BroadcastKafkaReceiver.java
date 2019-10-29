package org.eea.validation.kafka;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.io.DefaultKafkaReceiver;
import org.eea.kafka.io.KafkaReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * The type Broadcast kafka receiver.
 */
@Component
public class BroadcastKafkaReceiver extends KafkaReceiver {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultKafkaReceiver.class);


  /**
   * Listen message.
   *
   * @param message the message
   * @throws EEAException the EEA exception
   */
  @Override
  @KafkaListener(topics = "BROADCAST_TOPIC",
      containerFactory = "broadcastKafkaListenerContainerFactory")
  public void listenMessage(Message<EEAEventVO> message) throws EEAException {
    LOG.info("Received message {}", message.getPayload());
    if (null != handler) {
      handler.processMessage(message.getPayload());
    }
  }

}
