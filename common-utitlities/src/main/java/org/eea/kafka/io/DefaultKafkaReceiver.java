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
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * Listen message.
   *
   * @param message the message
   */
  @Override
  @KafkaListener(topics = "DATA_REPORTING_TOPIC")
  public void listenMessage(final Message<EEAEventVO> message) {
    LOG.info("Received message {}", message.getPayload());
    if (null != handler) {
      try {
        handler.processMessage(message.getPayload());
      } catch (EEAException e) {
        LOG_ERROR.error("Error processing event {}", message.getPayload(), e);
      } catch (Exception e) {
        LOG_ERROR.error("Undetermined  processing message {}", message, e);
      }
    }

  }
}
