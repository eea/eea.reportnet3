package org.eea.dataset.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.multitenancy.DatasetId;
import org.springframework.data.domain.Pageable;

/**
 * The interface Dataset service.
 */
public interface DatasetService {

  /**
   * Delete the dataSchema.
   *
   * @param datasetId the dataset id
   */
  void deleteDataSchema(@DatasetId String datasetId);

  /**
   * Delete import data.
   *
   * @param datasetId the data set id
   * @param deletePrefilledTables the delete prefilled tables
   */
  void deleteImportData(@DatasetId Long datasetId, Boolean deletePrefilledTables);

  /**
   * Gets the table values by id.
   *
   * @param datasetId the dataset id
   * @param mongoID the mongo ID
   * @param pageable the pageable
   * @param fields the fields
   * @param levelError the level error
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   *
   * @return the table values by id
   *
   * @throws EEAException the EEA exception
   */
  TableVO getTableValuesById(@DatasetId Long datasetId, String mongoID, Pageable pageable,
      String fields, ErrorTypeEnum[] levelError, String[] idRules, String fieldSchema,
      String fieldValue) throws EEAException;

  /**
   * Gets the position from any object id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @param type the type
   *
   * @return the position from any object id
   *
   * @throws EEAException the EEA exception
   */
  ValidationLinkVO getPositionFromAnyObjectId(String id, @DatasetId Long idDataset,
      EntityTypeEnum type) throws EEAException;

  /**
   * Gets the dataset by id.
   *
   * @param datasetId the dataset id
   *
   * @return the by id
   *
   * @throws EEAException the EEA exception
   * @deprecated this deprecated
   */
  @Deprecated
  DataSetVO getById(@DatasetId Long datasetId) throws EEAException;

  /**
   * Update dataset.
   *
   * @param datasetId the dataset id
   * @param dataset the dataset
   *
   * @throws EEAException the EEA exception
   */
  void updateDataset(@DatasetId Long datasetId, DataSetVO dataset) throws EEAException;

  /**
   * Gets the data flow id by id.
   *
   * @param datasetId the dataset id
   *
   * @return the data flow id by id
   */
  Long getDataFlowIdById(@DatasetId Long datasetId);

  /**
   * Update record.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param updateCascadePK the update cascade PK
   *
   * @throws EEAException the EEA exception
   */
  void updateRecords(@DatasetId Long datasetId, List<RecordVO> records, boolean updateCascadePK)
      throws EEAException;

  /**
   * Delete record.
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   * @param deleteCascade the delete cascade
   *
   * @throws EEAException the EEA exception
   */
  void deleteRecord(@DatasetId Long datasetId, String recordId, boolean deleteCascade)
      throws EEAException;

