package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.WebformService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.WebformController;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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


  /**
   * Gets the list webforms by dataset id.
   *
   * @param datasetId the dataset id
   * @return the list webforms by dataset id
   */
  @Override
  @HystrixCommand
  @GetMapping("/listAll")
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Gets a list with all the webforms", hidden = false)
  public List<WebformMetabaseVO> getListWebforms() {
    try {
      return webformService.getListWebforms();
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

}
