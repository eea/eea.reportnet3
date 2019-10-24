package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;

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
  Long createEmptyDataset(TypeDatasetEnum datasetType, String datasetName, String datasetSchemaId,
      Long dataflowId) throws EEAException;

  /**
   * Gets the dataset name.
   *
   * @param idDataset the id dataset
   * @return the dataset name
   */
  DataSetMetabaseVO findDatasetMetabase(Long idDataset);


  /**
   * Delete design dataset.
   *
   * @param datasetId the dataset id
   */
  void deleteDesignDataset(Long datasetId);

  /**
   * Update dataset name.
   *
   * @param datasetId the dataset id
   * @param datasetName the dataset name
   * @return true, if successful
   */
  boolean updateDatasetName(Long datasetId, String datasetName);
}
