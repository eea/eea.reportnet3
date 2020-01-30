package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.mapper.CodelistCategoryFullMapper;
import org.eea.dataset.mapper.CodelistCategoryMapper;
import org.eea.dataset.mapper.CodelistItemMapper;
import org.eea.dataset.mapper.CodelistMapper;
import org.eea.dataset.persistence.metabase.domain.Codelist;
import org.eea.dataset.persistence.metabase.domain.CodelistCategory;
import org.eea.dataset.persistence.metabase.repository.CodelistCategoryRepository;
import org.eea.dataset.persistence.metabase.repository.CodelistItemRepository;
import org.eea.dataset.persistence.metabase.repository.CodelistRepository;
import org.eea.dataset.service.CodelistService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.CodelistCategoryFullVO;
import org.eea.interfaces.vo.dataset.CodelistCategoryVO;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.eea.interfaces.vo.dataset.enums.CodelistStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class CodelistServiceImpl.
 */
@Service("codelistService")
public class CodelistServiceImpl implements CodelistService {

  /** The codelist repository. */
  @Autowired
  private CodelistRepository codelistRepository;

  /** The codelist item repository. */
  @Autowired
  private CodelistItemRepository codelistItemRepository;

  /** The codelist category repository. */
  @Autowired
  private CodelistCategoryRepository codelistCategoryRepository;

  /** The codelist mapper. */
  @Autowired
  private CodelistMapper codelistMapper;

  /** The codelist item mapper. */
  @Autowired
  private CodelistItemMapper codelistItemMapper;

  /** The codelist category mapper. */
  @Autowired
  private CodelistCategoryMapper codelistCategoryMapper;

  /** The codelist category full mapper. */
  @Autowired
  private CodelistCategoryFullMapper codelistCategoryFullMapper;

