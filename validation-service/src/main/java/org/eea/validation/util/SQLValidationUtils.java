package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.exception.EEAInvalidSQLException;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.SqlRulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class SQLValitaionUtils.
 */
@Component
public class SQLValidationUtils {

  /** The sql rules service. */
  private static SqlRulesService sqlRulesService;

  /** The schemas repository. */
  private static SchemasRepository schemasRepository;

  /** The dataset metabase controller zuul. */
  private static DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The table repository. */
  private static TableRepository tableRepository;

  /** The dataset repository. */
  private static DatasetRepository datasetRepository;

  /** The dataset repository. */
  private static RecordRepository recordRepository;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Sets the sql rules service.
   *
   * @param sqlRulesService the new sql rules service
   */
  @Autowired
  synchronized void setSqlRulesService(SqlRulesService sqlRulesService) {
    SQLValidationUtils.sqlRulesService = sqlRulesService;
  }

  /**
   * Sets the schemas repository.
   *
   * @param schemasRepository the new schemas repository
   */
  @Autowired
  synchronized void setSchemasRepository(SchemasRepository schemasRepository) {
    SQLValidationUtils.schemasRepository = schemasRepository;
  }

  /**
   * Sets the data set metabase controller zuul.
   *
   * @param datasetMetabaseControllerZuul the new data set metabase controller zuul
   */
  @Autowired
  synchronized void setDataSetMetabaseControllerZuul(
      DataSetMetabaseControllerZuul datasetMetabaseControllerZuul) {
    SQLValidationUtils.datasetMetabaseControllerZuul = datasetMetabaseControllerZuul;
  }

  /**
   * Sets the table repository.
   *
   * @param tableRepository the new table repository
   */
  @Autowired
  synchronized void setTableRepository(TableRepository tableRepository) {
    SQLValidationUtils.tableRepository = tableRepository;
  }

  /**
   * Sets the dataset repository.
   *
   * @param datasetRepository the new dataset repository
   */
  @Autowired
  synchronized void setDatasetRepository(DatasetRepository datasetRepository) {
    SQLValidationUtils.datasetRepository = datasetRepository;
  }


  /**
   * Sets the record repository.
   *
   * @param recordRepository the new record repository
   */
  @Autowired
  synchronized void setRecordRepository(RecordRepository recordRepository) {
    SQLValidationUtils.recordRepository = recordRepository;
  }

