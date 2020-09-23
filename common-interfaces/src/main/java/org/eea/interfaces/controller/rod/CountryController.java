package org.eea.interfaces.controller.rod;

import java.util.List;
import org.eea.interfaces.vo.rod.CountryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * The interface Country controller.
 */
public interface CountryController {

  /**
   * The interface Record store controller zull.
   */
  @FeignClient(value = "rodCountry", path = "/obligation_country")
  interface CountryControllerZull extends CountryController {

  }

  /**
   * Find all countries.
   *
   * @return the list
   */
  @GetMapping(value = "/")
  List<CountryVO> findAll();

}
