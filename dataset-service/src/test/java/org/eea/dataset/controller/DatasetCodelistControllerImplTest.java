package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.eea.dataset.service.CodelistService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.CodelistCategoryVO;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DatasetCodelistControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetCodelistControllerImplTest {

  /** The dataset codelist controller impl. */
  @InjectMocks
  DatasetCodelistControllerImpl datasetCodelistControllerImpl;

  /** The codelist service. */
  @Mock
  private CodelistService codelistService;

  /** The codelist VO. */
  private CodelistVO codelistVO;

  /** The codelist category VO. */
  private CodelistCategoryVO codelistCategoryVO;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    codelistVO = new CodelistVO();
    codelistCategoryVO = new CodelistCategoryVO();
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the by id exception 1 test.
   *
   * @return the by id exception 1 test
   */
  @Test
  public void getByIdException1Test() {
    try {
      datasetCodelistControllerImpl.getById(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Gets the by id exception 2 test.
   *
   * @return the by id exception 2 test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getByIdException2Test() throws EEAException {
    when(codelistService.getById(Mockito.any())).thenThrow(EEAException.class);
    try {
      datasetCodelistControllerImpl.getById(1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
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
    when(codelistService.getById(Mockito.any())).thenReturn(codelistVO);
    assertEquals("not equal", codelistVO, datasetCodelistControllerImpl.getById(1L));
  }

  /**
   * Creates the exception 1 test.
   */
  @Test
  public void createException1Test() {
    try {
      datasetCodelistControllerImpl.create(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Creates the exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createException2Test() throws EEAException {
    when(codelistService.create(Mockito.any(), Mockito.any())).thenThrow(EEAException.class);
    try {
      datasetCodelistControllerImpl.create(codelistVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
  }

  /**
   * Creates the success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createSuccessTest() throws EEAException {
    when(codelistService.create(Mockito.any(), Mockito.any())).thenReturn(1L);
    assertEquals((Long) 1L, datasetCodelistControllerImpl.create(codelistVO));
  }

  /**
   * Update exception 1 test.
   */
  @Test
  public void updateException1Test() {
    try {
      datasetCodelistControllerImpl.update(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Update exception 2 test.
   */
  @Test
  public void updateException2Test() {
    try {
      datasetCodelistControllerImpl.update(codelistVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Update exception 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateException3Test() throws EEAException {
    codelistVO.setId(1L);
    when(codelistService.update(Mockito.any())).thenThrow(EEAException.class);
    try {
      datasetCodelistControllerImpl.update(codelistVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
  }

  /**
   * Update success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateSuccessTest() throws EEAException {
    codelistVO.setId(1L);
    when(codelistService.update(Mockito.any())).thenReturn(1L);
    assertEquals((Long) 1L, datasetCodelistControllerImpl.update(codelistVO));
  }

  /**
   * Clone exception 1 test.
   */
  @Test
  public void cloneException1Test() {
    try {
      datasetCodelistControllerImpl.clone(null, null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Clone exception 2 test.
   */
  @Test
  public void cloneException2Test() {
    try {
      datasetCodelistControllerImpl.clone(null, codelistVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Clone exception 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void cloneException3Test() throws EEAException {
    codelistVO.setId(1L);
    when(codelistService.create(Mockito.any(), Mockito.any())).thenThrow(EEAException.class);
    try {
      datasetCodelistControllerImpl.clone(1L, codelistVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
  }

  /**
   * Clone success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void cloneSuccessTest() throws EEAException {
    codelistVO.setId(1L);
    when(codelistService.create(Mockito.any(), Mockito.any())).thenReturn(1L);
    assertEquals((Long) 1L, datasetCodelistControllerImpl.clone(1L, codelistVO));
  }

  /**
   * Delete exception test.
   */
  @Test
  public void deleteExceptionTest() {
    try {
      datasetCodelistControllerImpl.delete(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Delete success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteSuccessTest() throws EEAException {
    codelistVO.setId(1L);
    datasetCodelistControllerImpl.delete(1L);
    Mockito.verify(codelistService, times(1)).delete(Mockito.any());
  }

  /**
   * Gets the category by id exception 1 test.
   *
   * @return the category by id exception 1 test
   */
  @Test
  public void getCategoryByIdException1Test() {
    try {
      datasetCodelistControllerImpl.getCategoryById(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Gets the category by id exception 2 test.
   *
   * @return the category by id exception 2 test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getCategoryByIdException2Test() throws EEAException {
    when(codelistService.getCategoryById(Mockito.any())).thenThrow(EEAException.class);
    try {
      datasetCodelistControllerImpl.getCategoryById(1L);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
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
    when(codelistService.getCategoryById(Mockito.any())).thenReturn(codelistCategoryVO);
    assertEquals("not equal", codelistCategoryVO,
        datasetCodelistControllerImpl.getCategoryById(1L));
  }

  /**
   * Creates the category exception 1 test.
   */
  @Test
  public void createCategoryException1Test() {
    try {
      datasetCodelistControllerImpl.createCategory(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Creates the category exception 2 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createCategoryException2Test() throws EEAException {
    when(codelistService.createCategory(Mockito.any())).thenThrow(EEAException.class);
    try {
      datasetCodelistControllerImpl.createCategory(codelistCategoryVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
  }

  /**
   * Creates the category success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createCategorySuccessTest() throws EEAException {
    when(codelistService.createCategory(Mockito.any())).thenReturn(1L);
    assertEquals((Long) 1L, datasetCodelistControllerImpl.createCategory(codelistCategoryVO));
  }

  /**
   * Update category exception 1 test.
   */
  @Test
  public void updateCategoryException1Test() {
    try {
      datasetCodelistControllerImpl.updateCategory(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Update category exception 2 test.
   */
  @Test
  public void updateCategoryException2Test() {
    try {
      datasetCodelistControllerImpl.updateCategory(codelistCategoryVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Update category exception 3 test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateCategoryException3Test() throws EEAException {
    codelistCategoryVO.setId(1L);
    when(codelistService.updateCategory(Mockito.any())).thenThrow(EEAException.class);
    try {
      datasetCodelistControllerImpl.updateCategory(codelistCategoryVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }
  }

  /**
   * Update category success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateCategorySuccessTest() throws EEAException {
    codelistCategoryVO.setId(1L);
    when(codelistService.updateCategory(Mockito.any())).thenReturn(1L);
    assertEquals((Long) 1L, datasetCodelistControllerImpl.updateCategory(codelistCategoryVO));
  }

  /**
   * Delete category exception test.
   */
  @Test
  public void deleteCategoryExceptionTest() {
    try {
      datasetCodelistControllerImpl.deleteCategory(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e.getReason());
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  /**
   * Delete category success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteCategorySuccessTest() throws EEAException {
    codelistCategoryVO.setId(1L);
    datasetCodelistControllerImpl.deleteCategory(1L);
    Mockito.verify(codelistService, times(1)).deleteCategory(Mockito.any());
  }
}
