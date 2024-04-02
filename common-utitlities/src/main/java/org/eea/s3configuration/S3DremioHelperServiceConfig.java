package org.eea.s3configuration;

import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.impl.DremioHelperServiceImpl;
import org.eea.interfaces.controller.dremio.controller.DremioApiController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3DremioHelperServiceConfig {

  private final DremioApiController dremioApiController;

  public S3DremioHelperServiceConfig(DremioApiController dremioApiController) {
    this.dremioApiController = dremioApiController;
  }


  @Bean
  @Qualifier("publicS3DremioHelper")
  public DremioHelperService S3DremioHelperPublic(@Qualifier("publicS3Service") S3Service s3Service) {
    return new DremioHelperServiceImpl(dremioApiController, s3Service);
  }
}
