package org.eea.rod.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * The type Rod configuration.
 */
@Configuration
@EnableFeignClients("org.eea.rod.persistence.repository")
public class RodConfiguration {

}
