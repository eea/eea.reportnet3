package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;


/**
 * The Interface TestDatasetService.
 */
public interface TestDatasetService {


  /**
   * Gets the Test dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the Test dataset by dataflow id
   */
  List<TestDatasetVO> getTestDatasetByDataflowId(Long idDataflow);

  /**
   * Find test datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<DatasetsSummaryVO> findTestDatasetsSummaryList(Long dataflowId);

}
