package org.eea.dataflow.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataflowCountVO;
import org.eea.interfaces.vo.dataflow.DataflowPrivateVO;
import org.eea.interfaces.vo.dataflow.DataflowPublicVO;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataflow.PaginatedDataflowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
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
   * @param removeWeblinksAndDocuments the remove weblinks and documents
   * @return the by id
   * @throws EEAException the EEA exception
   */
  DataFlowVO getById(Long id, boolean removeWeblinksAndDocuments) throws EEAException;

  /**
   * Gets the name by id.
   *
   * @param id the id
   * @return the name
   * @throws EEAException the EEA exception
   */
  String getDataflowNameById(Long id);

  /**
   * Gets the by id with representatives filtered by user email.
   *
   * @param id the id
   * @param providerId the provider id
   * @return the by id with representatives filtered by user email
   * @throws EEAException the EEA exception
   */
  DataFlowVO getByIdWithRepresentativesFilteredByUserEmail(Long id, Long providerId)
      throws EEAException;

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
   * @param dataflowType the dataflow type
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param sizePage the size page
   * @param numPage the num page
   * @return the dataflows
   * @throws EEAException the EEA exception
   */
  PaginatedDataflowVO getDataflows(String userId, TypeDataflowEnum dataflowType,
      Map<String, String> filters, String orderHeader, boolean asc, Integer sizePage,
      Integer numPage) throws EEAException;

  /**
   * Gets the cloneable dataflows.
   *
   * @param userId the user id
   * @return the cloneable dataflows
   * @throws EEAException the EEA exception
   */
  List<DataFlowVO> getCloneableDataflows(String userId) throws EEAException;

  /**
   * Gets the dataflows count.
   *
   * @return the dataflows count
   * @throws EEAException the EEA exception
   */
  List<DataflowCountVO> getDataflowsCount() throws EEAException;

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
   * Creates the data flow.
   *
   * @param dataflowVO the dataflow VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long createDataFlow(DataFlowVO dataflowVO) throws EEAException;


  /**
   * Delete data flow.
   *
   * @param idDataflow the id dataflow
   */
  void deleteDataFlow(Long idDataflow);

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
   * @param filters the filters
   * @param orderHeader the order header
   * @param asc the asc
   * @param sizePage the size page
   * @param numPage the num page
   * @return the public dataflows
   * @throws EEAException the EEA exception
   */
  PaginatedDataflowVO getPublicDataflows(Map<String, String> filters, String orderHeader,
      boolean asc, Integer sizePage, Integer numPage) throws EEAException;

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
   * @param filters the filters
   * @return the public dataflows by country
   * @throws EEAException the EEA exception
   */
  PaginatedDataflowVO getPublicDataflowsByCountry(String countryCode, String header, boolean asc,
      int page, int pageSize, Map<String, String> filters) throws EEAException;


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


  /**
   * Checks if is dataflow type.
   *
   * @param dataflowType the dataflow type
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if is dataflow type
   */
  boolean isDataflowType(TypeDataflowEnum dataflowType, EntityClassEnum entity, Long entityId);

  /**
   * Checks if user is admin.
   *
   * @return true, if is admin
   */
  boolean isAdmin();

  /**
   * Checks if user is custodian.
   *
   * @return true, if is custodian
   */
  boolean isCustodian();

  /**
   * Gets the private dataflow by id.
   *
   * @param dataflowId the dataflow id
   * @return the private dataflow by id
   * @throws EEAException the EEA exception
   */
  DataflowPrivateVO getPrivateDataflowById(Long dataflowId) throws EEAException;


  /**
   * Gets the dataset summary.
   *
   * @param dataflowId the dataflow id
   * @return the dataset summary
   * @throws EEAException the EEA exception
   */
  List<DatasetsSummaryVO> getDatasetSummary(Long dataflowId) throws EEAException;

  /**
   * Validate all reporters.
   *
   * @param userId the user id
   * @throws EEAException the EEA exception
   */
  void validateAllReporters(String userId) throws EEAException;

  /**
   * Update data flow automatic reporting deletion.
   *
   * @param dataflowId the dataflow id
   * @param automaticReportingDeletion the automatic reporting deletion
   */
  void updateDataFlowAutomaticReportingDeletion(Long dataflowId,
      boolean automaticReportingDeletion);

  /**
   * Gets the dataflows metabase by id.
   *
   * @param dataflowIds the dataflow ids
   * @return the dataflows metabase by id
   */
  List<DataFlowVO> getDataflowsMetabaseById(List<Long> dataflowIds);

}
