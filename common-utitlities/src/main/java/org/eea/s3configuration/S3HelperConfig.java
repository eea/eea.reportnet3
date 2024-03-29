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

  private final S3Service s3Service;

  public S3HelperConfig(S3Service s3Service) {
    this.s3Service = s3Service;
  }

/*  @Bean
  @Qualifier("private")*/
  public S3Helper s3HelperPrivate(@Qualifier("s3PrivateConfiguration") S3Configuration s3LocalConfiguration) {
    return new S3HelperImpl(s3Service, s3LocalConfiguration);
  }

  @Bean
  @Qualifier("public")
  public S3Helper s3HelperPublic(@Qualifier("s3PublicConfiguration") S3Configuration s3PublicConfiguration) {
    return new S3HelperImpl(s3Service, s3PublicConfiguration);
  }
}
