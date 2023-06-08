package org.eea.validation.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.beans.factory.annotation.Value;


import java.net.URI;

@Configuration
public class S3ClientConfiguration {

    @Value("${amazon.s3.endpoint}")
    private String s3Endpoint;
    @Value("${amazon.s3.accessKey}")
    private String s3AccessKey;
    @Value("${amazon.s3.secretKey}")
    private String s3SecretKey;
    private final static Region s3Region = Region.US_EAST_1;

    @Bean
    public S3Client getS3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(s3AccessKey, s3SecretKey);
        return S3Client.builder().endpointOverride(URI.create(s3Endpoint))
                .region(s3Region)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).build();
    }
}
