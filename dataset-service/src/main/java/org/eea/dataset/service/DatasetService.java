package org.eea.dataset.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.ETLDatasetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.multitenancy.DatasetId;
import org.springframework.data.domain.Pageable;

/**
 * The interface Dataset service.
 */
public interface DatasetService {

  /**
   * Process the file: read, parse and save in the db.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the is
   * @param idTableSchema the id table schema
   *
   * @return the data set VO
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  DataSetVO processFile(@DatasetId Long datasetId, String fileName, InputStream is,
      String idTableSchema) throws EEAException, IOException;

  /**
   * Delete the dataSchema.
   *
   * @param datasetId the dataset id
   */
  void deleteDataSchema(@DatasetId String datasetId);

  /**
   * Delete import data.
   *
   * @param dataSetId the data set id
   */
  void deleteImportData(@DatasetId Long dataSetId);

  /**
   * Gets the table values by id.
   *
   * @param datasetId the dataset id
   * @param mongoID the mongo ID
   * @param pageable the pageable
   * @param fields the fields
   * @param levelError the level error
   *
   * @return the table values by id
   *
   * @throws EEAException the EEA exception
   */
  TableVO getTableValuesById(@DatasetId Long datasetId, String mongoID, Pageable pageable,
      String fields, ErrorTypeEnum[] levelError) throws EEAException;

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
   *
   * @throws EEAException the EEA exception
   */
  void updateRecords(@DatasetId Long datasetId, List<RecordVO> records) throws EEAException;

  /**
   * Delete record.
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   *
   * @throws EEAException the EEA exception
   */
  void deleteRecord(@DatasetId Long datasetId, String recordId) throws EEAException;

  /**
   * Delete table by schema.
   *
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   */
  void deleteTableBySchema(String idTableSchema, @DatasetId Long datasetId);

  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   * @param idTableSchema the id table schema
   *
   * @return the byte[]
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] exportFile(@DatasetId Long datasetId, String mimeType, String idTableSchema)
      throws EEAException, IOException;

  /**
   * Gets the file name.
   *
   * @param mimeType the mime type
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   *
   * @return the file name
   *
   * @throws EEAException the EEA exception
   */
  String getFileName(String mimeType, String idTableSchema, @DatasetId Long datasetId)
      throws EEAException;

  /**
   * Creates the records.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param idTableSchema the id table schema
   *
   * @throws EEAException the EEA exception
   */
  void createRecords(@DatasetId Long datasetId, List<RecordVO> records, String idTableSchema)
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
   *
   * @throws EEAException the EEA exception
   */
  void updateField(@DatasetId Long datasetId, FieldVO field) throws EEAException;

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
   *
   * @return the table VO
   *
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
   * Checks if is reporting dataset.
   *
   * @param datasetId the dataset id
   *
   * @return true, if is reporting dataset
   */
  boolean isReportingDataset(Long datasetId);

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
   * @param idPk the id pk
   * @param searchValue the search value
   *
   * @return the field values referenced
   */
  List<FieldVO> getFieldValuesReferenced(Long datasetId, String idPk, String searchValue);

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
   *
   * @return the ETL dataset VO
   *
   * @throws EEAException the EEA exception
   */
  ETLDatasetVO etlExportDataset(@DatasetId Long datasetId) throws EEAException;


  /**
   * Etl import dataset.
   *
   * @param datasetId the dataset id
   * @param etlDatasetVO the etl dataset VO
   * @param providerId the provider id
   *
   * @throws EEAException the EEA exception
   */
  void etlImportDataset(@DatasetId Long datasetId, ETLDatasetVO etlDatasetVO, Long providerId)
      throws EEAException;

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
   * Release lock.
   *
   * @param criteria the criteria
   */
  void releaseLock(Object... criteria);

  /**
   * Checks if is dataset reportable. Dataset is reportable when is designDataset in dataflow with
   * status design or reportingDataset in state Draft.
   * 
   * @param idDataset the id dataset
   * @return the boolean
   */
  Boolean isDatasetReportable(Long idDataset);

  /**
   * Gets the mimetype.
   *
   * @param file the file
   * @return the mimetype
   * @throws EEAException the EEA exception
   */
  String getMimetype(String file) throws EEAException;
}
