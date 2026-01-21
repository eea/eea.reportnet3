package org.eea.validation.kafka;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.io.KafkaReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class LowPriorityTaskKafkaReceiver extends KafkaReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(LowPriorityTaskKafkaReceiver.class);

    @Override
    @KafkaListener(topics = "LOW_PRIORITY_TASK_TOPIC")
    public void consumeMessage(Message<EEAEventVO> message) {
        LOG.info("Received message {}", message.getPayload());
        if (null != handler) {
            try {
                handler.processMessage(message.getPayload());
            } catch (EEAException e) {
                LOG.error("Error processing event {} due to reason {}", message.getPayload(), e);
            } catch (Exception e) {
                LOG.error("Undetermined  processing message {} due to reason {}", message, e);
            }
        }
    }
}