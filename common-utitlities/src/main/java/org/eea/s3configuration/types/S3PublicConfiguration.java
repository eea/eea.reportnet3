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
import software.amazon.awssdk.services.s3.model.PutBucketEncryptionRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryptionByDefault;
import software.amazon.awssdk.services.s3.model.ServerSideEncryptionConfiguration;
import software.amazon.awssdk.services.s3.model.ServerSideEncryptionRule;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.utils.AttributeMap;

import javax.annotation.PostConstruct;
import java.net.URI;

@Service
public class S3PublicConfiguration implements S3Configuration {
  @Value("${amazon.s3.public.endpoint}")
  private String s3Endpoint;
  @Value("${amazon.s3.public.accessKey}")
  private String s3AccessKey;
  @Value("${amazon.s3.public.secretKey}")
  private String s3SecretKey;

  @Value("${amazon.s3.public.needs.encryption}")
  private boolean needsEncryption;
  @Value("${amazon.s3.public.algorithm}")
  private String algorithm;

  @Value("${s3.default.public.bucket}")
  private String S3_DEFAULT_BUCKET;
  @Value("${s3.default.public.bucket.name}")
  private String S3_DEFAULT_BUCKET_NAME;
  @Value("${s3.default.public.bucket.path}")
  private String S3_DEFAULT_BUCKET_PATH;

  private final static Region s3Region = Region.EU_WEST_1;

  private static AwsBasicCredentials awsCredentials;

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

    S3Client s3Client = S3Client.builder()
        .endpointOverride(URI.create(s3Endpoint))
        .httpClient(httpClient)
        .region(s3Region)
        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
        .build();

    // Encryption Configuration
    if (needsEncryption) {
      s3Client.putBucketEncryption(PutBucketEncryptionRequest.builder()
          .bucket(getS3DefaultBucketName())
          .serverSideEncryptionConfiguration(ServerSideEncryptionConfiguration.builder()
              .rules(ServerSideEncryptionRule.builder()
                  .applyServerSideEncryptionByDefault(applyServerSideEncryptionByDefault())
                  .build())
              .build())
          .build());
    }

    return s3Client;
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

  @Override
  public String getS3DefaultBucketPath() {
    return S3_DEFAULT_BUCKET_PATH;
  }

  @Override
  public String getIcebergBucket() {
    return null;
  }

  @Override
  public String getS3IcebergBucketName() {
    return null;
  }

  @Override
  public String getS3IcebergBucketPath() {
    return null;
  }

  private ServerSideEncryptionByDefault applyServerSideEncryptionByDefault() {
    return ServerSideEncryptionByDefault.builder()
        .sseAlgorithm(algorithm)
        .build();
  }
}