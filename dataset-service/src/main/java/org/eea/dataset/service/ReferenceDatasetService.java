package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataset.ReferenceDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;



/**
 * The Interface ReferenceDatasetService.
 */
public interface ReferenceDatasetService {


  /**
   * Gets the reference dataset by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the reference dataset by dataflow id
   */
  List<ReferenceDatasetVO> getReferenceDatasetByDataflowId(Long dataflowId);


  /**
   * Gets the reference dataset public by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the reference dataset public by dataflow
   */
  List<ReferenceDatasetPublicVO> getReferenceDatasetPublicByDataflow(Long dataflowId);

}
