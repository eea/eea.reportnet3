package org.eea.kafka.io;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.kafka.common.PartitionInfo;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * The type Kafka sender.
 */
@Component
public class KafkaSender {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(KafkaSender.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * The kafka template.
   */
  @Autowired
  private KafkaTemplate<String, EEAEventVO> kafkaTemplate;


  /**
   * Send message.
   *
   * @param event the event
   */

  public void sendMessage(final EEAEventVO event) {

    kafkaTemplate.executeInTransaction(operations -> {
      event.getData().put("user", String.valueOf(ThreadPropertiesManager.getVariable("user")));
      event.getData().put("token",
          String.valueOf(SecurityContextHolder.getContext().getAuthentication().getCredentials()));

      Message<EEAEventVO> message = null;
      final List<PartitionInfo> partitions =
          kafkaTemplate.partitionsFor(event.getEventType().getTopic());
      if (event.getEventType().isSorted()) {
        // partition = hash(message_key)%number_of_partitions
        final Integer partitionId =
            Math.floorMod(event.getEventType().getKey().hashCode(), partitions.size());

        message = MessageBuilder.withPayload(event)
            .setHeader(KafkaHeaders.PARTITION_ID, partitionId)
            .setHeader(KafkaHeaders.MESSAGE_KEY, event.getEventType().getKey())
            .setHeader(KafkaHeaders.TOPIC, event.getEventType().getTopic()).build();
      } else {
        message = MessageBuilder.withPayload(event)
            .setHeader(KafkaHeaders.PARTITION_ID,
                ThreadLocalRandom.current().nextInt(partitions.size()))
            .setHeader(KafkaHeaders.MESSAGE_KEY, event.getEventType().getKey())
            .setHeader(KafkaHeaders.TOPIC, event.getEventType().getTopic()).build();
      }
      final ListenableFuture<SendResult<String, EEAEventVO>> future = operations.send(message);
      Boolean sendResult = true;

      try {
        SendResult<String, EEAEventVO> result = future.get();
        LOG.info("Sent message=[ {} ] to topic=[ {} ] with offset=[ {} ] and partition [ {} ]",
            event, event.getEventType().getTopic(),
            result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
      } catch (InterruptedException | ExecutionException e) {
        LOG_ERROR.error("Unable to send message=[ {} ] to topic=[ {} ] due to: {} ", event,
            event.getEventType().getTopic(), e.getMessage());
        sendResult = false;
      }
      return sendResult;
    });


  }


}
