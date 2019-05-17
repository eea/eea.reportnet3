package org.eea.validation.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
// @EnableJpaRepositories(basePackages = "org.eea.validation.repository")
@EnableTransactionManagement
// @EntityScan(basePackages = "org.eea.validation.model")
public class ValidationConfiguration {

}
