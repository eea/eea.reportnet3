package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.WebformService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.WebformController;
import org.eea.interfaces.vo.dataset.schemas.WebformConfigVO;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;


/**
 * The Class WebformControllerImpl.
 */
@RestController
@RequestMapping("/webform")
@ApiIgnore
public class WebformControllerImpl implements WebformController {


  /** The webform service. */
  @Autowired
  private WebformService webformService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(WebformControllerImpl.class);


  /**
   * Gets the webforms list
   *
   * @return the webforms list
   */
  @Override
  @HystrixCommand
  @GetMapping("/listAll")
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Gets a list with all the webforms", hidden = true)
  public List<WebformMetabaseVO> getListWebforms() {
    return webformService.getListWebforms();
  }


  /**
   * Insert webform config.
   *
   * @param webformConfig the webform config
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/webformConfig")
  @ApiOperation(value = "Insert webform config json into the system", hidden = true)
  public void insertWebformConfig(@RequestBody WebformConfigVO webformConfig) {
    try {
      LOG.info("Inserting webform config {} with type {}", webformConfig.getName(), webformConfig.getType());
      webformService.insertWebformConfig(webformConfig.getName(), webformConfig.getContent(),
          webformConfig.getType());
      LOG.info("Successfully inserted webform config {} with type {}", webformConfig.getName(), webformConfig.getType());
    } catch (EEAException e) {
      if(webformConfig != null){
        LOG_ERROR.error("Error when inserting webform config {} with type {}. Message: {}", webformConfig.getName(), webformConfig.getType(), e.getMessage());
      }
      else{
        LOG_ERROR.error("Error when inserting webform config because object is null. Message: {}", e.getMessage());
      }
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.NAME_DUPLICATED);
    }
  }


  /**
   * Update webform config.
   *
   * @param webformConfig the webform config
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/webformConfig")
  @ApiOperation(value = "Update webform config json into the system", hidden = true)
  public void updateWebformConfig(@RequestBody WebformConfigVO webformConfig) {
    try {
      LOG.info("Updating webform config {} with type {}", webformConfig.getName(), webformConfig.getType());
      webformService.updateWebformConfig(webformConfig.getIdReferenced(), webformConfig.getName(),
          webformConfig.getContent(), webformConfig.getType());
      LOG.info("Successfully updated webform config {} with type {}", webformConfig.getName(), webformConfig.getType());
    } catch (EEAException e) {
      if(webformConfig != null){
        LOG_ERROR.error("Error when updating webform config {} with type {}. Message: {}", webformConfig.getName(), webformConfig.getType(), e.getMessage());
      }
      else{
        LOG_ERROR.error("Error when updating webform config because object is null. Message: {}", e.getMessage());
      }
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.NAME_DUPLICATED);
    }
  }

  /**
   * Find webform config by id.
   *
   * @param id the id
   * @return the string
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/webformConfig/{id}")
  @ApiOperation(value = "Get the webform config json", hidden = true)
  public String findWebformConfigById(@PathVariable("id") Long id) {

    String json = "";
    try {
      json = webformService.findWebformConfigContentById(id);
    } catch (JsonProcessingException e) {
      LOG_ERROR.error("Error getting the json webconfig from the id {}", id, e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.OBTAINING_WEBFORM_CONFIG);
    }
    return json;
  }


  /**
   * Delete webform config.
   *
   * @param id the id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/webformConfig/{id}")
  @ApiOperation(value = "Delete webform config", hidden = true)
  public void deleteWebformConfig(@PathVariable("id") Long id) {
    try {
      LOG.info("Deleting webform config with id",id);
      webformService.deleteWebformConfig(id);
    } catch (EEAException e) {
      LOG_ERROR.error("Error when deleting webform config with id",id);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.ERROR_WEBFORM_IN_USE);
    }
  }

}
