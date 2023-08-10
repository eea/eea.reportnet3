package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
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

    String buildRecordsCountQueryDC(S3PathResolver s3PathResolver);

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
     * Gets filenames from table name folders
     * @param s3PathResolver
     * @return
     */
    List<S3Object> getFilenamesFromTableNames(S3PathResolver s3PathResolver);

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


    /**
     * Lists object in bucket
     *
     * @param prefix the prefix of the file structure
     * @return
     */
    List<ObjectIdentifier> listObjectsInBucket(String prefix);

    /**
     * Chceks if rule folder exists in s3 validation folder and deletes it
     * @param validationResolver
     * @param ruleVO
     */
    void deleteRuleFolderIfExists(S3PathResolver validationResolver, RuleVO ruleVO);

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
    void deleleTableNameDCFolder(S3PathResolver s3PathResolver);
}
