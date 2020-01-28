package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eea.dataset.mapper.CodelistCategoryFullMapper;
import org.eea.dataset.mapper.CodelistCategoryMapper;
import org.eea.dataset.mapper.CodelistItemMapper;
import org.eea.dataset.mapper.CodelistMapper;
import org.eea.dataset.persistence.metabase.domain.Codelist;
import org.eea.dataset.persistence.metabase.domain.CodelistCategory;
import org.eea.dataset.persistence.metabase.domain.CodelistItem;
import org.eea.dataset.persistence.metabase.repository.CodelistCategoryRepository;
import org.eea.dataset.persistence.metabase.repository.CodelistItemRepository;
import org.eea.dataset.persistence.metabase.repository.CodelistRepository;
import org.eea.dataset.service.impl.CodelistServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.CodelistCategoryFullVO;
import org.eea.interfaces.vo.dataset.CodelistCategoryVO;
import org.eea.interfaces.vo.dataset.CodelistItemVO;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.eea.interfaces.vo.dataset.enums.CodelistStatusEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * The Class CodelistServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CodelistServiceImplTest {

  /** The codelist service impl. */
  @InjectMocks
  CodelistServiceImpl codelistServiceImpl;

  /** The codelist repository. */
  @Mock
  private CodelistRepository codelistRepository;

  /** The codelist item repository. */
  @Mock
  private CodelistItemRepository codelistItemRepository;

  /** The codelist category repository. */
  @Mock
  private CodelistCategoryRepository codelistCategoryRepository;

  /** The codelist mapper. */
  @Mock
  private CodelistMapper codelistMapper;

  /** The codelist item mapper. */
  @Mock
  private CodelistItemMapper codelistItemMapper;

  /** The codelist category mapper. */
  @Mock
  private CodelistCategoryMapper codelistCategoryMapper;

  /** The codelist category full mapper. */
  @Mock
  CodelistCategoryFullMapper codelistCategoryFullMapper;

  /** The codelist VO. */
  private CodelistVO codelistVO;

  /** The codelist. */
  private Codelist codelist;

  /** The codelist category VO. */
  private CodelistCategoryVO codelistCategoryVO;

  /** The codelist category. */
  private CodelistCategory codelistCategory;

  /** The items. */
  private List<CodelistItem> items;

  /** The codelists. */
  private List<Codelist> codelists;

  /** The codelists VO. */
  private List<CodelistVO> codelistsVO;

  /** The codelist items VO. */
  private List<CodelistItemVO> codelistItemsVO;

  /** The codelist categories VO. */
  List<CodelistCategoryVO> codelistCategoriesVO;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    codelistVO = new CodelistVO();
    codelistVO.setId(1L);
    codelist = new Codelist();
    codelist.setId(1L);
    codelistCategoryVO = new CodelistCategoryVO();
    codelistCategoryVO.setId(1L);
    codelistCategory = new CodelistCategory();
    codelistCategory.setId(1L);
    codelistCategoriesVO = new ArrayList<>();
    codelistCategoriesVO.add(codelistCategoryVO);
    items = new ArrayList<>();
    items.add(new CodelistItem());
    codelists = new ArrayList<>();
    codelists.add(codelist);
    codelistsVO = new ArrayList<>();
    codelistsVO.add(codelistVO);
    codelistItemsVO = new ArrayList<>();
    codelistItemsVO.add(new CodelistItemVO());
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the by id exception 1 test.
   *
   * @return the by id exception 1 test
   */
  @Test
  public void getByIdExceptionNullTest() {
    try {
      codelistServiceImpl.getById(null);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Gets the by id exception 2 test.
   *
   * @return the by id exception 2 test
   */
  @Test
  public void getByIdExceptionNotFoundTest() {
    when(codelistRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      codelistServiceImpl.getById(1L);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Gets the by id success test.
   *
   * @return the by id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getByIdSuccessTest() throws EEAException {
    when(codelistRepository.findById(Mockito.any())).thenReturn(Optional.of(codelist));
    when(codelistMapper.entityToClass(Mockito.any())).thenReturn(codelistVO);
    assertEquals("not equal", codelistVO, codelistServiceImpl.getById(1L));
  }

  /**
   * Delete success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteSuccessTest() throws EEAException {
    codelistServiceImpl.delete(1L);
    Mockito.verify(codelistRepository, times(1)).deleteById(Mockito.any());
  }

  /**
   * Creates the exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createExceptionCloneNotFoundTest() throws EEAException {
    codelist.setItems(items);
    codelist.setId(1L);
    when(codelistMapper.classToEntity((Mockito.any()))).thenReturn(codelist);
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.empty());
    try {
      codelistServiceImpl.create(codelistVO, 1L);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Creates the success 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createSuccessCreateEmptyTest() throws EEAException {
    when(codelistMapper.classToEntity((Mockito.any()))).thenReturn(codelist);
    when(codelistRepository.save((Mockito.any()))).thenReturn(codelist);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.create(codelistVO, null));
  }

  /**
   * Creates the success 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createSuccessCreateWitItemsTest() throws EEAException {
    codelist.setItems(items);
    when(codelistMapper.classToEntity((Mockito.any()))).thenReturn(codelist);
    when(codelistRepository.save((Mockito.any()))).thenReturn(codelist);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.create(codelistVO, null));
  }

  /**
   * Creates the success 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createExceptionCloneVersionDuplicatedTest() throws EEAException {
    codelist.setItems(null);
    codelist.setId(1L);
    codelist.setVersion("1L");
    Codelist codelistOld = codelist;
    codelistOld.setCategory(new CodelistCategory());
    codelistOld.setVersion("1L");
    codelistOld.setStatus(CodelistStatusEnum.DESIGN);
    codelistOld.setDescription("Desc");
    codelistOld.setName("name");
    when(codelistMapper.classToEntity((Mockito.any()))).thenReturn(codelist);
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.of(codelistOld));
    when(codelistRepository.findAllByNameAndVersion(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(codelists));
    when(codelistMapper.entityListToClass(Mockito.any())).thenReturn(codelistsVO);
    try {
      codelistServiceImpl.create(codelistVO, 1L);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_VERSION_DUPLICATED, e.getMessage());
    }
  }

  /**
   * Creates the success 4 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createSuccessCloneNotDuplicatedTest() throws EEAException {
    codelist.setItems(items);
    codelist.setId(1L);
    codelist.setVersion(null);
    Codelist codelistOld = new Codelist();
    codelistOld.setId(1L);
    codelistOld.setVersion("1L");
    codelistOld.setItems(items);
    when(codelistMapper.classToEntity((Mockito.any()))).thenReturn(codelist);
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.of(codelistOld));
    when(codelistRepository.findAllByNameAndVersion(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());
    when(codelistRepository.save((Mockito.any()))).thenReturn(codelist);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.create(codelistVO, 1L));
  }

  /**
   * Update exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateExceptionNotFoundTest() throws EEAException {
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.empty());
    try {
      codelistServiceImpl.update(codelistVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Update exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateExceptionVersionDuplicatedTest() throws EEAException {
    codelistVO.setCategory(new CodelistCategoryVO());
    codelistVO.setVersion("1L");
    codelistVO.setStatus(CodelistStatusEnum.DESIGN);
    codelistVO.setDescription("Desc");
    codelistVO.setName("name");
    codelistVO.setItems(codelistItemsVO);
    codelist.setVersion("1L");
    codelist.setStatus(CodelistStatusEnum.DESIGN);
    codelistVO.setStatus(CodelistStatusEnum.READY);
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.of(codelist));
    when(codelistRepository.findAllByNameAndVersion(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(codelists));
    when(codelistMapper.entityListToClass(Mockito.any())).thenReturn(codelistsVO);
    try {
      codelistServiceImpl.update(codelistVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_VERSION_DUPLICATED, e.getMessage());
    }
  }

  /**
   * Update success 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateSuccessReadyToDeprecatedTest() throws EEAException {
    codelist.setItems(items);
    codelist.setStatus(CodelistStatusEnum.READY);
    codelistVO.setStatus(CodelistStatusEnum.DEPRECATED);
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.of(codelist));
    when(codelistRepository.save((Mockito.any()))).thenReturn(codelist);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.update(codelistVO));
  }

  /**
   * Update success 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateSuccessDesignToDeprecatedTest() throws EEAException {
    codelist.setItems(items);
    codelist.setStatus(CodelistStatusEnum.DESIGN);
    codelistVO.setStatus(CodelistStatusEnum.DEPRECATED);
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.of(codelist));
    when(codelistRepository.save((Mockito.any()))).thenReturn(codelist);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.update(codelistVO));
  }


  /**
   * Update success 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateSuccessWithItemsTest() throws EEAException {
    codelist.setItems(items);
    codelist.setStatus(CodelistStatusEnum.READY);
    codelistVO.setStatus(CodelistStatusEnum.READY);
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.of(codelist));
    when(codelistRepository.save((Mockito.any()))).thenReturn(codelist);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.update(codelistVO));
  }

  /**
   * Update success 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateSuccessDeprecatedToReadyTest() throws EEAException {
    codelist.setItems(items);
    codelist.setStatus(CodelistStatusEnum.DEPRECATED);
    codelistVO.setStatus(CodelistStatusEnum.READY);
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.of(codelist));
    when(codelistRepository.save((Mockito.any()))).thenReturn(codelist);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.update(codelistVO));
  }

  /**
   * Update success 4 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateSuccessNoChangesTest() throws EEAException {
    codelist.setItems(items);
    codelist.setStatus(CodelistStatusEnum.DEPRECATED);
    codelistVO.setStatus(CodelistStatusEnum.DEPRECATED);
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.of(codelist));
    when(codelistRepository.save((Mockito.any()))).thenReturn(codelist);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.update(codelistVO));
  }

  /**
   * Update success 5 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateSuccessToReadyTest() throws EEAException {
    codelist.setItems(items);
    codelist.setStatus(CodelistStatusEnum.DESIGN);
    codelistVO.setStatus(CodelistStatusEnum.READY);
    codelistVO.setVersion("1L");
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.of(codelist));
    when(codelistRepository.save((Mockito.any()))).thenReturn(codelist);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.update(codelistVO));
  }

  /**
   * Update success 7 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateSuccessDesignFullFieldsTest() throws EEAException {
    codelistVO.setCategory(new CodelistCategoryVO());
    codelistVO.setVersion("1L");
    codelistVO.setStatus(CodelistStatusEnum.DESIGN);
    codelistVO.setDescription("Desc");
    codelistVO.setName("name");
    codelistVO.setItems(codelistItemsVO);
    codelist.setVersion("1L");
    codelist.setStatus(CodelistStatusEnum.DESIGN);
    codelistVO.setStatus(CodelistStatusEnum.DESIGN);
    when(codelistRepository.findById((Mockito.any()))).thenReturn(Optional.of(codelist));
    when(codelistRepository.save((Mockito.any()))).thenReturn(codelist);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.update(codelistVO));
  }

  /**
   * Gets the category by id exception test.
   *
   * @return the category by id exception test
   */
  @Test
  public void getCategoryByIdExceptionTest() {
    when(codelistCategoryRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    try {
      codelistServiceImpl.getCategoryById(1L);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Gets the category by id success test.
   *
   * @return the category by id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getCategoryByIdSuccessTest() throws EEAException {
    when(codelistCategoryRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(codelistCategory));
    when(codelistCategoryMapper.entityToClass(Mockito.any())).thenReturn(codelistCategoryVO);
    assertEquals("not equal", codelistCategoryVO, codelistServiceImpl.getCategoryById(1L));
  }

  /**
   * Creates the category exception test.
   */
  @Test
  public void createCategoryExceptionTest() {
    try {
      codelistServiceImpl.createCategory(null);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Creates the category success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createCategorySuccessTest() throws EEAException {
    when(codelistCategoryMapper.classToEntity(Mockito.any())).thenReturn(codelistCategory);
    when(codelistCategoryRepository.save(Mockito.any())).thenReturn(codelistCategory);
    assertEquals("not equal", (Long) 1L, codelistServiceImpl.createCategory(codelistCategoryVO));
  }

  /**
   * Update category exception 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateCategoryExceptionNotFoundTest() throws EEAException {
    when(codelistCategoryRepository.findById((Mockito.any()))).thenReturn(Optional.empty());
    try {
      codelistServiceImpl.updateCategory(codelistCategoryVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Update category success 1 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateCategorySuccessNoEntryTest() throws EEAException {
    when(codelistCategoryRepository.findById((Mockito.any())))
        .thenReturn(Optional.of(codelistCategory));
    when(codelistCategoryRepository.save((Mockito.any()))).thenReturn(codelistCategory);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.updateCategory(codelistCategoryVO));
  }

  /**
   * Update category success 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateCategorySuccessWithDataTest() throws EEAException {
    codelistCategory.setShortCode("short");
    codelistCategory.setDescription("desc");
    when(codelistCategoryRepository.findById((Mockito.any())))
        .thenReturn(Optional.of(codelistCategory));
    when(codelistCategoryRepository.save((Mockito.any()))).thenReturn(codelistCategory);
    Assert.assertEquals((Long) 1L, codelistServiceImpl.updateCategory(codelistCategoryVO));
  }

  /**
   * Delete category success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteCategorySuccessTest() throws EEAException {
    codelistServiceImpl.deleteCategory(1L);
    Mockito.verify(codelistCategoryRepository, times(1)).deleteById(Mockito.any());
  }

  /**
   * Gets the all by ids exception test.
   *
   * @return the all by ids exception test
   */
  @Test
  public void getAllByIdsExceptionTest() {
    List<Long> ids = new ArrayList<>();
    ids.add(1L);
    when(codelistRepository.findAllByIdIn(Mockito.any())).thenReturn(Optional.empty());
    try {
      codelistServiceImpl.getAllByIds(ids);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Gets the by id success test.
   *
   * @return the by id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllByIdsSuccessTest() throws EEAException {
    List<Long> ids = new ArrayList<>();
    ids.add(1L);
    when(codelistRepository.findAllByIdIn(Mockito.any())).thenReturn(Optional.of(codelists));
    when(codelistMapper.entityListToClass(Mockito.any())).thenReturn(codelistsVO);
    assertEquals("not equal", codelistsVO, codelistServiceImpl.getAllByIds(ids));
  }

  /**
   * Gets the all categories success test.
   *
   * @return the all categories success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllCategoriesSuccessTest() throws EEAException {
    when(codelistCategoryRepository.findAll()).thenReturn(new ArrayList<CodelistCategory>());
    when(codelistCategoryMapper.entityListToClass(Mockito.any())).thenReturn(codelistCategoriesVO);
    when(codelistRepository.findAllByCategory_Id(Mockito.any())).thenReturn(Optional.empty());
    assertEquals("not equal", codelistCategoriesVO, codelistServiceImpl.getAllCategories());
  }

  /**
   * Gets the all by category id success test.
   *
   * @return the all by category id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllByCategoryIdSuccessTest() throws EEAException {
    when(codelistRepository.findAllByCategory_Id(Mockito.any())).thenReturn(Optional.of(codelists));
    assertEquals("not equal", new ArrayList<CodelistVO>(),
        codelistServiceImpl.getAllByCategoryId(1L));
  }

  /**
   * Gets the all categories complete success test.
   *
   * @return the all categories complete success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getAllCategoriesCompleteSuccessTest() throws EEAException {
    when(codelistCategoryRepository.findAll()).thenReturn(new ArrayList<CodelistCategory>());
    List<CodelistCategoryFullVO> codelistCategoriesFullVO = new ArrayList<>();
    when(codelistCategoryFullMapper.entityListToClass(Mockito.any()))
        .thenReturn(codelistCategoriesFullVO);
    assertEquals("not equal", codelistCategoriesFullVO,
        codelistServiceImpl.getAllCategoriesComplete());
  }

}
