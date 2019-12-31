package org.eea.validation.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.drools.template.ObjectDataCompiler;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
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


  /**
   * The data flow rules repository.
   */
  @Autowired
  private SchemasRepository schemasRepository;

  /**
   * The dataset metabase controller.
   */
  @Autowired
  private DatasetMetabaseController datasetMetabaseController;


  /**
   * Reload rules.
   *
   * @param datasetId the dataset id
   * @param datasetSchema
   *
   * @return the kie base
   *
   * @throws FileNotFoundException the file not found exception
   */
  public KieBase reloadRules(Long datasetId, String datasetSchema) throws FileNotFoundException {
    DataSetMetabaseVO dataSetMetabaseVO =
        datasetMetabaseController.findDatasetMetabaseById(datasetId);
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchema));

    List<Map<String, String>> ruleAttributes = new ArrayList<>();
    if (schema.getRuleDataSet() != null) {
      schema.getRuleDataSet().stream().forEach(rule -> {
        ruleAttributes.add(passDataToMap(rule.getIdDataSetSchema().toString(),
            rule.getRuleId().toString(), TypeValidation.DATASET,
            SchemasDrools.ID_DATASET_SCHEMA.getValue(), rule.getWhenCondition(),
            rule.getThenCondition().get(0), rule.getThenCondition().get(1),
            null == dataSetMetabaseVO ? "" : dataSetMetabaseVO.getDataSetName()));
      });
    }

    schema.getTableSchemas().stream().forEach(tableSchema -> {
      if (tableSchema.getRuleTable() != null) {
        tableSchema.getRuleTable().stream().filter(Objects::nonNull).forEach(ruleTable -> {

          ruleAttributes.add(passDataToMap(ruleTable.getIdTableSchema().toString(),
              ruleTable.getRuleId().toString(), TypeValidation.TABLE,
              SchemasDrools.ID_TABLE_SCHEMA.getValue(), ruleTable.getWhenCondition(),
              ruleTable.getThenCondition().get(0), ruleTable.getThenCondition().get(1),
              tableSchema.getNameTableSchema()));
        });
      }
      if (tableSchema.getRecordSchema() != null
          && tableSchema.getRecordSchema().getRuleRecord() != null) {
        tableSchema.getRecordSchema().getRuleRecord().stream().forEach(ruleRecord -> {
          ruleAttributes.add(passDataToMap(ruleRecord.getIdRecordSchema().toString(),
              ruleRecord.getRuleId().toString(), TypeValidation.RECORD,
              SchemasDrools.ID_RECORD_SCHEMA.getValue(), ruleRecord.getWhenCondition(),
              ruleRecord.getThenCondition().get(0), ruleRecord.getThenCondition().get(1),
              tableSchema.getNameTableSchema()));
        });
      }
      if (tableSchema.getRecordSchema() != null
          && tableSchema.getRecordSchema().getFieldSchema() != null) {
        tableSchema.getRecordSchema().getFieldSchema().stream()
            .filter(fieldSchema -> fieldSchema.getIdFieldSchema() != null
                && StringUtils.isNotBlank(fieldSchema.getIdFieldSchema().toString())
                && fieldSchema.getRuleField() != null)
            .forEach(fieldSchema -> {
              fieldSchema.getRuleField().forEach(ruleField -> {
                ruleAttributes.add(passDataToMap(ruleField.getIdFieldSchema().toString(),
                    ruleField.getRuleId().toString(), TypeValidation.FIELD,
                    SchemasDrools.ID_FIELD_SCHEMA.getValue(), ruleField.getWhenCondition(),
                    ruleField.getThenCondition().get(0), ruleField.getThenCondition().get(1),
                    tableSchema.getNameTableSchema()));
              });
            });
      }
    });

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
   *
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
