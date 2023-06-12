package org.eea.dataset.service;

public interface S3HandlerService {

    /**
     * Checks if bucket exists
     *
     * @param bucketName the bucket name
     * @return true if bucket exists else false
     */
    Boolean checkIfBucketExists(String bucketName);

    /**
     * Checks if bucket exists
     *
     * @param bucketName the bucket name
     * @param s3Path the s3 path
     * @param fileName the file name
     * @param filePath the file path
     * @return
     */
    void uploadFileToBucket(String bucketName, String s3Path, String fileName, String filePath);
}
