package org.eea.validation.controller;

import org.bson.types.ObjectId;
import org.eea.interfaces.controller.validation.RulesController;
import org.eea.validation.service.RulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
@RequestMapping(value = "/rules")
public class RulesControllerImpl implements RulesController {

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Autowired
  private RulesService rulesService;


  @Override
  @HystrixCommand
  @PostMapping(value = "/createEmptyRulesSchema")
  // @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN') AND
  // hasRole('DATA_CUSTODIAN')")
  public void createEmptyRulesSchema(@RequestParam("idDataSetSchema") String idDataSetSchema,
      @RequestParam("idRulesSchema") String idRulesSchema) {

    rulesService.createEmptyRulesScehma(new ObjectId(idDataSetSchema), new ObjectId(idRulesSchema));

  }

}
