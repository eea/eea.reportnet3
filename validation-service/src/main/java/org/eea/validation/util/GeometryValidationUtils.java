package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.FieldRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class GeometryValidationUtils.
 */
@Component
public class GeometryValidationUtils {


  /** The rules repository. */
  private static RulesRepository rulesRepository;

  /** The schemas repository. */
  private static SchemasRepository schemasRepository;

  /** The field repository. */
  private static FieldRepository fieldRepository;

  /**
   * Sets the rules repository.
   *
   * @param rulesRepository the new rules repository
   */
  @Autowired
  synchronized void setRulesRepository(RulesRepository rulesRepository) {
    GeometryValidationUtils.rulesRepository = rulesRepository;
  }

  /**
   * Sets the schema repository.
   *
   * @param schemasRepository the new schema repository
   */
  @Autowired
  synchronized void setSchemaRepository(SchemasRepository schemasRepository) {
    GeometryValidationUtils.schemasRepository = schemasRepository;
  }

  /**
   * Sets the field repository.
   *
   * @param fieldRepository the new field repository
   */
  @Autowired
  synchronized void setFieldRepository(FieldRepository fieldRepository) {
    GeometryValidationUtils.fieldRepository = fieldRepository;
  }

  /**
   * Checks if is geometry.
   *
   * @param fieldValue the field value
   * @return true, if is geometry
   */
  public static boolean isGeometry(FieldValue fieldValue) {
    String errorMsg = GeoJsonValidationUtils.checkGeoJson(fieldValue);
    if (!errorMsg.isEmpty()) {
      String datasetSchemaId =
          fieldValue.getRecord().getTableValue().getDatasetId().getIdDatasetSchema();
      Rule rule = retriveRules(datasetSchemaId, fieldValue);
      Document fieldSchemaDoc =
          schemasRepository.findFieldSchema(datasetSchemaId, fieldValue.getIdFieldSchema());
      Document tableSchemaDoc = schemasRepository.findTableSchema(datasetSchemaId,
          fieldValue.getRecord().getTableValue().getIdTableSchema());
      String fieldName = fieldSchemaDoc.get("headerName").toString();
      String tableName = tableSchemaDoc.get("nameTableSchema").toString();
      Validation validation = createValidation(rule, tableName, fieldName, errorMsg);
      createFieldValueValidations(fieldValue, validation);
      saveFieldValidations(fieldValue);
    }
    return true;
  }

  /**
   * Retrive rules.
   *
   * @param fieldValue the field value
   * @return the rule
   */
  private static Rule retriveRules(String datasetSchemaId, FieldValue fieldValue) {

    RulesSchema retrivedRules = rulesRepository.findRulesByreferenceId(
        new ObjectId(datasetSchemaId), new ObjectId(fieldValue.getIdFieldSchema()));
    Rule rule = null;
    for (Rule auxRule : retrivedRules.getRules()) {
      if (auxRule.getReferenceId().toString().equals(fieldValue.getIdFieldSchema())
          && auxRule.getWhenCondition().equals("isGeometry(this)")) {
        rule = auxRule;
      }
    }
    return rule;
  }

  /**
   * Creates the validation.
   *
   * @param rule the rule
   * @param tableName the table name
   * @param fieldName the field name
   * @param message the message
   * @return the validation
   */
  private static Validation createValidation(Rule rule, String tableName, String fieldName,
      String message) {
    Validation validation = new Validation();
    validation.setIdRule(rule.getRuleId().toString());
    validation.setLevelError(ErrorTypeEnum.valueOf(rule.getThenCondition().get(1)));
    validation.setMessage(rule.getThenCondition().get(0) + ": " + message);
    validation.setTypeEntity((rule.getType()));
    validation.setValidationDate(new Date().toString());
    validation.setTableName(tableName);
    if (null != fieldName) {
      validation.setFieldName(fieldName);
    }
    validation.setShortCode(rule.getShortCode());
    return validation;
  }

  /**
   * Save field validations.
   *
   * @param fieldValues the field values
   */
  @Transactional
  private static void saveFieldValidations(FieldValue field) {
    fieldRepository.save(field);
  }

  /**
   * Creates the field value validation query.
   *
   * @param fieldsToValidate the fields to validate
   * @param pkValidation the pk validation
   * @param errorFields the error fields
   */
  private static void createFieldValueValidations(FieldValue field, Validation validation) {
    List<FieldValidation> fieldValueValidations =
        field.getFieldValidations() != null ? field.getFieldValidations() : new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setValidation(validation);
    FieldValue fieldValue = new FieldValue();
    fieldValue.setId(field.getId());
    fieldValidation.setFieldValue(fieldValue);
    fieldValueValidations.add(fieldValidation);
    field.setFieldValidations(fieldValueValidations);
  }
}

