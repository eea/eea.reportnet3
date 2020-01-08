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

  /**
   * Delete.
   *
   * @param codelistId the codelist id
   */
  void delete(Long codelistId);

  /**
   * Creates the.
   *
   * @param codelistVO the codelist VO
   * @param codelistId the codelist id
   */
  Long create(CodelistVO codelistVO, Long codelistId) throws EEAException;

  /**
   * Update.
   *
   * @param codelistVO the codelist VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long update(CodelistVO codelistVO) throws EEAException;

}
