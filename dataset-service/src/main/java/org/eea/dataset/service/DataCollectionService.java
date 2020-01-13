package org.eea.dataset.service;

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



}
