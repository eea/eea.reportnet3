package org.eea.datalake.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.s3configuration.types.S3Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.eea.utils.LiteralConstants.*;

@Service
@Primary
public class S3HelperImpl implements S3Helper {

    private static final Logger LOG = LoggerFactory.getLogger(S3HelperImpl.class);

    private final S3Service s3Service;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String S3_DEFAULT_BUCKET_NAME;

    private final String S3_ICEBERG_BUCKET_NAME;

    @Autowired
    public S3HelperImpl(S3Service s3Service, @Qualifier("s3PrivateConfiguration") S3Configuration s3Configuration) {
        this.s3Service = s3Service;
        this.s3Client = s3Configuration.getS3Client();
        this.s3Presigner = s3Configuration.getS3Presigner();
        this.S3_DEFAULT_BUCKET_NAME = s3Configuration.getS3DefaultBucketName();
        this.S3_ICEBERG_BUCKET_NAME = s3Configuration.getS3IcebergBucketName();
    }

    /**
     * builds query for counting records
     * @param s3PathResolver
     * @return
     */
    @Override
    public String buildRecordsCountQuery(S3PathResolver s3PathResolver) {
        StringBuilder query = new StringBuilder();
        query.append("select count(*) from ");
        query.append(s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH));
        return query.toString();
    }

    /**
     * builds query for counting records
     * @param s3PathResolver
     * @return
     */
    @Override
    public String getRecordsCountQuery(S3PathResolver s3PathResolver) {
        StringBuilder query = new StringBuilder();
        query.append("select count(*) from ");
        query.append(s3Service.getTableAsFolderQueryPath(s3PathResolver));
        return query.toString();
    }

    /**
     * builds query for counting records
     * @param s3PathResolver
     * @return
     */
    @Override
    public String buildRecordsCountQueryDC(S3PathResolver s3PathResolver) {
        StringBuilder query = new StringBuilder();
        query.append("select count(*) from ");
        query.append(s3Service.getTableDCAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_DC_QUERY_PATH));
        return query.toString();
    }

    /**
     * checks if folder validation is created in the s3 storage for the specific dataset
     * @param s3PathResolver
     * @param path
     * @return
     */
    @Override
    public boolean checkFolderExist(S3PathResolver s3PathResolver, String path) {
        String key = s3Service.getTableAsFolderQueryPath(s3PathResolver, path);
        if(!key.endsWith("/")){
            //adding suffix to differentiate between folders with the same prefix
            key = key + "/";
        }
        String finalKey = key;
        String bucketName = (BooleanUtils.isTrue(s3PathResolver.getIsIcebergTable())) ? S3_ICEBERG_BUCKET_NAME : S3_DEFAULT_BUCKET_NAME;
        return s3Client.listObjects(b -> b.bucket(bucketName).prefix(finalKey)).contents().size() > 0;
    }

    /**
     * checks if folder validation is created in the s3 storage for the specific dataset
     * @param s3PathResolver
     * @return
     */
    @Override
    public boolean checkFolderExist(S3PathResolver s3PathResolver) {
        String key = s3Service.getTableAsFolderQueryPath(s3PathResolver);
        if(!key.endsWith("/")){
            //adding suffix to differentiate between folders with the same prefix
            key = key + "/";
        }
        String finalKey = key;
        String bucketName = (BooleanUtils.isTrue(s3PathResolver.getIsIcebergTable())) ? S3_ICEBERG_BUCKET_NAME : S3_DEFAULT_BUCKET_NAME;
        return s3Client.listObjects(b -> b.bucket(bucketName).prefix(finalKey)).contents().size() > 0;
    }

    /**
     * Deletes folder from s3
     * @param s3PathResolver
     * @param folderPath
     */
    @Override
    public void deleteFolder(S3PathResolver s3PathResolver, String folderPath) {
        String bucketName = (BooleanUtils.isTrue(s3PathResolver.getIsIcebergTable())) ? S3_ICEBERG_BUCKET_NAME : S3_DEFAULT_BUCKET_NAME;
        String folderName = s3Service.getTableAsFolderQueryPath(s3PathResolver, folderPath);
        ListObjectsV2Response result = s3Client.listObjectsV2(b -> b.bucket(bucketName).prefix(folderName));
        GetBucketVersioningResponse bucketVersioning = s3Client.getBucketVersioning(builder -> builder.bucket(bucketName));
        if (bucketVersioning.status()!=null && (bucketVersioning.status().equals(BucketVersioningStatus.ENABLED) || bucketVersioning.status().equals(BucketVersioningStatus.SUSPENDED))) {
            result.contents().forEach(s3Object -> {
                ListObjectVersionsResponse versions = s3Client.listObjectVersions(builder -> builder.bucket(bucketName).prefix(s3Object.key()));
                versions.versions().forEach(version -> s3Client.deleteObject(builder -> builder.bucket(bucketName).key(s3Object.key()).versionId(version.versionId())));
            });
        } else {
            result.contents().forEach(s3Object -> {
                s3Client.deleteObject(builder -> builder.bucket(bucketName).key(s3Object.key()));
            });
        }
    }

    /**
     * Deletes file from s3
     * @param filePath
     */
    @Override
    public void deleteFile(String filePath){
        s3Client.deleteObject(builder -> builder.bucket(S3_DEFAULT_BUCKET_NAME).key(filePath));
    }

    /**
     * Gets filenames from table name folders
     * @param s3PathResolver
     * @return
     */
    @Override
    public List<S3Object> getFilenamesFromTableNames(S3PathResolver s3PathResolver) {
        String bucketName = (BooleanUtils.isTrue(s3PathResolver.getIsIcebergTable())) ? S3_ICEBERG_BUCKET_NAME : S3_DEFAULT_BUCKET_NAME;
        String key = s3Service.getS3Path(s3PathResolver);
        return s3Client.listObjects(b -> b.bucket(bucketName).prefix(key)).contents();
    }

    /**
     * Gets file
     * @param key
     * @param fileName
     * @param path
     * @param fileType
     * @return
     */
    @Override
    public File getFileFromS3(String key, String fileName, String path, String fileType) throws IOException {
        byte[] data = getBytesFromS3(key);

        // Write the data to a local file.
        String filePath = null;
        if(StringUtils.isNotBlank(fileType)) {
            filePath = path + fileName + fileType;
        }
        else{
            filePath = path + fileName;
        }
        File file = new File(filePath);

        if(file.exists()){
            //if a file with the same name exists in the path, delete it so that it will be recreated
            file.delete();
        }
        Path textFilePath = Paths.get(file.toString());
        Files.createFile(textFilePath);
        OutputStream os = new FileOutputStream(file);
        os.write(data);
        LOG.info("Successfully obtained bytes from file: {}", filePath);
        os.close();
        return file;
    }

    /**
     * Gets file for export
     * @param key
     * @param fileName
     * @param path
     * @param fileType
     * @return
     */
    @Override
    public File getFileFromS3Export(String key, String fileName, String path, String fileType, Long datasetId) throws IOException {
        byte[] data = getBytesFromS3(key);

        // Write the data to a local file.
        File file = new File(new File(path, "dataset-" + datasetId), fileName + fileType);

        if(file.exists()){
            //if a file with the same name exists in the path, delete it so that it will be recreated
            file.delete();
        }
        Path textFilePath = Paths.get(file.toString());
        Files.createFile(textFilePath);
        OutputStream os = new FileOutputStream(file);
        os.write(data);
        LOG.info("Successfully obtained bytes from file: {}", fileName + fileType);
        os.close();
        return file;
    }

    /**
     * Uploads a file in s3
     * @param filePathInS3
     * @param filePathInReportnet
     * @return
     */
    @Override
    public void uploadFileToBucket(String filePathInS3, String filePathInReportnet) {
        PutObjectRequest putObjectRequest = getPutObjectRequest(filePathInS3);

        java.nio.file.Path file = Paths.get(filePathInReportnet);

        s3Client.putObject(putObjectRequest, file);
    }

    @Override
    public List<ObjectIdentifier> listObjectsInBucket(String prefix){
        List<ObjectIdentifier> objectKeys = new ArrayList<>();
        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .bucket(S3_DEFAULT_BUCKET_NAME)
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

    /**
     * checks if table names DC folders have been created in the s3 storage
     * @param s3PathResolver
     * @return
     */
    @Override
    public boolean checkTableNameDCProviderFolderExist(S3PathResolver s3PathResolver) {
        String key = s3Service.getS3Path(s3PathResolver);
        return s3Client.listObjects(b -> b.bucket(S3_DEFAULT_BUCKET_NAME).prefix(key)).contents().size() > 0;
    }

    /**
     * checks if table names DC fodlers have been created in the s3 storage
     * @param s3PathResolver
     * @return
     */
    @Override
    public boolean checkTableNameDCFolderExist(S3PathResolver s3PathResolver) {
        String key = s3Service.getS3Path(s3PathResolver);
        return s3Client.listObjects(b -> b.bucket(S3_DEFAULT_BUCKET_NAME).prefix(key)).contents().size() > 0;
    }


    /**
     * Deletes talbe name DC folder from s3
     * @param s3PathResolver
     */
    @Override
    public void deleteTableNameDCFolder(S3PathResolver s3PathResolver) {
        String folderName = s3Service.getS3Path(s3PathResolver);
        ListObjectsV2Response result = s3Client.listObjectsV2(b -> b.bucket(S3_DEFAULT_BUCKET_NAME).prefix(folderName));
        result.contents().forEach(s3Object -> s3Client.deleteObject(builder -> builder.bucket(S3_DEFAULT_BUCKET_NAME).key(s3Object.key())));
    }

    /**
     * Deletes snapshot folder from s3
     * @param s3PathResolver
     */
    @Override
    public void deleteSnapshotFolder(S3PathResolver s3PathResolver) {
        String folderName = s3Service.getS3Path(s3PathResolver);
        ListObjectsV2Response result = s3Client.listObjectsV2(b -> b.bucket(S3_DEFAULT_BUCKET_NAME).prefix(folderName));
        result.contents().stream()
            .filter(path -> path.key().contains("/snap-"+s3PathResolver.getSnapshotId()+"-"))
            .forEach(s3Object -> s3Client.deleteObject(builder -> builder.bucket(S3_DEFAULT_BUCKET_NAME).key(s3Object.key())));
    }

    /**
     * Generate s3 pre signed Url
     *
     * @param filePath the path where the file will be imported into
     * @return the url
     */
    @Override
    public String generatePUTPreSignedUrl(String filePath){
        PutObjectRequest putObjectRequest = getPutObjectRequest(filePath);

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        URL url = presignedRequest.url();


        return url.toString();
    }

    /**
     * Generate s3 pre signed Url
     *
     * @param filePath the path where the file will be imported into
     * @return the url
     */
    @Override
    public String generateGETPreSignedUrl(String filePath) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(S3_DEFAULT_BUCKET_NAME)
            .key(filePath)
            .build();

        GetObjectPresignRequest  presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .getObjectRequest(getObjectRequest)
            .build();

        PresignedGetObjectRequest objectRequest = s3Presigner.presignGetObject(presignRequest);
        URL url = objectRequest.url();

        return url.toString();
    }

    /**
     * Copies a file from one destination to another
     * @param source
     * @param destination
     * @return
     */
    @Override
    public void copyFileToAnotherDestination(String source, String destination){
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(S3_DEFAULT_BUCKET_NAME)
                .sourceKey(source)
                .destinationBucket(S3_DEFAULT_BUCKET_NAME)
                .destinationKey(destination)
                .build();

        s3Client.copyObject(copyReq);
    }

    @Override
    public S3Service getS3Service() {
        return s3Service;
    }

    @Override
    public void deleteFileFromS3(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .key(key)
            .bucket(S3_DEFAULT_BUCKET_NAME)
            .build();
        s3Client.deleteObject(deleteObjectRequest);
        LOG.info("File with key " + key + " deleted from S3 public bucket.");
    }

    private PutObjectRequest getPutObjectRequest(String filePathInS3) {
        return PutObjectRequest.builder()
            .bucket(S3_DEFAULT_BUCKET_NAME)
            .key(filePathInS3)
            .build();
    }

    private byte[] getBytesFromS3(String key) {
        GetObjectRequest objectRequest = GetObjectRequest
            .builder()
            .key(key)
            .bucket(S3_DEFAULT_BUCKET_NAME)
            .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
        return objectBytes.asByteArray();
    }
}
