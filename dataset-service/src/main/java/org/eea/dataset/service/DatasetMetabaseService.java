package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;

/**
 * The Interface DatasetMetabaseService.
 */
public interface DatasetMetabaseService {

  /**
   * Gets the data set id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data set id by dataflow id
   */
  List<DataSetMetabaseVO> getDataSetIdByDataflowId(Long idFlow);


  /**
   * Creates the empty dataset.
   *
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   * @param idDataflow the id dataflow
   * @throws EEAException the EEA exception
   */
  void createEmptyDataset(String datasetName, String idDatasetSchema, Long idDataflow)
      throws EEAException;

  /**
   * Gets the dataset name.
   *
   * @param idDataset the id dataset
   * @return the dataset name
   */
  DataSetMetabaseVO findDatasetMetabase(Long idDataset);

}
