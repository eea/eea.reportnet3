package org.eea.validation.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.TestDatasetController.TestDatasetControllerZuul;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.exception.EEAInvalidSQLException;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.SqlRulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * The Class SqlRulesServiceImpl.
 */
@Service("SQLRulesService")
public class SqlRulesServiceImpl implements SqlRulesService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(SqlRulesServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant KEYWORDS: {@value}. */
  private static final String KEYWORDS = "DELETE,INSERT,DROP";

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset schema controller. */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaController;

  /** The dataset metabase controller. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  /** The eu dataset controller. */
  @Autowired
  private EUDatasetControllerZuul euDatasetController;

  /** The data collection controller. */
  @Autowired
  private DataCollectionControllerZuul dataCollectionController;

  /** The representative controller. */
  @Autowired
  private RepresentativeControllerZuul representativeController;

  /** The test dataset controller zuul. */
  @Autowired
  private TestDatasetControllerZuul testDatasetControllerZuul;

  /** The rule mapper. */
  @Autowired
  private RuleMapper ruleMapper;

  /** The Constant DATASET_: {@value}. */
  private static final String DATASET = "dataset_";

  /** The Constant INNER_JOIN_DATASET: {@value}. */
  private static final String INNER_JOIN_DATASET = "inner join dataset_";

  /**
   * Validate SQL rule.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   */
  @Async
  @Override
  @Transactional
  public void validateSQLRule(Long datasetId, String datasetSchemaId, Rule rule) {

    EventType notificationEventType = null;
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName())
        .datasetSchemaId(datasetSchemaId).shortCode(rule.getShortCode())
        .error("The QC Rule is disabled").build();

    String query = proccessQuery(datasetId, rule.getSqlSentence());

    if (validateRule(query, datasetId, rule, Boolean.TRUE)) {
      notificationEventType = EventType.VALIDATED_QC_RULE_EVENT;
      rule.setVerified(true);
      LOG.info("Rule validation passed: {}", rule);
    } else {
      notificationEventType = EventType.INVALIDATED_QC_RULE_EVENT;
      rule.setVerified(false);
      rule.setEnabled(false);
      LOG.info("Rule validation not passed: {}", rule);
    }
    rule.setWhenCondition(new StringBuilder().append("isSQLSentenceWithCode(this.datasetId.id, '")
        .append(rule.getRuleId().toString())
        .append(
            "', this.records.size > 0 && this.records.get(0) != null && this.records.get(0).dataProviderCode != null ? this.records.get(0).dataProviderCode : 'XX'")
        .append(")").toString());

    rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
    releaseNotification(notificationEventType, notificationVO);
  }

  /**
   * Validate SQL rule from datacollection.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param ruleVO the rule VO
   * @return true, if successful
   */
  @Override
  public boolean validateSQLRuleFromDatacollection(Long datasetId, String datasetSchemaId,
      RuleVO ruleVO) {
    Rule rule = ruleMapper.classToEntity(ruleVO);

    String query = proccessQuery(datasetId, ruleVO.getSqlSentence());
    boolean verifAndEnabled = true;
    if (!validateRule(query, datasetId, rule, Boolean.TRUE)) {
      LOG.info("Rule validation not passed before pass to datacollection: {}", rule);
      verifAndEnabled = false;
      rule.setEnabled(verifAndEnabled);
    }
    rule.setVerified(verifAndEnabled);
    rule.setWhenCondition(new StringBuilder().append("isSQLSentenceWithCode(this.datasetId.id,'")
        .append(rule.getRuleId().toString()).append("', 'XX'").append(")").toString());
    rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);

    return verifAndEnabled;
  }

  /**
   * Gets the rule.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   * @return the rule
   */
  @Override
  public Rule getRule(Long datasetId, String ruleId) {
    String datasetSchemaId = datasetMetabaseController.findDatasetSchemaIdById(datasetId);
    RulesSchema rulechema =
        rulesRepository.getActiveAndVerifiedRules(new ObjectId(datasetSchemaId));
    for (Rule rule : rulechema.getRules()) {
      if (rule.getRuleId().equals(new ObjectId(ruleId))) {
        return rule;
      }
    }
    return null;
  }

  /**
   * Retrieve table data.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @param rule the rule
   * @param ischeckDC the ischeck DC
   * @return the table value
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  @Override
  public TableValue retrieveTableData(String query, Long datasetId, Rule rule, Boolean ischeckDC)
      throws EEAInvalidSQLException {
    DataSetSchemaVO schema = datasetSchemaController.findDataSchemaByDatasetId(datasetId);
    String entityName = "";
    Long idTable = null;

    String newQuery = proccessQuery(datasetId, query);

    switch (rule.getType()) {
      case FIELD:
        entityName = retriveFieldName(schema, rule.getReferenceId().toString());
        idTable =
            retriveIsTableFromFieldSchema(schema, rule.getReferenceId().toString(), datasetId);
        break;
      case TABLE:
        entityName = retriveTableName(schema, rule.getReferenceId().toString());
        idTable = datasetRepository.getTableId(rule.getReferenceId().toString(), datasetId);
        break;
      case RECORD:
        idTable =
            retriveIsTableFromRecordSchema(schema, rule.getReferenceId().toString(), datasetId);
        break;
      case DATASET:
        break;
    }
    LOG.info("Query to be executed: {}", newQuery);
    TableValue table = datasetRepository.queryRSExecution(newQuery, rule.getType(), entityName,
        datasetId, idTable);
    if (Boolean.FALSE.equals(ischeckDC) && null != table && null != table.getRecords()
        && !table.getRecords().isEmpty() && !EntityTypeEnum.TABLE.equals(rule.getType())) {
      retrieveValidations(table.getRecords(), datasetId);
    }
    return table;
  }

  /**
   * Validate SQL rules.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param showNotification the show notification
   */
  @Async
  @Override
  public void validateSQLRules(Long datasetId, String datasetSchemaId, Boolean showNotification) {
    List<RuleVO> rulesSql =
        ruleMapper.entityListToClass(rulesRepository.findSqlRules(new ObjectId(datasetSchemaId)));
    Long dataflowId = datasetMetabaseController.findDatasetMetabaseById(datasetId).getDataflowId();

    if (null != rulesSql && !rulesSql.isEmpty()) {
      rulesSql.stream().forEach(ruleVO -> {
        Rule rule = ruleMapper.classToEntity(ruleVO);
        if (validateRule(ruleVO.getSqlSentence(), datasetId, rule, Boolean.TRUE)) {
          rule.setVerified(true);
        } else {
          rule.setVerified(false);
          rule.setEnabled(false);
        }
        rule.setWhenCondition(new StringBuilder()
            .append("isSQLSentenceWithCode(this.datasetId.id, '")
            .append(rule.getRuleId().toString())
            .append(
                "', this.records.size > 0 && this.records.get(0) != null && this.records.get(0).dataProviderCode != null ? this.records.get(0).dataProviderCode : 'XX'")
            .append(")").toString());
        rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
      });
    }

    RulesSchema rulesDisabledSchema =
        rulesRepository.getAllDisabledRules(new ObjectId(datasetSchemaId));
    RulesSchema rulesUncheckedSchema =
        rulesRepository.getAllUncheckedRules(new ObjectId(datasetSchemaId));
    int rulesUnchecked = rulesUncheckedSchema.getRules().size();
    int rulesDisabled = rulesDisabledSchema.getRules().size();

    if (rulesDisabled > 0 || rulesUnchecked > 0) {
      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName())
          .datasetId(datasetId).dataflowId(dataflowId).invalidRules(rulesUnchecked)
          .disabledRules(rulesDisabled).build();
      LOG.info("SQL rules contains errors");
      if (showNotification == null || showNotification) {
        releaseNotification(EventType.VALIDATE_RULES_ERROR_EVENT, notificationVO);
      }
    } else {

      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName())
          .datasetId(datasetId).dataflowId(dataflowId).build();
      LOG.info("SQL rules contains 0 errors");
      if (showNotification == null || showNotification) {
        releaseNotification(EventType.VALIDATE_RULES_COMPLETED_EVENT, notificationVO);
      }
    }
  }

  /**
   * Release notification.
   *
   * @param eventType the event type
   * @param notificationVO the notification VO
   */
  private void releaseNotification(EventType eventType, NotificationVO notificationVO) {
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Unable to release notification: {}, {}", eventType, notificationVO);
    }
  }

  /**
   * Validate rule.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @param rule the rule
   * @param ischeckDC the ischeck DC
   * @return the boolean
   */
  private boolean validateRule(String query, Long datasetId, Rule rule, Boolean ischeckDC) {
    boolean isSQLCorrect = true;
    // validate query
    if (!StringUtils.isBlank(query)) {
      // validate query sintax
      if (checkQuerySyntax(query)) {
        try {
          checkQueryTestExecution(query.replace(";", ""), datasetId, rule);
        } catch (EEAInvalidSQLException e) {
          LOG_ERROR.error("SQL is not correct: {}", e.getMessage(), e);
          isSQLCorrect = false;
        }
      } else {
        isSQLCorrect = false;
      }
    } else {
      isSQLCorrect = false;
    }
    if (!isSQLCorrect) {
      LOG.info("Rule validation not passed: {}", rule);
    } else {
      LOG.info("Rule validation passed: {}", rule);
    }
    return isSQLCorrect;
  }

  /**
   * Check query syntax.
   *
   * @param query the query
   * @return the boolean
   */
  private boolean checkQuerySyntax(String query) {
    boolean queryContainsKeyword = true;
    String[] queryKeywords = KEYWORDS.split(",");
    for (String word : queryKeywords) {
      if (query.toLowerCase().contains(word.toLowerCase())) {
        queryContainsKeyword = false;
        break;
      }
    }
    return queryContainsKeyword;
  }

  /**
   * Check query test execution.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @param rule the rule
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  private void checkQueryTestExecution(String query, Long datasetId, Rule rule)
      throws EEAInvalidSQLException {
    String newQuery = proccessQuery(datasetId, query);
    datasetRepository.validateQuery("explain " + newQuery, datasetId);
  }

  /**
   * Retrive is table from field schema.
   *
   * @param schema the schema
   * @param fieldSchemaId the field schema id
   * @param datasetId the dataset id
   * @return the long
   */
  @Transactional
  private Long retriveIsTableFromFieldSchema(DataSetSchemaVO schema, String fieldSchemaId,
      Long datasetId) {
    String tableSchemaId = "";
    for (TableSchemaVO table : schema.getTableSchemas()) {
      for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
        if (field.getId().equals(fieldSchemaId)) {
          tableSchemaId = table.getIdTableSchema();
        }
      }
    }
    return datasetRepository.getTableId(tableSchemaId, datasetId);
  }

  /**
   * Retrive is table from record schema.
   *
   * @param schema the schema
   * @param recordSchemaId the record schema id
   * @param datasetId the dataset id
   * @return the long
   */
  @Transactional
  private Long retriveIsTableFromRecordSchema(DataSetSchemaVO schema, String recordSchemaId,
      Long datasetId) {
    String tableSchemaId = "";
    for (TableSchemaVO table : schema.getTableSchemas()) {
      if (table.getRecordSchema().getIdRecordSchema().equals(recordSchemaId)) {
        tableSchemaId = table.getIdTableSchema();
      }
    }
    return datasetRepository.getTableId(tableSchemaId, datasetId);
  }

  /**
   * Retrive table name.
   *
   * @param schema the schema
   * @param idTableSchema the id table schema
   * @return the string
   */
  private String retriveTableName(DataSetSchemaVO schema, String idTableSchema) {
    String tableName = "";
    for (TableSchemaVO table : schema.getTableSchemas()) {
      if (table.getIdTableSchema().equals(idTableSchema)) {
        tableName = table.getNameTableSchema();
      }
    }
    return tableName;
  }

  /**
   * Retrive field name.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   * @return the string
   */
  private String retriveFieldName(DataSetSchemaVO schema, String idFieldSchema) {
    String fieldName = "";
    for (TableSchemaVO table : schema.getTableSchemas()) {
      for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
        if (field.getId().equals(idFieldSchema)) {
          fieldName = field.getName();
        }
      }
    }
    return fieldName;
  }

  /**
   * Retrieve validations.
   *
   * @param records the records
   * @param datasetId the dataset id
   */
  private void retrieveValidations(List<RecordValue> records, Long datasetId) {
    // retrieve validations to set them into the final result
    List<String> recordIds = records.stream().map(RecordValue::getId).collect(Collectors.toList());
    Map<String, List<FieldValidation>> fieldValidations = getFieldValidations(recordIds, datasetId);
    Map<String, List<RecordValidation>> recordValidations =
        getRecordValidations(recordIds, datasetId);
    records.stream().forEach(record -> {
      record.getFields().stream().filter(Objects::nonNull).forEach(field -> {
        List<FieldValidation> validations = fieldValidations.get(field.getId());
        field.setFieldValidations(validations);
        if (null != validations && !validations.isEmpty()) {
          field.setLevelError(
              validations.stream().map(validation -> validation.getValidation().getLevelError())
                  .filter(error -> error.equals(ErrorTypeEnum.ERROR)).findFirst()
                  .orElse(ErrorTypeEnum.WARNING));
        }
      });

      List<RecordValidation> validations = recordValidations.get(record.getId());
      record.setRecordValidations(validations);
      if (null != validations && !validations.isEmpty()) {
        record.setLevelError(
            validations.stream().map(validation -> validation.getValidation().getLevelError())
                .filter(error -> error.equals(ErrorTypeEnum.ERROR)).findFirst()
                .orElse(ErrorTypeEnum.WARNING));
      }
    });
  }

  /**
   * Gets the field validations.
   *
   * @param recordIds the record ids
   * @param datasetId the dataset id
   * @return the field validations
   */
  private Map<String, List<FieldValidation>> getFieldValidations(final List<String> recordIds,
      Long datasetId) {

    StringBuilder query = new StringBuilder("select fval.ID as field_val_id," + "v.ID as val_id,"
        + "fv.ID as field_id," + "fval.ID_FIELD as field_validation_id_field,"
        + "fval.ID_VALIDATION as field_validation_id_validation," + "v.ID_RULE as rule_id,"
        + "v.LEVEL_ERROR as level_error, " + "v.MESSAGE as message," + "v.TABLE_NAME as table_name,"
        + "v.field_name as field_name, v.short_code as short_code,"
        + "v.TYPE_ENTITY as type_entity," + "v.VALIDATION_DATE as validation_date,"
        + "fv.ID_FIELD_SCHEMA as id_field_schema," + "fv.ID_RECORD as id_record,"
        + "fv.TYPE as field_value_type," + "fv.VALUE as value " + "from dataset_" + datasetId
        + ".FIELD_VALIDATION fval " + INNER_JOIN_DATASET + datasetId + ".VALIDATION v "
        + "on fval.ID_VALIDATION=v.ID " + INNER_JOIN_DATASET + datasetId + ".FIELD_VALUE fv "
        + "on fval.ID_FIELD=fv.ID " + "where fv.ID_RECORD " + "in (");

    for (int i = 0; i < recordIds.size(); i++) {
      query.append("'" + recordIds.get(i) + "'");
      if (recordIds.size() > i + 1) {
        query.append(",");
      }
    }
    query.append(")");
    List<FieldValidation> fieldValidations =
        datasetRepository.queryFieldValidationExecution(query.toString());

    Map<String, List<FieldValidation>> result = new HashMap<>();

    fieldValidations.stream().forEach(fieldValidation -> {
      if (!result.containsKey(fieldValidation.getFieldValue().getId())) {
        result.put(fieldValidation.getFieldValue().getId(), new ArrayList<>());
      }
      result.get(fieldValidation.getFieldValue().getId()).add(fieldValidation);
    });

    return result;
  }

  /**
   * Gets the record validations.
   *
   * @param recordIds the record ids
   * @param datasetId the dataset id
   * @return the record validations
   */
  private Map<String, List<RecordValidation>> getRecordValidations(final List<String> recordIds,
      Long datasetId) {

    StringBuilder query = new StringBuilder("select rval.ID as record_val_id," + "v.ID as val_id,"
        + "rv.ID as record_id," + "rval.ID_RECORD as record_validation_id_field,"
        + "rval.ID_VALIDATION as record_validation_id_validation," + "v.ID_RULE as rule_id,"
        + "v.LEVEL_ERROR as level_error," + "v.MESSAGE as message," + "v.TABLE_NAME as table_name,"
        + "v.field_name as field_name, v.short_code as short_code,"
        + "v.TYPE_ENTITY as type_entity," + "v.VALIDATION_DATE as validation_date,"
        + "rv.DATA_PROVIDER_CODE as data_provider_code,"
        + "rv.DATASET_PARTITION_ID as dataset_partition, "
        + "rv.ID_RECORD_SCHEMA as record_schema, " + "rv.ID_TABLE as id_table " + "from dataset_"
        + datasetId + ".RECORD_VALIDATION rval " + INNER_JOIN_DATASET + datasetId + ".VALIDATION v "
        + "on rval.ID_VALIDATION=v.ID " + INNER_JOIN_DATASET + datasetId + ".RECORD_VALUE rv "
        + "on rval.ID_RECORD=rv.ID " + "where rv.ID in (");

    for (int i = 0; i < recordIds.size(); i++) {
      query.append("'" + recordIds.get(i) + "'");
      if (recordIds.size() > i + 1) {
        query.append(",");
      }
    }
    query.append(")");
    List<RecordValidation> recordValidations =
        datasetRepository.queryRecordValidationExecution(query.toString());

    Map<String, List<RecordValidation>> result = new HashMap<>();

    recordValidations.stream().forEach(recordValidation -> {
      if (!result.containsKey(recordValidation.getRecordValue().getId())) {
        result.put(recordValidation.getRecordValue().getId(), new ArrayList<>());
      }
      result.get(recordValidation.getRecordValue().getId()).add(recordValidation);
    });

    return result;
  }

  /**
   * Proccess query.
   *
   * @param datasetId the dataset id
   * @param query the query
   * @return the string
   */
  private String proccessQuery(Long datasetId, String query) {

    if (query.contains(DATASET)) {
      Map<String, Long> datasetSchemasMap = getListOfDatasetsOnQuery(query);
      DatasetTypeEnum datasetType = datasetMetabaseController.getType(datasetId);
      Long dataflowId =
          datasetMetabaseController.findDatasetMetabaseById(datasetId).getDataflowId();

      Map<Long, Long> datasetIdOldNew = new HashMap<>();
      switch (datasetType) {
        case COLLECTION:
          query = modifyQueryForCollection(query, datasetSchemasMap, dataflowId, datasetIdOldNew);
          break;
        case EUDATASET:
          query = modifyQueryForEU(query, datasetSchemasMap, dataflowId, datasetIdOldNew);
          break;
        case REPORTING:
          query = modifyQueryForReportingDataset(datasetId, query, datasetSchemasMap, dataflowId,
              datasetIdOldNew);
          break;
        case TEST:
          query = modifyQueryForTestDataset(query, datasetSchemasMap, dataflowId, datasetIdOldNew);
          break;
        case DESIGN:
        default:
          break;
      }
    }

    return query;
  }

  /**
   * Modify query for test dataset.
   *
   * @param query the query
   * @param datasetSchemasMap the dataset schemas map
   * @param dataflowId the dataflow id
   * @param datasetIdOldNew the dataset id old new
   * @return the string
   */
  private String modifyQueryForTestDataset(String query, Map<String, Long> datasetSchemasMap,
      Long dataflowId, Map<Long, Long> datasetIdOldNew) {
    List<TestDatasetVO> testDatasetVOList =
        testDatasetControllerZuul.findTestDatasetByDataflowId(dataflowId);
    Map<String, Long> testDatasetSchamasMap = new HashMap<>();
    for (TestDatasetVO testDataset : testDatasetVOList) {
      testDatasetSchamasMap.put(testDataset.getDatasetSchema(), testDataset.getId());
    }
    for (Map.Entry<String, Long> auxDatasetMap : datasetSchemasMap.entrySet()) {
      String key = auxDatasetMap.getKey();
      Long testDatasetId = testDatasetSchamasMap.get(key);
      datasetIdOldNew.put(auxDatasetMap.getValue(), testDatasetId);
    }
    for (Map.Entry<Long, Long> auxDatasetOldAndNew : datasetIdOldNew.entrySet()) {
      query = query.replaceAll(DATASET + auxDatasetOldAndNew.getKey(),
          DATASET + auxDatasetOldAndNew.getValue());
    }
    return query;
  }

  /**
   * Modify query for collection.
   *
   * @param query the query
   * @param datasetSchemasMap the dataset schemas map
   * @param dataflowId the dataflow id
   * @param datasetIdOldNew the dataset id old new
   * @return the string
   */
  private String modifyQueryForCollection(String query, Map<String, Long> datasetSchemasMap,
      Long dataflowId, Map<Long, Long> datasetIdOldNew) {
    List<DataCollectionVO> dataCollectionList =
        dataCollectionController.findDataCollectionIdByDataflowId(dataflowId);
    Map<String, Long> dataCollectionSchamasMap = new HashMap<>();
    for (DataCollectionVO dataCollection : dataCollectionList) {
      dataCollectionSchamasMap.put(dataCollection.getDatasetSchema(), dataCollection.getId());
    }
    for (Map.Entry<String, Long> auxDatasetMap : datasetSchemasMap.entrySet()) {
      String key = auxDatasetMap.getKey();
      Long datasetDatacollection = dataCollectionSchamasMap.get(key);
      datasetIdOldNew.put(auxDatasetMap.getValue(), datasetDatacollection);
    }
    for (Map.Entry<Long, Long> auxDatasetOldAndNew : datasetIdOldNew.entrySet()) {
      query = query.replaceAll(DATASET + auxDatasetOldAndNew.getKey(),
          DATASET + auxDatasetOldAndNew.getValue());
    }
    return query;
  }

  /**
   * Modify query for EU.
   *
   * @param query the query
   * @param datasetSchemasMap the dataset schemas map
   * @param dataflowId the dataflow id
   * @param datasetIdOldNew the dataset id old new
   * @return the string
   */
  private String modifyQueryForEU(String query, Map<String, Long> datasetSchemasMap,
      Long dataflowId, Map<Long, Long> datasetIdOldNew) {
    List<EUDatasetVO> euDatasetList = euDatasetController.findEUDatasetByDataflowId(dataflowId);
    Map<String, Long> euDatasetSchamasMap = new HashMap<>();
    for (EUDatasetVO euDataset : euDatasetList) {
      euDatasetSchamasMap.put(euDataset.getDatasetSchema(), euDataset.getId());
    }

    for (Map.Entry<String, Long> auxDatasetMap : datasetSchemasMap.entrySet()) {
      String key = auxDatasetMap.getKey();
      Long datasetDatacollection = euDatasetSchamasMap.get(key);
      datasetIdOldNew.put(auxDatasetMap.getValue(), datasetDatacollection);
    }

    for (Map.Entry<Long, Long> auxDatasetOldAndNew : datasetIdOldNew.entrySet()) {
      query = query.replaceAll(DATASET + auxDatasetOldAndNew.getKey(),
          DATASET + auxDatasetOldAndNew.getValue());
    }
    return query;
  }

  /**
   * Modify query for reporting dataset.
   *
   * @param datasetId the dataset id
   * @param query the query
   * @param datasetSchemasMap the dataset schemas map
   * @param dataflowId the dataflow id
   * @param datasetIdOldNew the dataset id old new
   * @return the string
   */
  private String modifyQueryForReportingDataset(Long datasetId, String query,
      Map<String, Long> datasetSchemasMap, Long dataflowId, Map<Long, Long> datasetIdOldNew) {
    List<ReportingDatasetVO> reportingDatasetList =
        datasetMetabaseController.findReportingDataSetIdByDataflowId(dataflowId);
    List<RepresentativeVO> dataprovidersVOList =
        representativeController.findRepresentativesByIdDataFlow(dataflowId);
    Long providerId =
        datasetMetabaseController.findDatasetMetabaseById(datasetId).getDataProviderId();
    List<Long> dataprovidersIdList = new ArrayList<>();
    dataprovidersVOList.forEach(dataprovider -> {
      if (dataprovider.getDataProviderId().equals(providerId)) {
        dataprovidersIdList.add(dataprovider.getDataProviderId());
      }
    });
    List<String> datasetIdList = new ArrayList<>();
    for (ReportingDatasetVO dataset : reportingDatasetList) {
      datasetIdList.add(dataset.getDatasetSchema());
    }
    Map<String, Long> reportingDatasetSchamasMap = new HashMap<>();
    for (ReportingDatasetVO reportingDataset : reportingDatasetList) {
      if (dataprovidersIdList.contains(reportingDataset.getDataProviderId())) {
        reportingDatasetSchamasMap.put(reportingDataset.getDatasetSchema(),
            reportingDataset.getId());
      }
    }
    for (Map.Entry<String, Long> auxDatasetMap : datasetSchemasMap.entrySet()) {
      if (datasetIdList.contains(auxDatasetMap.getKey())) {
        String key = auxDatasetMap.getKey();
        Long datasetDatacollection = reportingDatasetSchamasMap.get(key);
        datasetIdOldNew.put(auxDatasetMap.getValue(), datasetDatacollection);
      }
    }
    for (Map.Entry<Long, Long> auxDatasetOldAndNew : datasetIdOldNew.entrySet()) {
      query = query.replaceAll(DATASET + auxDatasetOldAndNew.getKey(),
          DATASET + auxDatasetOldAndNew.getValue());
    }
    return query;
  }

  /**
   * Gets the list of datasets on query.
   *
   * @param query the query
   * @return the list of datasets on query
   */
  private Map<String, Long> getListOfDatasetsOnQuery(String query) {
    Map<String, Long> datasetSchamasMap = new HashMap<>();
    String[] palabras = query.split("\\s+");
    for (String palabra : palabras) {
      if (palabra.contains(DATASET)) {
        try {
          String datasetIdFromotherSchemas =
              palabra.substring(palabra.indexOf('_') + 1, palabra.indexOf('.'));

          datasetSchamasMap.put(
              datasetMetabaseController
                  .findDatasetSchemaIdById(Long.parseLong(datasetIdFromotherSchemas)),
              Long.parseLong(datasetIdFromotherSchemas));
        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
          LOG_ERROR.error("Error validating SQL rule, processing the sentence {}. Message {}",
              query, e.getMessage(), e);
        }
      }
    }
    return datasetSchamasMap;
  }
}
