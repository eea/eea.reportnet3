package org.eea.dataset.controller;

import org.eea.dataset.service.CodelistService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetCodelistController;
import org.eea.interfaces.vo.dataset.CodelistCategoryVO;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class DatasetCodelistControllerImpl.
 */
@RestController
@RequestMapping("/codelist")
public class DatasetCodelistControllerImpl implements DatasetCodelistController {

  /** The codelist service. */
  @Autowired
  private CodelistService codelistService;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Gets the by id.
   *
   * @param codelistId the codelist id
   * @return the by id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{codelistId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CodelistVO getById(Long codelistId) {
    if (codelistId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    CodelistVO codelistVO = new CodelistVO();
    try {
      codelistVO = codelistService.getById(codelistId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the codelist. Error message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.CODELIST_NOT_FOUND,
          e);
    }
    return codelistVO;
  }

  /**
   * Creates the.
   *
   * @param codelistVO the codelist VO
   * @return the long
   */
  @Override
  @HystrixCommand
  @PostMapping
  public Long create(CodelistVO codelistVO) {
    if (codelistVO == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    Long response;
    try {
      response = codelistService.create(codelistVO, null);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    return response;
  }

  /**
   * Update.
   *
   * @param codelistVO the codelist VO
   * @return the long
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/update")
  public Long update(CodelistVO codelistVO) {
    if (codelistVO == null || codelistVO.getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    Long response;
    try {
      response = codelistService.update(codelistVO);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    return response;
  }

  /**
   * Clone.
   *
   * @param codelistId the codelist id
   * @param codelistVO the codelist VO
   * @return the long
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/clone/{codelistId}")
  public Long clone(Long codelistId, CodelistVO codelistVO) {
    if (codelistVO == null || codelistId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    Long response;
    try {
      response = codelistService.create(codelistVO, codelistId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    return response;
  }

  /**
   * Delete codelist.
   *
   * @param codelistId the codelist id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{codelistId}")
  public void delete(Long codelistId) {
    if (codelistId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    codelistService.delete(codelistId);
  }

  /**
   * Gets the category by id.
   *
   * @param codelistCategoryId the codelist category id
   * @return the category by id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/category/{codelistCategoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CodelistCategoryVO getCategoryById(Long codelistCategoryId) {
    if (codelistCategoryId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND);
    }
    CodelistCategoryVO codelistCategoryVO = new CodelistCategoryVO();
    try {
      codelistCategoryVO = codelistService.getCategoryById(codelistCategoryId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the codelist category. Error message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e);
    }
    return codelistCategoryVO;
  }

  /**
   * Creates the category.
   *
   * @param codelistCategoryVO the codelist category VO
   * @return the long
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/category")
  public Long createCategory(CodelistCategoryVO codelistCategoryVO) {
    if (codelistCategoryVO == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND);
    }
    Long response;
    try {
      response = codelistService.createCategory(codelistCategoryVO);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND);
    }
    return response;
  }

  /**
   * Update category.
   *
   * @param codelistCategoryVO the codelist category VO
   * @return the long
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/category/update")
  public Long updateCategory(CodelistCategoryVO codelistCategoryVO) {
    if (codelistCategoryVO == null || codelistCategoryVO.getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND);
    }
    Long response;
    try {
      response = codelistService.updateCategory(codelistCategoryVO);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND);
    }
    return response;
  }

  /**
   * Delete category.
   *
   * @param codelistCategoryId the codelist category id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/category/{codelistCategoryId}")
  public void deleteCategory(Long codelistCategoryId) {
    if (codelistCategoryId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND);
    }
    codelistService.deleteCategory(codelistCategoryId);
  }

}
