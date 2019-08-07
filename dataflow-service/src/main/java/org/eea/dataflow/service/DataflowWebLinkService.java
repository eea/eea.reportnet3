package org.eea.dataflow.service;

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
  void removeWebLink(Long webLinkId) throws EEAException;

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
