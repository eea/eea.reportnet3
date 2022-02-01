package org.eea.dataset.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
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


  /**
   * Gets the dataflows referenced.
   *
   * @param dataflowId the dataflow id
   * @return the dataflows referenced
   */
  Set<DataFlowVO> getDataflowsReferenced(Long dataflowId);

  /**
   * Update updatable reference dataset.
   *
   * @param datasetId the dataset id
   * @param updatable the updatable
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void updateUpdatableReferenceDataset(Long datasetId, Boolean updatable)
      throws EEAException, IOException;
}
