package org.eea.dataset.service.impl;

import java.util.List;
import org.eea.dataset.mapper.DataCollectionMapper;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.service.DataCollectionService;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * The Class DataCollectionServiceImpl.
 */
@Service("dataCollectionService")
public class DataCollectionServiceImpl implements DataCollectionService {



  /** The data collection repository. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;



  /** The data collection mapper. */
  @Autowired
  private DataCollectionMapper dataCollectionMapper;


  /**
   * Gets the data collection id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data collection id by dataflow id
   */
  @Override
  public List<DataCollectionVO> getDataCollectionIdByDataflowId(Long idFlow) {

    List<DataCollection> datacollections = dataCollectionRepository.findByDataflowId(idFlow);
    return dataCollectionMapper.entityListToClass(datacollections);
  }



}
