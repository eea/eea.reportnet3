package org.eea.document.configuration;

import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The Class DocumentConfiguration.
 */
@Configuration
@EnableAspectJAutoProxy
@EnableWebMvc
public class DocumentConfiguration implements WebMvcConfigurer {

}
