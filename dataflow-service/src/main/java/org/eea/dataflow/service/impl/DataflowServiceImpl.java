package org.eea.dataflow.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
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

    List<Dataflow> dataflows = dataflowRepository.findCompleted();
    List<DataFlowVO> dataflowVOs = new ArrayList<>();
    if (!dataflows.isEmpty()) {
      int initIndex = pageable.getPageNumber() * pageable.getPageSize();
      int endIndex = (pageable.getPageNumber() + 1) * pageable.getPageSize() > dataflows.size()
          ? dataflows.size()
          : (pageable.getPageNumber() + 1) * pageable.getPageSize();
      List<Dataflow> pagedDataflows = dataflows.subList(initIndex, endIndex);

      dataflowVOs = dataflowMapper.entityListToClass(pagedDataflows);

    }

    return dataflowVOs;
  }

  @Override
  public List<DataFlowVO> getPendingByUser(Long userId, TypeRequestEnum type) throws EEAException {

    List<Dataflow> dataflows = dataflowRepository.findByStatusAndUserRequester(type, userId);
    return dataflowMapper.entityListToClass(dataflows);

  }


}
