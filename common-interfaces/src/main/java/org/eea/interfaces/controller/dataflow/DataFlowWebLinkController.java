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
  @GetMapping(value = "{idDataflow}/weblink/")
  List<WeblinkVO> getLink(@PathVariable("idDataflow") Long idDataflow);

  /**
   * Save link.
   *
   * @param idDataflow the id dataflow
   * @param url the url
   * @param description the description
   */
  @PostMapping(value = "{idDataflow}/weblink/save")
  void saveLink(@PathVariable("idDataflow") Long idDataflow,
      @RequestParam(value = "url") String url,
      @RequestParam(value = "description") String description);

  /**
   * Removes the link.
   *
   * @param idLink the id link
   */
  @DeleteMapping(value = "{idDataflow}/weblink/remove")
  void removeLink(@RequestParam(value = "idLink") Long idLink);

  /**
   * Update link.
   *
   * @param idLink the id link
   * @param url the url
   * @param description the description
   */
  @PutMapping(value = "{idDataflow}/weblink/update")
  void updateLink(@RequestParam(value = "idLink") Long idLink,
      @RequestParam(value = "url") String url,
      @RequestParam(value = "description") String description);
}
