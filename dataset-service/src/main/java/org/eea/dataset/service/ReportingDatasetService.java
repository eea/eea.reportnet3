package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;

/**
 * The Interface DatasetMetabaseService.
 */
public interface ReportingDatasetService {

  /**
   * Gets the data set id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data set id by dataflow id
   */
  List<ReportingDatasetVO> getDataSetIdByDataflowId(Long idFlow);

}
