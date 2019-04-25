package org.eea.validation.controller;

import org.eea.validation.model.Element;
import org.eea.validation.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/validation")
public class ValidationServiceController {

  
  private final ValidationService validationService;
  
  @Autowired
  public ValidationServiceController(ValidationService validationService) {
      this.validationService = validationService;
  }
  
  

  @RequestMapping(value = "/getLenght", method = RequestMethod.GET, produces = "application/json")
  public Element getQuestions(@RequestParam(required = true) String type) {

      Element element = new Element();
      element.setType(type);
      validationService.getElementLenght(element);
      return element;
  }
  
}