package org.eea.validation.util;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DataSetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueValidationUtils {
  /**
   * The rules repository.
   */
  private static RulesRepository rulesRepository;

  /**
   * The dataset metabase controller zuul.
   */
  private static DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /**
   * The schemas repository.
   */
  private static SchemasRepository schemasRepository;

  /**
   * The record repository.
   */
  private static RecordRepository recordRepository;

  /**
   * The data set schema controller zuul.
   */
  private static DataSetSchemaControllerZuul dataSetSchemaControllerZuul;


  @Autowired
  private void setRulesRepository(RulesRepository rulesRepository) {
    UniqueValidationUtils.rulesRepository = rulesRepository;
  }

  /**
   * Sets the data set metabase controller zuul.
   *
   * @param datasetMetabaseControllerZuul the new data set metabase controller zuul
   */
  @Autowired
  private void setDataSetMetabaseControllerZuul(
      DataSetMetabaseControllerZuul datasetMetabaseControllerZuul) {
    UniqueValidationUtils.datasetMetabaseControllerZuul = datasetMetabaseControllerZuul;
  }

  /**
   * Sets the schemas repository.
   *
   * @param schemasRepository the new schemas repository
   */
  @Autowired
  private void setSchemasRepository(SchemasRepository schemasRepository) {
    UniqueValidationUtils.schemasRepository = schemasRepository;
  }

  /**
   * Sets the dataset repository.
   *
   * @param fieldRepository the new dataset repository
   */
  @Autowired
  private void setRecordRepository(RecordRepository recordRepository) {
    UniqueValidationUtils.recordRepository = recordRepository;
  }

  /**
   * Sets the data set schema controller zuul.
   *
   * @param dataSetSchemaControllerZuul the new data set schema controller zuul
   */
  @Autowired
  private void setDataSetSchemaControllerZuul(
      DataSetSchemaControllerZuul dataSetSchemaControllerZuul) {
    UniqueValidationUtils.dataSetSchemaControllerZuul = dataSetSchemaControllerZuul;
  }

  /**
   * Creates the validation.
   *
   * @param idRule the id rule
   * @param idDatasetSchema the id dataset schema
   *
   * @return the validation
   */
  private static Validation createValidation(String idRule, String idDatasetSchema,
      TableSchema origname) {

    Rule rule = rulesRepository.findRule(new ObjectId(idDatasetSchema), new ObjectId(idRule));

    Validation validation = new Validation();
    validation.setIdRule(rule.getRuleId().toString());

    switch (rule.getThenCondition().get(1)) {
      case "WARNING":
        validation.setLevelError(ErrorTypeEnum.WARNING);
        break;
      case "ERROR":
        validation.setLevelError(ErrorTypeEnum.ERROR);
        break;
      case "INFO":
        validation.setLevelError(ErrorTypeEnum.INFO);
        break;
      case "BLOCKER":
        validation.setLevelError(ErrorTypeEnum.BLOCKER);
        break;
      default:
        validation.setLevelError(ErrorTypeEnum.BLOCKER);
        break;
    }

    validation.setMessage(rule.getThenCondition().get(0));
    validation.setTypeEntity(EntityTypeEnum.FIELD);
    validation.setValidationDate(new Date().toString());
    validation.setOriginName(origname.getNameTableSchema());

    return validation;
  }


  /**
   * Creates the field validations.
   *
   * @param fieldValues the field values
   */
  @Transactional
  private static void saveRecordValidations(List<RecordValue> recordValues) {
    recordRepository.saveAll(recordValues);
  }

  /**
   * Gets the table schema from id field schema.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   *
   * @return the table schema from id field schema
   */
  private static TableSchema getTableSchemaFromIdFieldSchema(DataSetSchema schema,
      String idFieldSchema) {

    TableSchema tableSchema = new TableSchema();
    Boolean locatedTable = false;

    for (TableSchema table : schema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().toString().equals(idFieldSchema)) {
          tableSchema = table;
          locatedTable = Boolean.TRUE;
          break;
        }
      }
      if (locatedTable.equals(Boolean.TRUE)) {
        break;
      }
    }
    return tableSchema;
  }

  /**
   * Check if field values are duplicated.
   *
   * @param mapFields the map fields
   * @param fieldsToFilter the fields to filter
   * @param field the field to check
   * @return true, if successful
   */
  private static boolean checkduplicated(Map<String, FieldValue> mapFields,
      List<FieldValue> fieldsToFilter, FieldValue field) {
    fieldsToFilter.remove(field);
    for (FieldValue fieldValue : fieldsToFilter) {
      if (mapFields.get(field.getValue()) != null) {
        return true;
      } else {
        if (field.getValue().equals(fieldValue.getValue())) {
          mapFields.put(field.getValue(), field);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if is unique field.
   *
   * @param datasetId the dataset id
   * @param idFieldSchema the id field schema
   * @param idRule the id rule
   * @return the boolean
   */
  public static Boolean uniqueConstraint(String uniqueIdConstraint, String idRule) {

    UniqueConstraintVO uniqueConstraint =
        dataSetSchemaControllerZuul.getUniqueConstraint(uniqueIdConstraint);

    // Get Schema
    String schemaId = uniqueConstraint.getDatasetSchemaId();
    schemasRepository.findByIdDataSetSchema(new ObjectId(schemaId));

    List<RecordValue> duplicatedRecords =
        recordRepository.getDuplicatedRecordsByFields(uniqueConstraint.getFieldSchemaIds());
    duplicatedRecords.size();

    // get Orig name
    // TableSchema origname = getTableSchemaFromIdFieldSchema(datasetSchema, idFieldSchema);
    //
    // // GetValidationData
    // Validation validation = createValidation(idRule, idFieldSchema, origname);
    // List<FieldValue> errorFields = new ArrayList<>();
    //
    // List<FieldValue> fieldsToFilter = fieldRepository.findByIdFieldSchema(idFieldSchema);
    //
    // Map<String, FieldValue> mapFields = new HashMap<>();
    //
    // for (FieldValue field : fieldsToFilter) {
    // if (checkduplicated(mapFields, fieldsToFilter, field)) {
    // List<FieldValidation> fieldValidationList = new ArrayList<>();
    // FieldValidation fieldValidation = new FieldValidation();
    // fieldValidation.setValidation(validation);
    // FieldValue fieldValue = new FieldValue();
    // fieldValue.setId(field.getId());
    // fieldValidation.setFieldValue(fieldValue);
    // fieldValidationList.add(fieldValidation);
    // field.setFieldValidations(fieldValidationList);
    // errorFields.add(field);
    // }
    // }

    // saveFieldValidations(errorFields);

    // Force true because we only need Field Validations
    return true;


  }
}
