package org.eea.interfaces.controller.dataset;

import java.util.List;

import org.eea.interfaces.vo.dataset.schemas.WebformConfigVO;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


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

  /**
   * Insert webform config.
   *
   * @param webformConfig the webform config
   */
  @PostMapping("/webformConfig")
  void insertWebformConfig(@RequestBody WebformConfigVO webformConfig);

  /***
   * Upload a webform config
   *
   * @param webformConfig The webform config
   * @param datasetId The dataset id
   * @param dataflowId The dataflow id
   * @param providerId The provider id
   * @return A response entity
   */
  @PostMapping("/{datasetId}/webformConfig")
  ResponseEntity<?> uploadWebformConfig(@RequestBody WebformConfigVO webformConfig,
                                        @PathVariable("datasetId") Long datasetId,
                                        @RequestParam(value = "dataflowId", required = false) Long dataflowId,
                                        @RequestParam(value = "providerId", required = false) Long providerId
                                        );

  /**
   * Update webform config.
   *
   * @param webformConfig the webform config
   */
  @PutMapping("/webformConfig")
  void updateWebformConfig(@RequestBody WebformConfigVO webformConfig);

  /**
   * Find webform config by id.
   *
   * @param id the id
   * @return the string
   */
  @GetMapping("/webformConfig/{id}")
  String findWebformConfigById(@PathVariable("id") Long id);

  /**
   * Delete webform config.
   *
   * @param id the id
   */
  @DeleteMapping("/webformConfig/{id}")
  void deleteWebformConfig(@PathVariable("id") Long id);
}
