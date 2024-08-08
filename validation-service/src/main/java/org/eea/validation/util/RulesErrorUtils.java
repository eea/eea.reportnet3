package org.eea.validation.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
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
import org.eea.validation.persistence.data.repository.FieldRepository;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class KieBaseManager.
 */
@Component
public class RulesErrorUtils {

  private static final Logger LOG = LoggerFactory.getLogger(RulesErrorUtils.class);

  private static final int THIRTYFOUR = 34;

  private static final int TEN = 10;

  /**
   * The schemas repository.
   */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /**
   * The field repository.
   */
  @Autowired
  private FieldRepository fieldRepository;

  /**
   * The dataset repository.
   */
  @Autowired
  private DatasetRepository datasetRepository;

  /**
   * The record repository.
   */
  @Autowired
  private RecordRepository recordRepository;

  /**
   * The table repository.
   */
  @Autowired
  private TableRepository tableRepository;

  /** The dataset metabase controller. */
  @Autowired
  private DatasetMetabaseController datasetMetabaseController;


  /** The Constant timeZone. */
  private static final ZoneId timeZone = ZoneId.of("UTC");


  /** The Constant MESSAGE_ERROR_VALIDATION. */
  private static final String MESSAGE_ERROR_VALIDATION = "Error executing rule with code: ";


  /** The Constant dateFormatter. */
  private static final DateTimeFormatter dateFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");


  /**
   * Creates the rule error exception.
   *
   * @param lvValue the lv value
   * @param e the e
   */
  public void createRuleErrorException(Object lvValue, RuntimeException e) {
    LOG.info("[272589-PROCESS] - ENTER createRuleErrorException method");

    Validation ruleValidation = new Validation();

    // with that part we extract the id rule who fail
    String idRuleException = e.getMessage();
    String idRule = idRuleException.substring(idRuleException.indexOf("in [Rule \"") + TEN,
        idRuleException.indexOf("in [Rule \"") + THIRTYFOUR);
    ruleValidation.setIdRule(idRule);

    // put more datas to create validation
    ruleValidation.setLevelError(ErrorTypeEnum.BLOCKER);
    ruleValidation.setValidationDate(ZonedDateTime.now(timeZone).format(dateFormatter));


    // we create the validation depends of the type value
    switch (lvValue.getClass().getName()) {
      case "org.eea.validation.persistence.data.domain.FieldValue":

        FieldValue fieldValue = fieldValueDatas(lvValue, ruleValidation, idRule);

        // we create the validation to save it with all datas for a fieldValidation
        FieldValidation ruleFVValidation = new FieldValidation();
        ruleFVValidation.setValidation(ruleValidation);
        List<FieldValidation> ruleFVValidations = new ArrayList<>();
        ruleFVValidations.add(ruleFVValidation);
        fieldValue.setFieldValidations(ruleFVValidations);
        fieldRepository.save(fieldValue);

        break;
      case "org.eea.validation.persistence.data.domain.RecordValue":

        RecordValue recordValue = recordValueDatas(lvValue, ruleValidation, idRule);

        // we create the validation to save it with all datas for a recordValidation
        RecordValidation ruleRCValidation = new RecordValidation();
        ruleRCValidation.setValidation(ruleValidation);
        List<RecordValidation> ruleRCValidations = new ArrayList<>();
        ruleRCValidations.add(ruleRCValidation);
        recordValue.setRecordValidations(ruleRCValidations);
        recordRepository.save(recordValue);

        break;
      case "org.eea.validation.persistence.data.domain.TableValue":
        LOG.info("[272589-PROCESS] - ENTER createRuleErrorException method - TableValue object");

        // we convert the Object to TableValue
        TableValue tableValue = tableValueDatas(lvValue, ruleValidation, idRule);

        // we create the validation to save it with all datas for a TableValidation
        TableValidation ruleTBValidation = new TableValidation();
        ruleTBValidation.setValidation(ruleValidation);
        List<TableValidation> ruleTBValidations = new ArrayList<>();
        ruleTBValidations.add(ruleTBValidation);
        tableValue.setTableValidations(ruleTBValidations);
        tableRepository.save(tableValue);
        LOG.info("[272589-PROCESS] - EXIT createRuleErrorException method - TableValue object");

        break;
      case "org.eea.validation.persistence.data.domain.DatasetValue":

        DatasetValue datasetValue = datasetValueDatas(lvValue, ruleValidation, idRule);

        // we create the validation to save it with all datas for a DatasetValidation
        DatasetValidation ruleDSValidation = new DatasetValidation();
        ruleDSValidation.setValidation(ruleValidation);
        List<DatasetValidation> ruleDSValidations = new ArrayList<>();
        ruleDSValidations.add(ruleDSValidation);
        datasetValue.setDatasetValidations(ruleDSValidations);
        datasetRepository.save(datasetValue);

        break;
      default:
        break;
    }

    LOG.info("[272589-PROCESS] - EXIT createRuleErrorException method");

  }


