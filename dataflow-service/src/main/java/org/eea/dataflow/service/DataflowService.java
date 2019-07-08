package org.eea.dataflow.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.springframework.data.domain.Pageable;

/**
 * The Interface DataflowService.
 */
public interface DataflowService {

  /**
   * Gets the by id.
   *
   * @param id the id
   * @return the by id
   * @throws EEAException the EEA exception
   */
  DataFlowVO getById(Long id) throws EEAException;

  /**
   * Gets the by status.
   *
   * @param status the status
   * @return the by status
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getByStatus(TypeStatusEnum status) throws EEAException;


  /**
   * Gets the pending accepted.
   *
   * @param userId the user id
   * @return the pending accepted
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getPendingAccepted(Long userId) throws EEAException;


  /**
   * Gets the completed.
   *
   * @param userId the user id
   * @param pageable the pageable
   * @return the completed
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getCompleted(Long userId, Pageable pageable) throws EEAException;


  /**
   * Gets the pending by user.
   *
   * @param userId the user id
   * @param type the type
   * @return the pending by user
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getPendingByUser(Long userId, TypeRequestEnum type) throws EEAException;


  void updateUserRequestStatus(Long userRequestId, TypeRequestEnum type) throws EEAException;

}
