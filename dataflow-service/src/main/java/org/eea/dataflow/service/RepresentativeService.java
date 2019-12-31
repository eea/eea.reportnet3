package org.eea.dataflow.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;

/**
 * The Interface RepresentativeService.
 */
public interface RepresentativeService {

  /**
   * Insert representative.
   *
   * @param dataflowId the dataflow id
   * @param representativeVO the representative VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long insertRepresentative(final Long dataflowId, final RepresentativeVO representativeVO)
      throws EEAException;

  /**
   * Delete dataflow representative.
   *
   * @param representativeId the representative id
   * @throws EEAException the EEA exception
   */
  void deleteDataflowRepresentative(final Long representativeId) throws EEAException;

  /**
   * Update dataflow representative.
   *
   * @param representativeVO the representative VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long updateDataflowRepresentative(final RepresentativeVO representativeVO) throws EEAException;

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
  List<RepresentativeVO> getRepresetativesByIdDataFlow(final Long dataflowId) throws EEAException;
}
