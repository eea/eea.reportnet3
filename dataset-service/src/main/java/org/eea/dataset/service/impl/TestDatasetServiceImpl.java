package org.eea.dataset.service.impl;

import java.util.List;
import org.eea.dataset.mapper.TestDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.TestDataset;
import org.eea.dataset.persistence.metabase.repository.TestDatasetRepository;
import org.eea.dataset.service.TestDatasetService;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
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
}
