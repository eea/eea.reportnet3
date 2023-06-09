package org.eea.dataset.service;

import org.springframework.web.multipart.MultipartFile;

public interface BigDataDatasetService {

    /**
     * Import big data.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaId the table schema id
     * @param file the file
     * @param replace the replace
     * @param integrationId the integration id
     * @param delimiter the delimiter
     * @param fmeJobId the fmeJobId
     * @return
     */
    void importBigData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId,
                       MultipartFile file, Boolean replace, Long integrationId, String delimiter, String fmeJobId);

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
