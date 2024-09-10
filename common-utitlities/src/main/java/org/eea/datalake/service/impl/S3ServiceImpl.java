package org.eea.datalake.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.s3configuration.types.S3Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;


import static org.eea.utils.LiteralConstants.*;


@Service
@Primary
public class S3ServiceImpl implements S3Service {

    private final DataSetControllerZuul dataSetControllerZuul;
    private final String S3_DEFAULT_BUCKET;
    private final String S3_DEFAULT_BUCKET_PATH;
    private final String S3_ICEBERG_BUCKET;

    private final String S3_ICEBERG_BUCKET_PATH;

    private final String S3_DEFAULT_BUCKET_NAME;

    private final String S3_ICEBERG_BUCKET_NAME;


    private static final Logger LOG = LoggerFactory.getLogger(S3ServiceImpl.class);

    public S3ServiceImpl(DataSetControllerZuul dataSetControllerZuul, @Qualifier("s3PrivateConfiguration") S3Configuration s3PrivateConfiguration) {
        this.dataSetControllerZuul = dataSetControllerZuul;
        this.S3_DEFAULT_BUCKET = s3PrivateConfiguration.getDefaultBucket();
        this.S3_DEFAULT_BUCKET_PATH = s3PrivateConfiguration.getS3DefaultBucketPath();
        this.S3_ICEBERG_BUCKET = s3PrivateConfiguration.getIcebergBucket();
        this.S3_ICEBERG_BUCKET_PATH = s3PrivateConfiguration.getS3IcebergBucketPath();
        this.S3_DEFAULT_BUCKET_NAME = s3PrivateConfiguration.getS3DefaultBucketName();
        this.S3_ICEBERG_BUCKET_NAME = s3PrivateConfiguration.getS3IcebergBucketName();
    }

    @Override
    public String getS3Path(S3PathResolver s3PathResolver) {
        String s3Path = calculateS3Path(s3PathResolver);
        return s3Path;
    }

    @Override
    public String getTableAsFolderQueryPath(S3PathResolver s3PathResolver, String path) {
        String s3TableNamePath = calculateS3TableAsFolderPath(s3PathResolver, path);
        return s3TableNamePath;
    }

