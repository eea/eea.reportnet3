package org.eea.validation.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
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
import org.eea.validation.util.model.QueryVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The Class SQLValitaionUtils.
 */
@Component
public class SQLValidationUtils {

  /** The sql rules service. */
  @Autowired
  private SqlRulesService sqlRulesService;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The record repository. */
  @Autowired
  private RecordRepository recordRepository;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(SQLValidationUtils.class);

  /**
   * Execute validation SQL rule.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   * @param dataProviderCode the data provider code
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  public void executeValidationSQLRule(Long datasetId, String ruleId, String dataProviderCode)
          throws EEAInvalidSQLException {
    Rule rule = sqlRulesService.getRule(datasetId, ruleId);
    rule.setSqlSentence(rule.getSqlSentence().replace(";", ""));
    DataSetMetabaseVO dataSetMetabaseVO =
            datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    QueryVO queryVO = new QueryVO(null, rule, null, dataSetMetabaseVO, null, null);
    queryVO = getTableToEvaluate(queryVO, dataProviderCode);
    Integer totalRecords =
            Integer.parseInt(datasetRepository.evaluateSqlRule(datasetId, "with tableAux as ("
                    + queryVO.getNewQuery() + ") select cast(count(*) as text) from tableaux;"));
    Optional<TableValue> tableValue = tableRepository.findById(queryVO.getIdTable());
    TableValue tableToEvaluate = null;
    Optional<DataSetSchema> dataSetSchema =
            schemasRepository.findById(new ObjectId(dataSetMetabaseVO.getDatasetSchema()));
    Long nHeaders = 0L;
    if (dataSetSchema.isPresent()) {
      TableSchema tableSchema = dataSetSchema
              .get().getTableSchemas().stream().filter(tableSchemaAux -> tableValue.get()
                      .getIdTableSchema().equals(tableSchemaAux.getIdTableSchema().toString()))
              .findFirst().orElse(null);
      if (tableSchema != null) {
        nHeaders =
                tableSchema.getRecordSchema().getFieldSchema().stream().collect(Collectors.counting());
      }
    }
    int batchSize = 100000L / nHeaders < 30000 ? (int) (100000L / nHeaders) : 30000;
    for (int i = 0; i < totalRecords; i += batchSize) {
      tableToEvaluate = sqlRulesService
              .queryTable(queryVO.getNewQuery() + " OFFSET " + i + " LIMIT " + batchSize, queryVO);
      if (null != tableToEvaluate && null != tableToEvaluate.getId()
              && CollectionUtils.isNotEmpty(tableToEvaluate.getRecords())) {
        String tableName = "";
        if (tableValue.isPresent()) {
          tableName = getTableName(dataSetSchema, tableValue.get().getIdTableSchema());
          tableToEvaluate.setIdTableSchema(tableValue.get().getIdTableSchema());
        }
        switch (rule.getType()) {
          case DATASET:
            executeDatasetSQLRuleValidation(datasetId, rule, tableName, dataSetSchema);
            break;
          case TABLE:
            executeTableSQLRuleValidation(rule, tableToEvaluate, dataSetSchema, tableValue,
                    tableName);
            break;
          case RECORD:
            executeRecordSQLRuleValidation(rule, tableToEvaluate, dataSetSchema, tableName);
            break;
          case FIELD:
            executeFieldSQLRuleValidation(rule, tableToEvaluate, dataSetSchema, tableName);
            break;

        }
        tableToEvaluate.getRecords().clear();
        tableToEvaluate = null;
        System.gc();
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
  private QueryVO getTableToEvaluate(QueryVO queryVO, String dataProviderCode) {
    String query = queryVO != null ? queryVO.getRule().getSqlSentence() : null;
    try {
      if (query != null) {
        String preparedquery = query.contains(";") ? query.replace(";", "") : query;
        String providerCodeAux = "XX";
        if (dataProviderCode != null && !"null".equals(dataProviderCode)) {
          DataProviderVO providerCode =
                  representativeControllerZuul.findDataProviderById(Long.valueOf(dataProviderCode));
          if (null != providerCode && StringUtils.isNotBlank(providerCode.getCode())) {
            providerCodeAux = providerCode.getCode();
          }
        }
        preparedquery = preparedquery.replace("{%R3_COUNTRY_CODE%}", providerCodeAux);
        preparedquery = preparedquery.replace("{%R3_COMPANY_CODE%}", providerCodeAux);
        preparedquery = preparedquery.replace("{%R3_ORGANIZATION_CODE%}", providerCodeAux);
        queryVO = sqlRulesService.retrieveTableData(preparedquery, queryVO, Boolean.FALSE);

      } else {
        throw new EEAInvalidSQLException("No sql found");
      }
    } catch (EEAInvalidSQLException e) {
      LOG.error("SQL can't be executed: {}", e.getMessage(), e);
    }
    return queryVO;
  }

  /**
   * Gets the table name.
   *
   * @param dataSetSchema the data set schema
   * @param tableValue the table value
   * @return the table name
   */
  private String getTableName(Optional<DataSetSchema> dataSetSchema, String tableSchemaId) {
    String tableName = "";
    if (dataSetSchema.isPresent() && tableSchemaId != null) {
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
  private String getFieldSchemaName(DataSetSchema datasetSchema, String tableSchemaId,
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
  private Set<String> createHashSet(TableValue tableToEvaluate) {
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
  private void executeFieldSQLRuleValidation(Rule rule, TableValue tableToEvaluate,
                                             Optional<DataSetSchema> dataSetSchema, String tableName) {
    List<RecordValue> records = recordRepository.findByIds(
            tableToEvaluate.getRecords().stream().map(RecordValue::getId).collect(Collectors.toList()));
    if (!dataSetSchema.isPresent() || records.isEmpty()) {
      return;
    }
    String tableSchemaId = tableToEvaluate.getIdTableSchema();
    String fieldSchemaId = rule.getReferenceId().toString();
    String fieldSchemaName = getFieldSchemaName(dataSetSchema.get(), tableSchemaId, fieldSchemaId);
    Set<String> fieldsToEvaluate = createHashSet(tableToEvaluate);
    for (RecordValue record : records) {
      for (FieldValue field : record.getFields()) {
        if (fieldSchemaId.equals(field.getIdFieldSchema())
                && fieldsToEvaluate.contains(field.getId())) {
          FieldValidation fieldValidation = new FieldValidation();
          fieldValidation.setFieldValue(field);
          fieldValidation.setValidation(createValidation(rule, tableName, fieldSchemaName,
                  prepareSQLErrorMessage(field, rule, dataSetSchema, tableToEvaluate, record)));
          List<FieldValidation> fieldValidations = field.getFieldValidations();
          if (null == fieldValidations || fieldValidations.isEmpty()) {
            fieldValidations = new ArrayList<>();
          }
          fieldValidations.add(fieldValidation);
          field.setFieldValidations(fieldValidations);
        }
      }
    }
    tableToEvaluate = null;
    saveRecords(records);
    fieldsToEvaluate.clear();
    fieldsToEvaluate = null;
    records.clear();
    System.gc();
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
  private void executeRecordSQLRuleValidation(Rule rule, TableValue tableToEvaluate,
                                              Optional<DataSetSchema> dataSetSchema, String tableName) {
    List<RecordValue> records = recordRepository.findByIds(
            tableToEvaluate.getRecords().stream().map(RecordValue::getId).collect(Collectors.toList()));
    if (!records.isEmpty()) {
      List<String> recordsToEvauate = tableToEvaluate.getRecords().stream().map(RecordValue::getId)
              .collect(Collectors.toList());
      for (RecordValue record : records) {
        if (recordsToEvauate.contains(record.getId())) {
          List<RecordValidation> recordValidations = record.getRecordValidations();
          if (null == recordValidations || recordValidations.isEmpty()) {
            recordValidations = new ArrayList<>();
          }
          RecordValidation recordValidation = new RecordValidation();
          recordValidation.setRecordValue(record);
          recordValidation.setValidation(createValidation(rule, tableName, null,
                  prepareSQLErrorMessage(record, rule, dataSetSchema, tableToEvaluate, record)));
          recordValidations.add(recordValidation);
          record.setRecordValidations(recordValidations);
        }
      }
      saveRecords(records);
      records.clear();
      System.gc();
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
  private void executeTableSQLRuleValidation(Rule rule, TableValue tableToEvaluate,
                                             Optional<DataSetSchema> dataSetSchema, Optional<TableValue> tableValue, String tableName) {
    if (tableValue.isPresent() && !tableToEvaluate.getRecords().isEmpty()) {
      TableValue table = tableValue.get();
      TableValidation tableValidation = new TableValidation();
      tableValidation.setTableValue(table);
      tableValidation.setValidation(createValidation(rule, tableName, null,
              prepareSQLErrorMessage(table, rule, dataSetSchema, tableToEvaluate, null)));
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
  private void executeDatasetSQLRuleValidation(Long datasetId, Rule rule, String tableName,
                                               Optional<DataSetSchema> dataSetSchema) {
    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(new DatasetValue());
    DataSetMetabaseVO datasetMetabase =
            datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    Validation validationDataset = createValidation(rule, tableName, null,
            prepareSQLErrorMessage(dataset, rule, dataSetSchema, null, null));
    validationDataset.setTableName(datasetMetabase.getDataSetName());
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setDatasetValue(dataset);
    datasetValidation.setValidation(validationDataset);
    List<DatasetValidation> datasetValidations =
            dataset.getDatasetValidations() != null ? dataset.getDatasetValidations()
                    : new ArrayList<>();
    datasetValidations.add(datasetValidation);
    dataset.setDatasetValidations(datasetValidations);
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
  private Validation createValidation(Rule rule, String tableName, String fieldName,
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
  private void saveDataset(DatasetValue dataset) {
    datasetRepository.save(dataset);
  }

  /**
   * Save table.
   *
   * @param table the table
   */
  @Transactional
  @Modifying(clearAutomatically = true)
  private void saveTable(TableValue table) {
    tableRepository.save(table);
  }

  /**
   * Save records.
   *
   * @param recordValues the record values
   */
  @Transactional
  @Modifying(clearAutomatically = true)
  private void saveRecords(List<RecordValue> recordValues) {
    recordRepository.flush();
    recordRepository.saveAll(recordValues);
    recordRepository.flush();
  }

  /**
   * Prepare SQL error message.
   *
   * @param object the object
   * @param rule the rule
   * @param dataSetSchema the data set schema
   * @param tableName the table name
   * @param tableToEvaluate
   * @return the string
   */
  private String prepareSQLErrorMessage(Object object, Rule rule,
                                        Optional<DataSetSchema> dataSetSchema, TableValue tableToEvaluate, RecordValue record) {
    String errorMessage = rule.getThenCondition().get(0);
    if (dataSetSchema.isPresent()) {
      String sql = rule.getSqlSentence();
      if (validateMessage(errorMessage)) {
        LOG.info("SQL Rule for check: {}", sql);
        LOG.info("Message Rule for check: {}", errorMessage);
        // get the fields from ruleMessage to replace later
        ArrayList<String> fieldsToReplace = getFieldsToReplace(errorMessage);
        errorMessage =
                rewriteMessage(object, errorMessage, fieldsToReplace, tableToEvaluate, record);
      } else {
        if (tableToEvaluate != null) {
          tableToEvaluate.getRecords().clear();
        }
      }
    }
    return errorMessage;
  }

  /**
   * Rewrite message.
   *
   * @param object the object
   * @param errorMessage the error message
   * @param fieldsToReplace the fields to replace
   * @param fieldsAndSchemas the fields and schemas
   * @param tableToEvaluate
   * @return the string
   */
  private String rewriteMessage(Object object, String errorMessage,
                                ArrayList<String> fieldsToReplace, TableValue tableToEvaluate, RecordValue rvAux) {
    if (object instanceof FieldValue) {
      for (String field : fieldsToReplace) {
        errorMessage = errorMessage.replace(field, getReplacement(field, rvAux, tableToEvaluate));
      }
    } else if (object instanceof RecordValue) {
      rvAux = (RecordValue) object;
      for (String field : fieldsToReplace) {
        errorMessage = errorMessage.replace(field, getReplacement(field, rvAux, tableToEvaluate));
      }
    } else if (object instanceof TableValue) {
      TableValue tvAux = (TableValue) object;
      for (String field : fieldsToReplace) {
        if (null != tvAux) {
          String replacement = "";
          for (RecordValue record : tvAux.getRecords()) {
            replacement = getReplacement(field, record, tableToEvaluate);
            if (!"".equals(replacement)) {
              break;
            }
          }
          errorMessage = errorMessage.replace(field, replacement);
        }
      }
    }
    return errorMessage;
  }


  /**
   * Gets the replacement.
   *
   * @param field the field
   * @param rvAux the Auxiliary RecordValue
   * @param tableToEvaluate The table to evaluate
   * @return the replacement
   */
  private String getReplacement(String field, RecordValue rvAux, TableValue tableToEvaluate) {
    String replacement = "";
    field = field.replaceAll("[{%}]", "");
    for (RecordValue record : tableToEvaluate.getRecords()) {
      if (record.getId().equals(rvAux.getId())) {
        for (FieldValue aux : record.getFields()) {
          if ((null != aux.getValue() || null != aux.getColumnName())
                  && field.equalsIgnoreCase(aux.getColumnName())
                  && (!aux.getColumnName().toLowerCase().contains("_id"))) {
            replacement = aux.getValue();
            break;
          }
        }
      }
    }
    return (null == replacement) ? "" : replacement;
  }

  /**
   * Validate message.
   *
   * @param errorMessage the error message
   * @return true, if successful
   */
  private boolean validateMessage(String errorMessage) {
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
  private ArrayList<String> getFieldsToReplace(String errorMessage) {
    ArrayList<String> auxList = new ArrayList<>();
    String regex = "\\{%\\w*%}";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(errorMessage);
    while (m.find()) {
      auxList.add(m.group());
    }
    return auxList;
  }

}
