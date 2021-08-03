package org.eea.dataflow.service;

import java.io.IOException;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.FMEUserVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;
import org.springframework.web.multipart.MultipartFile;

/** The Interface RepresentativeService. */
public interface RepresentativeService {

  /**
   * Creates the representative.
   *
   * @param dataflowId the dataflow id
   * @param representativeVO the representative VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long createRepresentative(Long dataflowId, RepresentativeVO representativeVO) throws EEAException;

  /**
   * Delete dataflow representative.
   *
   * @param representativeId the representative id
   * @throws EEAException the EEA exception
   */
  void deleteDataflowRepresentative(Long representativeId) throws EEAException;

  /**
   * Update dataflow representative.
   *
   * @param representativeVO the representative VO
   * @return the long
   */
  Long updateDataflowRepresentative(RepresentativeVO representativeVO);

  /**
   * Gets DataProviderGroup names based on TypeDataProviderEnum
   *
   * @param providerType Country or Company
   * @return all dataProviders matching the TypeDataProviderEnum provided
   */
  List<DataProviderCodeVO> getDataProviderGroupByType(TypeDataProviderEnum providerType);

  /**
   * Gets the all data provider by group id.
   *
   * @param groupId the group id
   * @return the all data provider by group id
   */
  List<DataProviderVO> getAllDataProviderByGroupId(Long groupId);

  /**
   * Gets the represetatives by id data flow.
   *
   * @param dataflowId the dataflow id
   * @return the represetatives by id data flow
   * @throws EEAException the EEA exception
   */
  List<RepresentativeVO> getRepresetativesByIdDataFlow(Long dataflowId) throws EEAException;


  /**
   * Gets the data provider by id.
   *
   * @param dataProviderId the data provider id
   * @return the data provider by id
   */
  DataProviderVO getDataProviderById(Long dataProviderId);

  /**
   * Find data providers by ids.
   *
   * @param dataProviderIds the data provider ids
   * @return the list
   */
  List<DataProviderVO> findDataProvidersByIds(List<Long> dataProviderIds);

  /**
   * Gets the represetatives by dataflow id and email.
   *
   * @param dataflowId the dataflow id
   * @param email the email
   * @return the represetatives by dataflow id and email
   */
  List<RepresentativeVO> getRepresetativesByDataflowIdAndEmail(Long dataflowId, String email);


  /**
   * Export file.
   *
   * @param dataflowId the dataflow id
   * @return the byte[]
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] exportFile(Long dataflowId) throws EEAException, IOException;



  /**
   * Export template reporters file.
   *
   * @param groupId the group id
   * @return the byte[]
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] exportTemplateReportersFile(Long groupId) throws EEAException, IOException;

  /**
   * Import file.
   *
   * @param dataflowId the dataflow id
   * @param groupId the group id
   * @param file the file
   * @return the byte[]
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] importFile(Long dataflowId, Long groupId, MultipartFile file)
      throws EEAException, IOException;

  /**
   * Creates the lead reporter.
   *
   * @param representativeId the representative id
   * @param leadReporterVO the lead reporter VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long createLeadReporter(Long representativeId, LeadReporterVO leadReporterVO) throws EEAException;

  /**
   * Update lead reporter.
   *
   * @param leadReporterVO the lead reporter VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long updateLeadReporter(LeadReporterVO leadReporterVO) throws EEAException;

  /**
   * Delete lead reporter.
   *
   * @param leadReporterId the lead reporter id
   * @throws EEAException the EEA exception
   */
  void deleteLeadReporter(Long leadReporterId) throws EEAException;

  /**
   * Update representative visibility restrictions.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param restrictFromPublic the restrict from public
   */
  void updateRepresentativeVisibilityRestrictions(Long dataflowId, Long dataProviderId,
      boolean restrictFromPublic);

  /**
   * Authorize by representative id.
   *
   * @param representativeId the representative id
   * @return true, if successful
   */
  boolean authorizeByRepresentativeId(Long representativeId);

  /**
   * Find data providers by code.
   *
   * @param code the code
   * @return the list
   */
  List<DataProviderVO> findDataProvidersByCode(String code);

  /**
   * Gets the provider ids.
   *
   * @return the provider ids
   * @throws EEAException the EEA exception
   */
  List<Long> getProviderIds() throws EEAException;

  /**
   * Find representatives by dataflow id and dataprovider list.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderIdList the data provider id list
   * @return the list
   */
  List<RepresentativeVO> findRepresentativesByDataflowIdAndDataproviderList(Long dataflowId,
      List<Long> dataProviderIdList);

  /**
   * Find fme users.
   *
   * @return the list
   */
  List<FMEUserVO> findFmeUsers();
}
