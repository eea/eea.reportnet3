package org.eea.document.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The Class DocumentConfiguration.
 */
@Configuration
@EnableAspectJAutoProxy
@EnableWebMvc
public class DocumentConfiguration implements WebMvcConfigurer {

  /**
   * The max file size.
   */
  @Value("${spring.servlet.multipart.max-file-size}")
  private Long maxFileSize;


  /**
   * The max request size.
   */
  @Value("${spring.servlet.multipart.max-request-size}")
  private Long maxRequestSize;

  /**
   * Multipart resolver.
   *
   * @return the multipart resolver
   */
  @Bean
  public MultipartResolver multipartResolver() {
    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    multipartResolver.setMaxUploadSize(maxFileSize);
    multipartResolver.setMaxUploadSizePerFile(maxRequestSize);
    return multipartResolver;
  }

}
