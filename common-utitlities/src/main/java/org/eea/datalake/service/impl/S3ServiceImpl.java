package org.eea.datalake.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static org.eea.utils.LiteralConstants.*;


@Service
public class S3ServiceImpl implements S3Service {

    private static final Logger LOG = LoggerFactory.getLogger(S3ServiceImpl.class);

    @Override
    public String getProviderPath(S3PathResolver s3PathResolver) {
        String s3ValidationPath = calculateS3ProviderPath(s3PathResolver);
        LOG.info("Method getProviderPath returns S3 Validation Path: {}", s3ValidationPath);
        return s3ValidationPath;
    }

    @Override
    public String getDCPath(S3PathResolver s3PathResolver) {
        String s3ValidationPath = calculateS3DCPath(s3PathResolver);
        LOG.info("Method getDCPath returns S3 Validation Path: {}", s3ValidationPath);
        return s3ValidationPath;
    }

    @Override
    public String getProviderQueryPath(S3PathResolver s3PathResolver) {
        String s3ImportPath = calculateS3ProviderPath(s3PathResolver);
        LOG.info("Method getImportProviderQueryPath returns S3 Import Path: {}", s3ImportPath);
        return s3ImportPath;
    }

    @Override
    public String getDCQueryPath(S3PathResolver s3PathResolver) {
        String s3ValidationPath = calculateS3DCPath(s3PathResolver);
        LOG.info("Method getValidationDCQueryPath returns S3 Validation Path: {}", s3ValidationPath);
        return s3ValidationPath;
    }

    @Override
    public String getTableAsFolderQueryPath(S3PathResolver s3PathResolver, String path) {
        String s3TableNamePath = calculateS3TableAsFolderPath(s3PathResolver, path);
        LOG.info("Method getTableAsFolderQueryPath returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    @Override
    public String getTableDCAsFolderQueryPath(S3PathResolver s3PathResolver, String path) {
        String s3TableNamePath = calculateS3TableDCAsFolderPath(s3PathResolver, path);
        LOG.info("Method getTableDCAsFolderQueryPath returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    private String calculateS3ProviderPath(S3PathResolver s3PathResolver) {
        LOG.info("Method calculateS3Path called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);
        String fileName = s3PathResolver.getFilename();
        String path = s3PathResolver.getPath();

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
                path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder);
                break;
            default:
                LOG.info("Wrong type value: {}", path);
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
        }
        return String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getTableName());
    }

    private String calculateS3TableDCAsFolderPath(S3PathResolver s3PathResolver, String path) {
        LOG.info("Method calculateS3TableDCAsFolderPath called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataCollectionFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_COLLECTION_PATTERN);

        switch (path) {
            case S3_TABLE_NAME_DC_QUERY_PATH:
                return String.format(path, dataflowFolder, S3_COLLECTIONS, dataCollectionFolder, s3PathResolver.getTableName());
            default:
                LOG.info("Wrong type value: {}", path);
                break;
        }
        return null;
    }

    private String calculateS3DCPath(S3PathResolver s3PathResolver) {
        LOG.info("Method calculateS3DCPath called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String fileName = s3PathResolver.getFilename();
        String dataCollectionFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_COLLECTION_PATTERN);
        String dataProviderFolder =  formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String path = s3PathResolver.getPath();
        String parquetFolder = s3PathResolver.getParquetFolder();

        switch (path) {
            case S3_VALIDATION_DC_PATH:
            case S3_VALIDATION_DC_QUERY_PATH:
                path = String.format(path, dataflowFolder,
                    S3_COLLECTIONS, dataCollectionFolder, s3PathResolver.getValidationId(), dataProviderFolder, fileName);
                break;
            case S3_TABLE_NAME_DC_PATH:
            case S3_TABLE_NAME_VALIDATE_DC_PATH:
            case S3_TABLE_NAME_VALIDATE_DC_QUERY_PATH:
                path = String.format(path, dataflowFolder,
                    S3_COLLECTIONS, dataCollectionFolder, s3PathResolver.getTableName(), dataProviderFolder, parquetFolder, fileName);
                break;
            case S3_EXPORT_PATH:
            case S3_EXPORT_QUERY_PATH:
                path = String.format(path, dataflowFolder,
                    S3_COLLECTIONS, dataCollectionFolder, fileName);
                break;
            case S3_EXPORT_FOLDER_PATH:
            case S3_TABLE_NAME_ROOT_DC_FOLDER_PATH:
                path = String.format(path, dataflowFolder,
                    S3_COLLECTIONS, dataCollectionFolder);
                break;
            case S3_TABLE_NAME_DC_PROVIDER_FOLDER_PATH:
                path = String.format(path, dataflowFolder, S3_COLLECTIONS, dataCollectionFolder, s3PathResolver.getTableName(), dataProviderFolder);
                break;
            case S3_TABLE_NAME_DC_FOLDER_PATH:
            case S3_TABLE_NAME_DC_QUERY_PATH:
                path = String.format(path, dataflowFolder, S3_COLLECTIONS, dataCollectionFolder, s3PathResolver.getTableName());
                break;
            case S3_DATAFLOW_REFERENCE_PATH:
                path = String.format(path, dataflowFolder, S3_REFERENCE, s3PathResolver.getTableName(), parquetFolder, fileName);
                break;
            default:
                LOG.info("Wrong type value: {}", path);
                break;
        }
        return path;
    }

    private String formatFolderName(Long id, String pattern) {
        String idStr = String.valueOf(id);
        String providerFolder = StringUtils.leftPad(idStr, S3_NAME_PATTERN_LENGTH, S3_LEFT_PAD);
        return String.format(pattern, providerFolder);
    }
}
