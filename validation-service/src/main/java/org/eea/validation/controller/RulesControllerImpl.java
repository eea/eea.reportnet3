package org.eea.validation.controller;

import org.apache.commons.lang3.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.validation.RulesController;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.validation.service.RulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/rules")
public class RulesControllerImpl implements RulesController {

  @Autowired
  @Qualifier("proxyDatasetService")
  private RulesService rulesService;

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Find rule schema by dataset id.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the rules schema VO
   */
  @Override
  @GetMapping(value = "/{idDatasetSchema}/rules", produces = MediaType.APPLICATION_JSON_VALUE)
  public RulesSchemaVO findRuleSchemaByDatasetId(String idDatasetSchema) {
    if (StringUtils.isBlank(idDatasetSchema)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    } else {
      return rulesService.getRulesSchemaByDatasetId(idDatasetSchema);
    }
  }
}
