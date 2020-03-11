package org.eea.validation.configuration;

import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ISOLATION_LEVEL_CONFIG;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eea.kafka.domain.EEAEventVO;
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
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
    // single group in one partition topic garantees broadcasting
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + UUID.randomUUID());
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
  public ConcurrentKafkaListenerContainerFactory<String, EEAEventVO> broadcastKafkaListenerContainerFactory() {

    final ConcurrentKafkaListenerContainerFactory<String, EEAEventVO> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(broadcastConsumerFactory());
    return factory;
  }
}
