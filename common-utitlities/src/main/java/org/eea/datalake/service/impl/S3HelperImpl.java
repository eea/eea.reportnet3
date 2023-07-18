package org.eea.datalake.service.impl;

import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.utils.LiteralConstants;
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
import java.nio.file.Paths;
import java.util.List;

import static org.eea.utils.LiteralConstants.*;

@Service
public class S3HelperImpl implements S3Helper {

    private static final Logger LOG = LoggerFactory.getLogger(S3HelperImpl.class);

    private S3Service s3Service;
    private S3Client s3Client;
    private DremioHelperService dremioHelperService;

    /**
     * The path export DL.
     */
    @Value("${exportDLPath}")
    private String exportDLPath;

    @Autowired
    public S3HelperImpl(S3Service s3Service, S3Client s3Client, DremioHelperService dremioHelperService) {
        this.s3Service = s3Service;
        this.s3Client = s3Client;
        this.dremioHelperService = dremioHelperService;
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
        result.contents().forEach(s3Object -> s3Client.deleteObject(builder -> builder.bucket(S3_BUCKET_NAME).key(s3Object.key())));
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
     * Gets filenames from table name folders
     * @param s3PathResolver
     * @return
     */
    @Override
    public List<S3Object> getFilenamesFromTableNames(S3PathResolver s3PathResolver) {
        String key = s3Service.getTableNameFolderPath(s3PathResolver);
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

    /**
     * checks if table names DC fodlers have been created in the s3 storage
     * @param s3PathResolver
     * @return
     */
    @Override
    public boolean checkTableNameDCFolderExist(S3PathResolver s3PathResolver) {
        String key = s3Service.getTableNameFolderDCPath(s3PathResolver);
        LOG.info("Table name DC folder exist with key: {}", key);
        return s3Client.listObjects(b -> b.bucket(S3_BUCKET_NAME).prefix(key)).contents().size() > 0;
    }


    /**
     * Deletes talbe name DC folder from s3
     * @param s3PathResolver
     */
    @Override
    public void deleleTableNameDCFolder(S3PathResolver s3PathResolver) {
        String folderName = s3Service.getTableNameFolderDCPath(s3PathResolver);
        ListObjectsV2Response result = s3Client.listObjectsV2(b -> b.bucket(S3_BUCKET_NAME).prefix(folderName));
        result.contents().forEach(s3Object -> s3Client.deleteObject(builder -> builder.bucket(S3_BUCKET_NAME).key(s3Object.key())));
    }

    /**
     * Upload file to talbe name DC folder
     * @param filePathInS3
     * @param filePathInReportnet
     */
    @Override
    public void uploadFileToBucket(String filePathInS3, String filePathInReportnet) {
        //TODO handle replace Data
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(LiteralConstants.S3_BUCKET_NAME)
            .key(filePathInS3)
            .build();

        java.nio.file.Path file = Paths.get(filePathInReportnet);

        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, file);
    }


    /**
     * Promote
     * @param s3PathResolver
     * @param fileName
     */
    @Override
    public void promoteFolder(S3PathResolver s3PathResolver, String fileName){
        Long providerId = s3PathResolver.getDataProviderId();
        dremioHelperService.promoteFolder(s3PathResolver, fileName);
    }
}
