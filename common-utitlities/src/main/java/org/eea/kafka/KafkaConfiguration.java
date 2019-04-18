package org.eea.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.serializer.EEAEventDeserializer;
import org.eea.kafka.serializer.EEAEventSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka
@ComponentScan("org.eea.kafka")
public class KafkaConfiguration {
  @Value(value = "${kafka.bootstrapAddress:localhost:9092}")
  private String bootstrapAddress;

  @Value(value = "${spring.application.name:test-consumer-group}")
  private String groupId;
  @Bean
  public ProducerFactory<String, EEAEventVO> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        bootstrapAddress);
    configProps.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class);
    configProps.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        EEAEventSerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, EEAEventVO> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
  @Bean
  public ConsumerFactory<String, EEAEventVO> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        bootstrapAddress);
    props.put(
        ConsumerConfig.GROUP_ID_CONFIG,
        groupId);
    props.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
        StringDeserializer.class);
    props.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
        EEAEventDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, EEAEventVO>
  kafkaListenerContainerFactory() {

    ConcurrentKafkaListenerContainerFactory<String, EEAEventVO> factory
        = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }
}
