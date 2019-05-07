package org.eea.validation.service;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.template.ObjectDataCompiler;
import org.eea.validation.model.Element;
import org.eea.validation.model.Rules;
import org.eea.validation.repository.RulesRepository;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;

@Service
public class ValidationService {

  private static final String REGULATION_TEMPLATE_FILE =
      "src/main/resources/ruletemplate/template01.drl";

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

  public KieBase loadNewRules(Rules rules) {

    Iterable<Rules> preRepositoryDB = rulesRepository.findAll();

    List<Rules> preRepository = Lists.newArrayList(preRepositoryDB);

    List<Map<String, String>> ruleAttributes = new ArrayList<>();

    for (int i = 0; i < preRepository.size(); i++) {
      Map<String, String> rule1 = new HashMap<>();
      rule1.put("ruleid", preRepository.get(i).getName());
      rule1.put("whencondition",
          preRepository.get(i).getAttribute() + preRepository.get(i).getConditionalElement());
      rule1.put("thencondition", preRepository.get(i).getAction());
      ruleAttributes.add(rule1);
    }

    ObjectDataCompiler compiler = new ObjectDataCompiler();

    String generatedDRL = null;
    try {
      generatedDRL =
          compiler.compile(ruleAttributes, new FileInputStream(REGULATION_TEMPLATE_FILE));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    KieServices kieServices = KieServices.Factory.get();

    KieHelper kieHelper = new KieHelper();

    // multiple such resoures/rules can be added
    byte[] b1 = generatedDRL.getBytes();
    Resource resource1 = kieServices.getResources().newByteArrayResource(b1);
    kieHelper.addResource(resource1, ResourceType.DRL);

    KieBase kieBase2 = kieHelper.build();
    this.kieBase = kieBase2;
    return kieBase;
  }

}
