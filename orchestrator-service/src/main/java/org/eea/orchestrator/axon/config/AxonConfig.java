package org.eea.orchestrator.axon.config;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider;
import org.eea.orchestrator.axon.interceptor.CorrelationDataInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

    @Autowired
    public void registerCreateProductCommandInterceptor(ApplicationContext context,
                                                        CommandBus commandBus) {
        commandBus.registerHandlerInterceptor(new CorrelationDataInterceptor(correlationDataProviders()));
    }

    @Bean
    CorrelationDataProvider correlationDataProviders() {
        return new SimpleCorrelationDataProvider("auth");
    }
}
