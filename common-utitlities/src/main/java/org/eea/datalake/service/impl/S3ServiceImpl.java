package org.eea.datalake.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static org.eea.utils.LiteralConstants.*;
import static org.eea.utils.LiteralConstants.S3_TABLE_NAME_PATH;


@Service
public class S3ServiceImpl implements S3Service {

    private static final Logger LOG = LoggerFactory.getLogger(S3ServiceImpl.class);

    //Data Provider Paths
    @Override
    public String getValidationProviderPath(S3PathResolver s3PathResolver) {
        String s3ValidationPath = calculateS3ProviderPath(s3PathResolver, S3_VALIDATION);
        LOG.info("Method getValidationS3Path returns S3 Validation Path: {}", s3ValidationPath);
        return s3ValidationPath;
    }

    @Override
    public String getImportProviderPath(S3PathResolver s3PathResolver) {
        String s3ImportPath = calculateS3ProviderPath(s3PathResolver, S3_IMPORT);
        LOG.info("Method getImportS3Path returns S3 Import Path: {}", s3ImportPath);
        return s3ImportPath;
    }

    @Override
    public String getTableNameProviderPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3ProviderPath(s3PathResolver, S3_TABLE_NAME);
        LOG.info("Method getTableNameS3Path returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    @Override
    public String getTableNameValidateProviderPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3ProviderPath(s3PathResolver, S3_TABLE_NAME_VALIDATIOM);
        LOG.info("Method getTableNameValidateS3Path returns S3 Table Name Validate Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    //Data Collection Paths
    @Override
    public String getValidationDCPath(S3PathResolver s3PathResolver) {
        String s3ValidationPath = calculateS3DCPath(s3PathResolver, S3_VALIDATION);
        LOG.info("Method getValidationS3Path returns S3 Validation Path: {}", s3ValidationPath);
        return s3ValidationPath;
    }

    @Override
    public String getTableNameDCPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3DCPath(s3PathResolver, S3_TABLE_NAME);
        LOG.info("Method getTableNameS3Path returns S3 Table Name Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    @Override
    public String getTableNameValidateDCPath(S3PathResolver s3PathResolver) {
        String s3TableNamePath = calculateS3DCPath(s3PathResolver, S3_TABLE_NAME_VALIDATIOM);
        LOG.info("Method getTableNameValidateS3Path returns S3 Table Name Validate Path: {}", s3TableNamePath);
        return s3TableNamePath;
    }

    //Other
    @Override
    public String getDataflowReferencePath(S3PathResolver s3PathResolver) {
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataflowReferencePath = String.format(S3_DATAFLOW_REFERENCE_PATH, dataflowFolder, s3PathResolver.getFilename());
        LOG.info("Method getDataflowReferenceS3Path returns S3 Dataflow Reference Path: {}", dataflowReferencePath);
        return dataflowReferencePath;
    }

    @Override
    public String getExportDCPath(S3PathResolver s3PathResolver) {
        String s3ExportPath = calculateS3DCPath(s3PathResolver, S3_EXPORT);
        LOG.info("Method getExportPath returns S3 Export Path: {}", s3ExportPath);
        return s3ExportPath;
    }

    private String calculateS3ProviderPath(S3PathResolver s3PathResolver, String type) {
        LOG.info("Method calculateS3Path called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);
        String fileName = s3PathResolver.getFilename();
        String path = null;

        switch (type) {
            case S3_IMPORT:
                path = String.format(S3_IMPORT_PATH, dataflowFolder,
                    dataProviderFolder, datasetFolder, fileName);
                break;
            case S3_VALIDATION:
                path = String.format(S3_VALIDATION_PATH, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getValidationId(), fileName);
                break;
            case S3_TABLE_NAME:
                path = String.format(S3_TABLE_NAME_PATH, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getTableName(), fileName);
                break;
            case S3_TABLE_NAME_VALIDATIOM:
                path = String.format(S3_TABLE_NAME_VALIDATE_PATH, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getTableName(), fileName);
                break;
            default:
                LOG.info("Wrong type value: {}", type);
                break;
        }
        return path;
    }

    private String calculateS3DCPath(S3PathResolver s3PathResolver, String type) {
        LOG.info("Method calculateS3Path called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String fileName = s3PathResolver.getFilename();
        String collectionsFolder = S3_COLLECTIONS;
        String dataCollectionFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_COLLECTION_PATTERN);
        String dataProviderFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_PROVIDER_PATTERN);
        String path = null;

        switch (type) {
            case S3_VALIDATION:
                path = String.format(S3_VALIDATION_DC_PATH, dataflowFolder,
                    collectionsFolder, dataCollectionFolder, s3PathResolver.getValidationId(), dataProviderFolder, fileName);
                break;
            case S3_TABLE_NAME:
                path = String.format(S3_TABLE_NAME_DC_PATH, dataflowFolder,
                    collectionsFolder, dataCollectionFolder, s3PathResolver.getTableName(), dataProviderFolder, fileName);
                break;
            case S3_TABLE_NAME_VALIDATIOM:
                path = String.format(S3_TABLE_NAME_VALIDATE_DC_PATH, dataflowFolder,
                    collectionsFolder, dataCollectionFolder, s3PathResolver.getTableName(), dataProviderFolder, fileName);
                break;
            case S3_EXPORT:
                path = String.format(S3_EXPORT_PATH, dataflowFolder,
                    collectionsFolder, dataCollectionFolder, fileName);
                break;
            default:
                LOG.info("Wrong type value: {}", type);
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
