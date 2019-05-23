package org.eea.dataset.service;

import java.io.IOException;
import java.util.List;
import org.eea.dataset.multitenancy.DatasetId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.springframework.web.multipart.MultipartFile;

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
   * @param file file to process
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void processFile(@DatasetId Long datasetId, MultipartFile file) throws EEAException, IOException;


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
   * Delete the datas imports.
   *
   *
   * @param datasetName the dataset name
   */
  void deleteImportData(Long idImported);
}
