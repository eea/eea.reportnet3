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

    //Data Provider Paths
    @Override
    public String getValidationProviderPath(S3PathResolver s3PathResolver) {
        String s3ValidationPath = calculateS3ProviderPath(s3PathResolver, S3_VALIDATION_PATH);
        LOG.info("Method getValidationS3Path returns S3 Validation Path: {}", s3ValidationPath);
        return s3ValidationPath;
    }

    @Override
    public String getImportProviderPath(S3PathResolver s3PathResolver) {
        String s3ImportPath = calculateS3ProviderPath(s3PathResolver, S3_IMPORT_PATH);
        LOG.info("Method getImportProviderPath returns S3 Import Path: {}", s3ImportPath);
        return s3ImportPath;
    }

    @Override
    public String getTableNameProviderPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3ProviderPath(s3PathResolver, S3_TABLE_NAME_PATH);
        LOG.info("Method getTableNameProviderPath returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    @Override
    public String getTableNameValidateProviderPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3ProviderPath(s3PathResolver, S3_TABLE_NAME_VALIDATE_PATH);
        LOG.info("Method getTableNameValidateProviderPath returns S3 Table Name Validate Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    //Data Collection Paths
    @Override
    public String getValidationDCPath(S3PathResolver s3PathResolver) {
        String s3ValidationPath = calculateS3DCPath(s3PathResolver, S3_VALIDATION_DC_PATH);
        LOG.info("Method getValidationDCPath returns S3 Validation Path: {}", s3ValidationPath);
        return s3ValidationPath;
    }

    @Override
    public String getTableNameDCPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3DCPath(s3PathResolver, S3_TABLE_NAME_DC_PATH);
        LOG.info("Method getTableNameDCPath returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    @Override
    public String getTableNameValidateDCPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3DCPath(s3PathResolver, S3_TABLE_NAME_VALIDATE_DC_PATH);
        LOG.info("Method getTableNameValidateDCPath returns S3 Table Name Validate Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    //Other
    @Override
    public String getDataflowReferencePath(S3PathResolver s3PathResolver) {
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataflowReferencePath = String.format(S3_DATAFLOW_REFERENCE_PATH, dataflowFolder, s3PathResolver.getFilename());
        LOG.info("Method getDataflowReferencePath returns S3 Dataflow Reference Path: {}", dataflowReferencePath);
        return dataflowReferencePath;
    }

    @Override
    public String getExportDCPath(S3PathResolver s3PathResolver) {
        String s3ExportPath = calculateS3DCPath(s3PathResolver, S3_EXPORT_PATH);
        LOG.info("Method getExportDCPath returns S3 Export Path: {}", s3ExportPath);
        return s3ExportPath;
    }

    //QUERY Paths
    //Data Provider Paths
    @Override
    public String getValidationProviderQueryPath(S3PathResolver s3PathResolver) {
        String s3ValidationPath = calculateS3ProviderPath(s3PathResolver, S3_VALIDATION_QUERY_PATH);
        LOG.info("Method getValidationProviderQueryPath returns S3 Validation Path: {}", s3ValidationPath);
        return s3ValidationPath;
    }

    @Override
    public String getImportProviderQueryPath(S3PathResolver s3PathResolver) {
        String s3ImportPath = calculateS3ProviderPath(s3PathResolver, S3_IMPORT_QUERY_PATH);
        LOG.info("Method getImportProviderQueryPath returns S3 Import Path: {}", s3ImportPath);
        return s3ImportPath;
    }

    @Override
    public String getTableNameProviderQueryPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3ProviderPath(s3PathResolver, S3_TABLE_NAME_QUERY_PATH);
        LOG.info("Method getTableNameProviderQueryPath returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    @Override
    public String getTableNameValidateProviderQueryPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3ProviderPath(s3PathResolver, S3_TABLE_NAME_VALIDATE_QUERY_PATH);
        LOG.info("Method getTableNameValidateProviderQueryPath returns S3 Table Name Validate Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    //Data Collection Paths
    @Override
    public String getValidationDCQueryPath(S3PathResolver s3PathResolver) {
        String s3ValidationPath = calculateS3DCPath(s3PathResolver, S3_VALIDATION_DC_QUERY_PATH);
        LOG.info("Method getValidationDCQueryPath returns S3 Validation Path: {}", s3ValidationPath);
        return s3ValidationPath;
    }

    @Override
    public String getTableNameDCQueryPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3DCPath(s3PathResolver, S3_TABLE_NAME_DC_QUERY_PATH);
        LOG.info("Method getTableNameDCQueryPath returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    @Override
    public String getTableAsFolderQueryPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3TableAsFolderPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        LOG.info("Method getTableAsFolderQueryPath returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    @Override
    public String getTableNameValidateDCQueryPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3DCPath(s3PathResolver, S3_TABLE_NAME_VALIDATE_DC_QUERY_PATH);
        LOG.info("Method getTableNameValidateDCQueryPath returns S3 Table Name Validate Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    //Other
    @Override
    public String getDataflowReferenceQueryPath(S3PathResolver s3PathResolver) {
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataflowReferencePath = String.format(S3_DATAFLOW_REFERENCE_QUERY_PATH, dataflowFolder, s3PathResolver.getFilename());
        LOG.info("Method getDataflowReferenceQueryPath returns S3 Dataflow Reference Path: {}", dataflowReferencePath);
        return dataflowReferencePath;
    }

    @Override
    public String getExportDCQueryPath(S3PathResolver s3PathResolver) {
        String s3ExportPath = calculateS3DCPath(s3PathResolver, S3_EXPORT_QUERY_PATH);
        LOG.info("Method getExportDCQueryPath returns S3 Export Path: {}", s3ExportPath);
        return s3ExportPath;
    }

    private String calculateS3ProviderPath(S3PathResolver s3PathResolver, String path) {
        LOG.info("Method calculateS3Path called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);
        String fileName = s3PathResolver.getFilename();

        switch (path) {
            case S3_IMPORT_PATH:
            case S3_IMPORT_QUERY_PATH:
                path = String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, fileName);
                break;
            case S3_VALIDATION_PATH:
            case S3_VALIDATION_QUERY_PATH:
                path = String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getValidationId(), fileName);
                break;
            case S3_TABLE_NAME_PATH:
            case S3_TABLE_NAME_QUERY_PATH:
            case S3_TABLE_NAME_VALIDATE_PATH:
            case S3_TABLE_NAME_VALIDATE_QUERY_PATH:
                path = String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getTableName(), fileName);
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
        return String.format(path, dataflowFolder,
                dataProviderFolder, datasetFolder, s3PathResolver.getTableName());
    }

    private String calculateS3DCPath(S3PathResolver s3PathResolver, String path) {
        LOG.info("Method calculateS3DCPath called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String fileName = s3PathResolver.getFilename();
        String collectionsFolder = S3_COLLECTIONS;
        String dataCollectionFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_COLLECTION_PATTERN);
        String dataProviderFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_PROVIDER_PATTERN);

        switch (path) {
            case S3_VALIDATION_DC_PATH:
            case S3_VALIDATION_DC_QUERY_PATH:
                path = String.format(path, dataflowFolder,
                    collectionsFolder, dataCollectionFolder, s3PathResolver.getValidationId(), dataProviderFolder, fileName);
                break;
            case S3_TABLE_NAME_DC_PATH:
            case S3_TABLE_NAME_DC_QUERY_PATH:
            case S3_TABLE_NAME_VALIDATE_DC_PATH:
            case S3_TABLE_NAME_VALIDATE_DC_QUERY_PATH:
                path = String.format(path, dataflowFolder,
                    collectionsFolder, dataCollectionFolder, s3PathResolver.getTableName(), dataProviderFolder, fileName);
                break;
            case S3_EXPORT_PATH:
            case S3_EXPORT_QUERY_PATH:
                path = String.format(path, dataflowFolder,
                    collectionsFolder, dataCollectionFolder, fileName);
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
