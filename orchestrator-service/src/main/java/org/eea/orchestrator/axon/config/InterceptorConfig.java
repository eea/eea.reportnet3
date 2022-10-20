//package org.eea.orchestrator.axon.config;
//
//import org.axonframework.commandhandling.CommandBus;
//import org.axonframework.common.jpa.EntityManagerProvider;
//import org.axonframework.config.EventProcessingConfigurer;
//import org.axonframework.messaging.correlation.CorrelationDataProvider;
//import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider;
//import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
//import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class InterceptorConfig {
//
//    @Autowired
//    public void registerCreateProductCommandInterceptor(ApplicationContext context,
//                                                        CommandBus commandBus) {
//        commandBus.registerHandlerInterceptor(new CorrelationDataInterceptor(correlationDataProviders()));
//    }
//
//    @Bean
//    CorrelationDataProvider correlationDataProviders() {
//        return new SimpleCorrelationDataProvider("auth");
//    }
//
//    @Autowired
//    public void configure(EventProcessingConfigurer config) {
//        config.usingSubscribingEventProcessors();
//    }
//
//
//}
