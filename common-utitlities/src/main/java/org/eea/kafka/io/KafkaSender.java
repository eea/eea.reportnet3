package org.eea.kafka.io;

import java.util.List;
import org.apache.kafka.common.PartitionInfo;
import org.eea.kafka.domain.EEAEventVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * The type Kafka sender.
 */
@Component
public class KafkaSender {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(KafkaSender.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /** The kafka template. */
  @Autowired
  private KafkaTemplate<String, EEAEventVO> kafkaTemplate;


  /**
   * Send message.
   *
   * @param event the event
   */
  public void sendMessage(EEAEventVO event) {
    List<PartitionInfo> partitions = kafkaTemplate.partitionsFor(event.getEventType().getTopic());
    // partition = hash(message_key)%number_of_partitions
    Integer partitionId = event.getEventType().getKey().hashCode() % partitions.size();


    Message<EEAEventVO> message =
        MessageBuilder.withPayload(event).setHeader(KafkaHeaders.PARTITION_ID, partitionId)
            .setHeader(KafkaHeaders.MESSAGE_KEY, event.getEventType().getKey())
            .setHeader(KafkaHeaders.TOPIC, event.getEventType().getTopic()).build();

    ListenableFuture<SendResult<String, EEAEventVO>> future = kafkaTemplate.send(message);

    future.addCallback(new ListenableFutureCallback<SendResult<String, EEAEventVO>>() {

      /**
       * On success.
       *
       * @param result the result
       */
      @Override
      public void onSuccess(SendResult<String, EEAEventVO> result) {
        if (result != null && result.getRecordMetadata() != null) {
          LOG.info(
              "Sent message=[" + event + "] with offset=[" + result.getRecordMetadata().offset()
                  + "] and partition [" + result.getRecordMetadata().partition() + "]");
        }
      }

      /**
       * On failure.
       *
       * @param ex the ex
       */
      @Override
      public void onFailure(Throwable ex) {
        LOG_ERROR.error("Unable to send message=[" + event + "] due to : " + ex.getMessage());
      }
    });
  }


}
