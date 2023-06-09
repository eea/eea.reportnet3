package org.eea.dataset.service.impl;

import org.eea.dataset.service.BigDataDatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.nio.file.Paths;

@Service
public class BigDataDatasetServiceImpl implements BigDataDatasetService {

    @Autowired
    S3Client s3Client;

    @Override
    public void importBigData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId,
                       MultipartFile file, Boolean replace, Long integrationId, String delimiter, String fmeJobId){


        /*
         * Part 1:
         * Lets say we got a zip file
         *
         * extract it
         *
         * convert csv files to parquet
         *
         * send parquet files to s3
         *
         * */

        /*
         * Part 2:
         *
         * Add job and handle it
         * */

        /*
         * Part 3:
         *
         * Add checks for wrong filenames or sth
         * */

        /*
         * Part 4:
         *
         * Case where zip file is in s3 and we need to download it first
         * */

        /*
         * Part 5:
         *
         * Case where we get notification from s3 that zip file has been uploaded
         * */
    }

    public Boolean checkIfBucketExists(String bucketName){
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

    public void uploadFileToBucket(String bucketName, String s3Path, String fileName, String filePath){
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        java.nio.file.Path file = Paths.get(filePath);

        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, file);
    }
}
