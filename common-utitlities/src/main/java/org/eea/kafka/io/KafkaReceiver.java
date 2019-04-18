package org.eea.kafka.io;

import java.util.Collection;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class KafkaReceiver {

  @Autowired(required = false)
  private EEAEventHandler handler;

  private static final Logger LOG = LoggerFactory.getLogger(KafkaReceiver.class);

//  @KafkaListener( topicPartitions = @TopicPartition(topic = "Hello-Kafka2",
//      partitionOffsets = {
//          @PartitionOffset(partition = "0", initialOffset = "0")
//      }))
//@KafkaListener(topics = "Hello-Kafka2")
  public void listen(Collection<EEAEventVO> messages) {
    messages.stream().forEach(message -> System.out.println("Received Messasge in group foo: " + message));

  }

//  @KafkaListener( topicPartitions = @TopicPartition(topic = "Hello-Kafka2",
//      partitionOffsets = {
//          @PartitionOffset(partition = "0", initialOffset = "8")
//      }))
@KafkaListener(topics = "Hello-Kafka2")
  public void listenMessage(Message<EEAEventVO> message) {
  LOG.info("Received message {}",message.getPayload());

  handler.processMessage(message.getPayload());

  }
}
