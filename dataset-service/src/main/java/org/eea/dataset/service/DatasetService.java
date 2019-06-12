package org.eea.dataset.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.eea.dataset.multitenancy.DatasetId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.springframework.data.domain.Pageable;

/**
 * The interface Dataset service.
 */
public interface DatasetService {

  /**
   * Create empty dataset.
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
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   */
  void processFile(@DatasetId Long datasetId, String fileName, InputStream is)
      throws EEAException, IOException, InterruptedException;


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
   * Gets the table from any object id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @param pageable the pageable
   * @param type the type
   * @return the table from any object id
   * @throws EEAException the EEA exception
   */
  Map<String,TableVO> getTableFromAnyObjectId(Long id, Long idDataset, Pageable pageable, 
      TypeEntityEnum type) throws EEAException;


  /**
   * Gets the dataset by id.
   *
   * @param datasetId the dataset id
   * @return the by id
   * @throws EEAException the EEA exception
   */
  DataSetVO getById(@DatasetId Long datasetId) throws EEAException;

  /**
   * Update dataset.
   *
   * @param dataset the dataset
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  void updateDataset(DataSetVO dataset) throws EEAException;

  /**
   * Gets the data flow id by id.
   *
   * @param datasetId the dataset id
   * @return the data flow id by id
   * @throws EEAException the EEA exception
   */
  Long getDataFlowIdById(Long datasetId) throws EEAException;
  
  
  
  /**
   * Gets the statistics.
   *
   * @param datasetId the dataset id
   * @return the statistics
   * @throws EEAException the EEA exception
   */
  StatisticsVO getStatistics(@DatasetId Long datasetId) throws EEAException;

  FailedValidationsDatasetVO getListValidations(@DatasetId Long datasetId, Pageable pageable) throws EEAException;
}
