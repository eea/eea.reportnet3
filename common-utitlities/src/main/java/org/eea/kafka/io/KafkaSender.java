package org.eea.kafka.io;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional
  public void sendMessage(final EEAEventVO event) {

    kafkaTemplate.execute(producer -> {
      event.getData().put("user", String.valueOf(ThreadPropertiesManager.getVariable("user")));
      event.getData().put("token",
          String.valueOf(SecurityContextHolder.getContext().getAuthentication().getCredentials()));

      Integer partitionId = null;
      final List<PartitionInfo> partitions =
          kafkaTemplate.partitionsFor(event.getEventType().getTopic());
      if (event.getEventType().isSorted()) {
        // partition = hash(message_key)%number_of_partitions
        partitionId =
            Math.floorMod(event.getEventType().getKey().hashCode(), partitions.size());
      } else {
        partitionId = ThreadLocalRandom.current().nextInt(partitions.size());
      }

      ProducerRecord producerRecord = new ProducerRecord(event.getEventType().getTopic(),
          partitionId, event.getEventType().getKey(), event);

      final Future<RecordMetadata> future = producer.send(producerRecord);
      Boolean sendResult = true;

      try {
        RecordMetadata result = future.get();
        LOG.info("Sent message=[ {} ] to topic=[ {} ] with offset=[ {} ] and partition [ {} ]",
            event, event.getEventType().getTopic(),
            result.offset(), result.partition());
      } catch (InterruptedException | ExecutionException e) {
        LOG_ERROR.error("Unable to send message=[ {} ] to topic=[ {} ] due to: {} ", event,
            event.getEventType().getTopic(), e.getMessage());
        sendResult = false;
        producer.close();
      } catch (KafkaException e) {
        sendResult = false;
        producer.abortTransaction();

      }
      return sendResult;
    });


  }


}
