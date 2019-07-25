package org.eea.dataflow.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.weblink.WeblinkVO;

/**
 * The Interface DataflowService.
 */
public interface DataflowWebLinkService {



  /**
   * Gets the web link.
   *
   * @param idDataflow the id dataflow
   * @return the web link
   */
  List<WeblinkVO> getWebLink(Long idDataflow) throws EEAException;

  /**
   * Removes the contributor from dataflow.
   *
   * @param idDataflow the id dataflow
   * @param url the url
   * @param description the description
   * @throws EEAException the EEA exception
   */
  void saveWebLink(Long idDataflow, String url, String description) throws EEAException;

  /**
   * Removes the web link.
   *
   * @param webLink the web link
   * @throws EEAException the EEA exception
   */
  void removeWebLink(Long webLink) throws EEAException;

  /**
   * Update web link.
   *
   * @param webLinkId the web link id
   * @param description the description
   * @param url the url
   * @throws EEAException the EEA exception
   */
  void updateWebLink(Long webLinkId, String description, String url) throws EEAException;
}
