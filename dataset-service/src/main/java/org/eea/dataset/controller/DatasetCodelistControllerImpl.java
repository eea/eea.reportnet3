package org.eea.dataset.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.CodelistService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetCodelistController;
import org.eea.interfaces.vo.dataset.CodelistCategoryFullVO;
import org.eea.interfaces.vo.dataset.CodelistCategoryVO;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
    CodelistVO codelistVO = null;
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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public Long create(CodelistVO codelistVO) {
    if (codelistVO == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    Long response;
    try {
      response = codelistService.create(codelistVO, null);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      if (EEAErrorMessage.CODELIST_VERSION_DUPLICATED.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.CODELIST_VERSION_DUPLICATED, e);
      }
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.CODELIST_NOT_FOUND);
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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public Long update(CodelistVO codelistVO) {
    if (codelistVO == null || codelistVO.getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    Long response;
    try {
      response = codelistService.update(codelistVO);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      if (EEAErrorMessage.CODELIST_VERSION_DUPLICATED.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.CODELIST_VERSION_DUPLICATED, e);
      }
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.CODELIST_NOT_FOUND);
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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public Long clone(Long codelistId, CodelistVO codelistVO) {
    if (codelistVO == null || codelistId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    Long response;
    try {
      response = codelistService.create(codelistVO, codelistId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      if (EEAErrorMessage.CODELIST_VERSION_DUPLICATED.equals(e.getMessage())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.CODELIST_VERSION_DUPLICATED, e);
      }
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.CODELIST_NOT_FOUND);
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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
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
    CodelistCategoryVO codelistCategoryVO = null;
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
   * Gets the all categories.
   *
   * @return the all categories
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/category/all", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<CodelistCategoryVO> getAllCategories() {
    List<CodelistCategoryVO> codelistCategoryVOs = null;
    try {
      codelistCategoryVOs = codelistService.getAllCategories();
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the codelist category. Error message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e);
    }
    return codelistCategoryVOs;
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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
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
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
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
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public void deleteCategory(Long codelistCategoryId) {
    if (codelistCategoryId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND);
    }
    codelistService.deleteCategory(codelistCategoryId);
  }

  /**
   * Gets the all by id.
   *
   * @param codelistIds the codelist ids
   * @return the all by id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/find", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<CodelistVO> getAllById(String codelistIds) {
    if (StringUtils.isBlank(codelistIds)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    Set<Long> codelistIdsSet = new HashSet<>();
    for (String item : codelistIds.split(",")) {
      codelistIdsSet.add(Long.valueOf(item));
    }
    List<CodelistVO> codelistVOs = null;
    try {
      codelistVOs =
          codelistService.getAllByIds(codelistIdsSet.stream().collect(Collectors.toList()));
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the codelists. Error message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.CODELIST_NOT_FOUND,
          e);
    }
    return codelistVOs;
  }

  /**
   * Gets the all by category id.
   *
   * @param codelistCategoryId the codelist category id
   * @return the all by category id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/find/category/{codelistCategoryId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public List<CodelistVO> getAllByCategoryId(Long codelistCategoryId) {
    if (codelistCategoryId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    List<CodelistVO> codelistVOs = null;
    try {
      codelistVOs = codelistService.getAllByCategoryId(codelistCategoryId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the codelists. Error message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.CODELIST_NOT_FOUND,
          e);
    }
    return codelistVOs;
  }

  @Override
  @HystrixCommand
  @GetMapping(value = "/category/complete", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<CodelistCategoryFullVO> getAllCategoriesComplete() {
    List<CodelistCategoryFullVO> codelistCategoryFullVOs = null;
    try {
      codelistCategoryFullVOs = codelistService.getAllCategoriesComplete();
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the codelist category list. Error message: {}", e.getMessage(),
          e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.CODELIST_CATEGORY_NOT_FOUND, e);
    }
    return codelistCategoryFullVOs;
  }

}
