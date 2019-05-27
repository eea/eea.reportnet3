package org.eea.validation.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The Class ValidationConfiguration.
 */
@Configuration
// @EnableJpaRepositories(basePackages = "org.eea.validation.repository")
@EnableTransactionManagement
// @EntityScan(basePackages = "org.eea.validation.model")
public class ValidationConfiguration {

}
