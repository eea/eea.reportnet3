package org.eea.rod.controller;

import java.util.List;
import org.eea.interfaces.controller.rod.CountryController;
import org.eea.interfaces.vo.rod.CountryVO;
import org.eea.rod.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Country controller.
 */
@RestController
@RequestMapping("/obligation_country")
public class CountryControllerImpl implements CountryController {

  @Autowired
  private CountryService countryService;


  @Override
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public List<CountryVO> findAll() {
    return countryService.findAll();
  }
}
