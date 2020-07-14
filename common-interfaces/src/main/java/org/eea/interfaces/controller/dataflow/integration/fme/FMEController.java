package org.eea.interfaces.controller.dataflow.integration.fme;

import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface FMEController.
 */
public interface FMEController {

  /**
   * Find repositories.
   *
   * @return the collection
   */
  @GetMapping(value = "/findRepositories", produces = MediaType.APPLICATION_JSON_VALUE)
  FMECollectionVO findRepositories();


  /**
   * Find items.
   *
   * @return the collection VO
   */
  @GetMapping(value = "/findItems", produces = MediaType.APPLICATION_JSON_VALUE)
  FMECollectionVO findItems(@RequestParam("repository") String repository);


}
