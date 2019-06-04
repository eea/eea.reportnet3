package org.eea.validation.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.mongodb.MongoClient;

/**
 * The Class ValidationConfiguration.
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableMongoRepositories(basePackages = "org.eea.dataset.persistence.rules.repository")
public class ValidationRulesConfiguration extends AbstractMongoConfiguration {

  private static final int DEFAULT_PORT = 27017;

  /** The dll. */
  @Value("${mongodb.hibernate.ddl-auto}")
  private String dll;

  /** The host. */
  @Value("${mongodb.primary.host}")
  private String host;

  /** The port. */
  @Value("${mongodb.primary.port}")
  private Integer port;

  /** The username. */
  @Value("${mongodb.primary.username}")
  private String username;

  /** The password. */
  @Value("${mongodb.primary.password}")
  private String password;

  /**
   * Schemas transaction manager.
   *
   * @param dbFactory the db factory
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
    return "Dataset_Rules";
  }

  /**
   * Mongo client.
   *
   * @return the mongo client
   */
  @Override
  public MongoClient mongoClient() {
    return new MongoClient(host, DEFAULT_PORT);
  }

}
