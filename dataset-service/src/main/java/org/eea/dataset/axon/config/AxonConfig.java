package org.eea.dataset.axon.config;

import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.SQLErrorCodesResolver;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.mode.CapabilityDiscoveryMode;
import org.axonframework.modelling.saga.repository.jpa.JpaSagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.axonframework.springboot.util.RegisterDefaultEntities;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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


@EnableTransactionManagement
@RegisterDefaultEntities(packages = {
        "org.axonframework.eventhandling.tokenstore",
        "org.axonframework.eventhandling.deadletter.jpa",
        "org.axonframework.modelling.saga.repository.jpa",
        "org.axonframework.eventsourcing.eventstore.jpa",
        "org.axonframework.eventhandling.saga.repository.jpa"
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

    private Predicate<ServiceInstance> serviceInstanceFilter = (serviceInstance) -> {
        //TODO replace hardoded IP
        return  serviceInstance.getHost().equals("192.168.1.4");
    };

    @Bean("springCloudCommandRouter")
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient, Registration localServiceInstance, RoutingStrategy routingStrategy, CapabilityDiscoveryMode capabilityDiscoveryMode, Serializer serializer) {
        return SpringCloudCommandRouter.builder().discoveryClient(discoveryClient).localServiceInstance(localServiceInstance).routingStrategy(routingStrategy).capabilityDiscoveryMode(capabilityDiscoveryMode).serializer(serializer)
                .serviceInstanceFilter(serviceInstanceFilter).build();
    }

    @Bean(name="axon")
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
        em.setPersistenceUnitName("eventStorePersistenceUnit");
        return em;
    }

    public Properties jpaProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
        properties.setProperty("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect","org.hibernate.dialect.PostgreSQLDialect");
        return properties;
    }

    @Bean(name="axonPlatformTransactionManager")
    @Qualifier("axonPlatformTransactionManager")
    public PlatformTransactionManager axonPlatformTransactionManager(@Qualifier("axonEntityManagerFactory") LocalContainerEntityManagerFactoryBean axonEntityManagerFactory) {
        JpaTransactionManager axonTransactionManager = new JpaTransactionManager();
        axonTransactionManager
                .setEntityManagerFactory(axonEntityManagerFactory.getObject());
        return axonTransactionManager;
    }

    @Bean(name="axonTransactionManager")
    @Qualifier(("axonTransactionManager"))
    public TransactionManager axonTransactionManager(@Qualifier("axonPlatformTransactionManager")PlatformTransactionManager axonPlatformTransactionManager) {
        return new SpringTransactionManager(axonPlatformTransactionManager);
    }

    /**
     * For axon framework
     * @param entityManagerFactory
     * @return
     */
    @Bean("axonEntityManagerProvider")
    public EntityManagerProvider entityManagerProvider(@Qualifier("axonEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        AxonEntityManagerProvider axonEntityManagerProvider = new AxonEntityManagerProvider();
        axonEntityManagerProvider.setEntityManager(entityManagerFactory.getObject().createEntityManager());
        return axonEntityManagerProvider;
    }

    @Primary
    @Bean(name="eventStorageEngine")
    public EventStorageEngine eventStorageEngine(Serializer defaultSerializer,@Qualifier("persistenceExceptionResolver") PersistenceExceptionResolver persistenceExceptionResolver, @Qualifier("eventSerializer") Serializer eventSerializer, org.axonframework.config.Configuration configuration, @Qualifier("axonEntityManagerProvider")EntityManagerProvider entityManagerProvider, @Qualifier("axonTransactionManager")TransactionManager transactionManager) {
        return JpaEventStorageEngine.builder().eventSerializer(JacksonSerializer.defaultSerializer()).snapshotSerializer(JacksonSerializer.defaultSerializer()).upcasterChain(configuration.upcasterChain()).persistenceExceptionResolver(persistenceExceptionResolver).eventSerializer(eventSerializer).snapshotFilter(configuration.snapshotFilter()).entityManagerProvider(entityManagerProvider).transactionManager(transactionManager).build();
    }

    @Bean
    @Primary
    public TokenStore tokenStore(Serializer serializer, @Qualifier("axonEntityManagerProvider")EntityManagerProvider entityManagerProvider) {
        return JpaTokenStore.builder()
                .entityManagerProvider(entityManagerProvider)
                .serializer(serializer)
                .build();
    }

    @Primary
    @Bean
    public JpaSagaStore sagaStore(Serializer serializer,@Qualifier("axonEntityManagerProvider")EntityManagerProvider entityManagerProvider) {
        return JpaSagaStore.builder()
                .entityManagerProvider(entityManagerProvider)
                .serializer(serializer)
                .build();
    }

    @Bean(name="persistenceExceptionResolver")
    @Primary
    public PersistenceExceptionResolver persistenceExceptionResolver(@Qualifier("axon")DataSource dataSource) throws SQLException {
        return new SQLErrorCodesResolver(dataSource);
    }

    @Primary
    @Bean
    public EmbeddedEventStore eventStore(@Qualifier("eventStorageEngine") EventStorageEngine storageEngine, org.axonframework.config.Configuration configuration) {
        return EmbeddedEventStore.builder().storageEngine(storageEngine).messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore")).spanFactory(configuration.spanFactory()).build();
    }
}
