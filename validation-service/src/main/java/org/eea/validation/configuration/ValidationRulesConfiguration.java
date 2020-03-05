package org.eea.validation.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.serializer.EEAEventDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * The Class ValidationConfiguration.
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableMongoRepositories(basePackages = "org.eea.validation.persistence.repository")
public class ValidationRulesConfiguration extends AbstractMongoConfiguration {

  /**
   * The dll.
   */
  @Value("${mongodb.hibernate.ddl-auto}")
  private String dll;

  /**
   * The host.
   */
  @Value("${mongodb.primary.host}")
  private String host;

  /**
   * The port.
   */
  @Value("${mongodb.primary.port}")
  private Integer port;

  /**
   * The username.
   */
  @Value("${mongodb.primary.username}")
  private String username;

  /**
   * The password.
   */
  @Value("${mongodb.primary.password}")
  private String password;

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
   * Schemas transaction manager.
   *
   * @param dbFactory the db factory
   *
   * @return the mongo transaction manager
   */
  @Bean
  public MongoTransactionManager schemasTransactionManager(MongoDbFactory dbFactory) {
    return new MongoTransactionManager(dbFactory);
  }

  /**
   * Gets the database name.
   *
   * @return the database name
   */
  @Override
  protected String getDatabaseName() {
    return "dataset_schema";
  }

  /**
   * Mongo client.
   *
   * @return the mongo client
   */
  @Override
  public MongoClient mongoClient() {
    return new MongoClient(host, port);
  }

  /**
   * Mongo database.
   *
   * @return the mongo database
   */
  @Bean
  public MongoDatabase mongoDatabase() {
    return mongoClient().getDatabase(getDatabaseName());
  }


  /**
   * Broadcast consumer factory consumer factory.
   *
   * @return the consumer factory
   */
  @Bean
  public ConsumerFactory<String, EEAEventVO> broadcastConsumerFactory() {
    final Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + UUID.randomUUID());// single group in one
                                                                           // partition topic
                                                                           // garantees broadcasting
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EEAEventDeserializer.class);
    props.put("heartbeat.interval.ms", 3000);
    props.put("session.timeout.ms", 150000);
    // props.put("enable.auto.commit", "false");
    props.put("isolation.level", "read_committed");

    return new DefaultKafkaConsumerFactory<>(props);
  }

  /**
   * Kafka listener container factory.
   *
   * @return the concurrent kafka listener container factory
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, EEAEventVO> broadcastKafkaListenerContainerFactory() {

    final ConcurrentKafkaListenerContainerFactory<String, EEAEventVO> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(broadcastConsumerFactory());
    return factory;
  }
}
