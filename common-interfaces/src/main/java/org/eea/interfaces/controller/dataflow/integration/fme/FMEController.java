package org.eea.interfaces.controller.dataflow.integration.fme;

import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface FMEController.
 */
public interface FMEController {


  /**
   * The Interface FMEControllerZuul.
   */
  @FeignClient(value = "dataflow", contextId = "fme", path = "/fme")
  interface FMEControllerZuul extends FMEController {

  }

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
   * @param repository the repository
   * @return the collection VO
   */
  @GetMapping(value = "/findItems", produces = MediaType.APPLICATION_JSON_VALUE)
  FMECollectionVO findItems(@RequestParam("repository") String repository);


}
