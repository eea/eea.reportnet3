package org.eea.datalake.service.impl;

import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static org.eea.utils.LiteralConstants.S3_BUCKET_NAME;
import static org.eea.utils.LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH;

@Service
public class S3HelperImpl implements S3Helper {

    private static final Logger LOG = LoggerFactory.getLogger(S3HelperImpl.class);

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
    public File getFileFromS3(String key) throws IOException {
        GetObjectRequest objectRequest = GetObjectRequest
            .builder()
            .key(key)
            .bucket(S3_BUCKET_NAME)
            .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
        byte[] data = objectBytes.asByteArray();

        String filename = new File(key).getName();
        // Write the data to a local file.
        File parquetFile = new File(exportDLPath + filename);
        LOG.info("Local file {}", parquetFile);
        OutputStream os = new FileOutputStream(parquetFile);
        os.write(data);
        LOG.info("Successfully obtained bytes from file: {}", filename);
        os.close();
        return parquetFile;
    }


}
