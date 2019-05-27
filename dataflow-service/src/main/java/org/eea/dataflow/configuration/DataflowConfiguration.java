package org.eea.dataflow.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * The Class DataflowConfiguration.
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.eea.dataflow.persistence.repository")
@EnableTransactionManagement
@EntityScan(basePackages = "org.eea.dataflow.persistence.domain")
public class DataflowConfiguration {


}
