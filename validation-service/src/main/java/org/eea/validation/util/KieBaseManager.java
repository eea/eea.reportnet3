package org.eea.validation.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.drools.template.ObjectDataCompiler;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.util.drools.compose.ConditionsDrools;
import org.eea.validation.util.drools.compose.SchemasDrools;
import org.eea.validation.util.drools.compose.TypeValidation;
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

  /**
   * The Constant REGULATION_TEMPLATE_FILE.
   */
  private static final String REGULATION_TEMPLATE_FILE = "/template01.drl";


  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;
  /**
   * The dataset metabase controller.
   */
  @Autowired
  private DatasetMetabaseController datasetMetabaseController;


  /**
   * Reload rules.
   *
   * @param datasetId the dataset id
   * @param datasetSchema the dataset schema
   * @return the kie base
   * @throws FileNotFoundException the file not found exception
   */
  public KieBase reloadRules(Long datasetId, String datasetSchema) throws FileNotFoundException {
    DataSetMetabaseVO dataSetMetabaseVO =
        datasetMetabaseController.findDatasetMetabaseById(datasetId);
    RulesSchema schemaRules = rulesRepository.findByIdDatasetSchema(new ObjectId(datasetSchema));

    List<Map<String, String>> ruleAttributes = new ArrayList<>();
    if (null != schemaRules.getRules() && !schemaRules.getRules().isEmpty()) {
      schemaRules.getRules().stream().forEach(rule -> {
        if (Boolean.TRUE.equals(rule.getEnabled())) {
          String schemasDrools = "";
          TypeValidation typeValidation = null;
          switch (rule.getType()) {
            case DATASET:
              schemasDrools = SchemasDrools.ID_DATASET_SCHEMA.getValue();
              typeValidation = TypeValidation.DATASET;
              break;
            case TABLE:
              schemasDrools = SchemasDrools.ID_TABLE_SCHEMA.getValue();
              typeValidation = TypeValidation.TABLE;
              break;
            case RECORD:
              schemasDrools = SchemasDrools.ID_RECORD_SCHEMA.getValue();
              typeValidation = TypeValidation.RECORD;
              break;
            case FIELD:
              schemasDrools = SchemasDrools.ID_FIELD_SCHEMA.getValue();
              typeValidation = TypeValidation.FIELD;
              break;
          }
          ruleAttributes.add(passDataToMap(rule.getReferenceId().toString(),
              rule.getRuleId().toString(), typeValidation, schemasDrools, rule.getWhenCondition(),
              rule.getThenCondition().get(0), rule.getThenCondition().get(1),
              null == dataSetMetabaseVO ? "" : dataSetMetabaseVO.getDataSetName()));
        }
      });
    }

    ObjectDataCompiler compiler = new ObjectDataCompiler();

    String generatedDRL =
        compiler.compile(ruleAttributes, getClass().getResourceAsStream(REGULATION_TEMPLATE_FILE));

    KieServices kieServices = KieServices.Factory.get();

    KieHelper kieHelper = new KieHelper();

    // multiple such resoures/rules can be added
    byte[] b1 = generatedDRL.getBytes();
    Resource resource1 = kieServices.getResources().newByteArrayResource(b1);
    kieHelper.addResource(resource1, ResourceType.DRL);
    // this is a shared variable in a single instanced object.
    return kieHelper.build();
  }

  /**
   * Pass data to map.
   *
   * @param idSchema the id schema
   * @param idRule the id rule
   * @param typeValidation the type validation
   * @param schemaName the schema name
   * @param whenCondition the when condition
   * @param message the message
   * @param error the error
   * @param tableSchemaName the table schema name
   * @return the map
   */
  private Map<String, String> passDataToMap(String idSchema, String idRule,
      TypeValidation typeValidation, String schemaName, String whenCondition, String message,
      String error, String tableSchemaName) {
    Map<String, String> ruleAdd = new HashMap<>();
    ruleAdd.put(ConditionsDrools.DATASCHEMA_ID.getValue(), idSchema);
    ruleAdd.put(ConditionsDrools.RULE_ID.getValue(), idRule);
    ruleAdd.put(ConditionsDrools.SCHEMA_NAME.getValue(), schemaName);
    ruleAdd.put(ConditionsDrools.RULE_ID.getValue(), idRule);
    ruleAdd.put(ConditionsDrools.TYPE_VALIDATION.getValue(), typeValidation.getValue());
    ruleAdd.put(ConditionsDrools.WHEN_CONDITION.getValue(), whenCondition);
    ruleAdd.put(ConditionsDrools.MESSAGE_FAIL_VALIDATION.getValue(), message);
    ruleAdd.put(ConditionsDrools.TYPE_FAIL_VALIDATION.getValue(), error);
    ruleAdd.put(ConditionsDrools.ORIGIN_NAME.getValue(),
        tableSchemaName != null ? tableSchemaName : "");
    return ruleAdd;
  }

}
