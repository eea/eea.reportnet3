package org.eea.dataset.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.eea.multitenancy.DatasetId;
import org.springframework.data.domain.Pageable;

/**
 * The interface Dataset service.
 */
public interface DatasetService {

  /**
   * Create removeDatasetData dataset.
   *
   * @param datasetName the dataset name
   */
  void createEmptyDataset(String datasetName);

  /**
   * Process the file: read, parse and save in the db.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the is
   * @param idTableSchema the id table schema
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void processFile(Long datasetId, String fileName, InputStream is, String idTableSchema)
      throws EEAException, IOException;


  /**
   * Delete the dataSchema.
   *
   * @param datasetId the dataset id
   */
  void deleteDataSchema(String datasetId);


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
   * @param idFieldSchema the id field schema
   * @param asc the asc
   *
   * @return the table values by id
   *
   * @throws EEAException the EEA exception
   */
  TableVO getTableValuesById(@DatasetId Long datasetId, String mongoID, Pageable pageable,
      String idFieldSchema, Boolean asc) throws EEAException;


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
   * @return the position from any object id
   * @throws EEAException the EEA exception
   */
  ValidationLinkVO getPositionFromAnyObjectId(Long id, Long idDataset, TypeEntityEnum type)
      throws EEAException;


  /**
   * Gets the dataset by id.
   *
   * @param datasetId the dataset id
   *
   * @return the by id
   *
   * @throws EEAException the EEA exception
   */
  @Deprecated
  DataSetVO getById(@DatasetId Long datasetId) throws EEAException;

  /**
   * Update dataset.
   *
   * @param datasetId the dataset id
   * @param dataset the dataset
   *
   * @return the data set VO
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
   * Gets the statistics.
   *
   * @param datasetId the dataset id
   *
   * @return the statistics
   *
   * @throws EEAException the EEA exception
   */
  StatisticsVO getStatistics(@DatasetId Long datasetId) throws EEAException;

  /**
   * Gets the field errors.
   *
   * @param datasetId the dataset id
   * @param mapNameTableSchema the map name table schema
   * @return the field errors
   */
  List<ErrorsValidationVO> getFieldErrors(@DatasetId Long datasetId,
      Map<String, String> mapNameTableSchema);

  /**
   * Gets the record errors.
   *
   * @param datasetId the dataset id
   * @param mapNameTableSchema the map name table schema
   * @return the record errors
   */
  List<ErrorsValidationVO> getRecordErrors(@DatasetId Long datasetId,
      Map<String, String> mapNameTableSchema);

  /**
   * Gets the table errors.
   *
   * @param datasetId the dataset id
   * @param mapNameTableSchema the map name table schema
   * @return the table errors
   */
  List<ErrorsValidationVO> getTableErrors(@DatasetId Long datasetId,
      Map<String, String> mapNameTableSchema);

  /**
   * Gets the dataset errors.
   *
   * @param dataset the dataset
   * @param mapNameTableSchema the map name table schema
   * @return the dataset errors
   */
  List<ErrorsValidationVO> getDatasetErrors(DatasetValue dataset,
      Map<String, String> mapNameTableSchema);

  /**
   * Gets the datase valuetby id.
   *
   * @param datasetId the dataset id
   * @return the datase valuetby id
   * @throws EEAException the EEA exception
   */
  DatasetValue getDatasetValuebyId(@DatasetId Long datasetId) throws EEAException;

  /**
   * Gets the find by id data set schema.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @return the find by id data set schema
   * @throws EEAException the EEA exception
   */
  DataSetSchema getfindByIdDataSetSchema(@DatasetId Long datasetId, ObjectId datasetSchemaId)
      throws EEAException;
}
