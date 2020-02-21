package org.eea.dataset.service;

import java.util.Date;
import java.util.List;
import org.eea.interfaces.vo.dataset.DataCollectionVO;


/**
 * The Interface DataCollectionService.
 */
public interface DataCollectionService {


  /**
   * Gets the data collection id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data collection id by dataflow id
   */
  List<DataCollectionVO> getDataCollectionIdByDataflowId(Long idFlow);

  /**
   * Checks if is design dataflow.
   *
   * @param dataflowId the dataflow id
   * @return true, if is design dataflow
   */
  boolean isDesignDataflow(Long dataflowId);

  /**
   * Undo data collection creation.
   *
   * @param datasetIds the dataset ids
   * @param dataflowId the dataflow id
   */
  void undoDataCollectionCreation(List<Long> datasetIds, Long dataflowId);

  /**
   * Creates the empty data collection.
   *
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   */
  void createEmptyDataCollection(Long dataflowId, Date dueDate);
}
