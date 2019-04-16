package org.eea.validation.service;


import org.eea.validation.model.Element;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {
  
  
  private KieContainer kieContainer;
 
  @Autowired
  public ValidationService(KieContainer kieContainer) {
      this.kieContainer = kieContainer;
  }

  public Element getElementLenght(Element element) {
      //get the stateful session
      KieSession kieSession = kieContainer.newKieSession("rulesSession");
      kieSession.insert(element);
      kieSession.fireAllRules();
      kieSession.dispose();
      return element;
  }

}
