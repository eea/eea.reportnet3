package org.eea.validation.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.template.ObjectDataCompiler;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.rules.ConditionsDrools;
import org.eea.validation.persistence.rules.TypeValidation;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RuleDataSet;
import org.eea.validation.persistence.schemas.rule.RuleField;
import org.eea.validation.persistence.schemas.rule.RuleRecord;
import org.eea.validation.persistence.schemas.rule.RuleTable;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;
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
  private SchemasRepository schemasRepository;

  /**
   * Reload rules.
   *
   * @param dataFlowId the data flow id
   * @return Kiebase session object
   * @throws FileNotFoundException the file not found exception
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public KieBase reloadRules(Long dataFlowId) throws FileNotFoundException {
    DataSetSchema schema =

        schemasRepository.findSchemaByIdFlow(dataFlowId);

    List<Rule> listRules = new ArrayList<>();

    for (RuleDataSet rule : schema.getRuleDataSet()) {
      listRules.add(rule);
    }
    for (TableSchema table : schema.getTableSchemas()) {

      for (RuleTable ruleTableList : table.getRuleTable()) {
        listRules.add(ruleTableList);
      }
      for (RuleRecord ruleRecordList : table.getRecordSchema().getRuleRecord()) {
        listRules.add(ruleRecordList);
      }
      for (FieldSchema fieldSchema : table.getRecordSchema().getFieldSchema()) {

        for (RuleField ruleField : fieldSchema.getRuleField()) {
          listRules.add(ruleField);
        }
      }
    }

    List<Map<String, String>> ruleAttributes = new ArrayList<>();
    String LVTypeValidation = null;
    for (int i = 0; i < listRules.size(); i++) {
      Map<String, String> ruleAdd = new HashMap<>();
      switch (listRules.get(i).getScope()) {
        case DATASET:
          LVTypeValidation = TypeValidation.DATASET.getValue();
          break;
        case FIELD:
          LVTypeValidation = TypeValidation.FIELD.getValue();
          break;
        case RECORD:
          LVTypeValidation = TypeValidation.RECORD.getValue();
          break;
        case TABLE:
          LVTypeValidation = TypeValidation.TABLE.getValue();
          break;
      }
      ruleAdd.put(ConditionsDrools.RULE_ID.getValue(), listRules.get(i).getRuleId().toString());
      ruleAdd.put(ConditionsDrools.TYPE_VALIDATION.getValue(), LVTypeValidation);
      ruleAdd.put(ConditionsDrools.WHEN_CONDITION.getValue(),
          listRules.get(i).getWhenCondition().trim());
      ruleAdd.put(ConditionsDrools.MESSAGE_FAIL_VALIDATION.getValue(),
          listRules.get(i).getThenCondition().get(0).toString());
      ruleAdd.put(ConditionsDrools.TYPE_FAIL_VALIDATION.getValue(),
          listRules.get(i).getThenCondition().get(1).toString());
      ruleAttributes.add(ruleAdd);

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
