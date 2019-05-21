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
   * Process the file: read, parse and save in the db
   * 
   * @param datasetId
   * @param file file to process
   * 
   * @return
   * @throws IOException
   * @throws Exception
   */
  void processFile(@DatasetId Long datasetId, MultipartFile file) throws EEAException, IOException;


  /**
   * Creates the data schema.
   *
   * @param datasetName the dataset name
   */
  void createDataSchema(String datasetName);


}
