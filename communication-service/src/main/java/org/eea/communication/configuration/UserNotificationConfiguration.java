package org.eea.communication.configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudHttpBackupCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableMongoRepositories(basePackages = "org.eea.communication.persistence.repository")
public class UserNotificationConfiguration extends AbstractMongoConfiguration {

  @Autowired
  DiscoveryClient discoveryClient;

  @Autowired
  private ApplicationContext appContext;

  public static final String AXON_SPRING_CLOUD_FALLBACK_URL="/message-routing-information";

  /** The username. */
  @Value("${spring.datasource.metasource.username}")
  private String username;

  /** The password. */
  @Value("${spring.datasource.metasource.password}")
  private String password;

  /** The driver. */
  @Value("${spring.datasource.metasource.driver-class-name}")
  private String driver;

  /**
   *
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



}
