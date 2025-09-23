package org.eea.dataset.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.mapper.HelperMultipartFileMapper;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.*;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.orchestrator.JobPresignedUrlInfo;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.multitenancy.DatasetId;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface BigDataDatasetService {

    /**
     * Import big data.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaId the table schema id
     * @param helperMultipartFileMapper the file
     * @param replace the replace
     * @param integrationId the integration id
     * @param delimiter the delimiter
     * @param jobId the jobId
     * @param fmeJobId the fmeJobId
     * @param dataflowVO the dataflowVO
     * @param helperMultipartFileMapper the helperMultipartFileMapper
     * @param job the job
     * @param importFileInDremioInfo the importFileInDremioInfo
     * @return
     */
    void importBigData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId,
                       Boolean replace, Long integrationId, String delimiter, Long jobId, String fmeJobId, DataFlowVO dataflowVO, HelperMultipartFileMapper helperMultipartFileMapper, JobVO job, ImportFileInDremioInfo importFileInDremioInfo) throws Exception;

    /**
     * Generate s3 presigned Url for import
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     */
    JobPresignedUrlInfo generateImportPreSignedUrl(Long datasetId, Long dataflowId, Long providerId, String fileName);

    String generateExportPreSignedUrl(Long datasetId, Long dataflowId, Long providerId, String fileName);

    /**
     * Delete table data
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaId the table schema id
     * @param jobId the job id
     */
    void deleteTableData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId, Long jobId) throws Exception;

    /**
     * Delete dataset data
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param deletePrefilledTables the deletePrefilledTables
     * @param technicallyAccepted the technicallyAccepted
     * @param jobId the job id
     */
    void deleteDatasetData(Long datasetId, Long dataflowId, Long providerId, Boolean deletePrefilledTables, Boolean technicallyAccepted, Long jobId) throws Exception;

    /**
     * Gets the attachment for big data dataflows.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataset id
     * @param providerId the dataset id
     * @param tableSchemaName the table name
     * @param fieldName the field name
     * @param fileName the file name
     * @param recordId the recordId
     * @param dataProviderCode the dataProviderCode
     * @return the attachment
     *
     */
    AttachmentDLVO getAttachmentDL(@DatasetId Long datasetId, Long dataflowId, Long providerId, String tableSchemaName,
                                   String fieldName, String fileName, String recordId, String dataProviderCode);

    /**
     * Delete attachment for big data dataflows.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataset id
     * @param providerId the dataset id
     * @param tableSchemaName the table name
     * @param fieldName the field name
     * @param fileName the file name
     * @param recordId the recordId
     *
     * @throws EEAException the EEA exception
     */
    void deleteAttachmentDL(@DatasetId Long datasetId, Long dataflowId, Long providerId, String tableSchemaName,
                            String fieldName, String fileName, String recordId);

    /**
     * Update attachment for big data dataflows.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataset id
     * @param providerId the dataset id
     * @param tableSchemaName the table name
     * @param fieldName the field name
     * @param multipartFile the file
     * @param recordId the recordId
     * @param previousFileName the previousFileName
     */
    void updateAttachmentDL(@DatasetId Long datasetId, Long dataflowId, Long providerId, String tableSchemaName,
                            String fieldName, MultipartFile multipartFile, String recordId, String previousFileName);

    /**
     * Convert Parquet To Iceberg Table
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaVO the tableSchemaVO
     * @param datasetSchemaId the datasetSchemaId
     * @param lockValue the lock value
     * @return true if table can be converted
     */
    Boolean convertParquetToIcebergTable(Long datasetId, Long dataflowId, Long providerId, TableSchemaVO tableSchemaVO, String datasetSchemaId, String lockValue) throws Exception;

    /**
     * Convert Iceberg To Parquet Table
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaVO the tableSchemaVO
     * @param datasetSchemaId the datasetSchemaId
     * @param lockValue the lock value
     * @return true if table can be converted
     */
    Boolean convertIcebergToParquetTable(Long datasetId, Long dataflowId, Long providerId, TableSchemaVO tableSchemaVO, String datasetSchemaId, String lockValue) throws Exception;

    /**
     * Convert Iceberg To Parquet Table
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaIds the tableSchema Ids
     * @param lockValue the lock value
     */
    void initiateParquetToIcebergConversion(Long datasetId, Long dataflowId, Long providerId, List<String> tableSchemaIds, String lockValue) throws Exception;

    /**
     * Convert Iceberg To Parquet Table
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaIds the tableSchema Ids
     * @param lockValue the lock value
     */
    void initiateIcebergToParquetConversion(Long datasetId, Long dataflowId, Long providerId, List<String> tableSchemaIds, String lockValue) throws Exception;

    /**
     * Insert records manually
     *
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param datasetId the dataset id
     * @param tableSchemaName the tableSchemaName
     * @param records the new editted records
     *
     */
    void insertRecords(Long dataflowId, Long providerId, Long datasetId, String tableSchemaName, List<RecordVO> records) throws Exception;

    /**
     * Update records manually
     *
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param datasetId the dataset id
     * @param records the new editted records
     * @param updateCascadePK the updateCascadePK
     *
     */
    void updateRecords(Long dataflowId, Long providerId, Long datasetId, TableSchemaVO tableSchemaVOe, List<RecordVO> records, boolean updateCascadePK) throws Exception;

    /**
     * Update field manually
     *
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param datasetId the dataset id
     * @param recordId the recordId
     * @param tableSchemaVO the tableSchemaVO
     * @param field the new field
     * @param updateCascadePK the updateCascadePK
     *
     */
    void updateField(Long dataflowId, Long providerId, Long datasetId, FieldVO field, String recordId, TableSchemaVO tableSchemaVO, boolean updateCascadePK) throws Exception;

    /**
     * Delete record manually
     *
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param datasetId the dataset id
     * @param tableSchemaVO the tableSchemaVO
     * @param recordIds the record ids to be removed
     * @param deleteCascadePK the deleteCascadePK
     *
     */
    void deleteRecord(Long dataflowId, Long providerId, Long datasetId, TableSchemaVO tableSchemaVO, List<String> recordIds, boolean deleteCascadePK) throws Exception;

    void removeRootDataflowFolderFromS3(Long dataflowId);

    void createReferenceFolder(S3PathResolver s3TablePathResolver) throws Exception;

    void createPrefilledTables(Long designDatasetId, String designDatasetSchemaId, Long datasetIdForCreation, Long providerId, String tableSchemaId) throws Exception;

    List<FieldVO> getFieldValuesReferencedDL(Long datasetIdOrigin, String datasetSchemaId,
                                     String fieldSchemaId, String conditionalValue, String searchValue, Integer resultsNumber) throws EEAException;

    List<TableSchemaIdNameVO> getAvailableForManualEditingTables(Long datasetId) throws EEAException;

    /**
     * Inserts records in multiple tables
     *
     * @param dataSetMetabaseVO the dataset
     * @param tableRecords the table records
     *
     */
    void insertRecordsInMultipleTables(DataSetMetabaseVO dataSetMetabaseVO, List<TableVO> tableRecords) throws Exception;

    /**
     * Get released dataset data info DL
     *
     * @param collectionDataset the collection dataset
     * @param reportingDataset the reporting dataset
     * @param dataflowId the dataflow id
     * @param dataProviderVO the data provider object
     * @param tableSchemaId the table schema id
     * @param datasetType the dataset type
     * @return a ReleasedDatasetDataInfoVO object
     *
     */
    ReleasedDatasetDataInfoVO getReleasedDatasetDataInfoDL(DataSetMetabaseVO collectionDataset, DataSetMetabaseVO reportingDataset, Long dataflowId,
                                                           DataProviderVO dataProviderVO, String tableSchemaId, DatasetTypeEnum datasetType) throws Exception;

    /***
     * ETL export for csv
     *
     * @param datasetId The dataset id
     * @param dataflowId The dataflow id
     * @param tableSchemaId The table schema id
     * @param jobId The job id
     * @param user The user id
     * @param processUUID The process UUID
     * @param includeAttachments include attachments boolean
     * @throws EEAException The exception
     */
    void etlExportCsv(Long datasetId, Long dataflowId ,String tableSchemaId, Long jobId, String user, String processUUID, Boolean includeAttachments) throws EEAException;

    /**
     * ETL export for parquet
     *
     * @param datasetId The dataset id
     * @param dataflowId The dataflow id
     * @param tableSchemaId The table schema id
     * @param jobId The job id
     * @param user The user id
     * @param processUUID The process UUID
     * @param includeAttachments include attachments boolean
     * @throws EEAException The exception
     */
    void etlExportParquet(Long datasetId, Long dataflowId, String tableSchemaId, Long jobId, String user, String processUUID, Boolean includeAttachments) throws EEAException;

    /**
     * If an import job is added in the db retrieve it, else create a new one
     *
     * @param importFileInDremioInfo The importFileInDremioInfo object
     * @param fmeJobId The fme job id
     * @param jobId The job id
     * @return a job object
     * @throws Exception The exception
     */
    JobVO retrieveOrAddImportJob(ImportFileInDremioInfo importFileInDremioInfo, String fmeJobId, Long jobId) throws Exception;
}
