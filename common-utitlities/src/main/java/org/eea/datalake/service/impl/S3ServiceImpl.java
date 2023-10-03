package org.eea.datalake.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.eea.utils.LiteralConstants.*;


@Service
public class S3ServiceImpl implements S3Service {

    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;

    private static final Logger LOG = LoggerFactory.getLogger(S3ServiceImpl.class);

    @Override
    public String getS3Path(S3PathResolver s3PathResolver) {
        String s3Path = calculateS3Path(s3PathResolver);
        LOG.info("Method calculateS3Path returns Path: {}", s3Path);
        return s3Path;
    }

    @Override
    public String getTableAsFolderQueryPath(S3PathResolver s3PathResolver, String path) {
        String s3TableNamePath = calculateS3TableAsFolderPath(s3PathResolver, path);
        LOG.info("Method getTableAsFolderQueryPath returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    @Override
    public String getTableAsFolderQueryPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3TableAsFolderPath(s3PathResolver);
        LOG.info("Method getTableAsFolderQueryPath returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    @Override
    public String getTableDCAsFolderQueryPath(S3PathResolver s3PathResolver, String path) {
        String s3TableNamePath = calculateS3TableDCAsFolderPath(s3PathResolver, path);
        LOG.info("Method getTableDCAsFolderQueryPath returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    private String calculateS3Path(S3PathResolver s3PathResolver) {
        LOG.info("Method calculateS3Path called with s3PathResolver: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);
        String fileName = s3PathResolver.getFilename();
        String path = s3PathResolver.getPath();
        String dataCollectionFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_COLLECTION_PATTERN);
        String parquetFolder = s3PathResolver.getParquetFolder();
        String snapshotFolder = formatSnapshotFolder();

        switch (path) {
            case S3_IMPORT_FILE_PATH:
            case S3_IMPORT_QUERY_PATH:
            case S3_TABLE_AS_FOLDER_QUERY_PATH:
            case S3_TABLE_NAME_PATH:
            case S3_TABLE_NAME_QUERY_PATH:
            case S3_IMPORT_CSV_FILE_QUERY_PATH:
            case S3_TABLE_NAME_VALIDATE_PATH:
            case S3_TABLE_NAME_VALIDATE_QUERY_PATH:
            case S3_VALIDATION_RULE_PATH:
                path = String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getTableName(), fileName);
                break;
            case S3_VALIDATION_PATH:
            case S3_VALIDATION_QUERY_PATH:
                path = String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getValidationId(), fileName);
                break;
            case S3_TABLE_NAME_FOLDER_PATH:
                path = String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getTableName());
                break;
            case S3_PROVIDER_IMPORT_PATH:
            case S3_CURRENT_PATH:
                path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder);
                break;
            case S3_VALIDATION_DC_PATH:
            case S3_VALIDATION_DC_QUERY_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder, s3PathResolver.getValidationId(), dataProviderFolder, fileName);
                break;
            case S3_TABLE_NAME_DC_PATH:
            case S3_TABLE_NAME_VALIDATE_DC_PATH:
            case S3_TABLE_NAME_VALIDATE_DC_QUERY_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder, s3PathResolver.getTableName(), dataProviderFolder, parquetFolder, fileName);
                break;
            case S3_EXPORT_PATH:
            case S3_EXPORT_QUERY_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder, fileName);
                break;
            case S3_EXPORT_FOLDER_PATH:
            case S3_TABLE_NAME_ROOT_DC_FOLDER_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder);
                break;
            case S3_TABLE_NAME_DC_PROVIDER_FOLDER_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder, s3PathResolver.getTableName(), dataProviderFolder);
                break;
            case S3_TABLE_NAME_DC_FOLDER_PATH:
            case S3_TABLE_NAME_DC_QUERY_PATH:
                path = String.format(path, dataflowFolder, dataCollectionFolder, s3PathResolver.getTableName());
                break;
            case S3_DATAFLOW_REFERENCE_PATH:
                path = String.format(path, dataflowFolder, s3PathResolver.getTableName(), parquetFolder, fileName);
                break;
            case S3_DATAFLOW_REFERENCE_FOLDER_PATH:
                path = String.format(path, dataflowFolder, s3PathResolver.getTableName());
                break;
            case S3_PROVIDER_SNAPSHOT_PATH:
                path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, snapshotFolder, s3PathResolver.getTableName(), parquetFolder, fileName);
                break;
            default:
                LOG.info("Wrong type value: {}", path);
                path = null;
                break;
        }

        return path;
    }

    private String calculateS3TableAsFolderPath(S3PathResolver s3PathResolver, String path) {
        LOG.info("Method calculateS3Path called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);

        if(path.equals(S3_IMPORT_FILE_PATH) || path.equals(S3_IMPORT_CSV_FILE_QUERY_PATH)){
            return String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getTableName(), s3PathResolver.getFilename());
        } else if (path.equals(S3_REFERENCE_FOLDER_PATH)) {
            return String.format(path, dataflowFolder);
        }
        return String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getTableName());
    }

    private String calculateS3TableAsFolderPath(S3PathResolver s3PathResolver) {
        LOG.info("Method calculateS3Path called with s3Path: {}", s3PathResolver);
        String path = s3PathResolver.getPath();
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);

        switch (path) {
            case S3_DATAFLOW_REFERENCE_FOLDER_PATH:
            case S3_DATAFLOW_REFERENCE_QUERY_PATH:
                return String.format(path, dataflowFolder, s3PathResolver.getTableName());
            case S3_TABLE_NAME_FOLDER_PATH:
                return String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, s3PathResolver.getTableName());
            default:
                LOG.info("Wrong type value: {}", path);
                break;
        }

        return null;
    }

    private String calculateS3TableDCAsFolderPath(S3PathResolver s3PathResolver, String path) {
        LOG.info("Method calculateS3TableDCAsFolderPath called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataCollectionFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_COLLECTION_PATTERN);

        switch (path) {
            case S3_TABLE_NAME_DC_QUERY_PATH:
                return String.format(path, dataflowFolder, dataCollectionFolder, s3PathResolver.getTableName());
            case S3_DATAFLOW_REFERENCE_QUERY_PATH:
                return String.format(path, dataflowFolder, s3PathResolver.getTableName());
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
        if (datasetTypeEnum.equals(DatasetTypeEnum.REFERENCE)) {
            String dataflowFolder = formatFolderName(dataflowId, S3_DATAFLOW_PATTERN);
            tablePath = String.format(S3_DATAFLOW_REFERENCE_QUERY_PATH, dataflowFolder, tableName);
        } else {
            tablePath = this.getTableAsFolderQueryPath(tableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        }
        return tablePath;
    }

    private String formatSnapshotFolder() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = dateFormat.format(new Date());
        return String.format(S3_SNAPSHOT_PATTERN, date);
    }
}
