package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DataSetSchemaControllerZuul;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
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


  /**
   * Sets the rules repository.
   *
   * @param rulesRepository the new rules repository
   */
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
      String origname) {

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
    validation.setTypeEntity(EntityTypeEnum.RECORD);
    validation.setValidationDate(new Date().toString());
    validation.setOriginName(origname);

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
  private static String getTableSchemaFromIdTableSchema(DataSetSchema schema,
      String idTableSchema) {

    String tableSchemaName = "";
    for (TableSchema table : schema.getTableSchemas()) {
      if (table.getIdTableSchema().toString().equals(idTableSchema)) {
        tableSchemaName = table.getNameTableSchema();
        break;
      }
    }
    return tableSchemaName;
  }

  /**
   * Mount query.
   *
   * @param fieldSchemaIds the field schema ids
   * @return the string
   */
  private static String mountQuery(List<String> fieldSchemaIds) {
    StringBuilder stringQuery = new StringBuilder("with table_1 as(select rv.id, ");
    Iterator<String> iterator = fieldSchemaIds.iterator();
    int i = 1;
    while (iterator.hasNext()) {
      String schemaId = iterator.next();
      stringQuery.append(
          "(select fv.value from field_value fv where fv.id_record=rv.id and fv.id_field_schema = '")
          .append(schemaId).append("') AS ").append("column_" + (i++));
      if (iterator.hasNext()) {
        stringQuery.append(",");
      }
    }
    stringQuery.append(
        " from record_value rv) select rv.* from record_value rv where rv.id in (select t.id from (select *,count(*) over(partition by ");
    iterator = fieldSchemaIds.iterator();
    i = 1;
    while (iterator.hasNext()) {
      iterator.next();
      stringQuery.append("column_" + (i++));
      if (iterator.hasNext()) {
        stringQuery.append(",");
      }
    }
    stringQuery.append(") as N from table_1 ) as t where n>1);");
    return stringQuery.toString();

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

    DataSetSchema datasetSchema = schemasRepository.findByIdDataSetSchema(new ObjectId(schemaId));

    // get Orig name
    String origname =
        getTableSchemaFromIdTableSchema(datasetSchema, uniqueConstraint.getTableSchemaId());

    // GetValidationData
    Validation validation = createValidation(idRule, schemaId, origname);

    // get duplicated records
    List<RecordValue> duplicatedRecords =
        recordRepository.queryExecutionRecord(mountQuery(uniqueConstraint.getFieldSchemaIds()));

    List<RecordValue> recordValues = new ArrayList<>();
    for (RecordValue record : duplicatedRecords) {
      RecordValidation recordValidation = new RecordValidation();
      recordValidation.setValidation(validation);
      recordValidation.setRecordValue(record);
      List<RecordValidation> recordValidations =
          record.getRecordValidations() != null ? record.getRecordValidations() : new ArrayList<>();
      recordValidations.add(recordValidation);
      record.setRecordValidations(recordValidations);
      recordValues.add(record);
    }

    // save records
    saveRecordValidations(recordValues);

    return duplicatedRecords.isEmpty();
  }

}