  /**
   * Delete table by schema.
   *
   * @param tableSchemaId the id table schema
   * @param datasetId the dataset id
   */
  void deleteTableBySchema(String tableSchemaId, @DatasetId Long datasetId);

  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   * @param tableSchemaId the table schema id
   *
   * @return the byte[]
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] exportFile(@DatasetId Long datasetId, String mimeType, String tableSchemaId)
      throws EEAException, IOException;

  /**
   * Creates the records.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param tableSchemaId the id table schema
   *
   * @throws EEAException the EEA exception
   */
  void insertRecords(@DatasetId Long datasetId, List<RecordVO> records, String tableSchemaId)
      throws EEAException;

  /**
   * Insert schema.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   *
   * @throws EEAException the EEA exception
   */
  void insertSchema(@DatasetId Long datasetId, String idDatasetSchema) throws EEAException;

  /**
   * Update field.
   *
   * @param datasetId the dataset id
   * @param field the field
   * @param updateCascadePK the update cascade PK
   *
   * @throws EEAException the EEA exception
   */
  void updateField(@DatasetId Long datasetId, FieldVO field, boolean updateCascadePK)
      throws EEAException;

  /**
   * Save all records.
   *
   * @param datasetId the dataset id
   * @param listaGeneral the lista general
   */
  void saveAllRecords(@DatasetId Long datasetId, List<RecordValue> listaGeneral);

  /**
   * Save table.
   *
   * @param datasetId the dataset id
   * @param tableValue the table value
   */
  void saveTable(@DatasetId Long datasetId, TableValue tableValue);

  /**
   * Find table id by table schema.
   *
   * @param datasetId the dataset id
   * @param idSchema the id schema
   *
   * @return the long
   */
  Long findTableIdByTableSchema(@DatasetId Long datasetId, String idSchema);

  /**
   * Delete record values to restore snapshot.
   *
   * @param datasetId the dataset id
   * @param partitionId the partition id
   *
   * @throws EEAException the EEA exception
   */
  void deleteRecordValuesToRestoreSnapshot(@DatasetId Long datasetId, Long partitionId)
      throws EEAException;

  /**
   * Save statistics.
   *
   * @param datasetId the dataset id
   *
   * @throws EEAException the EEA exception
   */
  void saveStatistics(@DatasetId Long datasetId) throws EEAException;

  /**
   * Delete table value.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   */
  void deleteTableValue(@DatasetId Long datasetId, String idTableSchema);

  /**
   * Save table propagation.
   *
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   * @throws EEAException the EEA exception
   */
  void saveTablePropagation(@DatasetId Long datasetId, TableSchemaVO tableSchema)
      throws EEAException;

  /**
   * Delete field values.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   */
  void deleteFieldValues(@DatasetId Long datasetId, String fieldSchemaId);

  /**
   * Update field value type.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   * @param type the type
   */
  void updateFieldValueType(@DatasetId Long datasetId, String fieldSchemaId, DataType type);

  /**
   * Delete table values.
   *
   * @param datasetId the dataset id
   */
  void deleteAllTableValues(@DatasetId Long datasetId);

  /**
   * Prepare new field propagation.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaVO the field schema VO
   *
   * @throws EEAException the EEA exception
   */
  void prepareNewFieldPropagation(@DatasetId Long datasetId, FieldSchemaVO fieldSchemaVO)
      throws EEAException;

  /**
   * Save new field propagation.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param idFieldSchema the id field schema
   * @param typeField the type field
   */
  void saveNewFieldPropagation(@DatasetId Long datasetId, String idTableSchema, Pageable pageable,
      String idFieldSchema, DataType typeField);

  /**
   * Delete record values.
   *
   * @param datasetId the dataset id
   * @param providerCode the provider code
   */
  void deleteRecordValuesByProvider(@DatasetId Long datasetId, String providerCode);

  /**
   * Gets the field values referenced.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param conditionalValue the conditional value
   * @param searchValue the search value
   * @param resultsNumber the results number
   * @return the field values referenced
   * @throws EEAException the EEA exception
   */
  List<FieldVO> getFieldValuesReferenced(Long datasetId, String datasetSchemaId,
      String fieldSchemaId, String conditionalValue, String searchValue, Integer resultsNumber)
      throws EEAException;

  /**
   * Gets the referenced dataset id.
   *
   * @param datasetId the dataset id
   * @param idPk the id pk
   *
   * @return the referenced dataset id
   */
  Long getReferencedDatasetId(Long datasetId, String idPk);


  /**
   * Etl export dataset.
   *
   * @param datasetId the dataset id
   * @param outputStream the output stream
   * @param tableSchemaId the table schema id
   * @param limit the limit
   * @param offset the offset
   * @param filterValue the filter value
   * @param columnName the column name
   */
  void etlExportDataset(@DatasetId Long datasetId, OutputStream outputStream, String tableSchemaId,
      Integer limit, Integer offset, String filterValue, String columnName);

  /**
   * Gets the table read only.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param type the type
   *
   * @return the table read only
   */
  Boolean getTableReadOnly(Long datasetId, String tableSchemaId, EntityTypeEnum type);

  /**
   * Checks if is dataset reportable. Dataset is reportable when is designDataset in dataflow with
   * status design or reportingDataset in state Draft.
   *
   * @param idDataset the id dataset
   *
   * @return the boolean
   */
  boolean isDatasetReportable(Long idDataset);



  /**
   * Check if dataset locked or read only.
   *
   * @param datasetId the dataset id
   * @param idRecordSchema the id record schema
   * @param entityType the entity type
   * @return true, if successful
   */
  boolean checkIfDatasetLockedOrReadOnly(Long datasetId, String idRecordSchema,
      EntityTypeEnum entityType);

  /**
   * Gets the mimetype.
   *
   * @param file the file
   *
   * @return the mimetype
   *
   * @throws EEAException the EEA exception
   */
  String getMimetype(String file) throws EEAException;


  /**
   * Copy data.
   *
   * @param dictionaryOriginTargetDatasetsId the dictionary origin target datasets id
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   */
  void copyData(Map<Long, Long> dictionaryOriginTargetDatasetsId,
      Map<String, String> dictionaryOriginTargetObjectId);

  /**
   * Gets the attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   *
   * @return the attachment
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  AttachmentValue getAttachment(@DatasetId Long datasetId, String idField)
      throws EEAException, IOException;

  /**
   * Delete attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   *
   * @throws EEAException the EEA exception
   */
  void deleteAttachment(@DatasetId Long datasetId, String idField) throws EEAException;

  /**
   * Update attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   * @param fileName the file name
   * @param is the is
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void updateAttachment(@DatasetId Long datasetId, String idField, String fileName, InputStream is)
      throws EEAException, IOException;


  /**
   * Gets the field by id.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   *
   * @return the field by id
   *
   * @throws EEAException the EEA exception
   */
  FieldVO getFieldById(@DatasetId Long datasetId, String idField) throws EEAException;


  /**
   * Delete attachment by field schema id.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   *
   * @throws EEAException the EEA exception
   */
  void deleteAttachmentByFieldSchemaId(@DatasetId Long datasetId, String fieldSchemaId)
      throws EEAException;


  /**
   * Export file through integration.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   *
   * @throws EEAException the EEA exception
   */
  void exportFileThroughIntegration(Long datasetId, Long integrationId) throws EEAException;

  /**
   * Gets the table fixed number of records.
   *
   * @param datasetId the dataset id
   * @param objectId the object id
   * @param type the type
   *
   * @return the table fixed number of records
   */
  Boolean getTableFixedNumberOfRecords(Long datasetId, String objectId, EntityTypeEnum type);

  /**
   * Find record schema id by id.
   *
   * @param datasetId the dataset id
   * @param idRecord the id record
   *
   * @return the string
   */
  String findRecordSchemaIdById(@DatasetId Long datasetId, String idRecord);

  /**
   * Find field schema id by id.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   *
   * @return the string
   */
  String findFieldSchemaIdById(@DatasetId Long datasetId, String idField);

  /**
   * Gets the dataset type, if it's a design, reporting, datacollection or eudataset .
   *
   * @param datasetId the dataset id
   *
   * @return the dataset type
   */
  DatasetTypeEnum getDatasetType(Long datasetId);

  /**
   * Gets the schema if reportable.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   *
   * @return the schema if reportable
   */
  DataSetSchema getSchemaIfReportable(Long datasetId, String tableSchemaId);


  /**
   * Creates the lock with signature.
   *
   * @param lockSignature the lock signature
   * @param mapCriteria the map criteria
   * @param userName the user name
   * @throws EEAException the EEA exception
   */
  void createLockWithSignature(LockSignature lockSignature, Map<String, Object> mapCriteria,
      String userName) throws EEAException;


  /**
   * Save public file.
   *
   * @param dataflowId the dataflow id
   * @param dataSetDataProvider the data set data provider
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void savePublicFiles(Long dataflowId, Long dataSetDataProvider) throws IOException;


  /**
   * Export public file.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param fileName the fileName
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  File exportPublicFile(Long dataflowId, Long dataProviderId, String fileName)
      throws IOException, EEAException;


  /**
   * Check any schema available in public.
   *
   * @param dataflowId the dataflow id
   * @return true, if successful
   */
  boolean checkAnySchemaAvailableInPublic(Long dataflowId);


  /**
   * Initialize dataset.
   *
   * @param datasetId the id dataset
   * @param idDatasetSchema the id dataset schema
   */
  void initializeDataset(Long datasetId, String idDatasetSchema);



  /**
   * Download exported file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  File downloadExportedFile(Long datasetId, String fileName) throws IOException, EEAException;

  /**
   * Update records with conditions.
   *
   * @param recordList the record list
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   */
  void updateRecordsWithConditions(List<RecordValue> recordList, Long datasetId,
      TableSchema tableSchema);

  /**
   * Store records.
   *
   * @param datasetId the dataset id
   * @param recordList the record list
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  void storeRecords(Long datasetId, List<RecordValue> recordList) throws IOException, SQLException;

  /**
   * Gets the total failed validations by id dataset.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @return the total failed validations by id dataset
   */
  FailedValidationsDatasetVO getTotalFailedValidationsByIdDataset(Long datasetId,
      String idTableSchema);

}
