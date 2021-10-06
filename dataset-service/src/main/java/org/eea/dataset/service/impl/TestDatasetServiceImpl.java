package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.mapper.TestDatasetMapper;
import org.eea.dataset.mapper.TestDatasetSummaryMapper;
import org.eea.dataset.persistence.metabase.domain.TestDataset;
import org.eea.dataset.persistence.metabase.repository.TestDatasetRepository;
import org.eea.dataset.service.TestDatasetService;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class TestDatasetServiceImpl.
 */
@Service
public class TestDatasetServiceImpl implements TestDatasetService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(TestDatasetServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /** The Test dataset repository. */
  @Autowired
  private TestDatasetRepository testDatasetRepository;

  /** The Test dataset mapper. */
  @Autowired
  private TestDatasetMapper testDatasetMapper;

  /** The test dataset summary mapper. */
  @Autowired
  private TestDatasetSummaryMapper testDatasetSummaryMapper;


  /**
   * Gets the EU dataset by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the Test dataset by dataflow id
   */
  @Override
  public List<TestDatasetVO> getTestDatasetByDataflowId(Long idDataflow) {
    List<TestDataset> testDatasets = testDatasetRepository.findByDataflowId(idDataflow);
    return testDatasetMapper.entityListToClass(testDatasets);
  }


  /**
   * Find test datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  public List<DatasetsSummaryVO> findTestDatasetsSummaryList(Long dataflowId) {
    List<DatasetsSummaryVO> testDatasetsSummaryList = new ArrayList<>();
    List<TestDatasetVO> testDatasetsVO = getTestDatasetByDataflowId(dataflowId);
    for (TestDatasetVO testDatasetVO : testDatasetsVO) {
      DatasetsSummaryVO datasetSummaryVO = testDatasetSummaryMapper.entityToClass(testDatasetVO);
      datasetSummaryVO.setDatasetTypeEnum(DatasetTypeEnum.TEST);
      testDatasetsSummaryList.add(datasetSummaryVO);
    }
    return testDatasetsSummaryList;
  }



}
