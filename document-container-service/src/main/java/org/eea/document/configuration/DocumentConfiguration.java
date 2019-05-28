package org.eea.document.configuration;

import org.springframework.context.annotation.Configuration;

/**
 * The Class DocumentConfiguration.
 */
@Configuration
public class DocumentConfiguration {

  static {
    System.setProperty("oak.documentMK.disableLeaseCheck", "true");
    System.setProperty("oak.documentMK.leaseDurationSeconds", "5");
  }

  /**
   * Instantiates a new document configuration.
   */
  private DocumentConfiguration() {}

}
