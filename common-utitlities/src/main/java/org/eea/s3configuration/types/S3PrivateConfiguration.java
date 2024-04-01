package org.eea.s3configuration.types;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.utils.AttributeMap;

import javax.annotation.PostConstruct;
import java.net.URI;

@Service
public class S3PrivateConfiguration implements S3Configuration {
  @Value("${amazon.s3.endpoint}")
  private String s3Endpoint;
  @Value("${amazon.s3.accessKey}")
  private String s3AccessKey;
  @Value("${amazon.s3.secretKey}")
  private String s3SecretKey;

  @Value("${s3.default.bucket}")
  private String S3_DEFAULT_BUCKET;
  @Value("${s3.default.bucket.name}")
  private String S3_DEFAULT_BUCKET_NAME;

  private static AwsBasicCredentials awsCredentials;

  private final static Region s3Region = Region.US_EAST_1;

  @PostConstruct
  public void getCredentials() {
    awsCredentials = AwsBasicCredentials.create(s3AccessKey, s3SecretKey);
  }

  @Override
  public S3Client getS3Client() {
    SdkHttpClient httpClient = UrlConnectionHttpClient.builder()
        .buildWithDefaults(AttributeMap.builder()
            .put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, Boolean.TRUE)
            .build());
    return S3Client.builder().endpointOverride(URI.create(s3Endpoint))
        .httpClient(httpClient)
        .region(s3Region)
        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).build();
  }

  @Override
  public S3Presigner getS3Presigner() {
    return S3Presigner.builder().endpointOverride(URI.create(s3Endpoint))
        .region(s3Region)
        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).build();
  }

  @Override
  public String getDefaultBucket() {
    return S3_DEFAULT_BUCKET;
  }

  @Override
  public String getS3DefaultBucketName() {
    return S3_DEFAULT_BUCKET_NAME;
  }
}
