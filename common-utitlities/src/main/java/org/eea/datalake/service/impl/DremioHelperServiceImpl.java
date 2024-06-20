package org.eea.datalake.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.apache.commons.lang3.BooleanUtils;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.DremioApiJob;
import org.eea.datalake.service.model.DremioItemTypeEnum;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.controller.dremio.controller.DremioApiController;
import org.eea.interfaces.vo.dremio.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.eea.utils.LiteralConstants.*;

@Service
@Primary
public class DremioHelperServiceImpl implements DremioHelperService {

    private static final Logger LOG = LoggerFactory.getLogger(DremioHelperServiceImpl.class);


    @Value("${dremio.username}")
    private String dremioUsername;

    @Value("${dremio.password}")
    private String dremioPassword;

    @Value("${dremio.jobPolling.numberOfRetries}")
    private Integer numberOfRetriesForJobPolling;

    @Value("${dremio.promote.numberOfRetries}")
    private Integer numberOfRetriesForPromoting;

    private static final String PROMOTED = "PROMOTED";
    private static final String BEARER = "Bearer ";
    public static String token = null;
    public static final String DATASET_TYPE = "PHYSICAL_DATASET";
    public static final String PARQUET_FORMAT_TYPE = "Parquet";
    public static final String CSV_FORMAT_TYPE = "Text";
    public static final String ENTITY_TYPE = "dataset";
    public static final String DREMIO_CONSTANT = "dremio:/";

    private final S3Service s3Service;
    private final DremioApiController dremioApiController;

    private final String S3_DEFAULT_BUCKET_PATH;

    private final String S3_ICEBERG_BUCKET_PATH;

    public DremioHelperServiceImpl(DremioApiController dremioApiController, S3Service s3Service) {
        this.dremioApiController = dremioApiController;
        this.s3Service = s3Service;
        this.S3_DEFAULT_BUCKET_PATH = s3Service.getS3DefaultBucketPath();
        this.S3_ICEBERG_BUCKET_PATH = s3Service.getS3IcebergBucketPath();
    }

    @Override
    public String getAuthToken() {
        DremioCredentials dremioCredentials = new DremioCredentials(dremioUsername, dremioPassword);
        DremioAuthResponse response = dremioApiController.login(dremioCredentials);
        return BEARER + response.getToken();
    }

