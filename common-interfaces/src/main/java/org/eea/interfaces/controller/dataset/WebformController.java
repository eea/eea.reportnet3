package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.schemas.WebformConfigVO;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



/**
 * The Interface WebformController.
 */
public interface WebformController {


  /**
   * The Interface WebformControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "webform", path = "/webform")
  interface WebformControllerZuul extends WebformController {

  }

  /**
   * Gets the webforms list.
   *
   * @return the webforms list
   */
  @GetMapping("/listAll")
  List<WebformMetabaseVO> getListWebforms();

  @PostMapping("/webformConfig")
  void insertWebformConfig(@RequestBody WebformConfigVO webformConfig);

  @GetMapping("/webformConfig/{id}")
  String findWebformConfigById(@PathVariable("id") Long id);

}
