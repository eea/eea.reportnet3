package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;


/**
 * The Interface DesignDatasetService.
 */
public interface DesignDatasetService {

  /**
   * Gets the design data set id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the design data set id by dataflow id
   */
  List<DesignDatasetVO> getDesignDataSetIdByDataflowId(Long idFlow);

  /**
   * Copy design datasets.
   *
   * @param idDataflowOrigin the id dataflow origin
   * @param idDataflowDestination the id dataflow destination
   * @throws EEAException the EEA exception
   */
  void copyDesignDatasets(Long idDataflowOrigin, Long idDataflowDestination) throws EEAException;

  /**
   * Gets the design dataset summary list.
   *
   * @param dataflowId the dataflow id
   * @return the design dataset summary list
   */
  List<DatasetsSummaryVO> getDesignDatasetSummaryList(Long dataflowId);
}
