package org.eea.dataset.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
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
   * @return the table values by id
   * @throws EEAException the EEA exception
   */
  TableVO getTableValuesById(@DatasetId Long datasetId, String mongoID, Pageable pageable,
      String fields, TypeErrorEnum[] levelError) throws EEAException;

  /**
   * Sets the dataschema tables.
   *
   * @param datasetId the dataset id
   * @param dataFlowId the data flow id
   * @param tableCollections the table collections
   *
   * @throws EEAException the EEA exception
   */
  void setDataschemaTables(@DatasetId Long datasetId, Long dataFlowId,
      TableCollectionVO tableCollections) throws EEAException;


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
  ValidationLinkVO getPositionFromAnyObjectId(Long id, @DatasetId Long idDataset,
      TypeEntityEnum type) throws EEAException;


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
   *
   * @throws EEAException the EEA exception
   */
  Long getDataFlowIdById(@DatasetId Long datasetId) throws EEAException;


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
  void deleteRecord(@DatasetId Long datasetId, Long recordId) throws EEAException;

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
   * @return the long
   */
  Long findTableIdByTableSchema(@DatasetId Long datasetId, String idSchema);


  /**
   * Delete record values to restore snapshot.
   *
   * @param datasetId the dataset id
   * @param partitionId the partition id
   * @throws EEAException the EEA exception
   */
  void deleteRecordValuesToRestoreSnapshot(@DatasetId Long datasetId, Long partitionId)
      throws EEAException;

  /**
   * Save statistics.
   *
   * @param datasetId the dataset id
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
  void updateFieldValueType(@DatasetId Long datasetId, String fieldSchemaId, String type);

  /**
   * Delete table values.
   *
   * @param datasetId the dataset id
   */
  void deleteAllTableValues(@DatasetId Long datasetId);
}
