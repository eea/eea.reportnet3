package org.eea.datalake.service.impl;

import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import static org.eea.utils.LiteralConstants.S3_BUCKET_NAME;
import static org.eea.utils.LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH;

@Service
public class S3HelperImpl implements S3Helper {

    private S3Service s3Service;
    private S3Client s3Client;

    @Autowired
    public S3HelperImpl(S3Service s3Service, S3Client s3Client) {
        this.s3Service = s3Service;
        this.s3Client = s3Client;
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
     * checks if folder validation is created in the s3 storage for the specific dataset
     * @param s3PathResolver
     * @param path
     * @return
     */
    @Override
    public boolean checkFolderExist(S3PathResolver s3PathResolver, String path) {
        String key = s3Service.getTableAsFolderQueryPath(s3PathResolver, path);
        return s3Client.listObjects(b -> b.bucket(S3_BUCKET_NAME).prefix(key)).contents().size() > 0;
    }
}
