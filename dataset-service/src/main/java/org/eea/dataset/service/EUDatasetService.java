package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataset.EUDatasetVO;


/**
 * The Interface EUDatasetService.
 */
public interface EUDatasetService {


  /**
   * Gets the EU dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the EU dataset by dataflow id
   */
  List<EUDatasetVO> getEUDatasetByDataflowId(Long idDataflow);


}
