package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.CodelistCategoryVO;
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
   * @throws EEAException the EEA exception
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
   * @return the long
   * @throws EEAException the EEA exception
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

  /**
   * Gets the category by id.
   *
   * @param codelistCategoryId the codelist category id
   * @return the category by id
   * @throws EEAException the EEA exception
   */
  CodelistCategoryVO getCategoryById(Long codelistCategoryId) throws EEAException;

  /**
   * Creates the category.
   *
   * @param codelistCategoryVO the codelist category VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long createCategory(CodelistCategoryVO codelistCategoryVO) throws EEAException;

  /**
   * Update category.
   *
   * @param codelistCategoryVO the codelist category VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  Long updateCategory(CodelistCategoryVO codelistCategoryVO) throws EEAException;

  /**
   * Delete category.
   *
   * @param codelistCategoryId the codelist category id
   */
  void deleteCategory(Long codelistCategoryId);

  /**
   * Gets the all by ids.
   *
   * @param codelistIds the codelist ids
   * @return the all by ids
   * @throws EEAException the EEA exception
   */
  List<CodelistVO> getAllByIds(List<Long> codelistIds) throws EEAException;

}
