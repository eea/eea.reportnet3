package org.eea.communication.configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.extensions.mongo.DefaultMongoTemplate;
import org.axonframework.extensions.mongo.MongoTemplate;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableMongoRepositories(basePackages = "org.eea.communication.persistence.repository")
public class UserNotificationConfiguration extends AbstractMongoConfiguration {

  /**
   * The dll.
   */
  @Value("${mongodb.hibernate.ddl-auto}")
  private String dll;

  /**
   * The mongo hosts
   */
  @Value("${mongodb.hosts}")
  private String mongoHosts;

  /**
   * Schemas transaction manager.
   *
   * @param dbFactory the db factory
   *
   * @return the mongo transaction manager
   */
//  @Bean
//  public MongoTransactionManager schemasTransactionManager(MongoDbFactory dbFactory) {
//    return new MongoTransactionManager(dbFactory);
//  }

  /**
   * Gets the database name.
   *
   * @return the database name
   */
  @Override
  protected String getDatabaseName() {
    return "communication";
  }

  /**
   * Mongo database mongo database.
   *
   * @return the mongo database
   */
  @Bean
  public MongoDatabase mongoDatabase() {
    return mongoClient().getDatabase(getDatabaseName());
  }

  /**
   * Mongo client.
   *
   * @return the mongo client
   */
  @Override
  @Bean
  public MongoClient mongoClient() {
    return new MongoClient(
        new MongoClientURI(new StringBuilder("mongodb://").append(mongoHosts).toString()));
  }

  @Bean
  public MongoTemplate axonMongoTemplate() {
    return DefaultMongoTemplate.builder()
            .mongoDatabase(mongoClient(), "axon_communication")
            .build();
  }

  @Bean
  public TokenStore tokenStore(Serializer serializer) {
    return MongoTokenStore.builder()
            .mongoTemplate(axonMongoTemplate())
            .serializer(serializer)
            .build();
  }

  @Bean
  public EventStorageEngine storageEngine(MongoClient client) {
    return MongoEventStorageEngine.builder()
            .mongoTemplate(DefaultMongoTemplate.builder()
                    .mongoDatabase(client)
                    .build()).eventSerializer(JacksonSerializer.defaultSerializer()).snapshotSerializer(JacksonSerializer.defaultSerializer())
            .build();
  }

  @Bean
  public EmbeddedEventStore eventStore(EventStorageEngine storageEngine, AxonConfiguration configuration) {
    return EmbeddedEventStore.builder()
            .storageEngine(storageEngine)
            .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
            .build();
  }
}
