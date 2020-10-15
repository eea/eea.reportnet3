package org.eea.validation.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
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
   * Execute validation SQL rule.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   */
  public static void executeValidationSQLRule(Long datasetId, String ruleId) {
    // Retrieve the rule
    Rule rule = sqlRulesService.getRule(datasetId, ruleId);
    // Retrieve sql sentence
    String query = rule.getSqlSentence();
    // Checking SQL sentence and if the rule does not work it will be disabled
    // TODO
    // Execute query
    TableValue tableToEvaluate = new TableValue();
    if (rule.getType().equals(EntityTypeEnum.TABLE)) {
      if (null != sqlRulesService.retriveFirstResult(query, datasetId)) {
        tableToEvaluate = tableRepository.findByIdTableSchema(rule.getReferenceId().toString());
      }
    } else {
      try {
        String preparedquery = "";
        if (query.contains(";")) {
          preparedquery = query.replace(";", "");
        } else {
          preparedquery = query;
        }
        tableToEvaluate = sqlRulesService.retrieveTableData(preparedquery, datasetId, rule);
      } catch (SQLException e) {
        LOG_ERROR.error("SQL can't be executed: ", e.getMessage(), e);
      }
    }
    if (null != tableToEvaluate.getId()) {
      String schemaId = datasetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);
      DataSetSchema schema =
          schemasRepository.findById(new ObjectId(schemaId)).orElse(new DataSetSchema());

      TableValue table = null;
      table = tableRepository.findById(tableToEvaluate.getId()).orElse(new TableValue());
      String tableName = "";
      for (TableSchema tableschema : schema.getTableSchemas()) {
        if (table.getIdTableSchema().equals(tableschema.getIdTableSchema().toString())) {
          tableName = tableschema.getNameTableSchema();
          break;
        }
      }
      String fieldName = null;



      EntityTypeEnum ruleType = rule.getType();
      switch (ruleType) {
        case DATASET:
          DatasetValue dataset = datasetRepository.findById(datasetId).orElse(new DatasetValue());
          DataSetMetabaseVO datasetMetabase =
              datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
          Validation validationDataset = createValidation(rule, tableName, null);
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
            datasetValidation.setValidation(createValidation(rule, tableName, null));
            datasetValidations.add(datasetValidation);
            dataset.setDatasetValidations(datasetValidations);
          }
          saveDataset(dataset);
          break;
        case TABLE:
          if (table.getTableValidations().isEmpty()) {
            TableValidation tableValidation = new TableValidation();
            tableValidation.setTableValue(tableToEvaluate);
            tableValidation.setValidation(createValidation(rule, tableName, null));
            List<TableValidation> tableValidations = new ArrayList<>();
            tableValidations.add(tableValidation);
            tableToEvaluate.setTableValidations(tableValidations);
          } else {
            List<TableValidation> tableValidations = tableToEvaluate.getTableValidations();
            TableValidation tablevalidation = new TableValidation();
            tablevalidation.setTableValue(tableToEvaluate);
            tablevalidation.setValidation(createValidation(rule, tableName, null));
            tableValidations.add(tablevalidation);
            tableToEvaluate.setTableValidations(tableValidations);
          }
          saveTable(table);
          break;
        case RECORD:
          List<String> recordsToEvauate = new ArrayList<>();
          tableToEvaluate.getRecords().stream().forEach(record -> {
            recordsToEvauate.add(record.getId());
          });

          for (RecordValue record : table.getRecords()) {
            if (recordsToEvauate.contains(record.getId())) {
              if (null == record.getRecordValidations()
                  || record.getRecordValidations().isEmpty()) {
                RecordValidation recordValidation = new RecordValidation();
                recordValidation.setRecordValue(record);
                recordValidation.setValidation(createValidation(rule, tableName, null));
                List<RecordValidation> recordValidations = new ArrayList<>();
                recordValidations.add(recordValidation);
                record.setRecordValidations(recordValidations);
              } else {
                List<RecordValidation> recordValidations = record.getRecordValidations();
                RecordValidation recordValidation = new RecordValidation();
                recordValidation.setRecordValue(record);
                recordValidation.setValidation(createValidation(rule, tableName, null));
                recordValidations.add(recordValidation);
                record.setRecordValidations(recordValidations);
              }
            }
          }
          saveTable(table);
          break;
        case FIELD:
          List<String> fieldsToEvauate = new ArrayList<>();
          tableToEvaluate.getRecords().stream().forEach(record -> {
            record.getFields().stream().forEach(field -> {
              fieldsToEvauate.add(field.getId());

            });
          });
          String fieldSchema = "";
          for (RecordValue record : table.getRecords()) {
            for (FieldValue field : record.getFields()) {
              if (rule.getReferenceId().toString().equals(field.getIdFieldSchema())) {
                fieldSchema = field.getIdFieldSchema();
                break;
              }
            }
          }

          for (TableSchema tableschema : schema.getTableSchemas()) {
            if (table.getIdTableSchema().equals(tableschema.getIdTableSchema().toString())) {
              for (FieldSchema field : tableschema.getRecordSchema().getFieldSchema()) {
                if (field.getIdFieldSchema().toString().equals(fieldSchema)) {
                  fieldName = field.getHeaderName();
                }
              }
            }
          }

          for (RecordValue record : table.getRecords()) {
            for (FieldValue field : record.getFields()) {
              if (rule.getReferenceId().toString().equals(field.getIdFieldSchema())) {
                if (fieldsToEvauate.contains(field.getId())) {
                  if (field.getFieldValidations().isEmpty()) {
                    FieldValidation fieldValidation = new FieldValidation();
                    fieldValidation.setFieldValue(field);
                    fieldValidation.setValidation(createValidation(rule, tableName, fieldName));
                    List<FieldValidation> fieldValidations = new ArrayList<>();
                    fieldValidations.add(fieldValidation);
                    field.setFieldValidations(fieldValidations);
                  } else {
                    List<FieldValidation> fieldValidations = field.getFieldValidations();
                    FieldValidation fieldValidation = new FieldValidation();
                    fieldValidation.setFieldValue(field);
                    fieldValidation.setValidation(createValidation(rule, tableName, fieldName));
                    fieldValidations.add(fieldValidation);
                    field.setFieldValidations(fieldValidations);
                  }
                }
              }
            }
          }
          saveTable(table);
          break;
      }
    }

  }

  private static Validation createValidation(Rule rule, String tableName, String fieldName) {
    Validation validation = new Validation();
    validation.setIdRule(rule.getRuleId().toString());
    validation.setLevelError(ErrorTypeEnum.valueOf(rule.getThenCondition().get(1)));
    validation.setMessage(rule.getThenCondition().get(0));
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
}
