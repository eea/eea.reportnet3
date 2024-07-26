package org.eea.dataset.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.AttachmentDLVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.orchestrator.JobPresignedUrlInfo;
import org.eea.multitenancy.DatasetId;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

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
     * @param dataflowVO the dataflowVO
     * @return
     */
    void importBigData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId,
                       MultipartFile file, Boolean replace, Long integrationId, String delimiter, Long jobId, String fmeJobId, DataFlowVO dataflowVO) throws Exception;

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
     */
    void deleteTableData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId) throws Exception;

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
     * Gets the attachment for big data dataflows.
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataset id
     * @param providerId the dataset id
     * @param tableSchemaName the table name
     * @param fieldName the field name
     * @param fileName the file name
     * @param recordId the recordId
     * @return the attachment
     *
     */
    AttachmentDLVO getAttachmentDL(@DatasetId Long datasetId, Long dataflowId, Long providerId, String tableSchemaName,
                                   String fieldName, String fileName, String recordId);

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
     *
     */
    void convertParquetToIcebergTable(Long datasetId, Long dataflowId, Long providerId, TableSchemaVO tableSchemaVO, String datasetSchemaId) throws Exception;

    /**
     * Convert Iceberg To Parquet Table
     *
     * @param datasetId the dataset id
     * @param dataflowId the dataflow id
     * @param providerId the provider id
     * @param tableSchemaVO the tableSchemaVO
     * @param datasetSchemaId the datasetSchemaId
     *
     */
    void convertIcebergToParquetTable(Long datasetId, Long dataflowId, Long providerId, TableSchemaVO tableSchemaVO, String datasetSchemaId) throws Exception;

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
     * @param tableSchemaName the tableSchemaName
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
     * @param recordId the record id to be removed
     * @param deleteCascadePK the deleteCascadePK
     *
     */
    void deleteRecord(Long dataflowId, Long providerId, Long datasetId, TableSchemaVO tableSchemaVO, String recordId, boolean deleteCascadePK) throws Exception;

    void createReferenceFolder(S3PathResolver s3TablePathResolver) throws Exception;

    void createPrefilledTables(Long designDatasetId, String designDatasetSchemaId, Long datasetIdForCreation, Long providerId, String tableSchemaId) throws Exception;

    List<FieldVO> getFieldValuesReferencedDL(Long datasetIdOrigin, String datasetSchemaId,
                                     String fieldSchemaId, String conditionalValue, String searchValue, Integer resultsNumber) throws EEAException;
}
