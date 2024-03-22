package org.eea.recordstore.configuration;

import org.eea.test.S3MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.eea.test.TypeOfS3.LOCAL_S3;

@Configuration
public class S3ClientConfiguration {

    private final S3MapService s3MapService;

    @Autowired
    public S3ClientConfiguration(S3MapService s3MapService) {
        this.s3MapService = s3MapService;
    }

    @Bean
    public S3Client getS3Client() {
        return s3MapService.getS3Configuration(LOCAL_S3.getName()).getS3Client();
    }

    @Bean
    public S3Presigner getS3Presigner() {
        return s3MapService.getS3Configuration(LOCAL_S3.getName()).getS3Presigner();
    }
}
