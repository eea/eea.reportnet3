package org.eea.dataflow.service;

import java.util.Date;
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
   * Get the dataflow by its id filtering representatives by the user email.
   *
   * @param id the id
   * @return the by id no representatives
   * @throws EEAException the EEA exception
   */
  DataFlowVO getByIdWithRepresentativesFilteredByUserEmail(Long id) throws EEAException;

  /**
   * Gets the by status.
   *
   * @param status the status
   *
   * @return the by status
   *
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getByStatus(TypeStatusEnum status) throws EEAException;


  /**
   * Gets the pending accepted.
   *
   * @param userId the user id
   *
   * @return the pending accepted
   *
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getPendingAccepted(String userId) throws EEAException;


  /**
   * Gets the completed.
   *
   * @param userId the user id
   * @param pageable the pageable
   *
   * @return the completed
   *
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getCompleted(String userId, Pageable pageable) throws EEAException;


  /**
   * Gets the pending by user.
   *
   * @param userId the user id
   * @param type the type
   *
   * @return the pending by user
   *
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getPendingByUser(String userId, TypeRequestEnum type) throws EEAException;


  /**
   * Update user request status.
   *
   * @param userRequestId the user request id
   * @param type the type
   *
   * @throws EEAException the EEA exception
   */
  void updateUserRequestStatus(Long userRequestId, TypeRequestEnum type) throws EEAException;


  /**
   * Adds the contributor to dataflow.
   *
   * @param idDataflow the id dataflow
   * @param idContributor the id contributor
   *
   * @throws EEAException the EEA exception
   */
  void addContributorToDataflow(Long idDataflow, String idContributor) throws EEAException;

  /**
   * Removes the contributor from dataflow.
   *
   * @param idDataflow the id dataflow
   * @param idContributor the id contributor
   *
   * @throws EEAException the EEA exception
   */
  void removeContributorFromDataflow(Long idDataflow, String idContributor) throws EEAException;


  /**
   * Creates the data flow.
   *
   * @param dataflowVO the dataflow VO
   * @throws EEAException the EEA exception
   */
  void createDataFlow(DataFlowVO dataflowVO) throws EEAException;


  /**
   * Delete data flow.
   *
   * @param idDataflow the id dataflow
   * @throws Exception the exception
   */
  void deleteDataFlow(Long idDataflow) throws Exception;

  /**
   * Update data flow.
   *
   * @param dataflowVO the dataflow VO
   * @throws EEAException the EEA exception
   */
  void updateDataFlow(DataFlowVO dataflowVO) throws EEAException;

  /**
   * Gets the reporting datasets id.
   *
   * @param dataschemaId the dataschema id
   * @return the reporting datasets id
   * @throws EEAException the EEA exception
   */
  DataFlowVO getReportingDatasetsId(String dataschemaId) throws EEAException;


  /**
   * Gets the metabase by id.
   *
   * @param id the id
   * @return the metabase by id
   * @throws EEAException the EEA exception
   */
  DataFlowVO getMetabaseById(Long id) throws EEAException;



  /**
   * Update data flow status.
   *
   * @param id the id
   * @param status the status
   * @param deadline the deadline
   * @throws EEAException the EEA exception
   */
  void updateDataFlowStatus(Long id, TypeStatusEnum status, Date deadline) throws EEAException;

  /**
   * Find provider code by id.
   *
   * @param providerId the provider id
   * @return the string
   */
  String getProviderCodeById(Long providerId);
}
