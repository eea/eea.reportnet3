package org.eea.dataflow.service.impl;

import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service("dataflowService")
public class DataflowServiceImpl implements DataflowService {


  @Autowired
  private DataflowRepository dataflowRepository;

  @Autowired
  private DataflowMapper dataflowMapper;

  @Override
  @Transactional
  public DataFlowVO getById(Long id) throws EEAException {

    if (id == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Dataflow result = dataflowRepository.findById(id).orElse(null);

    return dataflowMapper.entityToClass(result);
  }

  @Override
  public List<DataFlowVO> getByStatus(TypeStatusEnum status) throws EEAException {

    List<Dataflow> dataflows = dataflowRepository.findByStatus(status);
    return dataflowMapper.entityListToClass(dataflows);
  }

  @Override
  public List<DataFlowVO> getPendingAccepted() throws EEAException {

    List<Dataflow> dataflows = dataflowRepository.findPendingAccepted();
    return dataflowMapper.entityListToClass(dataflows);

  }

  @Override
  public List<DataFlowVO> getCompleted(Pageable pageable) throws EEAException {
    // TODO Auto-generated method stub
    return null;
  }

}
