package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;

public interface S3Service {

    /**
     * @return S3 path
     */
    String getS3Path(S3PathResolver s3PathResolver);

    /**
     * @param s3PathResolver
     * @return table name query path
     */
    String getTableAsFolderQueryPath(S3PathResolver s3PathResolver, String path);

    /**
     * @param s3PathResolver
     * @return table name query path
     */
    String getTableAsFolderQueryPath(S3PathResolver s3PathResolver);

    String getTableDCAsFolderQueryPath(S3PathResolver s3PathResolver, String path);

    /**
     * Gets table path by datasetType
     * @param dataflowId
     * @param datasetId
     * @param tableName
     * @param tableResolver
     * @return
     */
    String getTablePathByDatasetType(Long dataflowId, Long datasetId, String tableName, S3PathResolver tableResolver);

    /**
     *
     * @param dataset
     * @return
     */
    S3PathResolver getS3PathResolverByDatasetType(DataSetMetabaseVO dataset, String tableName, Boolean isIceberg);

    String getS3DefaultBucket();

    String getS3DefaultBucketPath();

    String getS3IcebergBucket();

    String getS3IcebergBucketPath();

    String getS3DefaultBucketName();

    String getS3IcebergBucketName();

    String formatFolderName(Long id, String pattern);
}
