package org.eea.dataset.service.impl;

import org.eea.dataset.service.S3HandlerService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.nio.file.Paths;

@Service
public class S3HandlerServiceImpl implements S3HandlerService {

    private static final Logger LOG = LoggerFactory.getLogger(S3HandlerServiceImpl.class);

    @Autowired
    S3Client s3Client;

    public void uploadFileToBucket(String filePathInS3, String filePathInReportnet) {
        //TODO handle replace Data
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(LiteralConstants.S3_BUCKET_NAME)
                .key(filePathInS3)
                .build();

        java.nio.file.Path file = Paths.get(filePathInReportnet);

        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, file);
    }
}
