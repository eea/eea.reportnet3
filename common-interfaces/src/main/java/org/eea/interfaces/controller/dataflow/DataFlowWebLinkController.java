package org.eea.interfaces.controller.dataflow;


import java.util.List;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The Interface DataFlowWebLinkController.
 */
public interface DataFlowWebLinkController {


  /**
   * The Interface DataFlowWebLinkControllerZuul.
   */
  @FeignClient(value = "weblink", contextId = "weblink", path = "/weblink")
  interface DataFlowWebLinkControllerZuul extends DataFlowWebLinkController {

  }

  /**
   * Gets the link.
   *
   * @param idLink the id link
   * @return the link
   */
  @GetMapping(value = "/private/{idLink}")
  WeblinkVO getLink(@PathVariable("idLink") Long idLink);

  /**
   * Save link.
   *
   * @param dataflowId the dataflow id
   * @param weblinkVO the weblink VO
   */
  @PostMapping(value = "/v1/dataflow/{dataflowId}")
  void saveLink(@PathVariable(value = "dataflowId") Long dataflowId,
      @RequestBody WeblinkVO weblinkVO);

  /**
   * Save link legacy.
   *
   * @param dataflowId the dataflow id
   * @param weblinkVO the weblink VO
   */
  @PostMapping(value = "/dataflow/{dataflowId}")
  void saveLinkLegacy(@PathVariable(value = "dataflowId") Long dataflowId,
      @RequestBody WeblinkVO weblinkVO);

  /**
   * Removes the link.
   *
   * @param idLink the id link
   * @param dataflowId the dataflow id
   */
  @DeleteMapping(value = "/{idLink}/dataflow/{dataflowId}")
  void removeLink(@PathVariable(value = "idLink") Long idLink,
      @PathVariable(value = "dataflowId") Long dataflowId);

  /**
   * Removes the link legacy.
   *
   * @param idLink the id link
   * @param dataflowId the dataflow id
   */
  @DeleteMapping(value = "/{idLink}/dataflow/{dataflowId}")
  void removeLinkLegacy(@PathVariable(value = "idLink") Long idLink,
      @PathVariable(value = "dataflowId") Long dataflowId);

  /**
   * Update link.
   *
   * @param weblinkVO the weblink VO
   * @param dataflowId the dataflow id
   */
  @PutMapping(value = "/v1/dataflow/{dataflowId}")
  void updateLink(@RequestBody WeblinkVO weblinkVO,
      @PathVariable(value = "dataflowId") Long dataflowId);

  /**
   * Update link legacy.
   *
   * @param weblinkVO the weblink VO
   * @param dataflowId the dataflow id
   */
  @PutMapping(value = "/dataflow/{dataflowId}")
  void updateLinkLegacy(@RequestBody WeblinkVO weblinkVO,
      @PathVariable(value = "dataflowId") Long dataflowId);

  /**
   * Gets the all weblinks by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the all weblinks by dataflow
   */
  @GetMapping(value = "/v1/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<WeblinkVO> getAllWeblinksByDataflow(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Gets the all weblinks by dataflow legacy.
   *
   * @param dataflowId the dataflow id
   * @return the all weblinks by dataflow legacy
   */
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<WeblinkVO> getAllWeblinksByDataflowLegacy(@PathVariable("dataflowId") Long dataflowId);

}

