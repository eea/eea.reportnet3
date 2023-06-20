package org.eea.dataset.service;

public interface S3HandlerService {

    /**
     * Uploads file to bucket
     *
     * @param fileName the file name
     * @param filePath the file path
     * @return
     */
    void uploadFileToBucket(String fileName, String filePath);
}
