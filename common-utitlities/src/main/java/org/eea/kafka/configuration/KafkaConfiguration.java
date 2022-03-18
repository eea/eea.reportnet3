package org.eea.kafka.configuration;

import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ISOLATION_LEVEL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.message.configuration.EnableMessageEventHandling;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * The Class KafkaConfiguration.
 */
@Configuration
@EnableKafka
@EnableMessageEventHandling
@ComponentScan("org.eea.kafka")
public class KafkaConfiguration {

  /**
   * The bootstrap address.
   */
  @Value(value = "${kafka.bootstrapAddress}")
  private String bootstrapAddress;

  /**
   * The group id.
   */
  @Value(value = "${spring.application.name}")
  private String groupId;


  /**
   * Kafka admin.
   *
   * @return the kafka admin
   */
  @Bean
  public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    return new KafkaAdmin(configs);
  }

  /**
   * Kafka admin client.
   *
   * @return the admin client
   */
  @Bean
  public AdminClient kafkaAdminClient() {
    return AdminClient.create(kafkaAdmin().getConfig());
  }

  /**
   * Producer factory.
   *
   * @return the producer factory
   */
  @Bean
  public ProducerFactory<String, EEAEventVO> producerFactory() {
    final Map<String, Object> configProps = new ConcurrentHashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    configProps.put(ACKS_CONFIG, "all");
    configProps.put(RETRIES_CONFIG, 3);
    JsonSerializer<EEAEventVO> serializer = new JsonSerializer<>();
    serializer.setAddTypeInfo(false);

    DefaultKafkaProducerFactory<String, EEAEventVO> defaultKafkaProducerFactory =
        new DefaultKafkaProducerFactory(configProps, new StringSerializer(), serializer);

    defaultKafkaProducerFactory.setTransactionIdPrefix(groupId + UUID.randomUUID());

    return defaultKafkaProducerFactory;
  }


  /**
   * Creates Kafka template with autoflush flag activated
   *
   * @return the kafka template
   */
  @Bean
  public KafkaTemplate<String, EEAEventVO> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory(), true);
  }

  /**
   * Consumer factory.
   *
   * @return the consumer factory
   */
  @Bean
  public ConsumerFactory<String, EEAEventVO> defaultConsumerFactory() {
    final Map<String, Object> props = new ConcurrentHashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ISOLATION_LEVEL_CONFIG, "read_committed");
    JsonDeserializer<EEAEventVO> deserializer = new JsonDeserializer<>(EEAEventVO.class);
    deserializer.addTrustedPackages("org.eea.kafka.domain");
    return new DefaultKafkaConsumerFactory(props, new StringDeserializer(), deserializer);
  }

  /**
   * Kafka listener container factory.
   *
   * @return the concurrent kafka listener container factory
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, EEAEventVO> kafkaListenerContainerFactory() {

    final ConcurrentKafkaListenerContainerFactory<String, EEAEventVO> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(defaultConsumerFactory());
    return factory;
  }


}
