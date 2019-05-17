/**
 * 
 */
package org.eea.dataset.configuration;

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
 * @author Mario Severa
 *
 */

@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableMongoRepositories(basePackages = "org.eea.dataset.schemas.repository")
public class SchemasConfiguration extends AbstractMongoConfiguration {


  @Value("${mongodb.hibernate.ddl-auto}")
  private String dll;
  @Value("${mongodb.primary.host}")
  private String host;
  @Value("${mongodb.primary.port}")
  private Integer port;
  @Value("${mongodb.primary.username}")
  private String username;
  @Value("${mongodb.primary.username}")
  private String password;

  @Bean
  public MongoTransactionManager schemasTransactionManager(MongoDbFactory dbFactory) {
    return new MongoTransactionManager(dbFactory);
  }

  @Override
  protected String getDatabaseName() {
    return "dataset_schema";
  }

  @Override
  public MongoClient mongoClient() {
    return new MongoClient(host, 27017);
  }

}