  /**
   * Dataset value datas.
   *
   * @param lvValue the lv value
   * @param ruleValidation the rule validation
   * @param idRule the id rule
   * @return the dataset value
   */
  private DatasetValue datasetValueDatas(Object lvValue, Validation ruleValidation, String idRule) {
    // we convert the Object to DatasetValue
    DatasetValue datasetValue = (DatasetValue) lvValue;
    ruleValidation.setTypeEntity(EntityTypeEnum.DATASET);

    DataSetMetabaseVO dataSetMetabaseVO =
        datasetMetabaseController.findDatasetMetabaseById(datasetValue.getId());
    Rule ruleDataset = rulesRepository.findRule(new ObjectId(datasetValue.getIdDatasetSchema()),
        new ObjectId(idRule));

    // we put the origin name and shorcode for the new validation
    ruleValidation
        .setTableName(null == dataSetMetabaseVO ? "" : dataSetMetabaseVO.getDataSetName());

    if (ruleDataset != null) {
      ruleValidation.setShortCode(ruleDataset.getShortCode());
      ruleValidation.setMessage(MESSAGE_ERROR_VALIDATION + ruleDataset.getShortCode());
    }
    return datasetValue;
  }


  /**
   * Table value datas.
   *
   * @param lvValue the lv value
   * @param ruleValidation the rule validation
   * @param idRule the id rule
   * @return the table value
   */
  private TableValue tableValueDatas(Object lvValue, Validation ruleValidation, String idRule) {
    TableValue tableValue = (TableValue) lvValue;
    ruleValidation.setTypeEntity(EntityTypeEnum.TABLE);

    DataSetSchema dataSetSchemaTable = schemasRepository
        .findByIdDataSetSchema(new ObjectId(tableValue.getDatasetId().getIdDatasetSchema()));

    Rule ruleTable = rulesRepository.findRule(
        new ObjectId(tableValue.getDatasetId().getIdDatasetSchema()), new ObjectId(idRule));

    // we put the origin name for the new validation
    if (ruleTable != null) {
      for (TableSchema table : dataSetSchemaTable.getTableSchemas()) {
        if (table.getIdTableSchema().equals(ruleTable.getReferenceId())) {
          ruleValidation.setTableName(table.getNameTableSchema());
        }
      }
      ruleValidation.setShortCode(ruleTable.getShortCode());
      ruleValidation.setMessage(MESSAGE_ERROR_VALIDATION + ruleTable.getShortCode());
    }
    return tableValue;
  }


  /**
   * Record value datas.
   *
   * @param lvValue the lv value
   * @param ruleValidation the rule validation
   * @param idRule the id rule
   * @return the record value
   */
  private RecordValue recordValueDatas(Object lvValue, Validation ruleValidation, String idRule) {
    // we convert the Object to recordValue
    RecordValue recordValue = (RecordValue) lvValue;
    ruleValidation.setTypeEntity(EntityTypeEnum.RECORD);

    DataSetSchema dataSetSchemaRecord = schemasRepository.findByIdDataSetSchema(
        new ObjectId(recordValue.getTableValue().getDatasetId().getIdDatasetSchema()));

    Rule ruleRecord =
        rulesRepository.findRule(dataSetSchemaRecord.getIdDataSetSchema(), new ObjectId(idRule));

    // we put the origin name for the new validation
    if (ruleRecord != null) {
      for (TableSchema table : dataSetSchemaRecord.getTableSchemas()) {
        if (table.getRecordSchema().getIdRecordSchema().equals(ruleRecord.getReferenceId())) {
          ruleValidation.setTableName(table.getNameTableSchema());
        }
      }
      ruleValidation.setShortCode(ruleRecord.getShortCode());
      ruleValidation.setMessage(MESSAGE_ERROR_VALIDATION + ruleRecord.getShortCode());
    }
    return recordValue;
  }


  /**
   * Field value datas.
   *
   * @param lvValue the lv value
   * @param ruleValidation the rule validation
   * @param idRule the id rule
   * @return the field value
   */
  private FieldValue fieldValueDatas(Object lvValue, Validation ruleValidation, String idRule) {
    // we convert the Object to fieldValue
    FieldValue fieldValue = (FieldValue) lvValue;
    ruleValidation.setTypeEntity(EntityTypeEnum.FIELD);

    DataSetSchema dataSetSchemaField = schemasRepository.findByIdDataSetSchema(
        new ObjectId(fieldValue.getRecord().getTableValue().getDatasetId().getIdDatasetSchema()));

    Rule ruleField =
        rulesRepository.findRule(dataSetSchemaField.getIdDataSetSchema(), new ObjectId(idRule));

    // we put the origin name for the new validation
    if (ruleField != null) {
      for (TableSchema table : dataSetSchemaField.getTableSchemas()) {
        for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
          if (field.getIdFieldSchema().equals(ruleField.getReferenceId())) {
            ruleValidation.setTableName(table.getNameTableSchema());
            ruleValidation.setFieldName(field.getHeaderName());
          }
        }
      }
      ruleValidation.setShortCode(ruleField.getShortCode());
      ruleValidation.setMessage(MESSAGE_ERROR_VALIDATION + ruleField.getShortCode());
    }
    return fieldValue;
  }
}
