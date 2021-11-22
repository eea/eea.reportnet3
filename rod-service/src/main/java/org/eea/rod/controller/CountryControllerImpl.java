package org.eea.rod.controller;

import java.util.List;
import org.eea.interfaces.controller.rod.CountryController;
import org.eea.interfaces.vo.rod.CountryVO;
import org.eea.rod.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The type Country controller.
 */
@RestController
@ApiIgnore
@RequestMapping("/obligation_country")
public class CountryControllerImpl implements CountryController {

  /** The country service. */
  @Autowired
  private CountryService countryService;

  /**
   * Find all.
   *
   * @return the list
   */
  @Override
  @GetMapping(value = "/")
  @ApiOperation(value = "Gets a list with all the obligation countries", response = CountryVO.class,
      responseContainer = "List", hidden = true)
  public List<CountryVO> findAll() {
    return countryService.findAll();
  }
}
