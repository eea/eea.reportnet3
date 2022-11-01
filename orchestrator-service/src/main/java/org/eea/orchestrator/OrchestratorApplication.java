package org.eea.orchestrator;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.messaging.StreamableMessageSource;
import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.eea.swagger.EnableEEASwagger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 * The Class OrchestratorApplication.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableEEASwagger
@EeaEnableSecurity
public class OrchestratorApplication {

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(OrchestratorApplication.class, args);
  }

  @Autowired
  public void configureSegmentCount(EventProcessingConfigurer processingConfigurer) {
    TrackingEventProcessorConfiguration tepConfig =
            TrackingEventProcessorConfiguration.forParallelProcessing(2)
                    .andInitialSegmentsCount(2).andInitialTrackingToken(StreamableMessageSource::createHeadToken);
    processingConfigurer.registerTrackingEventProcessorConfiguration("ReleaseSagaProcessor", config -> tepConfig);
  }

}
