package org.eea.communication.axon.config;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.axonframework.extensions.springcloud.commandhandling.mode.CapabilityDiscoveryMode;
import org.axonframework.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.function.Predicate;


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

    @Autowired
    DiscoveryClient discoveryClient;

    @Autowired
    private ApplicationContext appContext;

    private Predicate<ServiceInstance> serviceInstanceFilter = (serviceInstance) -> {
        return  serviceInstance.getHost().equals("192.168.1.3");
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

    @Bean("distributedCommandBus")
    @Primary
    @ConditionalOnBean({CommandBusConnector.class})
    @ConditionalOnMissingBean
    public DistributedCommandBus distributedCommandBus(CommandRouter commandRouter, CommandBusConnector commandBusConnector) {
        DistributedCommandBus commandBus = DistributedCommandBus.builder().commandRouter(commandRouter).connector(commandBusConnector).build();
        commandBus.updateLoadFactor(101);
        return commandBus;
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean("springHttpCommandBusConnector")
    @Primary
    @ConditionalOnMissingBean(CommandBusConnector.class)
    public CommandBusConnector springHttpCommandBusConnector(@Qualifier("localSegment") CommandBus localSegment,
                                                             RestTemplate restTemplate,
                                                             @Qualifier("messageSerializer") Serializer serializer) {
        return SpringHttpCommandBusConnector.builder()
                .localCommandBus(localSegment)
                .restOperations(restTemplate)
                .serializer(serializer)
                .build();
    }
}
