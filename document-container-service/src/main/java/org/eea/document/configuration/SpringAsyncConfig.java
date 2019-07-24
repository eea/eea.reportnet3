package org.eea.document.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The Class SpringAsyncConfig.
 */
@Configuration
@EnableAsync
public class SpringAsyncConfig implements AsyncConfigurer {

}
