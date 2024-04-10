package org.eea.s3configuration;

import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.impl.S3ServiceImpl;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.s3configuration.types.S3Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3ServiceConfig {

  private final DatasetController.DataSetControllerZuul dataSetControllerZuul;

  public S3ServiceConfig(DatasetController.DataSetControllerZuul dataSetControllerZuul) {
    this.dataSetControllerZuul = dataSetControllerZuul;
  }

  @Bean
  @Qualifier("publicS3Service")
  public S3Service s3ServicePublic(@Qualifier("s3PublicConfiguration") S3Configuration s3PublicConfiguration) {
    return new S3ServiceImpl(dataSetControllerZuul, s3PublicConfiguration);
  }
}
