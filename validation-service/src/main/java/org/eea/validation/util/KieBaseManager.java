package org.eea.validation.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.template.ObjectDataCompiler;
import org.eea.validation.persistence.rules.model.ConditionsDrools;
import org.eea.validation.persistence.rules.model.DataFlowRule;
import org.eea.validation.persistence.rules.model.TypeValidation;
import org.eea.validation.persistence.rules.repository.DataFlowRulesRepository;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;

/**
 * The Class KieBaseManager.
 */
@Component
public class KieBaseManager {

  /** The Constant REGULATION_TEMPLATE_FILE. */
  private static final String REGULATION_TEMPLATE_FILE = "src/main/resources/template01.drl";

  /** The kie base. */
  private KieBase kieBase;

  /** The data flow rules repository. */
  @Autowired
  private DataFlowRulesRepository dataFlowRulesRepository;

  /**
   * Reload rules.
   *
   * @param dataFlowId the data flow id
   * @return Kiebase session object
   * @throws FileNotFoundException the file not found exception
   */
  public KieBase reloadRules(Long dataFlowId) throws FileNotFoundException {
    Iterable<DataFlowRule> preRepositoryDB =
        dataFlowRulesRepository.findAllByDataFlowId(dataFlowId);
    List<DataFlowRule> preRepository = Lists.newArrayList(preRepositoryDB);
    List<Map<String, String>> ruleAttributes = new ArrayList<>();
    String LVTypeValidation = null;
    for (int i = 0; i < preRepository.size(); i++) {
      Map<String, String> rule1 = new HashMap<>();

      switch (preRepository.get(i).getRuleScope()) {
        case DATASET:
          LVTypeValidation = TypeValidation.DATASETVO.getValue();
          break;
        case FIELD:
          LVTypeValidation = TypeValidation.FIELDVO.getValue();
          break;
        case RECORD:
          LVTypeValidation = TypeValidation.RECORDVO.getValue();
          break;
        case TABLE:
          LVTypeValidation = TypeValidation.TABLEVO.getValue();
          break;
      }
      rule1.put(ConditionsDrools.RULE_ID.getValue(), preRepository.get(i).getRuleId().toString());
      rule1.put(ConditionsDrools.TYPE_VALIDATION.getValue(), LVTypeValidation);
      rule1.put(ConditionsDrools.WHEN_CONDITION.getValue(),
          preRepository.get(i).getWhenCondition().trim());
      rule1.put(ConditionsDrools.MESSAGE_FAIL_VALIDATION.getValue(),
          preRepository.get(i).getThenCondition().get(0));
      rule1.put(ConditionsDrools.TYPE_FAIL_VALIDATION.getValue(),
          preRepository.get(i).getThenCondition().get(1));
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
