package org.eea.dataflow.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.weblink.WeblinkVO;


/**
 * The Interface DataflowWebLinkService.
 */
public interface DataflowWebLinkService {



  /**
   * Gets the web link.
   *
   * @param idLink the id link
   * @return the web link
   * @throws EEAException the EEA exception
   */
  WeblinkVO getWebLink(Long idLink) throws EEAException;

  /**
   * Removes the contributor from dataflow.
   *
   * @param idDataflow the id dataflow
   * @param weblinkVO the weblink VO
   * @throws EEAException the EEA exception
   */
  void saveWebLink(Long idDataflow, WeblinkVO weblinkVO) throws EEAException;


  /**
   * Removes the web link.
   *
   * @param webLinkId the web link id
   * @throws EEAException the EEA exception
   */
  void removeWebLink(Long webLinkId) throws EEAException;

  /**
   * Update web link.
   *
   * @param weblinkVO the weblink VO
   * @throws EEAException the EEA exception
   */
  void updateWebLink(WeblinkVO weblinkVO) throws EEAException;

  /**
   * Gets the all weblinks by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the all weblinks by dataflow id
   * @throws EEAException the EEA exception
   */
  List<WeblinkVO> getAllWeblinksByDataflowId(Long dataflowId) throws EEAException;
}
