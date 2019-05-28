package org.eea.dataset.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.eea.dataset.multitenancy.DatasetId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.metabese.TableCollectionVO;
import org.springframework.data.domain.Pageable;

// TODO: Auto-generated Javadoc
/**
 * The interface Dataset service.
 */
public interface DatasetService {

  /**
   * Gets dataset by id.
   *
   * @param datasetId the dataset id
   *
   * @return the dataset by id
   */
  DataSetVO getDatasetById(@DatasetId Long datasetId);

  /**
   * Add record to dataset.
   *
   * @param datasetId the dataset id
   * @param record the record
   */
  void addRecordToDataset(@DatasetId Long datasetId, List<RecordVO> record);

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
   */
  void processFile(@DatasetId Long datasetId, String fileName, InputStream is)
      throws EEAException, IOException;


  /**
   * Gets the dataset values by id.
   *
   * @param datasetId the dataset id
   * @return the dataset values by id
   * @throws EEAException the EEA exception
   */
  DataSetVO getDatasetValuesById(@DatasetId Long datasetId) throws EEAException;



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
  void deleteImportData(Long dataSetId);

  /**
   * Gets the table values by id.
   *
   * @param MongoID the mongo ID
   * @param pageable the pageable
   * @return the table values by id
   * @throws EEAException the EEA exception
   */
  TableVO getTableValuesById(String MongoID, Pageable pageable) throws EEAException;


  /**
   * Count table data.
   *
   * @param tableId the table id
   * @return the long
   */
  Long countTableData(Long tableId);



  /**
   * Sets the mongo tables.
   *
   * @param datasetId the dataset id
   * @param dataFlowId the data flow id
   * @param tableName the table name
   * @param Headers the headers
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */

  void setMongoTables(@DatasetId Long datasetId, Long dataFlowId,
      TableCollectionVO tableCollections) throws EEAException, IOException;

}
