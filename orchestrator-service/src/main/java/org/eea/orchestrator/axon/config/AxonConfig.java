package org.eea.orchestrator.axon.config;

import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.mode.CapabilityDiscoveryMode;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.function.Predicate;

@Configuration
public class AxonConfig {


    @Autowired
    DiscoveryClient discoveryClient;

    @Autowired
    private ApplicationContext appContext;

    //configuring how many sagas can run in parallel
    @Autowired
    public void configureSegmentCount(EventProcessingConfigurer processingConfigurer) {
        TrackingEventProcessorConfiguration tepConfig =
                TrackingEventProcessorConfiguration.forParallelProcessing(2)
                        .andInitialSegmentsCount(2).andInitialTrackingToken(StreamableMessageSource::createHeadToken);
        processingConfigurer.registerTrackingEventProcessorConfiguration("ReleaseSagaProcessor", config -> tepConfig);
    }

    /** The url. */
    @Value("${spring.datasource.metasource.url}")
    private String url;

    /** The username. */
    @Value("${spring.datasource.metasource.username}")
    private String username;

    /** The password. */
    @Value("${spring.datasource.metasource.password}")
    private String password;

    /** The driver. */
    @Value("${spring.datasource.metasource.driver-class-name}")
    private String driver;


    private Predicate<ServiceInstance> serviceInstanceFilter = (serviceInstance) -> {
        //TODO replace hardoded IP
        return  serviceInstance.getHost().equals("192.168.1.4");
    };


    @Bean
    public DataSource axon() {
        DriverManagerDataSource metaDataSource = new DriverManagerDataSource();
        metaDataSource.setDriverClassName(driver);
        metaDataSource.setUrl("jdbc:postgresql://localhost/axon");
        metaDataSource.setUsername(username);
        metaDataSource.setPassword(password);
        return metaDataSource;
    }

    @Bean("springCloudCommandRouter")
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient, Registration localServiceInstance, RoutingStrategy routingStrategy, CapabilityDiscoveryMode capabilityDiscoveryMode, Serializer serializer) {
        return SpringCloudCommandRouter.builder().discoveryClient(discoveryClient).localServiceInstance(localServiceInstance).routingStrategy(routingStrategy).capabilityDiscoveryMode(capabilityDiscoveryMode).serializer(serializer)
                .serviceInstanceFilter(serviceInstanceFilter).build();
    }

//    @Bean(name = "axonEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean axonEntityManagerFactory() {
//        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
//        em.setDataSource(axon());
//        em.setPackagesToScan(
//                "org.axonframework.eventsourcing.eventstore.jpa",
//                "org.axonframework.eventhandling.saga.repository.jpa",
//                "org.axonframework.modelling.saga.repository.jpa",
//                "org.axonframework.eventhandling.tokenstore.jpa");
//        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//        em.setJpaVendorAdapter(vendorAdapter);
//        em.setJpaProperties(jpaProperties());
////        em.setMappingResources("/orm.xml");
//        return em;
//    }
//
//    public Properties jpaProperties() {
//        Properties properties = new Properties();
//        properties.setProperty("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
//        properties.setProperty("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
//        properties.setProperty("hibernate.hbm2ddl.auto", "update");
//        properties.setProperty("hibernate.show_sql", "true");
//        properties.setProperty("hibernate.dialect","org.hibernate.dialect.PostgreSQL94Dialect");
//        return properties;
//    }
//
//    @Bean
//    public PlatformTransactionManager axonPlatformTransactionManager() {
//        JpaTransactionManager axonTransactionManager = new JpaTransactionManager();
//        axonTransactionManager
//                .setEntityManagerFactory(axonEntityManagerFactory().getObject());
//        return axonTransactionManager;
//    }

}
