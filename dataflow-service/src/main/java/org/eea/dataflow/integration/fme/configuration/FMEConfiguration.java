package org.eea.dataflow.integration.fme.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("org.eea.dataflow.integration.fme.repository")
public class FMEConfiguration {

}
