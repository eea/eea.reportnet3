//package org.eea.recordstore.configuration;
//
//import com.mongodb.MongoClient;
//import com.mongodb.MongoClientURI;
//import org.axonframework.config.EventProcessingConfigurer;
//import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
//import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
//import org.axonframework.eventsourcing.eventstore.EventStore;
//import org.axonframework.extensions.mongo.DefaultMongoTemplate;
//import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
//import org.axonframework.serialization.json.JacksonSerializer;
//import org.axonframework.spring.config.AxonConfiguration;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//
///**
// * The type Schemas configuration.
// */
//@Configuration
//@EnableTransactionManagement
//public class MongoConfiguration {
//
//  /**
//   * The mongo hosts
//   */
//  @Value("${mongodb.hosts}")
//  private String mongoHosts;
//
//  /**
//   * Mongo client.
//   *
//   * @return the mongo client
//   */
//  @Bean
//  public MongoClient mongoClient() {
//    return new MongoClient(
//        new MongoClientURI(new StringBuilder("mongodb://").append(mongoHosts).toString()));
//  }
//
////  @Bean
////  public MongoTemplate axonMongoTemplate() {
////    return DefaultMongoTemplate.builder()
////            .mongoDatabase(mongoClient(), "axon_recordstore")
////            .build();
////  }
//
////  @Bean
////  public TokenStore tokenStore(Serializer serializer) {
////    return MongoTokenStore.builder()
////            .mongoTemplate(axonMongoTemplate())
////            .serializer(serializer)
////            .build();
////  }

//
//  @Bean
//  public EventStorageEngine storageEngine(MongoClient client) {
//    return MongoEventStorageEngine.builder()
//            .mongoTemplate(DefaultMongoTemplate.builder()
//                    .mongoDatabase(client)
//                    .build()).eventSerializer(JacksonSerializer.defaultSerializer()).snapshotSerializer(JacksonSerializer.defaultSerializer())
//            .build();
//  }
//
//  @Bean
//  public EmbeddedEventStore eventStore(EventStorageEngine storageEngine, AxonConfiguration configuration) {
//    return EmbeddedEventStore.builder()
//            .storageEngine(storageEngine)
//            .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
//            .build();
//  }
//}
