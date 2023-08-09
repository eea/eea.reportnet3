package org.eea.datalake.service.impl;

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
import java.util.ArrayList;
import java.util.List;

import static org.eea.utils.LiteralConstants.*;

@Service
public class S3HelperImpl implements S3Helper {

    private static final Logger LOG = LoggerFactory.getLogger(S3HelperImpl.class);

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

    /**
     * Deletes folder from s3
     * @param s3PathResolver
     * @param folderPath
     */
    @Override
    public void deleleFolder(S3PathResolver s3PathResolver, String folderPath) {
        String folderName = s3Service.getTableAsFolderQueryPath(s3PathResolver, folderPath);
        ListObjectsV2Response result = s3Client.listObjectsV2(b -> b.bucket(S3_BUCKET_NAME).prefix(folderName));
        result.contents().forEach(s3Object -> {
            ListObjectVersionsResponse versions = s3Client.listObjectVersions(builder -> builder.bucket(S3_BUCKET_NAME).prefix(s3Object.key()));
            versions.versions().forEach(version -> s3Client.deleteObject(builder -> builder.bucket(S3_BUCKET_NAME).key(s3Object.key()).versionId(version.versionId())));
        });
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
     * @param fileName
     * @param path
     * @param fileType
     * @return
     */
    @Override
    public File getFileFromS3(String key, String fileName, String path, String fileType) throws IOException {
        GetObjectRequest objectRequest = GetObjectRequest
            .builder()
            .key(key)
            .bucket(S3_BUCKET_NAME)
            .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
        byte[] data = objectBytes.asByteArray();

        // Write the data to a local file.
        File file = new File(path + fileName + fileType);
        LOG.info("Local file {}", file);
        OutputStream os = new FileOutputStream(file);
        os.write(data);
        LOG.info("Successfully obtained bytes from file: {}", fileName + fileType);
        os.close();
        return file;
    }

    /**
     * Uploads a file in s3
     * @param filePathInS3
     * @param filePathInReportnet
     * @return
     */
    @Override
    public void uploadFileToBucket(String filePathInS3, String filePathInReportnet) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(LiteralConstants.S3_BUCKET_NAME)
                .key(filePathInS3)
                .build();

        java.nio.file.Path file = Paths.get(filePathInReportnet);

        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, file);
    }

    @Override
    public List<ObjectIdentifier> listObjectsInBucket(String prefix){
        List<ObjectIdentifier> objectKeys = new ArrayList<>();
        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .bucket(LiteralConstants.S3_BUCKET_NAME)
                .prefix(prefix)
                .build();
        ListObjectsResponse listObjectsResponse;
        do {
            listObjectsResponse = s3Client.listObjects(listObjectsRequest);
            listObjectsResponse.contents().stream().forEach(s3Object -> {
                ObjectIdentifier objectId = ObjectIdentifier.builder()
                        .key(s3Object.key())
                        .build();
                objectKeys.add(objectId);
            });
        } while (listObjectsResponse.isTruncated());
        return objectKeys;
    }
}
