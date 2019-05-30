package org.eea.document.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The Class DocumentConfiguration.
 */
@Configuration
@EnableWebMvc
public class DocumentConfiguration implements WebMvcConfigurer {


  @Override
  public void addCorsMappings(final CorsRegistry registry) {
    registry.addMapping("/**");
  }

  static {
    System.setProperty("oak.documentMK.disableLeaseCheck", "true");
    System.setProperty("oak.documentMK.leaseDurationSeconds", "5");
  }

  /**
   * Instantiates a new document configuration.
   */
  private DocumentConfiguration() {
  }

}
