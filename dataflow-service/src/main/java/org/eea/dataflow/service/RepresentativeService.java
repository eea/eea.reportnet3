package org.eea.dataflow.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;

/**
 * The Interface DataflowWebLinkService.
 */
public interface RepresentativeService {

  /**
   * Insert dataflow representative.
   *
   * @param dataflowId the dataflow id
   * @param dataflowRepresentativeVO the dataflow representative VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long insertRepresentative(final Long dataflowId, final RepresentativeVO representativeVO)
      throws EEAException;

  /**
   * Delete dataflow representative.
   *
   * @param dataflowRepresentativeId the dataflow representative id
   * @throws EEAException the EEA exception
   */
  void deleteDataflowRepresentative(final Long representativeId) throws EEAException;

  /**
   * Update dataflow representative.
   *
   * @param dataflowRepresentativeVO the dataflow representative VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long updateDataflowRepresentative(final RepresentativeVO representativeVO) throws EEAException;

  /**
   * Gets the all representative types.
   *
   * @return the all representative types
   */
  List<DataProviderCodeVO> getAllDataProviderTypes();

  /**
   * Gets the all representative by type.
   *
   * @param type the type
   * @return the all representative by type
   */
  List<DataProviderVO> getAllDataProviderByGroupId(Long groupId);

  /**
   * Gets the dataflow represetatives by id data flow.
   *
   * @param dataflowId the dataflow id
   * @return the dataflow represetatives by id data flow
   * @throws EEAException the EEA exception
   */
  List<RepresentativeVO> getRepresetativesByIdDataFlow(final Long dataflowId) throws EEAException;
}
