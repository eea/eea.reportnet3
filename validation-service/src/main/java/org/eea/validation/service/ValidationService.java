package org.eea.validation.service;


import org.eea.validation.model.Element;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {
   
  private KieBase kieBase;
   
  @Autowired
  public ValidationService (KieBase kieBase) {
    this.kieBase = kieBase;
  }
  
  
    
  public Element getElementLenght(Element element) {
      //get the stateful session
      
      KieSession kieSession = kieBase.newKieSession();
      kieSession.insert(element);
      kieSession.fireAllRules();
      kieSession.dispose();
      return element;
  }
}
