package org.eea.s3configuration;

import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.impl.S3HelperImpl;
import org.eea.s3configuration.types.S3Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3HelperConfig {

  private final S3Service s3PublicService;

  public S3HelperConfig(@Qualifier("publicS3Service") S3Service s3PublicService) {
    this.s3PublicService = s3PublicService;
  }

  @Bean
  @Qualifier("publicS3Helper")
  public S3Helper s3HelperPublic(@Qualifier("s3PublicConfiguration") S3Configuration s3PublicConfiguration) {
    return new S3HelperImpl(s3PublicService, s3PublicConfiguration);
  }
}
