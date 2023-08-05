package org.eea.datalake.service.impl;

import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import static org.eea.utils.LiteralConstants.*;

@Service
public class S3HelperImpl implements S3Helper {

    private static final Logger LOG = LoggerFactory.getLogger(S3HelperImpl.class);
    private static final String VALIDATION="validation";
    private static final String PARQUET_FILE_NAME="/0_0_0.parquet";

    private S3Service s3Service;
    private S3Client s3Client;

    /**
     * The path export DL.
     */
    @Value("${exportDLPath}")
    private String exportDLPath;

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

    /**
     * Deletes folder from s3
     * @param s3PathResolver
     * @param folderPath
     */
    @Override
    public void deleleFolder(S3PathResolver s3PathResolver, String folderPath) {
        String folderName = s3Service.getTableAsFolderQueryPath(s3PathResolver, folderPath);
        ListObjectsV2Response result = s3Client.listObjectsV2(b -> b.bucket(S3_BUCKET_NAME).prefix(folderName));
        GetBucketVersioningResponse bucketVersioning = s3Client.getBucketVersioning(builder -> builder.bucket(S3_BUCKET_NAME));
        if (bucketVersioning.status()!=null && (bucketVersioning.status().equals(BucketVersioningStatus.ENABLED) || bucketVersioning.status().equals(BucketVersioningStatus.SUSPENDED))) {
            result.contents().forEach(s3Object -> {
                ListObjectVersionsResponse versions = s3Client.listObjectVersions(builder -> builder.bucket(S3_BUCKET_NAME).prefix(s3Object.key()));
                versions.versions().forEach(version -> s3Client.deleteObject(builder -> builder.bucket(S3_BUCKET_NAME).key(s3Object.key()).versionId(version.versionId())));
            });
        } else {
            result.contents().forEach(s3Object -> {
                s3Client.deleteObject(builder -> builder.bucket(S3_BUCKET_NAME).key(s3Object.key()));
            });
        }
    }

    /**
     * Gets filenames from export folder
     * @param s3PathResolver
     * @return
     */
    @Override
    public List<S3Object> getFilenamesFromFolderExport(S3PathResolver s3PathResolver) {
        String key = s3Service.getExportFolderPath(s3PathResolver);
        return s3Client.listObjects(b -> b.bucket(S3_BUCKET_NAME).prefix(key)).contents();
    }

    /**
     * Gets file
     * @param key
     * @return
     */
    @Override
    public File getFileFromS3(String key, String datasetName) throws IOException {
        GetObjectRequest objectRequest = GetObjectRequest
            .builder()
            .key(key)
            .bucket(S3_BUCKET_NAME)
            .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
        byte[] data = objectBytes.asByteArray();

        // Write the data to a local file.
        File parquetFile = new File(exportDLPath + datasetName + PARQUET_TYPE);
        LOG.info("Local file {}", parquetFile);
        OutputStream os = new FileOutputStream(parquetFile);
        os.write(data);
        LOG.info("Successfully obtained bytes from file: {}", datasetName + PARQUET_TYPE);
        os.close();
        return parquetFile;
    }

    @Override
    public void deleteRuleFolderIfExists(S3PathResolver validationResolver, RuleVO ruleVO) {
        String validationFolderName = s3Service.getTableAsFolderQueryPath(validationResolver, S3_VALIDATION_TABLE_PATH);
        ListObjectsV2Response result = s3Client.listObjectsV2(b -> b.bucket(S3_BUCKET_NAME).prefix(validationFolderName));
        if (result!=null && result.contents().size()>0) {
            Optional<S3Object> ruleFile = result.contents().stream().filter(s3Object -> s3Object.key().contains(ruleVO.getShortCode())).findFirst();
            if (ruleFile.isPresent()) {
                int valIdx = validationFolderName.indexOf(VALIDATION);
                int startIdx = valIdx + 10;
                String ruleFolderName = ruleFile.get().key();
                if (ruleFolderName.contains(PARQUET_FILE_NAME)) {
                    int endIdx = ruleFolderName.indexOf(PARQUET_FILE_NAME);
                    ruleFolderName = ruleFolderName.substring(startIdx, endIdx);
                } else {
                    ruleFolderName = ruleFolderName.substring(startIdx);
                }
                validationResolver.setTableName(ruleFolderName);
                this.deleleFolder(validationResolver, S3_VALIDATION_RULE_PATH);
            }
            validationResolver.setTableName(S3_VALIDATION);
        }
    }
}
