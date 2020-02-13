package org.eea.interfaces.controller.validation;

import org.springframework.cloud.openfeign.FeignClient;

public interface RulesController {

  @FeignClient(value = "validation", contextId = "rules", path = "/rules")
  interface RulesControllerZuul extends RulesController {

  }

}
