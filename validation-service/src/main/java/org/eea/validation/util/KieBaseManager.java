package org.eea.validation.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.template.ObjectDataCompiler;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.rules.ConditionsDrools;
import org.eea.validation.persistence.rules.SchemasDrools;
import org.eea.validation.persistence.rules.TypeValidation;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
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
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public KieBase reloadRules(Long dataFlowId) throws FileNotFoundException {
    DataSetSchema schema =

        schemasRepository.findSchemaByIdFlow(dataFlowId);

    List<Map<String, String>> ruleAttributes = new ArrayList<>();
    for (RuleDataSet rule : schema.getRuleDataSet()) {
      ruleAttributes.add(passDataToMap(rule.getIdDataSetSchema().toString(),
          rule.getRuleId().toString(), TypeValidation.DATASET,
          SchemasDrools.ID_DATASET_SCHEMA.getValue(), rule.getWhenCondition(),
          rule.getThenCondition().get(0), rule.getThenCondition().get(1)));
    }
    for (TableSchema table : schema.getTableSchemas()) {

      for (RuleTable ruleTableList : table.getRuleTable()) {
        ruleAttributes.add(passDataToMap(ruleTableList.getIdTableSchema().toString(),
            ruleTableList.getRuleId().toString(), TypeValidation.TABLE,
            SchemasDrools.ID_TABLE_SCHEMA.getValue(), ruleTableList.getWhenCondition(),
            ruleTableList.getThenCondition().get(0), ruleTableList.getThenCondition().get(1)));
      }
      for (RuleRecord ruleRecordList : table.getRecordSchema().getRuleRecord()) {
        ruleAttributes.add(passDataToMap(ruleRecordList.getIdRecordSchema().toString(),
            ruleRecordList.getRuleId().toString(), TypeValidation.RECORD,
            SchemasDrools.ID_RECORD_SCHEMA.getValue(), ruleRecordList.getWhenCondition(),
            ruleRecordList.getThenCondition().get(0), ruleRecordList.getThenCondition().get(1)));
      }
      for (FieldSchema fieldSchema : table.getRecordSchema().getFieldSchema()) {

        for (RuleField ruleField : fieldSchema.getRuleField()) {
          ruleAttributes.add(passDataToMap(ruleField.getIdFieldSchema().toString(),
              ruleField.getRuleId().toString(), TypeValidation.FIELD,
              SchemasDrools.ID_FIELD_SCHEMA.getValue(), ruleField.getWhenCondition(),
              ruleField.getThenCondition().get(0), ruleField.getThenCondition().get(1)));
        }
      }
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

  private Map<String, String> passDataToMap(String idSchema, String idRule,
      TypeValidation typeValidation, String schemaName, String whenCondition, String message,
      String error) {
    Map<String, String> ruleAdd = new HashMap<>();
    ruleAdd.put(ConditionsDrools.DATASCHEMA_ID.getValue(), idSchema);
    ruleAdd.put(ConditionsDrools.RULE_ID.getValue(), idRule);
    ruleAdd.put(ConditionsDrools.SCHEMA_NAME.getValue(), schemaName);
    ruleAdd.put(ConditionsDrools.RULE_ID.getValue(), idRule);
    ruleAdd.put(ConditionsDrools.TYPE_VALIDATION.getValue(), typeValidation.getValue());
    ruleAdd.put(ConditionsDrools.WHEN_CONDITION.getValue(), whenCondition);
    ruleAdd.put(ConditionsDrools.MESSAGE_FAIL_VALIDATION.getValue(), message);
    ruleAdd.put(ConditionsDrools.TYPE_FAIL_VALIDATION.getValue(), error);
    return ruleAdd;
  }

}
