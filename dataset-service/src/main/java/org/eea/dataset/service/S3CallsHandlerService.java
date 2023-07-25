package org.eea.dataset.service;

import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.util.List;

public interface S3CallsHandlerService {

    /**
     * Uploads file to bucket
     *
     * @param fileName the file name
     * @param filePath the file path
     * @return
     */
    void uploadFileToBucket(String fileName, String filePath);

    /**
     * Deletes objects from bucket
     *
     * @param filePath the file path
     * @return
     */
    void deleteObjectsFromBucket(String filePath);

    /**
     * Lists object in bucket
     *
     * @param prefix the prefix of the file structure
     * @return
     */
    List<ObjectIdentifier> listObjectsInBucket(String prefix);
}
