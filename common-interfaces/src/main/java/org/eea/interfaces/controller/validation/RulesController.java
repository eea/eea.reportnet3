package org.eea.interfaces.controller.validation;

import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface RulesController {

  @FeignClient(value = "validation", contextId = "rules", path = "/rules")
  interface RulesControllerZuul extends RulesController {

  }

  /**
   * Find rule schema by dataset id.
   *
   * @param datasetId the dataset id
   * @return the rules schema VO
   */
  @GetMapping(value = "/{idDatasetSchema}/rules", produces = MediaType.APPLICATION_JSON_VALUE)
  RulesSchemaVO findRuleSchemaByDatasetId(@PathVariable("idDatasetSchema") String idDatasetSchema);

}
