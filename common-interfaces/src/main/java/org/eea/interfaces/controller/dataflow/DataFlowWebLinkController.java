package org.eea.interfaces.controller.dataflow;

import java.util.List;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface DataFlowWebLinkController.
 */
public interface DataFlowWebLinkController {


  /**
   * The Interface DataFlowWebLinkControllerZuul.
   */
  @FeignClient(value = "weblink", path = "/weblink")
  interface DataFlowWebLinkControllerZuul extends DataFlowWebLinkController {
  }

  /**
   * Gets the link.
   *
   * @param idDataflow the id dataflow
   * @return the link
   */
  @GetMapping(value = "{idLink}")
  WeblinkVO getLink(@RequestParam("idLink") Long idLink);

  /**
   * Save link.
   *
   * @param idDataflow the id dataflow
   * @param url the url
   * @param description the description
   */
  @PutMapping
  void saveLink(@RequestParam("idDataflow") Long idDataflow,
      @RequestParam(value = "url") String url,
      @RequestParam(value = "description") String description);

  /**
   * Removes the link.
   *
   * @param idLink the id link
   */
  @DeleteMapping(value = "{idLink}")
  void removeLink(@RequestParam(value = "idLink") Long idLink);

  /**
   * Update link.
   *
   * @param idLink the id link
   * @param url the url
   * @param description the description
   */
  @PostMapping(value = "{idLink}")
  void updateLink(@RequestParam(value = "idLink") Long idLink,
      @RequestParam(value = "url") String url,
      @RequestParam(value = "description") String description);
}

