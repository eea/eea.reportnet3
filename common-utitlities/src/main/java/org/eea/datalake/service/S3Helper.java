package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface S3Helper {

    /**
     * builds query for counting records
     * @param s3PathResolver
     * @return
     */
    String buildRecordsCountQuery(S3PathResolver s3PathResolver);

    /**
     * get query for counting records
     * @param s3PathResolver
     * @return
     */
    String getRecordsCountQuery(S3PathResolver s3PathResolver);

    String buildRecordsCountQueryDC(S3PathResolver s3PathResolver);

    /**
     * checks if folder validation is created in the s3 storage for the specific dataset
     * @param s3PathResolver
     * @param path
     * @return
     */
    boolean checkFolderExist(S3PathResolver s3PathResolver, String path);
    /**
     * checks if folder validation is created in the s3 storage for the specific dataset
     * @param s3PathResolver
     * @return
     */
    boolean checkFolderExist(S3PathResolver s3PathResolver);

    /**
     * Deletes folder from s3
     * @param s3PathResolver
     * @param folderPath
     */
    void deleteFolder(S3PathResolver s3PathResolver, String folderPath);

    /**
     * Deletes file from s3
     * @param filePath
     */
    void deleteFile(String filePath);


    /**
     * Gets filenames from table name folders
     * @param s3PathResolver
     * @return
     */
    List<S3Object> getFilenamesFromTableNames(S3PathResolver s3PathResolver);

    /**
     * Gets file from S3
     * @param key
     * @param fileName
     * @param path
     * @param fileType
     * @return
     */
    File getFileFromS3(String key, String fileName, String path, String fileType) throws IOException;

    /**
     * Gets file from S3 for export
     * @param key
     * @param fileName
     * @param path
     * @param fileType
     * @return
     */
    File getFileFromS3Export(String key, String fileName, String path, String fileType, Long datasetId) throws IOException;

    /**
     * Uploads a file in s3
     * @param filePathInS3
     * @param filePathInReportnet
     * @return
     */
    void uploadFileToBucket(String filePathInS3, String filePathInReportnet);


    /**
     * Lists object in bucket
     *
     * @param prefix the prefix of the file structure
     * @return
     */
    List<ObjectIdentifier> listObjectsInBucket(String prefix);

    /**
     * checks if table names DC fodlers have been created in the s3 storage
     * @param s3PathResolver
     * @return
     */
    boolean checkTableNameDCProviderFolderExist(S3PathResolver s3PathResolver);

    boolean checkTableNameDCFolderExist(S3PathResolver s3PathResolver);

    /**
     * Deletes talbe name DC folder from s3
     * @param s3PathResolver
     */
    void deleteTableNameDCFolder(S3PathResolver s3PathResolver);

    /**
     * Deletes snapshot folder from s3
     * @param s3PathResolver
     */
    void deleteSnapshotFolder(S3PathResolver s3PathResolver);

    /**
     * Generate s3 presigned Url
     *
     * @param filePath the path where the file will be imported into
     * @return the url
     */
    String generatePresignedUrl(String filePath);

    /**
     * Copies a file from one destination to another
     * @param source
     * @param destination
     * @return
     */
    void copyFileToAnotherDestination(String source, String destination);
}