  /**
   * Execute validation SQL rule.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   * @param dataProviderCode the data provider code
   */
  public static void executeValidationSQLRule(Long datasetId, String ruleId,
      String dataProviderCode) {
    Rule rule = sqlRulesService.getRule(datasetId, ruleId);
    TableValue tableToEvaluate = getTableToEvaluate(datasetId, rule, dataProviderCode);
    if (null != tableToEvaluate && null != tableToEvaluate.getId()) {
      String schemaId = datasetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);
      Optional<DataSetSchema> dataSetSchema = schemasRepository.findById(new ObjectId(schemaId));
      Optional<TableValue> tableValue = tableRepository.findById(tableToEvaluate.getId());
      String tableName = getTableName(dataSetSchema, tableValue);
      switch (rule.getType()) {
        case DATASET:
          executeDatasetSQLRuleValidation(datasetId, rule, tableName, dataSetSchema);
          break;
        case TABLE:
          executeTableSQLRuleValidation(rule, tableToEvaluate, dataSetSchema, tableValue,
              tableName);
          break;
        case RECORD:
          executeRecordSQLRuleValidation(rule, tableToEvaluate, dataSetSchema, tableValue,
              tableName);
          break;
        case FIELD:
          executeFieldSQLRuleValidation(rule, tableToEvaluate, dataSetSchema, tableValue,
              tableName);
          break;
      }
    }

  }

  /**
   * Gets the table to evaluate.
   *
   * @param datasetId the dataset id
   * @param rule the rule
   * @param dataProviderCode the data provider code
   * @return the table to evaluate
   */
  private static TableValue getTableToEvaluate(Long datasetId, Rule rule, String dataProviderCode) {
    TableValue table = null;
    String query = rule.getSqlSentence();
    try {
      String preparedquery = query.contains(";") ? query.replace(";", "") : query;
      if (dataProviderCode != null) {
        preparedquery = preparedquery.replace("{%R3_COUNTRY_CODE%}", dataProviderCode);
        preparedquery = preparedquery.replace("{%R3_COMPANY_CODE%}", dataProviderCode);
      }
      table = sqlRulesService.retrieveTableData(preparedquery, datasetId, rule, Boolean.FALSE);
    } catch (EEAInvalidSQLException e) {
      LOG_ERROR.error("SQL can't be executed: {}", e.getMessage(), e);
    }
    return table;
  }

  /**
   * Gets the table name.
   *
   * @param dataSetSchema the data set schema
   * @param tableValue the table value
   * @return the table name
   */
  private static String getTableName(Optional<DataSetSchema> dataSetSchema,
      Optional<TableValue> tableValue) {
    String tableName = "";
    if (dataSetSchema.isPresent() && tableValue.isPresent()) {
      String tableSchemaId = tableValue.get().getIdTableSchema();
      for (TableSchema tableschema : dataSetSchema.get().getTableSchemas()) {
        if (tableSchemaId.equals(tableschema.getIdTableSchema().toString())) {
          tableName = tableschema.getNameTableSchema();
          break;
        }
      }
    }
    return tableName;
  }

  /**
   * Gets the field schema name.
   *
   * @param datasetSchema the dataset schema
   * @param tableSchemaId the table schema id
   * @param fieldSchemaId the field schema id
   * @return the field schema name
   */
  private static String getFieldSchemaName(DataSetSchema datasetSchema, String tableSchemaId,
      String fieldSchemaId) {

    String fieldSchemaName = "";
    TableSchema tmpTableSchema = null;

    for (TableSchema tableSchema : datasetSchema.getTableSchemas()) {
      if (tableSchemaId.equals(tableSchema.getIdTableSchema().toString())) {
        tmpTableSchema = tableSchema;
        break;
      }
    }

    if (null != tmpTableSchema) {
      for (FieldSchema fieldSchema : tmpTableSchema.getRecordSchema().getFieldSchema()) {
        if (fieldSchemaId.equals(fieldSchema.getIdFieldSchema().toString())) {
          fieldSchemaName = fieldSchema.getHeaderName();
          break;
        }
      }
    }

    return fieldSchemaName;
  }

  /**
   * Creates the hash set.
   *
   * @param tableToEvaluate the table to evaluate
   * @return the sets the
   */
  private static Set<String> createHashSet(TableValue tableToEvaluate) {
    Set<String> fieldsToEvaluate = new HashSet<>();
    for (RecordValue record : tableToEvaluate.getRecords()) {
      for (FieldValue field : record.getFields()) {
        fieldsToEvaluate.add(field.getId());
      }
    }
    return fieldsToEvaluate;
  }

  /**
   * Execute field SQL rule validation.
   *
   * @param rule the rule
   * @param tableToEvaluate the table to evaluate
   * @param dataSetSchema the data set schema
   * @param tableValue the table value
   * @param tableName the table name
   */
  private static void executeFieldSQLRuleValidation(Rule rule, TableValue tableToEvaluate,
      Optional<DataSetSchema> dataSetSchema, Optional<TableValue> tableValue, String tableName) {

    if (!dataSetSchema.isPresent() || !tableValue.isPresent()) {
      return;
    }

    TableValue table = tableValue.get();
    String tableSchemaId = table.getIdTableSchema();
    String fieldSchemaId = rule.getReferenceId().toString();
    String fieldSchemaName = getFieldSchemaName(dataSetSchema.get(), tableSchemaId, fieldSchemaId);
    Set<String> fieldsToEvaluate = createHashSet(tableToEvaluate);

    for (RecordValue record : table.getRecords()) {
      for (FieldValue field : record.getFields()) {
        if (fieldSchemaId.equals(field.getIdFieldSchema())
            && fieldsToEvaluate.contains(field.getId())) {
          FieldValidation fieldValidation = new FieldValidation();
          fieldValidation.setFieldValue(field);
          fieldValidation.setValidation(createValidation(rule, tableName, fieldSchemaName,
              prepareSQLErrorMessage(field, rule, dataSetSchema, tableName)));
          List<FieldValidation> fieldValidations = field.getFieldValidations();
          if (null == fieldValidations || fieldValidations.isEmpty()) {
            fieldValidations = new ArrayList<>();
          }
          fieldValidations.add(fieldValidation);
          field.setFieldValidations(fieldValidations);
        }
      }
    }

    saveTable(table);
  }

  /**
   * Execute record SQL rule validation.
   *
   * @param rule the rule
   * @param tableToEvaluate the table to evaluate
   * @param dataSetSchema the data set schema
   * @param tableValue the table value
   * @param tableName the table name
   */
  private static void executeRecordSQLRuleValidation(Rule rule, TableValue tableToEvaluate,
      Optional<DataSetSchema> dataSetSchema, Optional<TableValue> tableValue, String tableName) {
    if (tableValue.isPresent()) {
      TableValue table = tableValue.get();
      List<String> recordsToEvauate = tableToEvaluate.getRecords().stream().map(RecordValue::getId)
          .collect(Collectors.toList());
      for (RecordValue record : table.getRecords()) {
        if (recordsToEvauate.contains(record.getId())) {
          List<RecordValidation> recordValidations = record.getRecordValidations();
          if (null == recordValidations || recordValidations.isEmpty()) {
            recordValidations = new ArrayList<>();
          }
          RecordValidation recordValidation = new RecordValidation();
          recordValidation.setRecordValue(record);
          recordValidation.setValidation(createValidation(rule, tableName, null,
              prepareSQLErrorMessage(record, rule, dataSetSchema, tableName)));
          recordValidations.add(recordValidation);
          record.setRecordValidations(recordValidations);
        }
      }
      saveTable(table);
    }
  }

  /**
   * Execute table SQL rule validation.
   *
   * @param rule the rule
   * @param tableToEvaluate the table to evaluate
   * @param dataSetSchema the data set schema
   * @param tableValue the table value
   * @param tableName the table name
   */
  private static void executeTableSQLRuleValidation(Rule rule, TableValue tableToEvaluate,
      Optional<DataSetSchema> dataSetSchema, Optional<TableValue> tableValue, String tableName) {
    if (tableValue.isPresent() && !tableToEvaluate.getRecords().isEmpty()) {
      TableValue table = tableValue.get();
      TableValidation tableValidation = new TableValidation();
      tableValidation.setTableValue(table);
      tableValidation.setValidation(createValidation(rule, tableName, null,
          prepareSQLErrorMessage(table, rule, dataSetSchema, tableName)));
      table.getTableValidations().add(tableValidation);
      saveTable(table);
    }
  }

  /**
   * Execute dataset SQL rule validation.
   *
   * @param datasetId the dataset id
   * @param rule the rule
   * @param tableName the table name
   * @param dataSetSchema the data set schema
   */
  private static void executeDatasetSQLRuleValidation(Long datasetId, Rule rule, String tableName,
      Optional<DataSetSchema> dataSetSchema) {
    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(new DatasetValue());
    DataSetMetabaseVO datasetMetabase =
        datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    Validation validationDataset = createValidation(rule, tableName, null,
        prepareSQLErrorMessage(dataset, rule, dataSetSchema, tableName));
    validationDataset.setTableName(datasetMetabase.getDataSetName());
    if (dataset.getDatasetValidations().isEmpty()) {
      DatasetValidation datasetValidation = new DatasetValidation();
      datasetValidation.setDatasetValue(dataset);
      datasetValidation.setValidation(validationDataset);
      List<DatasetValidation> datasetValidations = new ArrayList<>();
      datasetValidations.add(datasetValidation);
      dataset.setDatasetValidations(datasetValidations);
    } else {
      List<DatasetValidation> datasetValidations = dataset.getDatasetValidations();
      DatasetValidation datasetValidation = new DatasetValidation();
      datasetValidation.setDatasetValue(dataset);
      datasetValidation.setValidation(createValidation(rule, tableName, null,
          prepareSQLErrorMessage(dataset, rule, dataSetSchema, tableName)));
      datasetValidations.add(datasetValidation);
      dataset.setDatasetValidations(datasetValidations);
    }
    saveDataset(dataset);
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
    validation.setMessage(message);
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
   * Save dataset.
   *
   * @param dataset the dataset
   */
  @Transactional
  private static void saveDataset(DatasetValue dataset) {
    datasetRepository.save(dataset);
  }

  /**
   * Save table.
   *
   * @param table the table
   */
  @Transactional
  private static void saveTable(TableValue table) {
    tableRepository.save(table);
  }


  /**
   * Prepare SQL error message.
   *
   * @param object the object
   * @param rule the rule
   * @param dataSetSchema the data set schema
   * @return the string
   */
  private static String prepareSQLErrorMessage(Object object, Rule rule,
      Optional<DataSetSchema> dataSetSchema, String tableName) {
    String errorMessage = rule.getThenCondition().get(0);
    if (dataSetSchema.isPresent()) {
      String sql = rule.getSqlSentence();
      if (validateMessage(errorMessage)) {
        // get the fields from ruleMessage to replace later
        ArrayList<String> fieldsToReplace = getFieldsToReplace(errorMessage);
        // get the original field name, and id
        Map<String, String> fieldsAndSchemas =
            getFieldsAndSchemasFromSQL(sql, dataSetSchema.get(), tableName);
        errorMessage = rewriteMessage(object, errorMessage, fieldsToReplace, fieldsAndSchemas);
      }
    }
    return errorMessage;
  }

  /**
   * Rewrite message.
   *
   * @param object the object
   * @param errorMessage the error message
   * @param fieldsAndSchemas
   * @param fieldsToReplace
   * @return the string
   */
  private static String rewriteMessage(Object object, String errorMessage,
      ArrayList<String> fieldsToReplace, Map<String, String> fieldsAndSchemas) {
    if (object instanceof FieldValue) {
      FieldValue fvAux = (FieldValue) object;
      if (fieldsAndSchemas.size() == 1) {
        errorMessage = errorMessage.replace(fieldsToReplace.get(0), fvAux.getValue());
      } else {
        RecordValue rvAux = recordRepository.findFieldsByIdRecord(fvAux.getRecord().getId());
        for (String field : fieldsToReplace) {
          errorMessage =
              errorMessage.replace(field, getReplacement(field, rvAux, fieldsAndSchemas));
        }
      }
    } else if (object instanceof RecordValue) {
      RecordValue rvAux = (RecordValue) object;
      for (String field : fieldsToReplace) {
        errorMessage = errorMessage.replace(field, getReplacement(field, rvAux, fieldsAndSchemas));
      }
    }
    return errorMessage;
  }


  /**
   * Gets the replacement.
   *
   * @param rvAux the rv aux
   * @param fieldsAndSchemas the fields and schemas
   * @return the replacement
   */
  private static String getReplacement(String field, RecordValue rvAux,
      Map<String, String> fieldsAndSchemas) {
    String replacement = "";
    for (Map.Entry fieldNameOrAlias : fieldsAndSchemas.entrySet()) {
      for (FieldValue fieldAux : rvAux.getFields()) {
        if (field.contains(fieldNameOrAlias.getKey().toString())
            && fieldAux.getIdFieldSchema().equals(fieldNameOrAlias.getValue())) {
          replacement = fieldAux.getValue();
        }
      }
    }
    return replacement;
  }

  /**
   * Validate message.
   *
   * @param errorMessage the error message
   * @return true, if successful
   */
  private static boolean validateMessage(String errorMessage) {
    String regex = "\\{%\\w*%}";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(errorMessage);
    return m.find();
  }


  /**
   * Gets the fields to replace.
   *
   * @param errorMessage the error message
   * @return the fields to replace
   */
  private static ArrayList<String> getFieldsToReplace(String errorMessage) {
    ArrayList<String> auxList = new ArrayList<String>();
    String[] wordsList = errorMessage.split(" ");
    for (String word : wordsList) {
      if (word.startsWith("{%") && word.endsWith("%}")) {
        auxList.add(word);
      }
    }
    return auxList;
  }

  /**
   * Gets the fields and schemas from SQL.
   *
   * @param sql the sql
   * @param dataSetSchema the data set schema
   * @param tableName the table name
   * @return the fields and schemas from SQL
   */
  private static Map<String, String> getFieldsAndSchemasFromSQL(String sql,
      DataSetSchema dataSetSchema, String tableName) {
    Map<String, String> fieldsAndSchemas = new HashMap<>();
    String auxSQL = sql.toLowerCase().substring(sql.indexOf("select") + 6, sql.indexOf("from"));
    if ((auxSQL.contains("*") && auxSQL.contains(","))
        || (!auxSQL.contains("*") && auxSQL.contains(","))) {
      String[] fieldList = auxSQL.split(",");
      // get fields from Schema to replace value
      List<FieldSchema> indexFields = getNamesAndIdsFromSchema(dataSetSchema, tableName);
      for (String field : fieldList) {
        if (field.contains(".*")) {
          continue;
        }
        String queryField = "";
        String fieldAlias = "";
        String fieldTrimmed = field.trim();
        if (fieldTrimmed.contains(".") || fieldTrimmed.contains("as")) {

          fieldAlias = field.substring(fieldTrimmed.indexOf(' ') + 1).trim();

          if (fieldAlias.contains("as")) {
            fieldAlias = fieldAlias.trim().substring(fieldAlias.indexOf(' ')).trim();
          }
          queryField = fieldTrimmed
              .substring(fieldTrimmed.indexOf('.') + 1, fieldTrimmed.indexOf(' ') + 1).trim();
        } else {
          queryField = fieldTrimmed;
          fieldAlias = fieldTrimmed;
        }
        for (FieldSchema auxIndexField : indexFields) {
          if (auxIndexField.getHeaderName().equals(queryField)) {
            fieldsAndSchemas.put(fieldAlias, auxIndexField.getIdFieldSchema().toString());
          }
        }
      }
    }
    return fieldsAndSchemas;
  }

  /**
   * Gets the names and ids from schema.
   *
   * @param dataSetSchema the data set schema
   * @param tableName the table name
   * @return the names and ids from schema
   */
  private static List<FieldSchema> getNamesAndIdsFromSchema(DataSetSchema dataSetSchema,
      String tableName) {
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    for (TableSchema table : dataSetSchema.getTableSchemas()) {
      if (table.getNameTableSchema().equals(tableName)) {
        for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
          fieldSchemas.add(field);
        }
      }
    }
    return fieldSchemas;
  }

}
