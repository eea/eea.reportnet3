package org.eea.dataset.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.CodelistVO;

/**
 * The Interface CodelistService.
 */
public interface CodelistService {

  /**
   * Gets the codelist by id.
   *
   * @param codelistId the codelist id
   * @return the codelist by id
   * @throws EEAException
   */
  CodelistVO getById(Long codelistId) throws EEAException;

}
