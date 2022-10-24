package org.eea.dataset.axon.config;

import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.mode.CapabilityDiscoveryMode;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.modelling.saga.repository.jpa.JpaSagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.axonframework.springboot.util.RegisterDefaultEntities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Predicate;


//@EnableJpaRepositories(entityManagerFactoryRef = "axonEntityManagerFactory",
//        transactionManagerRef = "axonPlatformTransactionManager",
//        basePackages = {"org.axonframework.eventsourcing.eventstore.jpa",
//                "org.axonframework.modelling.saga.repository.jpa"})
//@EnableTransactionManagement
@RegisterDefaultEntities(packages = {
        "org.axonframework.eventhandling.tokenstore",
        "org.axonframework.eventhandling.deadletter.jpa",
        "org.axonframework.modelling.saga.repository.jpa",
        "org.axonframework.eventsourcing.eventstore.jpa"
})
@Configuration
public class AxonConfig {

    /** The url. */
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

//    @Autowired
//    public void configure(EventProcessingConfigurer config) {
//        config.usingTrackingEventProcessors().configureDefaultStreamableMessageSource(org.axonframework.config.Configuration::eventStore);
////        config.registerTrackingEventProcessor("ReleaseSagaProcessor", org.axonframework.config.Configuration::eventStore);
//    }

    private Predicate<ServiceInstance> serviceInstanceFilter = (serviceInstance) -> {
        //TODO replace hardoded IP
        return  serviceInstance.getHost().equals("192.168.1.4");
    };

    @Bean("springCloudCommandRouter")
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient, Registration localServiceInstance, RoutingStrategy routingStrategy, CapabilityDiscoveryMode capabilityDiscoveryMode, Serializer serializer) {
        return SpringCloudCommandRouter.builder().discoveryClient(discoveryClient).localServiceInstance(localServiceInstance).routingStrategy(routingStrategy).capabilityDiscoveryMode(capabilityDiscoveryMode).serializer(serializer)
                .serviceInstanceFilter(serviceInstanceFilter).build();
    }

//    @Bean("distributedCommandBus")
//    @Primary
//    @ConditionalOnBean({CommandBusConnector.class})
//    @ConditionalOnMissingBean
//    public DistributedCommandBus distributedCommandBus(CommandRouter commandRouter, CommandBusConnector commandBusConnector) {
//        DistributedCommandBus commandBus = DistributedCommandBus.builder().commandRouter(commandRouter).connector(commandBusConnector).build();
//        commandBus.updateLoadFactor(101);
//        return commandBus;
//    }
//
//    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
//    @Bean("springHttpCommandBusConnector")
//    @Primary
//    @ConditionalOnMissingBean(CommandBusConnector.class)
//    public CommandBusConnector springHttpCommandBusConnector(@Qualifier("localSegment") CommandBus localSegment,
//                                                             RestTemplate restTemplate,
//                                                             @Qualifier("messageSerializer") Serializer serializer) {
//        return SpringHttpCommandBusConnector.builder()
//                .localCommandBus(localSegment)
//                .restOperations(restTemplate)
//                .serializer(serializer)
//                .build();
//    }

    @Bean("axon")
    public DataSource axon() {
        DriverManagerDataSource metaDataSource = new DriverManagerDataSource();
        metaDataSource.setDriverClassName(driver);
        metaDataSource.setUrl("jdbc:postgresql://localhost/axon");
        metaDataSource.setUsername(username);
        metaDataSource.setPassword(password);
        return metaDataSource;
    }

    @Bean(name = "axonEntityManagerFactory")
    @Qualifier("axonEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean axonEntityManagerFactory(@Qualifier("axon") DataSource axon) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(axon);
        em.setPackagesToScan(
                "org.axonframework.eventsourcing.eventstore.jpa",
                "org.axonframework.eventhandling.saga.repository.jpa",
                "org.axonframework.modelling.saga.repository.jpa",
                "org.axonframework.eventhandling.tokenstore.jpa");
        final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(jpaProperties());
//        em.setMappingResources("/orm.xml");
        return em;
    }

    public Properties jpaProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
        properties.setProperty("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect","org.hibernate.dialect.PostgreSQL94Dialect");
        return properties;
    }

    @Bean("axonPlatformTransactionManager")
    @Qualifier("axonPlatformTransactionManager")
    public PlatformTransactionManager axonPlatformTransactionManager(@Qualifier("axonEntityManagerFactory") LocalContainerEntityManagerFactoryBean axonEntityManagerFactory) {
        JpaTransactionManager axonTransactionManager = new JpaTransactionManager();
        axonTransactionManager
                .setEntityManagerFactory(axonEntityManagerFactory.getObject());
        return axonTransactionManager;
    }

    @Bean
    public TransactionManager axonTransactionManager(PlatformTransactionManager axonPlatformTransactionManager) {
        return new SpringTransactionManager(axonPlatformTransactionManager);
    }


    /**
     * For axon framework
     * @param entityManagerFactory
     * @return
     */
    @Bean
    public EntityManagerProvider entityManagerProvider(@Qualifier("axonEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return () -> entityManagerFactory.getObject().createEntityManager();
    }

    @Bean
    public EventStorageEngine eventStorageEngine(EntityManagerProvider entityManagerProvider, TransactionManager axonTransactionManager) throws SQLException {
        return JpaEventStorageEngine.builder().dataSource(axon()).entityManagerProvider(entityManagerProvider).transactionManager(axonTransactionManager).build();
    }

    @Bean
    public TokenStore tokenStore(Serializer serializer, EntityManagerProvider entityManagerProvider) {
        return JpaTokenStore.builder()
                .entityManagerProvider(entityManagerProvider)
                .serializer(serializer)
                .build();
    }


}
