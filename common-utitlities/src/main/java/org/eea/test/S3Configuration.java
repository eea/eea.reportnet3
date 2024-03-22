package org.eea.test;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public interface S3Configuration {
  S3Client getS3Client();
  S3Presigner getS3Presigner();
  String getDefaultBucket();
}