    @Override
    public boolean checkFolderPromoted(S3PathResolver s3PathResolver, String folderName) {
        DremioDirectoryItemsResponse directoryItems = getDirectoryItems(s3PathResolver, folderName);
        if (directoryItems!=null) {
            Integer itemPosition;
            if (S3_IMPORT_FILE_PATH.equals(s3PathResolver.getPath())) {
                itemPosition = 8;
            } else if (S3_DATAFLOW_REFERENCE_FOLDER_PATH.equals(s3PathResolver.getPath())) {
                itemPosition = 4;
            } else if (S3_EU_SNAPSHOT_ROOT_PATH.equals(s3PathResolver.getPath())) {
                itemPosition = 5;
            } else {
                itemPosition = 6;
            }

            Optional<DremioDirectoryItem> itemOptional = directoryItems.getChildren().stream().filter(di -> di.getPath().get(itemPosition).equals(folderName)).findFirst();
            if (itemOptional.isPresent()) {
                DremioDirectoryItem item = itemOptional.get();
                if (item.getType().equals(DremioItemTypeEnum.DATASET.getValue()) && item.getDatasetType().equals(PROMOTED)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public DremioDirectoryItemsResponse getDirectoryItems(S3PathResolver s3PathResolver, String folderName) {
        String bucketName = (BooleanUtils.isTrue(s3PathResolver.getIsIcebergTable())) ? S3_ICEBERG_BUCKET_PATH : S3_DEFAULT_BUCKET_PATH;
        String directoryPath = null;
        if(S3_IMPORT_FILE_PATH.equals(s3PathResolver.getPath())) {
            directoryPath = bucketName + "/" + s3Service.getTableAsFolderQueryPath(s3PathResolver,
                S3_IMPORT_TABLE_NAME_FOLDER_PATH);
        } else if (S3_TABLE_NAME_ROOT_DC_FOLDER_PATH.equals(s3PathResolver.getPath())
            || S3_EU_SNAPSHOT_ROOT_PATH.equals(s3PathResolver.getPath())) {
            directoryPath = bucketName + "/" + s3Service.getS3Path(s3PathResolver);
        } else if (S3_DATAFLOW_REFERENCE_FOLDER_PATH.equals(s3PathResolver.getPath())) {
            directoryPath = bucketName + "/" + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_REFERENCE_FOLDER_PATH);
        } else {
            directoryPath = bucketName + "/" + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_CURRENT_PATH);
        }
        DremioDirectoryItemsResponse directoryItems = null;
        try {
            directoryItems = dremioApiController.getDirectoryItems(token, directoryPath);
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                directoryItems = dremioApiController.getDirectoryItems(token, directoryPath);
            } else {
                throw e;
            }
        }
        return directoryItems;
    }

    @Override
    public String getFolderId(S3PathResolver s3PathResolver, String folderName) {
        String folderId = null;
        DremioDirectoryItemsResponse directoryItems = getDirectoryItems(s3PathResolver, folderName);
        if (directoryItems!=null) {
            Integer itemPosition;
            if (S3_IMPORT_FILE_PATH.equals(s3PathResolver.getPath())) {
                itemPosition = 8;
            } else if (S3_DATAFLOW_REFERENCE_FOLDER_PATH.equals(s3PathResolver.getPath())) {
                itemPosition = 4;
            } else {
                itemPosition = 6;
            }
            Optional<DremioDirectoryItem> itemOptional = directoryItems.getChildren().stream().filter(di -> di.getPath().get(itemPosition).equals(folderName)).findFirst();
            if (itemOptional.isPresent()) {
                DremioDirectoryItem item = itemOptional.get();
                folderId = item.getId();
            }
        }
        LOG.info("Found folderId {} for folderName {}", folderId, folderName);
        return folderId;
    }

    @Override
    public void promoteFolderOrFile(S3PathResolver s3PathResolver, String folderName) {
        String directoryPath;
        String folderId;
        DremioPromotionRequestBody requestBody;
        if(S3_IMPORT_FILE_PATH.equals(s3PathResolver.getPath())) {
            directoryPath = S3_DEFAULT_BUCKET_PATH + "/" + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_FILE_PATH);
            String[] path = directoryPath.split("/");
            requestBody = new DremioCSVPromotionRequestBody(ENTITY_TYPE, DREMIO_CONSTANT + directoryPath, path, DATASET_TYPE, new DremioCSVPromotionRequestBody.Format(CSV_FORMAT_TYPE, true));
        }
        else{
            directoryPath = S3_DEFAULT_BUCKET_PATH + "/" +  s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH);
            String[] path = directoryPath.split("/");
            requestBody = new DremioParquetPromotionRequestBody(ENTITY_TYPE, DREMIO_CONSTANT + directoryPath, path, DATASET_TYPE, new DremioParquetPromotionRequestBody.Format(PARQUET_FORMAT_TYPE));
        }

        if(checkFolderPromoted(s3PathResolver, folderName)){
            LOG.info("Folder {} is already promoted", directoryPath);
            return;
        }

        folderId = getFolderId(s3PathResolver, folderName);

