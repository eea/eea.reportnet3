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
import org.springframework.web.bind.annotation.RequestParam;

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
  @GetMapping(value = "{idLink}")
  WeblinkVO getLink(@PathVariable("idLink") Long idLink);

  /**
   * Save link.
   *
   * @param idDataflow the id dataflow
   * @param weblinkVO the weblink VO
   */
  @PostMapping
  void saveLink(@RequestParam(value = "idDataFlow") Long idDataflow,
      @RequestBody WeblinkVO weblinkVO);

  /**
   * Removes the link.
   *
   * @param idLink the id link
   */
  @DeleteMapping(value = "/{idLink}")
  void removeLink(@PathVariable(value = "idLink") Long idLink);

  /**
   * Update link.
   *
   * @param weblinkVO the weblink VO
   */
  @PutMapping
  void updateLink(@RequestBody WeblinkVO weblinkVO);

  /**
   * Gets the all weblinks by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the all weblinks by dataflow
   */
  @GetMapping(value = "/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<WeblinkVO> getAllWeblinksByDataflow(@PathVariable("dataflowId") Long dataflowId);

}

