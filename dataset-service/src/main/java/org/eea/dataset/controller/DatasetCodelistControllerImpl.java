package org.eea.dataset.controller;

import org.eea.dataset.service.CodelistService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetCodelistController;
import org.eea.interfaces.vo.dataset.CodelistVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
@RequestMapping("/codelist")
public class DatasetCodelistControllerImpl implements DatasetCodelistController {

  @Autowired
  private CodelistService codelistService;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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

  @Override
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

  @Override
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

  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{codelistId}")
  public void deleteDocument(Long codelistId) {
    if (codelistId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CODELIST_NOT_FOUND);
    }
    codelistService.delete(codelistId);
  }

}
