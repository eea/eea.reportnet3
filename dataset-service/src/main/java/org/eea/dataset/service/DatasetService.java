package org.eea.dataset.service;

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
  DataSetVO getDatasetById(@DatasetId String datasetId);

  /**
   * Add record to dataset.
   *
   * @param datasetId the dataset id
   * @param record the record
   */
  void addRecordToDataset(@DatasetId String datasetId, List<RecordVO> record);

  /**
   * Create empty dataset.
   *
   * @param datasetName the dataset name
   */
  void createEmptyDataset(String datasetName);

  /**
   * Process the file: read, parse and save in the db.
   *
   * @param datasetId
   * @param file file to process
   * @throws EEAException
   */
  void processFile(@DatasetId String datasetId, MultipartFile file) throws EEAException;


  /**
   * Creates the data schema.
   *
   * @param datasetName the dataset name
   */
  void createDataSchema(String datasetName);


  /**
   * Delete the dataSchema
   *
   * @param datasetName for id
   */
  void deleteDataSchema(String datasetId);
}
