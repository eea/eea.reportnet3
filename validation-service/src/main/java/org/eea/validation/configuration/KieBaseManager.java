package org.eea.validation.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.template.ObjectDataCompiler;
import org.eea.validation.persistence.rules.model.DataFlowRules;
import org.eea.validation.persistence.rules.repository.DataFlowRulesRepository;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;

@Component
public class KieBaseManager {

  private static final String REGULATION_TEMPLATE_FILE =
      "src/main/resources/ruletemplate/template01.drl";


  private KieBase kieBase;

  @Autowired
  private DataFlowRulesRepository dataFlowRulesRepository;

  /**
   * Reload rules.
   *
   * @param dataflowId idDataflow to know the rules associates to the dataflow
   * @return Kiebase session object
   * @throws FileNotFoundException the file not found exception
   */
  public KieBase reloadRules(Integer dataflowId) throws FileNotFoundException {

    Iterable<DataFlowRules> preRepositoryDB = dataFlowRulesRepository.findAll();
    List<DataFlowRules> preRepository = Lists.newArrayList(preRepositoryDB);
    List<Map<String, String>> ruleAttributes = new ArrayList<>();

    for (int i = 0; i < preRepository.size(); i++) {
      Map<String, String> rule1 = new HashMap<>();
      rule1.put("ruleid", preRepository.get(i).getId_Rules().toString());
      rule1.put("whencondition", preRepository.get(i).getWhenCondition());
      rule1.put("thencondition", preRepository.get(i).getThenCondition());
      ruleAttributes.add(rule1);
    }

    ObjectDataCompiler compiler = new ObjectDataCompiler();

    String generatedDRL =
        compiler.compile(ruleAttributes, new FileInputStream(REGULATION_TEMPLATE_FILE));

    KieServices kieServices = KieServices.Factory.get();

    KieHelper kieHelper = new KieHelper();

    // multiple such resoures/rules can be added
    byte[] b1 = generatedDRL.getBytes();
    Resource resource1 = kieServices.getResources().newByteArrayResource(b1);
    kieHelper.addResource(resource1, ResourceType.DRL);
    // this is a shared variable in a single instanced object.
    KieBase newBase = kieHelper.build();
    this.kieBase = newBase;
    return this.kieBase;
  }


}
