package org.eea.kafka.configuration;

import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.serializer.EEAEventDeserializer;
import org.eea.kafka.serializer.EEAEventSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
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
import org.springframework.kafka.transaction.KafkaTransactionManager;

/**
 * The Class KafkaConfiguration.
 */
@Configuration
@EnableKafka
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
    final Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EEAEventSerializer.class);
    configProps.put("enable.idempotence", "true");
    configProps.put(ACKS_CONFIG, "all");
    configProps.put("transactional.id", groupId + UUID
        .randomUUID());//Single transactional id since every sender must use a different one
    DefaultKafkaProducerFactory<String, EEAEventVO> defaultKafkaProducerFactory = new DefaultKafkaProducerFactory<>(
        configProps);
    defaultKafkaProducerFactory.setTransactionIdPrefix("txn-ingestor");
    return defaultKafkaProducerFactory;
  }

  @Bean
  public KafkaTransactionManager<String, EEAEventVO> kafkaTransactionManager(
      @Autowired ProducerFactory<String, EEAEventVO> pf) {
    return new KafkaTransactionManager<>(pf);
  }

  /**
   * Kafka template.
   *
   * @return the kafka template
   */
  @Bean
  public KafkaTemplate<String, EEAEventVO> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
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
    props.put("isolation.level", "read_committed");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EEAEventDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(props);
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

  /**
   * Kafka health indicator.
   *
   * @return the health indicator
   */
  @Bean
  public HealthIndicator kafkaHealthIndicator() {
    final DescribeClusterOptions describeClusterOptions =
        new DescribeClusterOptions().timeoutMs(1000);
    final AdminClient adminClient = kafkaAdminClient();
    return () -> {
      final DescribeClusterResult describeCluster =
          adminClient.describeCluster(describeClusterOptions);
      try {
        final String clusterId = describeCluster.clusterId().get();
        final int nodeCount = describeCluster.nodes().get().size();
        return Health.up().withDetail("clusterId", clusterId).withDetail("nodeCount", nodeCount)
            .build();
      } catch (final InterruptedException | ExecutionException e) {
        // NOPMD false positive, I really need the thread to go on since this is a healtchecker.
        // Exception is managed by Spring Actuator
        return Health.down().withException(e).build();
      }
    };

  }
}
