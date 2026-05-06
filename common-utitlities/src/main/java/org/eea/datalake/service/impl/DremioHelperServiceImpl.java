package org.eea.datalake.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.BooleanUtils;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.DremioApiJob;
import org.eea.datalake.service.model.DremioItemTypeEnum;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.exception.DremioApiException;
import org.eea.exception.EEAException;
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
import  java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
        DremioDirectoryItemsResponse directoryItems;
        try {
            directoryItems = getDirectoryItems(s3PathResolver, folderName);
        } catch (Exception e) {
            LOG.warn("Could not get directory items for folder {}", folderName, e);
            return false;
        }

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

    @SneakyThrows
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
            directoryPath = bucketName + "/" + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_PROVIDER_PATH);
        }
        DremioDirectoryItemsResponse directoryItems = null;
        try {
            directoryItems = dremioApiController.getDirectoryItems(token, directoryPath);
        } catch (FeignException e) {
            //retry call if there is an authentication error
            String errorMessage = "Could not retrieve directory items for datasetId " + s3PathResolver.getDatasetId() + " and table " + s3PathResolver.getTableName();
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                try {
                    directoryItems = dremioApiController.getDirectoryItems(token, directoryPath);
                }
                catch (Exception e2){
                    throw new DremioApiException(errorMessage);
                }
            } else {
                throw new DremioApiException(errorMessage);
            }
        }
        return directoryItems;
    }

    @Override
    public String getFolderId(S3PathResolver s3PathResolver, String folderName) {
        String folderId = null;
        DremioDirectoryItemsResponse directoryItems;
        try {
            directoryItems = getDirectoryItems(s3PathResolver, folderName);
        } catch (Exception e) {
            LOG.warn("Could not retrieve directory items for datasetId" + s3PathResolver.getDatasetId(), e);
            return null;
        }

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
                folderId = item.getId();
            }
        }
        LOG.info("Found folderId {} for folderName {}", folderId, folderName);
        return folderId;
    }

    @SneakyThrows
    @Override
    public void promoteFolderOrFile(S3PathResolver s3PathResolver, String folderName) {
        String directoryPath;
        String folderId;
        DremioPromotionRequestBody requestBody;
        if (S3_IMPORT_FILE_PATH.equals(s3PathResolver.getPath())) {
            directoryPath = S3_DEFAULT_BUCKET_PATH + "/" + s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_IMPORT_FILE_PATH);
            String[] path = directoryPath.split("/");
            requestBody = new DremioCSVPromotionRequestBody(ENTITY_TYPE, DREMIO_CONSTANT + directoryPath, path, DATASET_TYPE, new DremioCSVPromotionRequestBody.Format(CSV_FORMAT_TYPE, true));
        } else if (S3_TABLE_NAME_ROOT_DC_FOLDER_PATH.equals(s3PathResolver.getPath()) || S3_EU_SNAPSHOT_ROOT_PATH.equals(s3PathResolver.getPath())) {
            directoryPath = S3_DEFAULT_BUCKET_PATH + "/" + s3Service.getS3Path(s3PathResolver) + "/" + folderName;
            String[] path = directoryPath.split("/");
            requestBody = new DremioParquetPromotionRequestBody(ENTITY_TYPE, DREMIO_CONSTANT + directoryPath, path, DATASET_TYPE, new DremioParquetPromotionRequestBody.Format(PARQUET_FORMAT_TYPE));
        } else if (S3_TABLE_NAME_EU_QUERY_PATH.equals(s3PathResolver.getPath())) {
            directoryPath = S3_DEFAULT_BUCKET_PATH + "/" + s3Service.getS3Path(s3PathResolver) + "/" + folderName;
            String[] path = directoryPath.split("/");
            requestBody = new DremioParquetPromotionRequestBody(ENTITY_TYPE, DREMIO_CONSTANT + directoryPath, path, DATASET_TYPE, new DremioParquetPromotionRequestBody.Format(PARQUET_FORMAT_TYPE));
        } else {
            directoryPath = S3_DEFAULT_BUCKET_PATH + "/" +  s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_NAME_FOLDER_PATH);
            String[] path = directoryPath.split("/");
            requestBody = new DremioParquetPromotionRequestBody(ENTITY_TYPE, DREMIO_CONSTANT + directoryPath, path, DATASET_TYPE, new DremioParquetPromotionRequestBody.Format(PARQUET_FORMAT_TYPE));
        }

        if(checkFolderPromoted(s3PathResolver, folderName)){
            LOG.info("Folder {} is already promoted", directoryPath);
            return;
        }

        folderId = getFolderId(s3PathResolver, folderName);
        if (folderId == null) {
            try {
                LOG.info("Folder id was null, so I am trying again");
                Thread.sleep(2000);
                folderId = getFolderId(s3PathResolver, folderName);
                LOG.info("Folder id {} after second try", folderId);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            dremioApiController.promote(token, folderId, requestBody);
        } catch (FeignException e) {
            //retry call if there is an authentication error
            String errorMessage = "Could not promote for datasetId " + s3PathResolver.getDatasetId() + " and folder " + folderName;
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                try {
                    dremioApiController.promote(token, folderId, requestBody);
                }
                catch (Exception e2){
                    throw new DremioApiException(errorMessage);
                }
            } else {
                throw new DremioApiException(errorMessage);
            }
        }
        LOG.info("Promoted folder {}", directoryPath);
    }

    @SneakyThrows
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
            //retry call if there is an authentication error
            String errorMessage = "Could not demote for datasetId " + s3PathResolver.getDatasetId() + " and folder " + folderName;
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                try {
                    dremioApiController.demote(token, folderId);
                }
                catch (Exception e2){
                    throw new DremioApiException(errorMessage);
                }
            } else {
                throw new DremioApiException(errorMessage);
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

    @SneakyThrows
    @Override
    public String executeSqlStatement(String sqlStatement){
        DremioSqlRequestBody dremioSqlRequestBody = new DremioSqlRequestBody(sqlStatement);
        try {
            return dremioApiController.sqlQuery(token, dremioSqlRequestBody).getId();
        } catch (FeignException e) {
            //retry call if there is an authentication error
            String errorMessage = "Could not execute sql statement " + sqlStatement;
            if (e.status()== HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                try {
                    return dremioApiController.sqlQuery(token, dremioSqlRequestBody).getId();
                }
                catch (Exception e2){
                    throw new DremioApiException(errorMessage);
                }
            } else {
                throw new DremioApiException(errorMessage);
            }
        }
    }

    @SneakyThrows
    @Override
    public DremioJobStatusResponse pollForJobStatus(String id) {
        try {
            return dremioApiController.pollForJobStatus(token, id);
        } catch (FeignException e) {
            //retry call if there is an authentication error
            String errorMessage = "Could not poll for dremio job status for job id" + id;
            if (e.status() == HttpStatus.UNAUTHORIZED.value()) {
                token = this.getAuthToken();
                try {
                    return dremioApiController.pollForJobStatus(token, id);
                } catch (Exception e2) {
                    throw new DremioApiException(errorMessage);
                }
            } else {
                throw new DremioApiException(errorMessage);
            }
        }
    }

    @Override
    public LinkedHashMap<String, Object> executeSqlStatementGet(String sqlStatement) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        DremioSqlRequestBody requestBody = new DremioSqlRequestBody(sqlStatement);
        String result;
        DremioApiJob dremioApiJob;

        try {
            // Execute SQL query and get job ID
            result = executeWithTokenRefresh(() -> dremioApiController.sqlQueryString(token, requestBody));
            dremioApiJob = objectMapper.readValue(result, DremioApiJob.class);

            LinkedHashMap<String, Object> results = getResults(dremioApiJob.getId());
            if (results == null) {
                LOG.error("In executeSqlStatementGet {} results=null", sqlStatement);
                throw new EEAException("Failed to fetch results within retry limit.");
            }

            return results;

        } catch (FeignException | IOException e) {
            LOG.error("Failed to execute SQL statement: {}", sqlStatement, e);
            throw new EEAException("Failed to execute SQL statement");
        }
        catch (Exception e){
            LOG.error("Unexpected error! Failed to execute SQL statement: {}", sqlStatement, e);
            throw new EEAException("Failed to execute SQL statement");
        }
    }

    private LinkedHashMap<String, Object> getResults(String dremioJobId) throws InterruptedException, EEAException {
        int retryCount = 0;
        int maxRetries = 5;
        int pollIntervalMillis = 2000;
        LinkedHashMap<String, Object> results = null;

        while (results == null && retryCount < maxRetries) {
            try {
                Thread.sleep(pollIntervalMillis);
                results = (LinkedHashMap<String, Object>) dremioApiController.sqlApiResults(token, dremioJobId);
            } catch (FeignException e) {
                if (e.status() == HttpStatus.ACCEPTED.value()) { // 202 Accepted indicates results are not ready
                    LOG.info("Results not ready, retrying...");
                    retryCount++;
                } else if (e.status() == HttpStatus.UNAUTHORIZED.value()) {
                    token = getAuthToken();
                    try {
                        results = (LinkedHashMap<String, Object>) dremioApiController.sqlApiResults(token, dremioJobId);
                    } catch (Exception ex) {
                        LOG.error("Retry failed after token refresh.", ex);
                        throw new EEAException("Retry failed after token refresh.");
                    }
                } else {
                    LOG.error("In getResults for dremio job {} status={} message={}", dremioJobId, e.status(), e.getMessage());
                    throw new EEAException("Failed to fetch results within retry limit. statusCode=" + e.status());
                }
            }
        }
        return results;
    }

    @Override
    public long getRowCount(String tablePath) throws Exception {
        String headerName = "myRowCount";
        String query = String.format(
            "SELECT COUNT(*) AS %s FROM %s ",
            headerName,
            tablePath
        );
        try {
            // Extract and return the row count
            List<LinkedHashMap<String,Object>> rows =  (List<LinkedHashMap<String,Object>>) executeSqlStatementGet(query).get("rows");

            return rows.stream()
                .filter(Objects::nonNull)
                .map(linkedHashMap -> Long.valueOf((Integer) linkedHashMap.get(headerName)))
                .findFirst()
                .orElse(0L);
        } catch (FeignException | IOException e) {
            LOG.error("Failed to execute SQL statement: {}", query, e);
            throw new EEAException("Failed to execute SQL statement.");
        }
    }

    private <T> T executeWithTokenRefresh(Callable<T> operation) throws Exception {
        try {
            return operation.call();
        } catch (FeignException e) {
            if (e.status() == HttpStatus.UNAUTHORIZED.value()) {
                token = getAuthToken();
                try {
                    return operation.call();
                } catch (Exception ex) {
                    LOG.error("Retry failed after token refresh.", ex);
                    throw ex;
                }
            }
            throw e;
        } catch (Exception e) {
            throw new IOException("Execution error", e);
        }
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
                    Thread.sleep(5000);
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
        String refreshTableAndDemoteQuery = "ALTER TABLE " + tablePath + " FORGET METADATA";
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
                Thread.sleep(2000);
            }
        }
        if(!folderWasPromoted) {
            throw new Exception("Could not promote folder " + tablePath);
        }
        LOG.info("Failover demote promote - Started {}", refreshTableAndDemoteQuery);
        executeSqlStatement(refreshTableAndDemoteQuery);
        Thread.sleep(5000);
        executeSqlStatement(refreshTableAndPromoteQuery);
        Thread.sleep(5000);
        LOG.info("Failover demote promote - Ended {}", refreshTableAndPromoteQuery);
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
        LOG.info("Executing query with processId {} in dremio. Query: {}", processId, createNewTableQuery);
        checkIfDremioProcessFinishedSuccessfully(createNewTableQuery, processId, null);
    }

    @Override
    public Long compareNumberOfRecords(String table1Path, String table2Path) throws Exception{
        String recordCountAlias = "table1_count";
        String compareRecordsQuery = "SELECT COUNT(*) AS " + recordCountAlias + " FROM " + table1Path + " HAVING COUNT(*) = (SELECT COUNT(*) FROM " + table2Path + " );";
        String processId = executeSqlStatement(compareRecordsQuery);
        checkIfDremioProcessFinishedSuccessfully(compareRecordsQuery, processId, null);
        LinkedHashMap<String, Object> queryResults = getResults(processId);
        List<LinkedHashMap<String,Object>> rows =  (List<LinkedHashMap<String,Object>>) queryResults.get("rows");

        Optional<Long> numberOfRecords = rows.stream()
            .filter(Objects::nonNull)
            .map(linkedHashMap -> Long.valueOf((Integer) linkedHashMap.get(recordCountAlias)))
            .findFirst();

        if(numberOfRecords.isEmpty()){
            throw new Exception("Could not find " + recordCountAlias + " in query " + compareRecordsQuery);
        }

        return numberOfRecords.get();
    }
}
