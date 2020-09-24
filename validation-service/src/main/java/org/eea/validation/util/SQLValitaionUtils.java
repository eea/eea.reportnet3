package org.eea.validation.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.SqlRulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class SQLValitaionUtils.
 */
@Component
public class SQLValitaionUtils {

  /** The sql rules service. */
  private static SqlRulesService sqlRulesService;

  private static SchemasRepository schemasRepository;

  private static DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /**
   * Sets the sql rules service.
   *
   * @param sqlRulesService the new sql rules service
   */
  @Autowired
  synchronized void setSqlRulesService(SqlRulesService sqlRulesService) {
    SQLValitaionUtils.sqlRulesService = sqlRulesService;
  }

  @Autowired
  synchronized void setSchemasRepository(SchemasRepository schemasRepository) {
    SQLValitaionUtils.schemasRepository = schemasRepository;
  }

  @Autowired
  synchronized void setDataSetMetabaseControllerZuul(
      DataSetMetabaseControllerZuul datasetMetabaseControllerZuul) {
    SQLValitaionUtils.datasetMetabaseControllerZuul = datasetMetabaseControllerZuul;
  }


  public static void executeValidationSQLRule(Long datasetId, String ruleId) {
    // retrive the rule
    Rule rule = sqlRulesService.getRule(datasetId, ruleId);
    // retrive sql sentence
    String query = rule.getSqlSentence();
    // adapt query to our data model
    String preparedStatement = sqlRulesService.queryTreat(query, datasetId);
    // Execute query

    TableValue tableToEvaluate = new TableValue();
    try {
      tableToEvaluate = sqlRulesService.retrivedata(preparedStatement, datasetId);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    String schemaId = datasetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);
    DataSetSchema schema = schemasRepository.findById(new ObjectId(schemaId)).orElse(null);

    Validation validation = new Validation();
    validation.setIdRule(rule.getRuleId().toString());
    validation.setLevelError(ErrorTypeEnum.valueOf(rule.getThenCondition().get(0)));
    validation.setMessage(rule.getThenCondition().get(1));


    // validation.setOriginName(originName);


    validation.setTypeEntity((rule.getType()));
    validation.setValidationDate(new Date().toString());

    EntityTypeEnum ruleType = rule.getType();
    switch (ruleType) {
      case DATASET:
        break;
      case TABLE:
        if (tableToEvaluate.getTableValidations().isEmpty()) {
          TableValidation tablevalidation = new TableValidation();
          tablevalidation.setTableValue(tableToEvaluate);
          tablevalidation.setValidation(validation);
          List<TableValidation> tableValidations = new ArrayList<>();
          tableValidations.add(tablevalidation);
          tableToEvaluate.setTableValidations(tableValidations);
        } else {
          List<TableValidation> tableValidations = tableToEvaluate.getTableValidations();
          TableValidation tablevalidation = new TableValidation();
          tablevalidation.setTableValue(tableToEvaluate);
          tablevalidation.setValidation(validation);
          tableValidations.add(tablevalidation);
          tableToEvaluate.setTableValidations(tableValidations);
        }
        break;
      case RECORD:
        break;
      case FIELD:
        break;
    }


  }


}