        try {
            dremioApiController.promote(token, folderId, requestBody);
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                dremioApiController.promote(token, folderId, requestBody);
            } else {
                throw e;
            }
        }
        LOG.info("Promoted folder {}", directoryPath);
    }

    @Override
    public void demoteFolderOrFile(S3PathResolver s3PathResolver, String folderName) {
        String bucketName = (BooleanUtils.isTrue(s3PathResolver.getIsIcebergTable())) ? S3_ICEBERG_BUCKET_PATH : S3_DEFAULT_BUCKET_PATH;
        String directoryPath = bucketName + "/" + s3Service.getTableAsFolderQueryPath(s3PathResolver, s3PathResolver.getPath());
        if(!checkFolderPromoted(s3PathResolver, folderName)){
            LOG.info("Folder {} is not promoted", directoryPath);
            return;
        }
        String folderId = getFolderId(s3PathResolver, folderName);
        try {
            dremioApiController.demote(token, folderId);
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                dremioApiController.demote(token, folderId);
            } else {
                throw e;
            }
        }
        LOG.info("Demoted folder {}", directoryPath);
    }

    @Override
    public void deleteFileFromR3IfExists(String parquetFile) throws Exception {
        // Check that the parquet file exists, if so delete it
        java.nio.file.Path path = Paths.get(parquetFile);
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new Exception("Could not delete file " + parquetFile);
            }
        }
    }

    @Override
    public String executeSqlStatement(String sqlStatement){
        DremioSqlRequestBody dremioSqlRequestBody = new DremioSqlRequestBody(sqlStatement);
        try {
            return dremioApiController.sqlQuery(token, dremioSqlRequestBody).getId();
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                return dremioApiController.sqlQuery(token, dremioSqlRequestBody).getId();
            } else {
                LOG.error("Could not execute sql statement {} in dremio", sqlStatement);
                throw e;
            }
        }
    }

    @Override
    public DremioJobStatusResponse pollForJobStatus(String id){
        try {
            return dremioApiController.pollForJobStatus(token, id);
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                return dremioApiController.pollForJobStatus(token, id);
            } else {
                LOG.error("Could not retrieve dremio job status for id {}", id);
                throw e;
            }
        }
    }

    @Override
    public String executeSqlStatementPost(String sqlStatement){
        DremioSqlRequestBody dremioSqlRequestBody = new DremioSqlRequestBody(sqlStatement);
        String result = null;
        try {
            result = dremioApiController.sqlQueryString(token, dremioSqlRequestBody);
        } catch (FeignException e) {
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                result = dremioApiController.sqlQueryString(token, dremioSqlRequestBody);
            } else {
                LOG.error("Could not execute sql statement {} in dremio", sqlStatement);
                throw e;
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            DremioApiJob dremioApiJob = objectMapper.readValue(result, DremioApiJob.class);
            Object results = dremioApiController.sqlApiResults(token, dremioApiJob.getId());
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void checkIfDremioProcessFinishedSuccessfully(String query, String processId, Long optionalTimeoutMs) throws Exception {
        Boolean processIsFinished = false;
        for(int i=0; i < numberOfRetriesForJobPolling; i++) {
            DremioJobStatusResponse response = this.pollForJobStatus(processId);
            String jobState = response.getJobState().getValue();
            if(jobState.equals(DremioJobStatusEnum.COMPLETED.getValue())) {
                processIsFinished = true;
                break;
            }
            else if(jobState.equals(DremioJobStatusEnum.CANCELED.getValue()) || jobState.equals(DremioJobStatusEnum.FAILED.getValue())){
                processIsFinished = false;
                break;
            }
            else {
                if(optionalTimeoutMs == null){
                    //use default timeout
                    Thread.sleep(10000);
                }
                else{
                    Thread.sleep(optionalTimeoutMs);
                }

            }
        }
        if(!processIsFinished){
            throw new Exception("Could not execute dremio query " + query + " with dremio process Id " + processId);
        }
    }

    @Override
    public void refreshTableMetadataAndPromote(Long jobId, String tablePath, S3PathResolver s3PathResolver, String tableName) throws Exception {
        String refreshTableAndPromoteQuery = "ALTER TABLE " + tablePath + " REFRESH METADATA AUTO PROMOTION";
        Boolean folderWasPromoted = false;
        //we keep trying to promote the folder for a number of retries
        for(int i=0; i < numberOfRetriesForPromoting; i++) {
            executeSqlStatement(refreshTableAndPromoteQuery);
            if(checkFolderPromoted(s3PathResolver, tableName)) {
                LOG.info("For job {} and datasetId {} promoted table {} in retry #{}", jobId, s3PathResolver.getDatasetId(), tablePath, i+1);
                folderWasPromoted = true;
                break;
            }
            else {
                Thread.sleep(10000);
            }
        }
        if(!folderWasPromoted) {
            throw new Exception("Could not promote folder " + tablePath);
        }
    }

    /**
     * Create a table from another table
     *
     * @param oldTablePathInDremio the old path
     * @param newTablePathInDremio the new path
     */
    @Override
    public void createTableFromAnotherTable(String oldTablePathInDremio, String newTablePathInDremio) throws Exception {
        String createNewTableQuery = "CREATE TABLE " + newTablePathInDremio + " AS SELECT * FROM " + oldTablePathInDremio;
        String processId = executeSqlStatement(createNewTableQuery);
        checkIfDremioProcessFinishedSuccessfully(createNewTableQuery, processId, null);
    }
}
