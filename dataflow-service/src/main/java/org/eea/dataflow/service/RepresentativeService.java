package org.eea.dataflow.service;

import java.io.IOException;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;

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
   * @throws EEAException the EEA exception
   */
  Long updateDataflowRepresentative(RepresentativeVO representativeVO) throws EEAException;

  /**
   * Gets the all data provider types.
   *
   * @return the all data provider types
   */
  List<DataProviderCodeVO> getAllDataProviderTypes();

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
   * @param mimeType the mime type
   * @return the byte[]
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] exportFile(Long dataflowId, String mimeType) throws EEAException, IOException;
}
