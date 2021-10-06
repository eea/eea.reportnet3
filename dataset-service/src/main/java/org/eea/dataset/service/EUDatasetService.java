package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
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

  /**
   * Populate EU dataset with data collection.
   *
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  void populateEUDatasetWithDataCollection(Long dataflowId) throws EEAException;

  /**
   * Removes the locks related to populate EU.
   *
   * @param dataflowId the dataflow id
   * @return the boolean if successful lock removed
   */
  Boolean removeLocksRelatedToPopulateEU(Long dataflowId);

}
