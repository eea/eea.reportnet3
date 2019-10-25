package org.eea.enums;

import org.eea.enums.utils.StringToEnumConverterFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class EnumsConfiguration implements WebMvcConfigurer {

  /**
   * Adds the formatters.
   *
   * @param registry the registry
   */
  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverterFactory(new StringToEnumConverterFactory());
  }

}
