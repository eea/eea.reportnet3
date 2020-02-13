package org.eea.validation.controller;

import org.eea.interfaces.controller.validation.RulesController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/rules")
public class RulesControllerImpl implements RulesController {

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

}