    @Override
    public String getTableAsFolderQueryPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3TableAsFolderPath(s3PathResolver);
        return s3TableNamePath;
    }

    @Override
    public String getTableDCAsFolderQueryPath(S3PathResolver s3PathResolver, String path) {
        String s3TableNamePath = calculateS3TableDCAsFolderPath(s3PathResolver, path);
        return s3TableNamePath;
    }

    private String calculateS3Path(S3PathResolver s3PathResolver) {
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = (s3PathResolver.getDataProviderName() == null)
            ? formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN)
            : s3PathResolver.getDataProviderName();
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);
        String fileName = s3PathResolver.getFilename();
        String path = s3PathResolver.getPath();
        String dataCollectionFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_COLLECTION_PATTERN);
        String parquetFolder = s3PathResolver.getParquetFolder();
        String snapshotFolder = formatSnapshotFolder(s3PathResolver.getSnapshotId());
        String euDatasetFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_EU_DATASET_PATTERN);
        String tableName = s3PathResolver.getTableName();

        switch (path) {
            case S3_IMPORT_QUERY_PATH:
            case S3_TABLE_AS_FOLDER_QUERY_PATH:
            case S3_TABLE_NAME_QUERY_PATH:
            case S3_IMPORT_CSV_FILE_QUERY_PATH:
            case S3_TABLE_NAME_VALIDATE_QUERY_PATH:
                path = S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataProviderFolder,
                    datasetFolder, tableName, fileName);
                break;
            case S3_IMPORT_FILE_PATH:
            case S3_TABLE_NAME_PATH:
            case S3_VALIDATION_RULE_PATH:
            case S3_TABLE_NAME_VALIDATE_PATH:
            case S3_ATTACHMENTS_PATH:
                path = String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, tableName, fileName);
                break;
            case S3_VALIDATION_QUERY_PATH:
                path = S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataProviderFolder,
                    datasetFolder, s3PathResolver.getValidationId(), fileName);
                break;
            case S3_VALIDATION_PATH:
                String.format(path, dataflowFolder, dataProviderFolder, datasetFolder,
                    s3PathResolver.getValidationId(), fileName);
                break;
            case S3_TABLE_NAME_FOLDER_PATH:
                path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder,
                    tableName);
                break;
            case S3_PROVIDER_IMPORT_PATH:
                if (!s3PathResolver.isDeleteFile()) {
                    fileName = System.currentTimeMillis() + "_" + fileName; //generating unique name, avoiding conflicts
                }
                path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, fileName);
                break;
            case S3_CURRENT_PATH:
            case S3_SNAPSHOT_FOLDER_PATH:
                path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder);
                break;
            case S3_VALIDATION_DC_QUERY_PATH:
                path = S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataCollectionFolder,
                    s3PathResolver.getValidationId(), dataProviderFolder, fileName);
                break;
            case S3_VALIDATION_DC_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder,
                    s3PathResolver.getValidationId(), dataProviderFolder, fileName);
                break;
            case S3_TABLE_NAME_VALIDATE_DC_QUERY_PATH:
                path = S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataCollectionFolder,
                    tableName, dataProviderFolder, parquetFolder, fileName);
                break;
            case S3_TABLE_NAME_DC_PATH:
            case S3_TABLE_NAME_VALIDATE_DC_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder, tableName,
                    dataProviderFolder, parquetFolder, fileName);
                break;
            case S3_EXPORT_QUERY_PATH:
                path = S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataCollectionFolder,
                    fileName);
                break;
            case S3_EXPORT_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder, fileName);
                break;
            case S3_EXPORT_FOLDER_PATH:
            case S3_TABLE_NAME_ROOT_DC_FOLDER_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder);
                break;
            case S3_TABLE_NAME_DC_PROVIDER_FOLDER_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder, tableName,
                    dataProviderFolder);
                break;
            case S3_TABLE_NAME_DC_QUERY_PATH:
                path = S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataCollectionFolder,
                    tableName);
                break;
            case S3_TABLE_NAME_DC_FOLDER_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder, tableName);
                break;
            case S3_DATAFLOW_REFERENCE_PATH:
                path = String.format(path, dataflowFolder, tableName, parquetFolder, fileName);
                break;
            case S3_DATAFLOW_REFERENCE_FOLDER_PATH:
                path = String.format(path, dataflowFolder, tableName);
                break;
            case S3_PROVIDER_SNAPSHOT_PATH:
                path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder,
                    snapshotFolder, tableName, parquetFolder, fileName);
                break;
            case S3_TABLE_NAME_WITH_PARQUET_FOLDER_PATH:
                path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder,
                    tableName, parquetFolder, fileName);
                break;
            case S3_EU_SNAPSHOT_PATH:
                path = String.format(path, dataflowFolder, euDatasetFolder, tableName,
                    dataProviderFolder, parquetFolder, fileName);
                break;
            case S3_EU_SNAPSHOT_ROOT_PATH:
                path = String.format(path, dataflowFolder, euDatasetFolder);
                break;
            case S3_EU_SNAPSHOT_TABLE_PATH:
                path = String.format(path, dataflowFolder, euDatasetFolder, tableName);
                break;
            case S3_SUPPORTING_DOCUMENTS_FILE_PATH:
                path = String.format(path, dataflowFolder, fileName);
                break;
            case S3_TECHNICAL_ACCEPTANCE_FILE_PATH:
                path = String.format(path, dataflowFolder, dataProviderFolder, fileName);
                break;
            case S3_ATTACHMENTS_DC_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder, tableName,
                        dataProviderFolder, fileName);
                break;
            case S3_ATTACHMENTS_EU_PATH:
                path = String.format(path, dataflowFolder, euDatasetFolder, tableName,
                        dataProviderFolder, fileName);
                break;
            default:
                LOG.info("Wrong type value: {}", path);
                path = null;
                break;
        }

        return path;
    }

    private String calculateS3TableAsFolderPath(S3PathResolver s3PathResolver, String path) {
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);
        String dataCollectionFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_COLLECTION_PATTERN);
        String euDatasetFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_EU_DATASET_PATTERN);

        switch (path) {
            case S3_EXPORT_PREFILLED_TABLE_FILE_PATH:
            case S3_IMPORT_FILE_PATH:
            case S3_TABLE_NAME_PATH:
                return String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, s3PathResolver.getTableName(), s3PathResolver.getFilename());
            case S3_IMPORT_CSV_FILE_QUERY_PATH:
                return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, s3PathResolver.getTableName(), s3PathResolver.getFilename());
            case S3_REFERENCE_FOLDER_PATH:
                return String.format(path, dataflowFolder);
            case S3_DATAFLOW_REFERENCE_FOLDER_PATH:
                return String.format(path, dataflowFolder, s3PathResolver.getTableName());
            case S3_DATAFLOW_REFERENCE_QUERY_PATH:
                return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, s3PathResolver.getTableName());
            case S3_EXPORT_PREFILLED_TABLE_AS_FOLDER_QUERY_PATH:
            case S3_TABLE_AS_FOLDER_QUERY_PATH:
                if(BooleanUtils.isTrue(s3PathResolver.getIsIcebergTable())){
                    return S3_ICEBERG_BUCKET + String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, s3PathResolver.getTableName());
                }
                return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, s3PathResolver.getTableName());
            case S3_IMPORT_TABLE_NAME_FOLDER_PATH:
            case S3_VALIDATION_TABLE_PATH:
            case S3_TABLE_NAME_FOLDER_PATH:
            case S3_ATTACHMENTS_TABLE_PATH:
            case S3_CURRENT_PATH:
                return String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, s3PathResolver.getTableName());
            case S3_ATTACHMENTS_DC_FOLDER_PATH:
                return String.format(path, dataflowFolder, dataCollectionFolder, s3PathResolver.getTableName(), dataProviderFolder);
            case S3_ATTACHMENTS_EU_FOLDER_PATH:
                return String.format(path, dataflowFolder, euDatasetFolder, s3PathResolver.getTableName(), dataProviderFolder);
            default:
                LOG.info("Wrong type value: {}", path);
                break;
        }

        return null;
    }

    private String calculateS3TableAsFolderPath(S3PathResolver s3PathResolver) {
        String path = s3PathResolver.getPath();
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);
        String euDatasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_EU_DATASET_PATTERN);

        switch (path) {
            case S3_DATAFLOW_REFERENCE_FOLDER_PATH:
                return String.format(path, dataflowFolder, s3PathResolver.getTableName());
            case S3_DATAFLOW_REFERENCE_QUERY_PATH:
                return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder,
                    s3PathResolver.getTableName());
            case S3_TABLE_NAME_FOLDER_PATH:
                return String.format(path, dataflowFolder, dataProviderFolder, datasetFolder,
                    s3PathResolver.getTableName());
            case S3_TABLE_NAME_EU_QUERY_PATH:
                return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, euDatasetFolder,
                    s3PathResolver.getTableName());
            default:
                LOG.info("Wrong type value: {}", path);
                break;
        }

        return null;
    }

    private String calculateS3TableDCAsFolderPath(S3PathResolver s3PathResolver, String path) {
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataCollectionFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_COLLECTION_PATTERN);
        String dataProviderId =  formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetId =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);
        String euDatasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_EU_DATASET_PATTERN);

        switch (path) {
            case S3_TABLE_NAME_DC_QUERY_PATH:
                return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataCollectionFolder,
                    s3PathResolver.getTableName());
            case S3_DATAFLOW_REFERENCE_QUERY_PATH:
                return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder,
                    s3PathResolver.getTableName());
            case S3_TABLE_AS_FOLDER_QUERY_PATH:
                return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataProviderId,
                    datasetId, s3PathResolver.getTableName());
            case S3_TABLE_NAME_EU_QUERY_PATH:
                return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, euDatasetFolder,
                    s3PathResolver.getTableName());
            default:
                LOG.info("Wrong type value: {}", path);
                break;
        }
        return null;
    }

    private String formatFolderName(Long id, String pattern) {
        String idStr = String.valueOf(id);
        String providerFolder = StringUtils.leftPad(idStr, S3_NAME_PATTERN_LENGTH, S3_LEFT_PAD);
        return String.format(pattern, providerFolder);
    }

    @Override
    public String getTablePathByDatasetType(Long dataflowId, Long datasetId, String tableName, S3PathResolver tableResolver) {
        DatasetTypeEnum datasetTypeEnum = dataSetControllerZuul.getDatasetType(datasetId);
        String tablePath = null;
        switch (datasetTypeEnum) {
            case REFERENCE:
                String dataflowFolder = formatFolderName(dataflowId, S3_DATAFLOW_PATTERN);
                tablePath = S3_DEFAULT_BUCKET + String.format(S3_DATAFLOW_REFERENCE_QUERY_PATH,
                    dataflowFolder, tableName);
                break;
            default:
                tablePath =
                    this.getTableAsFolderQueryPath(tableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
                break;
        }
        return tablePath;
    }

    @Override
    public S3PathResolver getS3PathResolverByDatasetType(DataSetMetabaseVO dataset, String tableName, Boolean isIceberg) {
        S3PathResolver s3PathResolver;

        if(!isIceberg && dataset.getDatasetTypeEnum() == DatasetTypeEnum.REFERENCE){
            s3PathResolver = new S3PathResolver(dataset.getDataflowId(),
                    dataset.getDataProviderId() != null ? dataset.getDataProviderId() : 0,
                    dataset.getId(), tableName);
            s3PathResolver.setPath(S3_DATAFLOW_REFERENCE_FOLDER_PATH);
        }
        else if(!isIceberg && dataset.getDatasetTypeEnum() == DatasetTypeEnum.COLLECTION){
            s3PathResolver =
                    new S3PathResolver(dataset.getDataflowId(), dataset.getId(), tableName,
                            S3_TABLE_NAME_ROOT_DC_FOLDER_PATH);
        }
        else if(!isIceberg && dataset.getDatasetTypeEnum() == DatasetTypeEnum.EUDATASET){
            s3PathResolver =
                    new S3PathResolver(dataset.getDataflowId(), dataset.getId(), tableName,
                            S3_EU_SNAPSHOT_ROOT_PATH);
        }
        else {
            s3PathResolver = new S3PathResolver(dataset.getDataflowId(),
                    dataset.getDataProviderId() != null ? dataset.getDataProviderId() : 0,
                    dataset.getId(), tableName);
            s3PathResolver.setPath(S3_TABLE_NAME_FOLDER_PATH);
        }

        return s3PathResolver;
    }

    private String formatSnapshotFolder(Long snapshotId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = dateFormat.format(new Date());
        return String.format(S3_SNAPSHOT_PATTERN, snapshotId, date);
    }

    @Override
    public String getS3DefaultBucket() {
        return S3_DEFAULT_BUCKET;
    }

    @Override
    public String getS3DefaultBucketPath() {
        return S3_DEFAULT_BUCKET_PATH;
    }

    @Override
    public String getS3IcebergBucket(){
        return S3_ICEBERG_BUCKET;
    }

    @Override
    public String getS3IcebergBucketPath(){
        return S3_ICEBERG_BUCKET_PATH;
    }

    @Override
    public String getS3DefaultBucketName(){
        return S3_DEFAULT_BUCKET_NAME;
    }

    @Override
    public String getS3IcebergBucketName(){
        return S3_ICEBERG_BUCKET_NAME;
    }


}
