package org.eea.dataflow.service;

import java.util.Date;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataflowPublicPaginatedVO;
import org.eea.interfaces.vo.dataflow.DataflowPublicVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.eea.interfaces.vo.ums.DataflowUserRoleVO;
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
   * Gets the dataflows.
   *
   * @param userId the user id
   * @return the dataflows
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getDataflows(String userId) throws EEAException;


  /**
   * Gets the reference dataflows.
   *
   * @param userId the user id
   * @return the reference dataflows
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getReferenceDataflows(String userId) throws EEAException;


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
  Long createDataFlow(DataFlowVO dataflowVO) throws EEAException;


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
   * Gets the public dataflows.
   *
   * @return the public dataflows
   */
  List<DataflowPublicVO> getPublicDataflows();

  /**
   * Gets the public dataflow by id.
   *
   * @param dataflowId the dataflow id
   * @return the public dataflow by id
   * @throws EEAException the EEA exception
   */
  DataflowPublicVO getPublicDataflowById(Long dataflowId) throws EEAException;

  /**
   * Update data flow public status.
   *
   * @param dataflowId the dataflow id
   * @param showPublicInfo the show public info
   */
  void updateDataFlowPublicStatus(Long dataflowId, boolean showPublicInfo);

  /**
   * Gets the user roles.
   *
   * @param dataProviderId the data provider id
   * @param dataflowList the dataflow list
   * @return the user roles
   */
  List<DataflowUserRoleVO> getUserRoles(Long dataProviderId, List<DataFlowVO> dataflowList);

  /**
   * Gets the public dataflows by country.
   *
   * @param countryCode the country code
   * @param header the header
   * @param asc the asc
   * @param page the page
   * @param pageSize the page size
   * @return the public dataflows by country
   */
  DataflowPublicPaginatedVO getPublicDataflowsByCountry(String countryCode, String header,
      boolean asc, int page, int pageSize);


  /**
   * Gets the dataflows by data provider ids.
   *
   * @param dataProviderIds the data provider ids
   * @return the dataflows by data provider ids
   */
  List<DataFlowVO> getDataflowsByDataProviderIds(List<Long> dataProviderIds);

  /**
   * Checks if is reference dataflow draft.
   *
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if is reference dataflow draft
   */
  boolean isReferenceDataflowDraft(EntityClassEnum entity, Long entityId);

}
