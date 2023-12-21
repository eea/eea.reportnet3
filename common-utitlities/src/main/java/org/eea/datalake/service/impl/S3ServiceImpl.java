package org.eea.datalake.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum.REFERENCE;
import static org.eea.utils.LiteralConstants.*;


@Service
public class S3ServiceImpl implements S3Service {

    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;

    /** The Constant S3_DEFAULT_BUCKET: {@value}. */
    @Value("${s3.default.bucket}")
    private String S3_DEFAULT_BUCKET;

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

        String format = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, tableName,
                fileName);
        String formatWithParquetFolder = String.format(path, dataflowFolder, dataCollectionFolder, tableName,
                dataProviderFolder, parquetFolder, fileName);
        if (S3_IMPORT_QUERY_PATH.equals(path)
            || S3_TABLE_AS_FOLDER_QUERY_PATH.equals(path)
            || S3_TABLE_NAME_QUERY_PATH.equals(path) || S3_IMPORT_CSV_FILE_QUERY_PATH.equals(path)
            || S3_TABLE_NAME_VALIDATE_QUERY_PATH.equals(
            path)) {
            path = S3_DEFAULT_BUCKET + format;
        } else if (S3_IMPORT_FILE_PATH.equals(path) || S3_TABLE_NAME_PATH.equals(path) || S3_VALIDATION_RULE_PATH.equals(path) ||
                S3_TABLE_NAME_VALIDATE_PATH.equals(path) ) {
            path = format;
        } else if (S3_VALIDATION_QUERY_PATH.equals(path)) {
            path = S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, s3PathResolver.getValidationId(), fileName);
        } else if (S3_VALIDATION_PATH.equals(path)) {
            String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, s3PathResolver.getValidationId(), fileName);
        } else if (S3_TABLE_NAME_FOLDER_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, tableName);
        } else if (S3_PROVIDER_IMPORT_PATH.equals(path) || S3_CURRENT_PATH.equals(path) || S3_SNAPSHOT_FOLDER_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder);
        } else if (S3_VALIDATION_DC_QUERY_PATH.equals(path)) {
            path = S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataCollectionFolder, s3PathResolver.getValidationId(), dataProviderFolder, fileName);
        } else if (S3_VALIDATION_DC_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, dataCollectionFolder, s3PathResolver.getValidationId(), dataProviderFolder, fileName);
        } else if (S3_TABLE_NAME_VALIDATE_DC_QUERY_PATH.equals(path)) {
            path = S3_DEFAULT_BUCKET + formatWithParquetFolder;
        } else if (S3_TABLE_NAME_DC_PATH.equals(path) || S3_TABLE_NAME_VALIDATE_DC_PATH.equals(path)) {
            path = formatWithParquetFolder;
        } else if (S3_EXPORT_QUERY_PATH.equals(path)) {
            path = S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataCollectionFolder, fileName);
        } else if (S3_EXPORT_PATH.equals(path)) {
           path = String.format(path, dataflowFolder, dataCollectionFolder, fileName);
        } else if (S3_EXPORT_FOLDER_PATH.equals(path) || S3_TABLE_NAME_ROOT_DC_FOLDER_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, dataCollectionFolder);
        } else if (S3_TABLE_NAME_DC_PROVIDER_FOLDER_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, dataCollectionFolder, tableName, dataProviderFolder);
        } else if (S3_TABLE_NAME_DC_QUERY_PATH.equals(path)) {
            path = S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataCollectionFolder, tableName);
        } else if (S3_TABLE_NAME_DC_FOLDER_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, dataCollectionFolder, tableName);
        } else if (S3_DATAFLOW_REFERENCE_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, tableName, parquetFolder, fileName);
        } else if (S3_DATAFLOW_REFERENCE_FOLDER_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, tableName);
        } else if (S3_PROVIDER_SNAPSHOT_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, snapshotFolder, tableName, parquetFolder, fileName);
        } else if (S3_TABLE_NAME_WITH_PARQUET_FOLDER_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, tableName, parquetFolder, fileName);
        } else if (S3_EU_SNAPSHOT_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, euDatasetFolder, tableName, dataProviderFolder, parquetFolder, fileName);
        } else if (S3_EU_SNAPSHOT_ROOT_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, euDatasetFolder);
        } else if (S3_EU_SNAPSHOT_TABLE_PATH.equals(path)) {
            path = String.format(path, dataflowFolder, euDatasetFolder, tableName);
        } else {
            LOG.info("Wrong type value: {}", path);
            path = null;
        }

        return path;
    }

    private String calculateS3TableAsFolderPath(S3PathResolver s3PathResolver, String path) {
        LOG.info("Method calculateS3Path called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);

        if (path.equals(S3_IMPORT_FILE_PATH) || path.equals(S3_TABLE_NAME_PATH)) {
           return String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getTableName(), s3PathResolver.getFilename());
        } else if(path.equals(S3_IMPORT_CSV_FILE_QUERY_PATH)){
            return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder,
                    dataProviderFolder, datasetFolder, s3PathResolver.getTableName(), s3PathResolver.getFilename());
        } else if (path.equals(S3_REFERENCE_FOLDER_PATH)) {
            return String.format(path, dataflowFolder);
        } else if (path.equals(S3_DATAFLOW_REFERENCE_FOLDER_PATH)) {
            return String.format(path, dataflowFolder, s3PathResolver.getTableName());
        } else if (path.equals(S3_DATAFLOW_REFERENCE_QUERY_PATH)) {
            return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, s3PathResolver.getTableName());
        } else if (path.equals(S3_TABLE_AS_FOLDER_QUERY_PATH)) {
            return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, s3PathResolver.getTableName());
        } else if (path.equals(S3_IMPORT_TABLE_NAME_FOLDER_PATH) || path.equals(S3_VALIDATION_TABLE_PATH) ||
                path.equals(S3_TABLE_NAME_FOLDER_PATH) || path.equals(S3_CURRENT_PATH)) {
            return String.format(path, dataflowFolder, dataProviderFolder, datasetFolder, s3PathResolver.getTableName());
        } else {
            LOG.info("Wrong type value: {}", path);
        }

        return null;
    }

    private String calculateS3TableAsFolderPath(S3PathResolver s3PathResolver) {
        LOG.info("Method calculateS3Path called with s3Path: {}", s3PathResolver);
        String path = s3PathResolver.getPath();
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataProviderFolder = formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);
        String euDatasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_EU_DATASET_PATTERN);

        if (S3_DATAFLOW_REFERENCE_FOLDER_PATH.equals(path)) {
          return String.format(path, dataflowFolder, s3PathResolver.getTableName());
        } else if (S3_DATAFLOW_REFERENCE_QUERY_PATH.equals(path)) {
            return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, s3PathResolver.getTableName());
        } else if (S3_TABLE_NAME_FOLDER_PATH.equals(path)) {
            return String.format(path, dataflowFolder, dataProviderFolder, datasetFolder,
                s3PathResolver.getTableName());
        } else if (S3_TABLE_NAME_EU_QUERY_PATH.equals(path)) {
            return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, euDatasetFolder,
                s3PathResolver.getTableName());
        } else {
            LOG.info("Wrong type value: {}", path);
        }

        return null;
    }

    private String calculateS3TableDCAsFolderPath(S3PathResolver s3PathResolver, String path) {
        LOG.info("Method calculateS3TableDCAsFolderPath called with s3Path: {}", s3PathResolver);
        String dataflowFolder = formatFolderName(s3PathResolver.getDataflowId(), S3_DATAFLOW_PATTERN);
        String dataCollectionFolder =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATA_COLLECTION_PATTERN);
        String dataProviderId =  formatFolderName(s3PathResolver.getDataProviderId(), S3_DATA_PROVIDER_PATTERN);
        String datasetId =  formatFolderName(s3PathResolver.getDatasetId(), S3_DATASET_PATTERN);
        String euDatasetFolder = formatFolderName(s3PathResolver.getDatasetId(), S3_EU_DATASET_PATTERN);

        if (S3_TABLE_NAME_DC_QUERY_PATH.equals(path)) {
            return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataCollectionFolder,
                s3PathResolver.getTableName());
        } else if (S3_DATAFLOW_REFERENCE_QUERY_PATH.equals(path)) {
            return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, s3PathResolver.getTableName());
        } else if (S3_TABLE_AS_FOLDER_QUERY_PATH.equals(path)) {
            return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, dataProviderId, datasetId,
                s3PathResolver.getTableName());
        } else if (S3_TABLE_NAME_EU_QUERY_PATH.equals(path)) {
            return S3_DEFAULT_BUCKET + String.format(path, dataflowFolder, euDatasetFolder,
                s3PathResolver.getTableName());
        } else {
            LOG.info("Wrong type value: {}", path);
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
            tablePath = S3_DEFAULT_BUCKET + String.format(S3_DATAFLOW_REFERENCE_QUERY_PATH, dataflowFolder, tableName);
        } else {
            tablePath = this.getTableAsFolderQueryPath(tableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        }
        return tablePath;
    }

    @Override
    public S3PathResolver getS3PathResolverByDatasetType(DataSetMetabaseVO dataset, String tableName) {
        S3PathResolver s3PathResolver;
        if (REFERENCE.equals(dataset.getDatasetTypeEnum())) {
            s3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getDataProviderId()!=null ? dataset.getDataProviderId() : 0, dataset.getId(), tableName);
            s3PathResolver.setPath(S3_DATAFLOW_REFERENCE_FOLDER_PATH);
        }  else if (dataset.getDatasetTypeEnum().equals(DatasetTypeEnum.COLLECTION)) {
            s3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getId(), tableName, S3_TABLE_NAME_ROOT_DC_FOLDER_PATH);
        } else if (dataset.getDatasetTypeEnum().equals(DatasetTypeEnum.EUDATASET)) {
            s3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getId(), tableName, S3_EU_SNAPSHOT_ROOT_PATH);
        } else {
            s3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getDataProviderId()!=null ? dataset.getDataProviderId() : 0, dataset.getId(), tableName);
            s3PathResolver.setPath(S3_TABLE_NAME_FOLDER_PATH);
        }
        return s3PathResolver;
    }

    private String formatSnapshotFolder(Long snapshotId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = dateFormat.format(new Date());
        return String.format(S3_SNAPSHOT_PATTERN, snapshotId, date);
    }
}
