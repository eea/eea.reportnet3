package org.eea.communication.axon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;


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
//        config.usingSubscribingEventProcessors();
//    }

    @Bean
    public DataSource axon() {
        DriverManagerDataSource metaDataSource = new DriverManagerDataSource();
        metaDataSource.setDriverClassName(driver);
        metaDataSource.setUrl("jdbc:postgresql://localhost/axon");
        metaDataSource.setUsername(username);
        metaDataSource.setPassword(password);

        return metaDataSource;
    }

//    @Bean(name="axonEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//        LocalContainerEntityManagerFactoryBean metadataSetsEM =
//                new LocalContainerEntityManagerFactoryBean();
//        metadataSetsEM.setDataSource(axon());
//        metadataSetsEM.setPackagesToScan("org.axonframework.eventsourcing.eventstore.jpa",
//                "org.axonframework.eventhandling.saga.repository.jpa",
//                "org.axonframework.modelling.saga.repository.jpa");
//        JpaVendorAdapter vendorMetabaseAdapter = new HibernateJpaVendorAdapter();
//        metadataSetsEM.setJpaVendorAdapter(vendorMetabaseAdapter);
//        metadataSetsEM.setJpaProperties(jpaProperties());
//        return metadataSetsEM;
//    }
//
//    /**
//     * For axon framework
//     * @param entityManagerFactory
//     * @return
//     */
//    @Bean
//    public EntityManagerProvider entityManagerProvider(@Qualifier("axonEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
//        return () -> entityManagerFactory.getObject().createEntityManager();
//    }
//
//    private Properties jpaProperties() {
//        Properties props = new Properties();
//        props.setProperty("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
//        props.setProperty("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
//        props.setProperty("hibernate.hbm2ddl.auto", "update");
//        props.setProperty("hibernate.show_sql", "true");
//        props.setProperty("hibernate.dialect", dialect);
//        return props;
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManagerAxon() {
//
//        JpaTransactionManager metabasetransactionManager = new JpaTransactionManager();
//        metabasetransactionManager
//                .setEntityManagerFactory(entityManagerFactory().getObject());
//        return metabasetransactionManager;
//    }

}
