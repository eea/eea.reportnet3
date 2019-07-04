package org.eea.dataset.service.impl;

import java.util.List;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("datasetMetabaseService")
public class DatasetMetabaseServiceImpl implements DatasetMetabaseService {

  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  @Autowired
  private DataSetMetabaseMapper dataSetMetabaseMapper;


  @Override
  public List<DataSetVO> getDataSetIdByDataflowId(Long idFlow) {


    List<DataSetMetabase> datasets = dataSetMetabaseRepository.findByDataflowId(idFlow);


    return dataSetMetabaseMapper.entityListToClass(datasets);
  }

}
