package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.WebformService;
import org.eea.interfaces.controller.dataset.WebformController;
import org.eea.interfaces.vo.dataset.schemas.WebformConfigVO;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiOperation;


/**
 * The Class WebformControllerImpl.
 */
@RestController
@RequestMapping("/webform")
public class WebformControllerImpl implements WebformController {


  /** The webform service. */
  @Autowired
  private WebformService webformService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


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
  @PostMapping("/private/webformConfig")
  @ApiOperation(value = "Insert webform config json into the system", hidden = true)
  public void insertWebformConfig(@RequestBody WebformConfigVO webformConfig) {
    webformService.insertWebformConfig(webformConfig.getIdReferenced(), webformConfig.getName(),
        webformConfig.getContent());
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
      LOG_ERROR.error("Error getting the json webconfig from the id {}", id);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
    return json;
  }

}
