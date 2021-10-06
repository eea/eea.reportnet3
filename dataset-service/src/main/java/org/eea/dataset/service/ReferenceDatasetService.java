package org.eea.dataset.service;

import java.util.List;
import java.util.Set;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
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
   * Update updatable.
   *
   * @param datasetId the dataset id
   * @param updatable the updatable
   * @throws EEAException the EEA exception
   */
  void updateUpdatable(Long datasetId, Boolean updatable) throws EEAException;

  /**
   * Find reference dataset summary list.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DatasetsSummaryVO> findReferenceDatasetSummaryList(Long dataflowId);
}
