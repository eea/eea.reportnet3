package org.eea.validation.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class S3ClientConfiguration {
/*
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

    @Bean
    public S3Configuration getS3Configuration() {
        return s3MapService.getS3Configuration(LOCAL_S3.getName());
    }*/
}
