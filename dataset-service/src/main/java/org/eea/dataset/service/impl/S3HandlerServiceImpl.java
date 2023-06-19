package org.eea.dataset.service.impl;

import org.eea.dataset.service.S3HandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.nio.file.Paths;

@Service
public class S3HandlerServiceImpl implements S3HandlerService {

    private static final Logger LOG = LoggerFactory.getLogger(S3HandlerServiceImpl.class);

    @Autowired
    S3Client s3Client;


    public Boolean checkIfBucketExists(String bucketName) {
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();
        try {
            s3Client.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }

    public void uploadFileToBucket(String bucketName, String s3Path, String fileName, String filePath) {
        //TODO add presigned url
        //TODO send files to specific folders
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        java.nio.file.Path file = Paths.get(filePath);

        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, file);
    }
}
