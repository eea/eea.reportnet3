package org.eea.interfaces.producer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface ProducerController.
 */
@FeignClient(value = "serviceProvider")
public interface ProducerController {

  /**
   * Produce text.
   *
   * @param extraTest the extra test
   * @return the string
   */
  @GetMapping("/produce")
  String produceText(@RequestParam("extraText") String extraTest);
}
