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
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.RulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueValidationUtils {
  /**
   * The rules repository.
   */
  private static RulesRepository rulesRepository;

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

  /** The table repository. */
  private static TableRepository tableRepository;

  /** The rules service. */
  private static RulesService rulesService;

  /** The data set metabase controller zuul. */
  private static DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

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
   * Sets the table repository.
   *
   * @param tableRepository the new table repository
   */
  @Autowired
  private void setTableRepository(TableRepository tableRepository) {
    UniqueValidationUtils.tableRepository = tableRepository;
  }

  @Autowired
  private void setRulesService(RulesService rulesService) {
    UniqueValidationUtils.rulesService = rulesService;
  }

  /**
   * Sets the data set metabase controller zuul.
   *
   * @param dataSetMetabaseControllerZuul the new data set metabase controller zuul
   */
  @Autowired
  private void setDataSetMetabaseControllerZuul(
      DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul) {
    UniqueValidationUtils.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
  }

  /**
   * Creates the validation.
   *
   * @param idRule the id rule
   * @param idDatasetSchema the id dataset schema
   *
   * @return the validation
   */
  private static Validation createValidation(String idRule, String idDatasetSchema, String origname,
      EntityTypeEnum typeEnum) {

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
      default:
        validation.setLevelError(ErrorTypeEnum.BLOCKER);
        break;
    }

    validation.setMessage(rule.getThenCondition().get(0));
    validation.setTypeEntity(typeEnum);
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

  @Transactional
  private static void saveTableValidations(TableValue tableValue) {
    tableRepository.save(tableValue);
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

  private static TableSchema getTableSchemaFromIdFieldSchema(DataSetSchema datasetSchema,
      String idFieldSchema) {
    for (TableSchema table : datasetSchema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().toString().equals(idFieldSchema)) {
          return table;
        }
      }
    }
    return new TableSchema();
  }

  /**
   * Mount query.
   *
   * @param fieldSchemaIds the field schema ids
   * @return the string
   */
  private static String mountDuplicatedQuery(List<String> fieldSchemaIds) {
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
    stringQuery.append(") as N from table_1 where column_1 is not null) as t where n>1);");
    return stringQuery.toString();

  }

  /**
   * Mount integrity query.
   *
   * @param originFields the origin fields
   * @param referencedFields the referenced fields
   * @param datasetOriginId the dataset origin id
   * @param datasetReferencedId the dataset referenced id
   * @return the string
   */
  private static String mountIntegrityQuery(List<String> originFields,
      List<String> referencedFields, Long datasetOriginId, Long datasetReferencedId) {

    StringBuilder stringQuery = new StringBuilder("with table_1 as(select rv.id, ");
    Iterator<String> iterator = originFields.iterator();
    int i = 1;
    while (iterator.hasNext()) {
      String schemaId = iterator.next();
      stringQuery
          .append("(select fv.value from dataset_" + datasetOriginId
              + ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '")
          .append(schemaId).append("') AS ").append("column_" + (i++));
      if (iterator.hasNext()) {
        stringQuery.append(",");
      }
    }
    stringQuery.append(
        " from dataset_" + datasetOriginId + ".record_value rv), table_2 as(select rv.id, ");
    iterator = referencedFields.iterator();
    i = 1;
    while (iterator.hasNext()) {
      String schemaId = iterator.next();
      stringQuery
          .append("(select fv.value from dataset_" + datasetReferencedId
              + ".field_value fv where fv.id_record=rv.id and fv.id_field_schema = '")
          .append(schemaId).append("') AS ").append("column_" + (i++));
      if (iterator.hasNext()) {
        stringQuery.append(",");
      }
    }
    stringQuery.append(" from dataset_" + datasetReferencedId
        + ".record_value rv) select t2.id from table_1 t1 right join table_2 t2 on t1.column_1 = t2.column_1");
    iterator = originFields.iterator();
    i = 2;
    iterator.next();
    while (iterator.hasNext()) {
      iterator.next();
      stringQuery.append(" and t1.column_" + i + " = t2.column_" + i);
      i++;
    }
    stringQuery.append(" where t1.column_1 is null and t2.column_1 is not null ;");
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
    Validation validation = createValidation(idRule, schemaId, origname, EntityTypeEnum.RECORD);

    // get duplicated records
    List<RecordValue> duplicatedRecords = recordRepository
        .queryExecutionRecord(mountDuplicatedQuery(uniqueConstraint.getFieldSchemaIds()));

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

  /**
   * Checks if is integrity constraint.
   *
   * @param integrityId the integrity id
   * @param idRule the id rule
   * @return the boolean
   */
  public static Boolean isIntegrityConstraint(DatasetValue datasetId, String integrityId,
      String idRule) {
    // GetValidationData
    IntegrityVO integrityVO = rulesService.getIntegrityConstraint(integrityId);
    long datasetIdOrigin = datasetId.getId();
    long datasetIdReferenced = dataSetMetabaseControllerZuul.getIntegrityDatasetId(datasetIdOrigin,
        integrityVO.getOriginDatasetSchemaId(), integrityVO.getReferencedDatasetSchemaId());
    String schemaId = integrityVO.getOriginDatasetSchemaId();

    DataSetSchema datasetSchema = schemasRepository.findByIdDataSetSchema(new ObjectId(schemaId));

    TableSchema tableSchema =
        getTableSchemaFromIdFieldSchema(datasetSchema, integrityVO.getOriginFields().get(0));

    Validation validation =
        createValidation(idRule, schemaId, tableSchema.getNameTableSchema(), EntityTypeEnum.TABLE);

    List<String> notUtilizedRecords =
        recordRepository.queryExecution(mountIntegrityQuery(integrityVO.getOriginFields(),
            integrityVO.getReferencedFields(), datasetIdOrigin, datasetIdReferenced));
    if (!notUtilizedRecords.isEmpty()) {
      TableValidation tableValidation = new TableValidation();
      tableValidation.setValidation(validation);
      TableValue tableValue =
          tableRepository.findByIdTableSchema(tableSchema.getIdTableSchema().toString());
      List<TableValidation> tableValidations =
          tableValue.getTableValidations() != null ? tableValue.getTableValidations()
              : new ArrayList<>();
      tableValidation.setTableValue(tableValue);
      tableValidations.add(tableValidation);
      tableValue.setTableValidations(tableValidations);
      saveTableValidations(tableValue);
    }
    if (datasetIdOrigin != datasetIdReferenced) {
      schemaId = integrityVO.getReferencedDatasetSchemaId();
      datasetSchema = schemasRepository.findByIdDataSetSchema(new ObjectId(schemaId));
    }
    if (Boolean.TRUE.equals(integrityVO.getIsDoubleReferenced())) {
      tableSchema =
          getTableSchemaFromIdFieldSchema(datasetSchema, integrityVO.getReferencedFields().get(0));
      notUtilizedRecords =
          recordRepository.queryExecution(mountIntegrityQuery(integrityVO.getReferencedFields(),
              integrityVO.getOriginFields(), datasetIdOrigin, datasetIdReferenced));
      if (!notUtilizedRecords.isEmpty()) {
        validation = createValidation(idRule, schemaId, tableSchema.getNameTableSchema(),
            EntityTypeEnum.TABLE);
        TableValidation tableValidation = new TableValidation();
        tableValidation.setValidation(validation);
        TableValue tableValue =
            tableRepository.findByIdTableSchema(tableSchema.getIdTableSchema().toString());
        List<TableValidation> tableValidations =
            tableValue.getTableValidations() != null ? tableValue.getTableValidations()
                : new ArrayList<>();
        tableValidation.setTableValue(tableValue);
        tableValidations.add(tableValidation);
        tableValue.setTableValidations(tableValidations);
        saveTableValidations(tableValue);
      }
    }
    return !notUtilizedRecords.isEmpty();

  }

}
