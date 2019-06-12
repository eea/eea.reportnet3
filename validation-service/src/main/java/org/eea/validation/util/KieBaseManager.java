package org.eea.validation.util;

import java.io.FileNotFoundException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.kie.api.KieBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
  private DatasetSchemaController controllerSchema;

  /**
   * Reload rules.
   *
   * @param dataFlowId the data flow id
   * @return Kiebase session object
   * @throws FileNotFoundException the file not found exception
   */
  public KieBase reloadRules(Long dataFlowId) throws FileNotFoundException {
    DataSetSchemaVO preRepositoryDB =
        // dataFlowRulesRepository.findAllByDataFlowId(dataFlowId);

        controllerSchema.findDataSchemaByDataflow(dataFlowId);


    // List<DataFlowRule> preRepository = Lists.newArrayList(preRepositoryDB);
    // List<Map<String, String>> ruleAttributes = new ArrayList<>();
    // String LVTypeValidation = null;
    // for (int i = 0; i < preRepository.size(); i++) {
    // Map<String, String> rule1 = new HashMap<>();
    //
    // switch (preRepository.get(i).getRuleScope()) {
    // case DATASET:
    // LVTypeValidation = TypeValidation.DATASETVO.getValue();
    // break;
    // case FIELD:
    // LVTypeValidation = TypeValidation.FIELDVO.getValue();
    // break;
    // case RECORD:
    // LVTypeValidation = TypeValidation.RECORDVO.getValue();
    // break;
    // case TABLE:
    // LVTypeValidation = TypeValidation.TABLEVO.getValue();
    // break;
    // }
    // rule1.put(ConditionsDrools.RULE_ID.getValue(), preRepository.get(i).getRuleId().toString());
    // rule1.put(ConditionsDrools.TYPE_VALIDATION.getValue(), LVTypeValidation);
    // rule1.put(ConditionsDrools.WHEN_CONDITION.getValue(),
    // preRepository.get(i).getWhenCondition().trim());
    // rule1.put(ConditionsDrools.MESSAGE_FAIL_VALIDATION.getValue(),
    // preRepository.get(i).getThenCondition().get(0));
    // rule1.put(ConditionsDrools.TYPE_FAIL_VALIDATION.getValue(),
    // preRepository.get(i).getThenCondition().get(1));
    // ruleAttributes.add(rule1);
    // }
    //
    // ObjectDataCompiler compiler = new ObjectDataCompiler();
    //
    // String generatedDRL =
    // compiler.compile(ruleAttributes, new FileInputStream(REGULATION_TEMPLATE_FILE));
    //
    // KieServices kieServices = KieServices.Factory.get();
    //
    // KieHelper kieHelper = new KieHelper();
    //
    // // multiple such resoures/rules can be added
    // byte[] b1 = generatedDRL.getBytes();
    // Resource resource1 = kieServices.getResources().newByteArrayResource(b1);
    // kieHelper.addResource(resource1, ResourceType.DRL);
    // // this is a shared variable in a single instanced object.
    // KieBase newBase = kieHelper.build();
    // this.kieBase = newBase;
    return this.kieBase;
  }

}
