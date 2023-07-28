package org.eea.dataset.service.impl;

import org.eea.dataset.service.S3CallsHandlerService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3CallsHandlerServiceImpl implements S3CallsHandlerService {

    private static final Logger LOG = LoggerFactory.getLogger(S3CallsHandlerServiceImpl.class);

    @Autowired
    S3Client s3Client;

    @Override
    public void uploadFileToBucket(String filePathInS3, String filePathInReportnet) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(LiteralConstants.S3_BUCKET_NAME)
                .key(filePathInS3)
                .build();

        java.nio.file.Path file = Paths.get(filePathInReportnet);

        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, file);
    }

    @Override
    public List<ObjectIdentifier> listObjectsInBucket(String prefix){
        List<ObjectIdentifier> objectKeys = new ArrayList<>();
        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .bucket(LiteralConstants.S3_BUCKET_NAME)
                .prefix(prefix)
                .build();
        ListObjectsResponse listObjectsResponse;
        do {
            listObjectsResponse = s3Client.listObjects(listObjectsRequest);
            listObjectsResponse.contents().stream().forEach(s3Object -> {
                ObjectIdentifier objectId = ObjectIdentifier.builder()
                        .key(s3Object.key())
                        .build();
                objectKeys.add(objectId);
            });
        } while (listObjectsResponse.isTruncated());
        return objectKeys;
    }

    @Override
    public void deleteObjectsFromBucket(String filePath){
        List<ObjectIdentifier> objectKeys = listObjectsInBucket(filePath);
        if(objectKeys == null || objectKeys.size() == 0){
            return;
        }
        // Delete multiple objects in one request.
        Delete del = Delete.builder()
                .objects(objectKeys)
                .build();
        DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(LiteralConstants.S3_BUCKET_NAME)
                .delete(del)
                .build();

        DeleteObjectsResponse deleteObjectsResponse = s3Client.deleteObjects(deleteObjectsRequest);
    }

    @Override
    public void deleteObjectFromBucket(String fileName){
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(LiteralConstants.S3_BUCKET_NAME)
                .key(fileName)
                .build();

        DeleteObjectResponse deleteObjectResponse = s3Client.deleteObject(deleteObjectRequest);
    }
}