  /**
   * Gets the by id.
   *
   *
   * @param codelistId the codelist id
   * @return the by id
   * @throws EEAException the EEA exception
   */
  // Forcing the metabase transaction manager because in the subentity load loses the
  // session
  @Override
  @Transactional(transactionManager = "metabaseDataSetsTransactionManager")
  public CodelistVO getById(Long codelistId) throws EEAException {
    Codelist codelist = codelistRepository.findById(codelistId).orElse(null);
    if (null == codelist) {
      throw new EEAException(EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    return codelistMapper.entityToClass(codelist);
  }

  /**
   * Find duplicated.
   *
   * @param name the name
   * @param version the version
   * @return the list
   */
  private List<CodelistVO> findDuplicated(String name, String version) {
    List<Codelist> codelists =
        codelistRepository.findAllByNameAndVersion(name, version).orElse(null);
    if (null != codelists) {
      return codelistMapper.entityListToClass(codelists);
    }
    return new ArrayList<>();
  }

  /**
   * Delete.
   *
   * @param codelistId the codelist id
   */
  @Override
  @Transactional
  public void delete(Long codelistId) {
    codelistRepository.deleteById(codelistId);
  }

  /**
   * Creates the.
   *
   * @param codelistVO the codelist VO
   * @param codelistId the codelist id
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public Long create(CodelistVO codelistVO, Long codelistId) throws EEAException {
    Long response;
    codelistVO.setStatus(CodelistStatusEnum.DESIGN);
    Codelist codelist = codelistMapper.classToEntity(codelistVO);
    if (codelistId == null) {
      response = codelistRepository.save(codelist).getId();
    } else {
      Codelist oldCodelist = codelistRepository.findById(codelistId).orElse(null);
      modifyWhenClone(codelist, oldCodelist);
      codelist.setStatus(CodelistStatusEnum.DESIGN);
      response = codelistRepository.save(codelist).getId();
    }
    if (response != null && codelist.getItems() != null && !codelist.getItems().isEmpty()) {
      Codelist codelisttemp = new Codelist();
      codelisttemp.setId(response);
      codelist.getItems().stream().forEach(item -> item.setCodelist(codelisttemp));
      codelistItemRepository.saveAll(codelist.getItems());
    }
    return response;
  }


  private void modifyWhenClone(Codelist codelist, Codelist oldCodelist) throws EEAException {
    if (oldCodelist == null) {
      throw new EEAException(EEAErrorMessage.CODELIST_NOT_FOUND);
    }

    if (codelist.getCategory() == null) {
      codelist.setCategory(oldCodelist.getCategory());
    }
    if (codelist.getDescription() == null) {
      codelist.setDescription(oldCodelist.getDescription());
    }
    if (codelist.getItems() == null) {
      codelist.setItems(oldCodelist.getItems());
    }
    if (codelist.getName() == null) {
      codelist.setName(oldCodelist.getName());
    }
    if (codelist.getVersion() == null) {
      codelist.setVersion(oldCodelist.getVersion());
    }
    if (!findDuplicated(codelist.getName(), codelist.getVersion()).isEmpty()) {
      throw new EEAException(EEAErrorMessage.CODELIST_VERSION_DUPLICATED);
    }
  }

  /**
   * Update.
   *
   * @param codelistVO the codelist VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional(transactionManager = "metabaseDataSetsTransactionManager")
  public Long update(CodelistVO codelistVO) throws EEAException {
    Codelist oldCodelist = codelistRepository.findById(codelistVO.getId()).orElse(null);
    if (oldCodelist == null) {
      throw new EEAException(EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    switch (oldCodelist.getStatus()) {
      case DESIGN:
        modifyCodelistDesignState(codelistVO, oldCodelist);
        break;
      case DEPRECATED:
        if (CodelistStatusEnum.READY.equals(codelistVO.getStatus())) {
          oldCodelist.setStatus(CodelistStatusEnum.READY);
        }
        break;
      case READY:
        if (CodelistStatusEnum.DEPRECATED.equals(codelistVO.getStatus())) {
          oldCodelist.setStatus(CodelistStatusEnum.DEPRECATED);
        }
        break;
      default:
    }
    Codelist auxCodelist = new Codelist();
    auxCodelist.setId(oldCodelist.getId());
    oldCodelist.getItems().stream().forEach(item -> item.setCodelist(auxCodelist));
    codelistRepository.save(oldCodelist).getId();
    return codelistVO.getId();
  }

  /**
   * Modify codelist design state.
   *
   * @param codelistVO the codelist VO
   * @param oldCodelist the old codelist
   * @throws EEAException the EEA exception
   */
  private void modifyCodelistDesignState(CodelistVO codelistVO, Codelist oldCodelist)
      throws EEAException {
    String version =
        codelistVO.getVersion() != null ? codelistVO.getVersion() : oldCodelist.getVersion();
    List<CodelistVO> list = findDuplicated(codelistVO.getName(), version);
    list.removeIf(item -> codelistVO.getId().equals(item.getId()));
    if (!list.isEmpty()) {
      throw new EEAException(EEAErrorMessage.CODELIST_VERSION_DUPLICATED);
    }
    if (codelistVO.getVersion() != null) {
      oldCodelist.setVersion(version);
    }
    if (CodelistStatusEnum.READY.equals(codelistVO.getStatus())) {
      oldCodelist.setStatus(CodelistStatusEnum.READY);
    }
    if (CodelistStatusEnum.DEPRECATED.equals(codelistVO.getStatus())) {
      oldCodelist.setStatus(CodelistStatusEnum.DEPRECATED);
    }
    if (codelistVO.getName() != null) {
      oldCodelist.setName(codelistVO.getName());
    }
    if (codelistVO.getDescription() != null) {
      oldCodelist.setDescription(codelistVO.getDescription());
    }
    if (codelistVO.getCategory() != null) {
      oldCodelist.setCategory(codelistCategoryMapper.classToEntity(codelistVO.getCategory()));
    }
    if (codelistVO.getItems() != null) {
      codelistItemRepository.deleteAll(oldCodelist.getItems());
      oldCodelist.setItems(codelistItemMapper.classListToEntity(codelistVO.getItems()));
    }
  }

  /**
   * Gets the category by id.
   *
   * @param codelistCategoryId the codelist category id
   * @return the category by id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public CodelistCategoryVO getCategoryById(Long codelistCategoryId) throws EEAException {
    CodelistCategory codelistCategory =
        codelistCategoryRepository.findById(codelistCategoryId).orElse(null);
    if (null == codelistCategory) {
      throw new EEAException(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND);
    }
    CodelistCategoryVO codelistCategoryVO = codelistCategoryMapper.entityToClass(codelistCategory);
    List<Codelist> list =
        codelistRepository.findAllByCategory_Id(codelistCategoryId).orElse(new ArrayList<>());
    codelistCategoryVO.setCodelistNumber(list.size());
    return codelistCategoryVO;
  }

  /**
   * Gets the all categories.
   *
   * @return the all categories
   * @throws EEAException the EEA exception
   */
  @Override
  public List<CodelistCategoryVO> getAllCategories() throws EEAException {
    List<CodelistCategory> codelistCategories = codelistCategoryRepository.findAll();
    List<CodelistCategoryVO> categoriesVO =
        codelistCategoryMapper.entityListToClass(codelistCategories);
    for (CodelistCategoryVO categoryVO : categoriesVO) {
      categoryVO.setCodelistNumber(codelistRepository.findAllByCategory_Id(categoryVO.getId())
          .orElse(new ArrayList<Codelist>()).size());
    }
    return categoriesVO;
  }

  /**
   * Creates the category.
   *
   * @param codelistCategoryVO the codelist category VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public Long createCategory(CodelistCategoryVO codelistCategoryVO) throws EEAException {
    if (codelistCategoryVO == null) {
      throw new EEAException(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND);
    }
    CodelistCategory codelistCategory = codelistCategoryMapper.classToEntity(codelistCategoryVO);
    return codelistCategoryRepository.save(codelistCategory).getId();
  }

  /**
   * Update category.
   *
   * @param codelistCategoryVO the codelist category VO
   * @return the long
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public Long updateCategory(CodelistCategoryVO codelistCategoryVO) throws EEAException {
    CodelistCategory oldCodelistCategory =
        codelistCategoryRepository.findById(codelistCategoryVO.getId()).orElse(null);
    if (oldCodelistCategory == null) {
      throw new EEAException(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND);
    }
    if (oldCodelistCategory.getShortCode() != null) {
      oldCodelistCategory.setShortCode(codelistCategoryVO.getShortCode());
    }
    if (oldCodelistCategory.getDescription() != null) {
      oldCodelistCategory.setDescription(codelistCategoryVO.getDescription());
    }
    return codelistCategoryRepository.save(oldCodelistCategory).getId();


  }

  /**
   * Delete category.
   *
   * @param codelistCategoryId the codelist category id
   */
  @Override
  @Transactional
  public void deleteCategory(Long codelistCategoryId) {
    codelistCategoryRepository.deleteById(codelistCategoryId);
  }

  /**
   * Gets the all by ids.
   *
   * @param codelistIds the codelist ids
   * @return the all by ids
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public List<CodelistVO> getAllByIds(List<Long> codelistIds) throws EEAException {
    List<Codelist> codelists = codelistRepository.findAllByIdIn(codelistIds).orElse(null);
    if (null == codelists) {
      throw new EEAException(EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    return codelistMapper.entityListToClass(codelists);
  }

  /**
   * Gets the all by category id.
   *
   * @param codelistCategoryId the codelist category id
   * @return the all by category id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public List<CodelistVO> getAllByCategoryId(Long codelistCategoryId) throws EEAException {
    List<Codelist> codelists = codelistRepository.findAllByCategory_Id(codelistCategoryId)
        .orElse(new ArrayList<Codelist>());
    return codelistMapper.entityListToClass(codelists);
  }

  /**
   * Gets the all categories complete.
   *
   * @return the all categories complete
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional(transactionManager = "metabaseDataSetsTransactionManager")
  public List<CodelistCategoryFullVO> getAllCategoriesComplete() throws EEAException {
    List<CodelistCategory> codelistCategories = codelistCategoryRepository.findAll();
    return codelistCategoryFullMapper.entityListToClass(codelistCategories);
  }
}
