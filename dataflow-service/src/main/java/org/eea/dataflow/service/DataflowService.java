package org.eea.dataflow.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.springframework.data.domain.Pageable;

public interface DataflowService {

  DataFlowVO getById(Long id) throws EEAException;

  List<DataFlowVO> getByStatus(TypeStatusEnum status) throws EEAException;

  List<DataFlowVO> getPendingAccepted() throws EEAException;

  List<DataFlowVO> getCompleted(Pageable pageable) throws EEAException;

}
