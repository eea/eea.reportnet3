package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;
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
     * checks if folder validation is created in the s3 storage for the specific dataset
     * @param s3PathResolver
     * @param path
     * @return
     */
    boolean checkFolderExist(S3PathResolver s3PathResolver, String path);

    /**
     * Deletes folder from s3
     * @param s3PathResolver
     * @param folderPath
     */
    void deleleFolder(S3PathResolver s3PathResolver, String folderPath);

    /**
     * Gets filenames from export folder
     * @param s3PathResolver
     * @return
     */
    List<S3Object> getFilenamesFromFolderExport(S3PathResolver s3PathResolver);

    /**
     * Gets file from S3
     * @param filename
     * @return
     */
    File getFileFromS3(String filename, String datasetName) throws IOException;

    /**
     * Uploads a file in s3
     * @param filePathInS3
     * @param filePathInReportnet
     * @return
     */
    void uploadFileToBucket(String filePathInS3, String filePathInReportnet);
}
