package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;

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
}
