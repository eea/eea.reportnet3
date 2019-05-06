package org.eea.validation.service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.validation.model.Element;
import org.eea.validation.model.Rules;
import org.eea.validation.repository.RulesRepository;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;

@Service
public class ValidationService {

  @Autowired
  private RulesRepository rulesRepository;

  private KieBase kieBase;

  @Autowired
  public ValidationService(KieBase kieBase) {
    this.kieBase = kieBase;
  }



  public Element getElementLenght(Element element) {
    // get the stateful session

    KieSession kieSession = kieBase.newKieSession();
    kieSession.insert(element);
    kieSession.fireAllRules();
    kieSession.dispose();
    return element;

  }

  public List<Map<String, String>> getRules(Rules rules) {
    Iterable<Rules> preRepositoryDB = rulesRepository.findAll();
    List<Rules> preRepository = Lists.newArrayList(preRepositoryDB);
    List<Map<String, String>> ruleAttributes = new ArrayList<>();
    for (int i = 0; i < preRepository.size(); i++) {
      Map<String, String> rule1 = new HashMap<>();
      rule1.put("ruleid", preRepository.get(i).getName());
      ruleAttributes.add(rule1);
    }
    return ruleAttributes;
  }

  public void setNewRules(Rules newRules) {

    rulesRepository.save(newRules);

  }
}
