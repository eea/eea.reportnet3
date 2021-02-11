package org.eea.kafka.io;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.eea.kafka.domain.EEAEventVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

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
      event.getData().put("user", SecurityContextHolder.getContext().getAuthentication().getName());
      event.getData().put("token",
          String.valueOf(SecurityContextHolder.getContext().getAuthentication().getCredentials()));

      final List<PartitionInfo> partitions =
          kafkaTemplate.partitionsFor(event.getEventType().getTopic());
      Integer partitionId = null;
      if (event.getEventType().isSorted()) {
        // partition = hash(message_key)%number_of_partitions
        partitionId = Math.floorMod(event.getEventType().getKey().hashCode(), partitions.size());
      } else {
        partitionId = ThreadLocalRandom.current().nextInt(partitions.size());
      }
      final ListenableFuture<SendResult<String, EEAEventVO>> future =
          operations.send(new ProducerRecord(event.getEventType().getTopic(), partitionId,
              event.getEventType().getKey(), event));
      Boolean sendResult = true;

      try {
        SendResult<String, EEAEventVO> result = future.get();
        LOG.info("Sent message=[ {} ] to topic=[ {} ] with offset=[ {} ] and partition [ {} ]",
            event, event.getEventType().getTopic(), result.getRecordMetadata().offset(),
            result.getRecordMetadata().partition());
      } catch (InterruptedException | ExecutionException e) {
        LOG_ERROR.error("Unable to send message=[ {} ] to topic=[ {} ] due to: {} ", event,
            event.getEventType().getTopic(), e.getMessage());
        sendResult = false;
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
      }
      return sendResult;
    });


  }


}
