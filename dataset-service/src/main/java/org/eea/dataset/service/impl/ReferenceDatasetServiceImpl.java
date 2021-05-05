package org.eea.dataset.service.impl;

import java.util.List;
import org.eea.dataset.mapper.ReferenceDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.service.ReferenceDatasetService;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The Class ReferenceDatasetServiceImpl.
 */
@Service
public class ReferenceDatasetServiceImpl implements ReferenceDatasetService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ReferenceDatasetServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");



  /** The reference dataset repository. */
  @Autowired
  private ReferenceDatasetRepository referenceDatasetRepository;


  /** The reference dataset mapper. */
  @Autowired
  private ReferenceDatasetMapper referenceDatasetMapper;


  /**
   * Gets the reference dataset by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the reference dataset by dataflow id
   */
  @Override
  public List<ReferenceDatasetVO> getReferenceDatasetByDataflowId(Long dataflowId) {
    List<ReferenceDataset> referenceDatasets =
        referenceDatasetRepository.findByDataflowId(dataflowId);
    return referenceDatasetMapper.entityListToClass(referenceDatasets);
  }


}
