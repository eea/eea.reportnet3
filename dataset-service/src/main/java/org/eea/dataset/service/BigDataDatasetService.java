package org.eea.dataset.service;

import org.eea.exception.EEAException;
import org.springframework.web.multipart.MultipartFile;

public interface BigDataDatasetService {

    /**
     * Import big data.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaId the table schema id
     * @param file the file
     * @param replace the replace
     * @param integrationId the integration id
     * @param delimiter the delimiter
     * @param jobId the jobId
     * @param fmeJobId the fmeJobId
     * @param filePathInS3 the filePathInS3
     * @return
     */
    void importBigData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId,
                       MultipartFile file, Boolean replace, Long integrationId, String delimiter, Long jobId, String fmeJobId, String filePathInS3) throws Exception;

    /**
     * Generate s3 presigned Url for import
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     */
    String generateImportPresignedUrl(Long datasetId, Long dataflowId, Long providerId);

    /**
     * Delete table data
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaId the table schema id
     * @param tableSchemaName the table schema id
     */
    void deleteTableData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId, String tableSchemaName) throws Exception;

    /**
     * Delete dataset data
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param deletePrefilledTables the deletePrefilledTables
     */
    void deleteDatasetData(Long datasetId, Long dataflowId, Long providerId, Boolean deletePrefilledTables) throws Exception;

    /**
     * Convert parquet to iceberg table
     *
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param datasetId the dataset id
     * @param tableSchemaName the table schema id
     */
    void convertParquetToIcebergTable(Long dataflowId, Long providerId, Long datasetId, String tableSchemaName) throws Exception;

}
