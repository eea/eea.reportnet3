package org.eea.dataset.service;

import java.util.List;
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

}
