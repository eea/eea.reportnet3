package org.eea.validation.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.TestDatasetController.TestDatasetControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.interfaces.vo.dataset.ValueVO;
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
import org.eea.validation.exception.EEAForbiddenSQLCommandException;
import org.eea.validation.exception.EEAInvalidSQLException;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.SqlRulesService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
  private static final String KEYWORDS = "DELETE,INSERT,DROP,UPDATE,TRUNCATE";

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

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

  /** The entity manager. */
  @PersistenceContext
  private EntityManager entityManager;

  /** The dataflow controller. */
  @Autowired
  private DataFlowControllerZuul dataFlowController;

  /** The reference dataset controller. */
  @Autowired
  private ReferenceDatasetControllerZuul referenceDatasetController;

  /** The dataset schema controller zuul. */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaControllerZuul;



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
    String query = "";
    String sqlError = "";
    EventType notificationEventType = null;
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName())
        .datasetSchemaId(datasetSchemaId).shortCode(rule.getShortCode())
        .error("The QC Rule is disabled").build();

    try {
      DataSetMetabaseVO dataSetMetabaseVO =
          datasetMetabaseController.findDatasetMetabaseById(datasetId);
      query = proccessQuery(dataSetMetabaseVO, rule.getSqlSentence());
      sqlError = validateRule(query, dataSetMetabaseVO, rule);
    } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
      sqlError = "Syntax not allowed";
    }
    if (StringUtils.isBlank(sqlError)) {
      notificationEventType = EventType.VALIDATED_QC_RULE_EVENT;
      rule.setVerified(true);
      LOG.info("Rule validation passed: {}", rule);
    } else {
      notificationEventType = EventType.INVALIDATED_QC_RULE_EVENT;
      rule.setVerified(false);
      rule.setEnabled(false);
      rule.setSqlError(sqlError);
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

    DataSetMetabaseVO dataSetMetabaseVO =
        datasetMetabaseController.findDatasetMetabaseById(datasetId);
    String query = proccessQuery(dataSetMetabaseVO, ruleVO.getSqlSentence());
    boolean verifAndEnabled = true;
    if (!StringUtils.isBlank(validateRule(query, dataSetMetabaseVO, rule))) {
      LOG.info("Rule validation not passed before pass to datacollection: {}", rule);
      verifAndEnabled = false;
      rule.setEnabled(verifAndEnabled);
    }
    rule.setVerified(verifAndEnabled);
    rule.setWhenCondition(new StringBuilder().append("isSQLSentenceWithCode(this.datasetId.id,'")
        .append(rule.getRuleId().toString())
        .append(
            "', this.records.size > 0 && this.records.get(0) != null && this.records.get(0).dataProviderCode != null ? this.records.get(0).dataProviderCode : 'XX'")
        .append(")").toString());
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
   * @param dataSetMetabaseVO the data set metabase VO
   * @param rule the rule
   * @param ischeckDC the ischeck DC
   * @return the table value
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  @Override
  public TableValue retrieveTableData(String query, DataSetMetabaseVO dataSetMetabaseVO, Rule rule,
      Boolean ischeckDC) throws EEAInvalidSQLException {
    DataSetSchema dataschema =
        schemasRepository.findByIdDataSetSchema(new ObjectId(dataSetMetabaseVO.getDatasetSchema()));
    String entityName = "";
    Long idTable = null;

    String newQuery = proccessQuery(dataSetMetabaseVO, query);

    switch (rule.getType()) {
      case FIELD:
        entityName = retriveFieldName(dataschema, rule.getReferenceId());
        idTable = retriveIsTableFromFieldSchema(dataschema, rule.getReferenceId(),
            dataSetMetabaseVO.getId());
        break;
      case TABLE:
        entityName = retriveTableName(dataschema, rule.getReferenceId());
        idTable = datasetRepository.getTableId(rule.getReferenceId().toString(),
            dataSetMetabaseVO.getId());
        break;
      case RECORD:
        idTable = retriveIsTableFromRecordSchema(dataschema, rule.getReferenceId(),
            dataSetMetabaseVO.getId());
        break;
      case DATASET:
        break;
    }
    LOG.info("Query from ruleCode {} to be executed: {}", rule.getShortCode(), newQuery);
    TableValue table = datasetRepository.queryRSExecution(newQuery, rule.getType(), entityName,
        dataSetMetabaseVO.getId(), idTable);
    if (Boolean.FALSE.equals(ischeckDC) && null != table && null != table.getRecords()
        && !table.getRecords().isEmpty() && !EntityTypeEnum.TABLE.equals(rule.getType())) {
      retrieveValidations(table.getRecords(), dataSetMetabaseVO.getId());
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
    DataSetMetabaseVO dataSetMetabaseVO =
        datasetMetabaseController.findDatasetMetabaseById(datasetId);
    if (null != rulesSql && !rulesSql.isEmpty()) {
      rulesSql.stream().forEach(ruleVO -> {
        Rule rule = ruleMapper.classToEntity(ruleVO);
        String sqlError = validateRule(ruleVO.getSqlSentence(), dataSetMetabaseVO, rule);
        if (StringUtils.isBlank(sqlError)) {
          rule.setVerified(true);
        } else {
          rule.setVerified(false);
          rule.setEnabled(false);
          rule.setSqlError(sqlError);
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
          .datasetId(datasetId).dataflowId(dataSetMetabaseVO.getDataflowId())
          .invalidRules(rulesUnchecked).disabledRules(rulesDisabled).build();
      LOG.info("SQL rules contains errors");
      if (showNotification == null || showNotification) {
        releaseNotification(EventType.VALIDATE_RULES_ERROR_EVENT, notificationVO);
      }
    } else {

      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName())
          .datasetId(datasetId).dataflowId(dataSetMetabaseVO.getDataflowId()).build();
      LOG.info("SQL rules contains 0 errors");
      if (showNotification == null || showNotification) {
        releaseNotification(EventType.VALIDATE_RULES_COMPLETED_EVENT, notificationVO);
      }
    }
  }

  /**
   * Run SQL rule with limited results.
   *
   * @param datasetId the dataset id
   * @param sqlRule the sql rule about to be run
   * @param showInternalFields the show internal fields
   * @return the string formatted as JSON
   * @throws EEAException the EEA exception
   */
  @Override
  public List<List<ValueVO>> runSqlRule(Long datasetId, String sqlRule, boolean showInternalFields)
      throws EEAException {

    StringBuilder sb = new StringBuilder("");
    List<List<ValueVO>> result = new ArrayList<>();
    DataSetMetabaseVO dataSetMetabaseVO =
        datasetMetabaseController.findDatasetMetabaseById(datasetId);
    List<String> ids = new ArrayList<>();
    List<String> datasetIds;

    try {

      if (checkQuerySyntax(sqlRule)) {
        ids = getListOfDatasetsOnQuery(sqlRule);
        datasetIds = new ArrayList<>(ids);
        checkDatasetFromSameDataflow(dataSetMetabaseVO, ids);
        if (!ids.isEmpty()) {
          checkDatasetFromReferenceDataflow(ids);
        }
      } else {
        throw new EEAForbiddenSQLCommandException("SQL Command not allowed in SQL Rule.");
      }

      if (!ids.isEmpty() || ids.contains(datasetId.toString())) {
        throw new EEAException();
      } else {

        if (showInternalFields) {
          sb.append("SELECT * FROM (");
          sb.append(sqlRule);
          sb.append(") as userSelect OFFSET 0 LIMIT 10");
        } else {
          sb = buildWithTableQuery(datasetIds, sb, sqlRule);
        }

        result = datasetRepository.runSqlRule(datasetId, sb.toString());
      }
    } catch (StringIndexOutOfBoundsException e) {
      throw new StringIndexOutOfBoundsException("SQL sentence has wrong format, please check.");
    } catch (EEAForbiddenSQLCommandException e) {
      throw new EEAForbiddenSQLCommandException("SQL Command not allowed in SQL Rule.", e);
    } catch (EEAInvalidSQLException e) {
      throw new EEAInvalidSQLException("Couldn't execute the SQL Rule", e);
    } catch (NumberFormatException e) {
      throw new NumberFormatException("Wrong id for dataset in SQL Rule execution");
    } catch (EEAException e) {
      throw new EEAException("User doesn't have access to one of the datasets", e);
    }
    return result;
  }

  /**
   * Evaluates the SQL rule and returns its total cost
   *
   * @param datasetId the dataset id
   * @param sqlRule the sql rule about to be evaluated
   * @return the double containing the total cost
   * @throws EEAException the EEA exception
   */
  @Override
  public Double evaluateSqlRule(Long datasetId, String sqlRule)
      throws EEAException, ParseException {

    StringBuilder sb = new StringBuilder("");
    Double sqlCost = 0.0;
    DataSetMetabaseVO dataSetMetabaseVO =
        datasetMetabaseController.findDatasetMetabaseById(datasetId);
    List<String> ids = new ArrayList<>();

    try {
      if (checkQuerySyntax(sqlRule)) {
        ids = getListOfDatasetsOnQuery(sqlRule);
        checkDatasetFromSameDataflow(dataSetMetabaseVO, ids);
        if (!ids.isEmpty()) {
          checkDatasetFromReferenceDataflow(ids);
        }
      } else {
        throw new EEAForbiddenSQLCommandException("SQL Command not allowed in SQL Rule.");
      }

      if (!ids.isEmpty() || ids.contains(datasetId.toString())) {
        throw new EEAException();
      } else {
        sb.append("EXPLAIN (FORMAT JSON) ");
        sb.append(sqlRule);
        String result = datasetRepository.evaluateSqlRule(datasetId, sb.toString());
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(result);
        JSONObject jsonObject = (JSONObject) jsonArray.get(0);
        JSONObject plan = (JSONObject) jsonObject.get("Plan");
        sqlCost = Double.parseDouble(String.valueOf(plan.get("Total Cost")));
      }
    } catch (StringIndexOutOfBoundsException e) {
      throw new StringIndexOutOfBoundsException("SQL sentence has wrong format, please check it");
    } catch (EEAForbiddenSQLCommandException e) {
      throw new EEAForbiddenSQLCommandException("SQL Command not allowed in SQL Rule.", e);
    } catch (EEAInvalidSQLException e) {
      throw new EEAInvalidSQLException(e.getCause().getCause().getMessage(), e);
    } catch (NumberFormatException e) {
      throw new EEAInvalidSQLException("Wrong id for dataset in SQL Rule execution");
    } catch (EEAException e) {
      throw new EEAException("User doesn't have access to one of the datasets", e);
    } catch (ParseException e) {
      throw new ParseException(e.getErrorType(), e.getPosition());
    }
    return sqlCost;
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
   * @param dataSetMetabaseVO the data set metabase VO
   * @param rule the rule
   * @return the boolean
   */
  private String validateRule(String query, DataSetMetabaseVO dataSetMetabaseVO, Rule rule) {
    String isSQLCorrect = "";
    // validate query
    if (!StringUtils.isBlank(query)) {
      // validate query sintax
      if (checkQuerySyntax(query)) {
        List<String> ids = getListOfDatasetsOnQuery(query);
        checkDatasetFromSameDataflow(dataSetMetabaseVO, ids);
        if (!ids.isEmpty()) {
          checkDatasetFromReferenceDataflow(ids);
        }
        // all the dataset ids present on the query exists on the dataflow, or are inside reference
        // dataflows
        if (ids.isEmpty()) {
          try {
            checkQueryTestExecution(query.replace(";", ""), dataSetMetabaseVO, rule);
          } catch (EEAInvalidSQLException e) {
            LOG_ERROR.error(String.format("SQL is not correct: %s.  %s", rule.getSqlSentence(),
                e.getCause().getCause().getMessage()));
            isSQLCorrect = e.getCause().getCause().getMessage();
          }
        } else {
          isSQLCorrect = "Datasets " + ids.toString() + " not from this dataflow";
        }
      } else {
        isSQLCorrect = "Syntax not allowed";
      }
    } else {
      isSQLCorrect = "Empty query";
    }
    if (!StringUtils.isBlank(isSQLCorrect)) {
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
   * @param dataSetMetabaseVO the data set metabase VO
   * @param rule the rule
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  private void checkQueryTestExecution(String query, DataSetMetabaseVO dataSetMetabaseVO, Rule rule)
      throws EEAInvalidSQLException {
    String newQuery = proccessQuery(dataSetMetabaseVO, query);
    datasetRepository.validateQuery("explain " + newQuery, dataSetMetabaseVO.getId());
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
  private Long retriveIsTableFromFieldSchema(DataSetSchema schema, ObjectId fieldSchemaId,
      Long datasetId) {
    ObjectId tableSchemaId = new ObjectId();
    for (TableSchema table : schema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().equals(fieldSchemaId)) {
          tableSchemaId = table.getIdTableSchema();
        }
      }
    }
    return datasetRepository.getTableId(tableSchemaId.toString(), datasetId);
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
  private Long retriveIsTableFromRecordSchema(DataSetSchema schema, ObjectId recordSchemaId,
      Long datasetId) {
    ObjectId tableSchemaId = new ObjectId();
    for (TableSchema table : schema.getTableSchemas()) {
      if (table.getRecordSchema().getIdRecordSchema().equals(recordSchemaId)) {
        tableSchemaId = table.getIdTableSchema();
      }
    }
    return datasetRepository.getTableId(tableSchemaId.toString(), datasetId);
  }

  /**
   * Retrive table name.
   *
   * @param schema the schema
   * @param idTableSchema the id table schema
   * @return the string
   */
  private String retriveTableName(DataSetSchema schema, ObjectId idTableSchema) {
    String tableName = "";
    for (TableSchema table : schema.getTableSchemas()) {
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
  private String retriveFieldName(DataSetSchema schema, ObjectId idFieldSchema) {
    String fieldName = "";
    for (TableSchema table : schema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().equals(idFieldSchema)) {
          fieldName = field.getHeaderName();
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
   * Retrieve tables.
   *
   * @param datasetIds the dataset ids
   * @return the list
   */
  private List<TableSchemaVO> retrieveTables(List<String> datasetIds) {

    List<TableSchemaVO> tables = new ArrayList<>();

    for (String id : datasetIds) {
      DataSetSchemaVO schema =
          datasetSchemaControllerZuul.findDataSchemaByDatasetId(Long.parseLong(id));
      if (schema.getTableSchemas() != null) {
        tables.addAll(schema.getTableSchemas());
      }
    }
    return tables;

  }

  /**
   * Builds the with table query.
   *
   * @param datasetIds the dataset ids
   * @param sb the sb
   * @param sqlRule the sql rule
   * @return the string builder
   */
  private StringBuilder buildWithTableQuery(List<String> datasetIds, StringBuilder sb,
      String sqlRule) {
    List<TableSchemaVO> tables;

    for (String dataset : datasetIds) {
      sqlRule = sqlRule.replace(DATASET + dataset + ".", "");
    }

    tables = retrieveTables(datasetIds);
    sb.append("WITH ");
    for (int i = 0; i < tables.size(); i++) {
      sb.append(tables.get(i).getNameTableSchema() + " AS ");
      sb.append("(SELECT ");

      List<FieldSchemaVO> fields = tables.get(i).getRecordSchema().getFieldSchema();
      for (int j = 0; j < fields.size(); j++) {
        sb.append(fields.get(j).getName());
        if (j < fields.size() - 1) {
          sb.append(",");
        }
      }
      sb.append(" FROM ");
      sb.append(tables.get(i).getNameTableSchema() + ")");

      if (i < tables.size() - 1) {
        sb.append(",");
      }
    }
    sb.append(" SELECT * FROM (");
    sb.append(sqlRule);
    sb.append(") as userSelect OFFSET 0 LIMIT 10");

    return sb;
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
   * @param dataSetMetabaseVO the data set metabase VO
   * @param query the query
   * @return the string
   */
  private String proccessQuery(DataSetMetabaseVO dataSetMetabaseVO, String query) {

    if (query.contains(DATASET)) {
      Map<String, Long> datasetSchemasMap = getMapOfDatasetsOnQuery(query);
      DatasetTypeEnum datasetType = dataSetMetabaseVO.getDatasetTypeEnum();
      Long dataflowId = dataSetMetabaseVO.getDataflowId();
      List<ReferenceDatasetVO> referenceDatasets =
          referenceDatasetController.findReferenceDatasetByDataflowId(dataflowId);
      Map<Long, Long> datasetIdOldNew = new HashMap<>();
      switch (datasetType) {
        case COLLECTION:
          query = modifyQueryForCollection(query, datasetSchemasMap, dataflowId, datasetIdOldNew,
              referenceDatasets);
          break;
        case EUDATASET:
          query = modifyQueryForEU(query, datasetSchemasMap, dataflowId, datasetIdOldNew,
              referenceDatasets);
          break;
        case REPORTING:
          query = modifyQueryForReportingDataset(dataSetMetabaseVO.getDataProviderId(), query,
              datasetSchemasMap, dataflowId, datasetIdOldNew, referenceDatasets);
          break;
        case TEST:
          query = modifyQueryForTestDataset(query, datasetSchemasMap, dataflowId, datasetIdOldNew,
              referenceDatasets);
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
   * @param referenceDatasets the reference datasets
   * @return the string
   */
  private String modifyQueryForTestDataset(String query, Map<String, Long> datasetSchemasMap,
      Long dataflowId, Map<Long, Long> datasetIdOldNew,
      List<ReferenceDatasetVO> referenceDatasets) {
    List<TestDatasetVO> testDatasetVOList =
        testDatasetControllerZuul.findTestDatasetByDataflowId(dataflowId);

    Map<String, Long> testDatasetSchamasMap = new HashMap<>();
    for (TestDatasetVO testDataset : testDatasetVOList) {
      testDatasetSchamasMap.put(testDataset.getDatasetSchema(), testDataset.getId());
    }
    for (ReferenceDatasetVO referenceDataset : referenceDatasets) {
      testDatasetSchamasMap.put(referenceDataset.getDatasetSchema(), referenceDataset.getId());
    }
    for (Map.Entry<String, Long> auxDatasetMap : datasetSchemasMap.entrySet()) {
      String key = auxDatasetMap.getKey();
      Long testDatasetId = testDatasetSchamasMap.get(key);
      if (null != testDatasetId) {
        datasetIdOldNew.put(auxDatasetMap.getValue(), testDatasetId);
      }
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
   * @param referenceDatasets the reference datasets
   * @return the string
   */
  private String modifyQueryForCollection(String query, Map<String, Long> datasetSchemasMap,
      Long dataflowId, Map<Long, Long> datasetIdOldNew,
      List<ReferenceDatasetVO> referenceDatasets) {
    List<DataCollectionVO> dataCollectionList =
        dataCollectionController.findDataCollectionIdByDataflowId(dataflowId);
    Map<String, Long> dataCollectionSchamasMap = new HashMap<>();
    for (DataCollectionVO dataCollection : dataCollectionList) {
      dataCollectionSchamasMap.put(dataCollection.getDatasetSchema(), dataCollection.getId());
    }
    for (ReferenceDatasetVO referenceDataset : referenceDatasets) {
      dataCollectionSchamasMap.put(referenceDataset.getDatasetSchema(), referenceDataset.getId());
    }
    for (Map.Entry<String, Long> auxDatasetMap : datasetSchemasMap.entrySet()) {
      String key = auxDatasetMap.getKey();
      Long datasetDatacollection = dataCollectionSchamasMap.get(key);
      if (null != datasetDatacollection) {
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
   * Modify query for EU.
   *
   * @param query the query
   * @param datasetSchemasMap the dataset schemas map
   * @param dataflowId the dataflow id
   * @param datasetIdOldNew the dataset id old new
   * @param referenceDatasets the reference datasets
   * @return the string
   */
  private String modifyQueryForEU(String query, Map<String, Long> datasetSchemasMap,
      Long dataflowId, Map<Long, Long> datasetIdOldNew,
      List<ReferenceDatasetVO> referenceDatasets) {
    List<EUDatasetVO> euDatasetList = euDatasetController.findEUDatasetByDataflowId(dataflowId);
    Map<String, Long> euDatasetSchamasMap = new HashMap<>();
    for (EUDatasetVO euDataset : euDatasetList) {
      euDatasetSchamasMap.put(euDataset.getDatasetSchema(), euDataset.getId());
    }
    for (ReferenceDatasetVO referenceDataset : referenceDatasets) {
      euDatasetSchamasMap.put(referenceDataset.getDatasetSchema(), referenceDataset.getId());
    }

    for (Map.Entry<String, Long> auxDatasetMap : datasetSchemasMap.entrySet()) {
      String key = auxDatasetMap.getKey();
      Long eudataset = euDatasetSchamasMap.get(key);
      if (null != eudataset) {
        datasetIdOldNew.put(auxDatasetMap.getValue(), eudataset);
      }
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
   * @param dataProviderId the data provider id
   * @param query the query
   * @param datasetSchemasMap the dataset schemas map
   * @param dataflowId the dataflow id
   * @param datasetIdOldNew the dataset id old new
   * @param referenceDatasets the reference datasets
   * @return the string
   */
  private String modifyQueryForReportingDataset(Long dataProviderId, String query,
      Map<String, Long> datasetSchemasMap, Long dataflowId, Map<Long, Long> datasetIdOldNew,
      List<ReferenceDatasetVO> referenceDatasets) {
    List<ReportingDatasetVO> reportingDatasetList =
        datasetMetabaseController.findReportingDataSetIdByDataflowId(dataflowId);
    List<RepresentativeVO> dataprovidersVOList =
        representativeController.findRepresentativesByIdDataFlow(dataflowId);
    List<Long> dataprovidersIdList = new ArrayList<>();
    dataprovidersVOList.forEach(dataprovider -> {
      if (dataprovider.getDataProviderId().equals(dataProviderId)) {
        dataprovidersIdList.add(dataprovider.getDataProviderId());
      }
    });
    List<String> datasetIdList = new ArrayList<>();
    for (ReportingDatasetVO dataset : reportingDatasetList) {
      datasetIdList.add(dataset.getDatasetSchema());
    }

    for (ReferenceDatasetVO referenceDataset : referenceDatasets) {
      datasetIdList.add(referenceDataset.getDatasetSchema());
    }
    Map<String, Long> reportingDatasetSchemasMap = new HashMap<>();
    for (ReportingDatasetVO reportingDataset : reportingDatasetList) {
      if (dataprovidersIdList.contains(reportingDataset.getDataProviderId())) {
        reportingDatasetSchemasMap.put(reportingDataset.getDatasetSchema(),
            reportingDataset.getId());
      }
    }
    for (ReferenceDatasetVO referenceDataset : referenceDatasets) {
      reportingDatasetSchemasMap.put(referenceDataset.getDatasetSchema(), referenceDataset.getId());
    }
    for (Map.Entry<String, Long> auxDatasetMap : datasetSchemasMap.entrySet()) {
      if (datasetIdList.contains(auxDatasetMap.getKey())) {
        String key = auxDatasetMap.getKey();
        Long reportingDataset = reportingDatasetSchemasMap.get(key);
        if (null != reportingDataset) {
          datasetIdOldNew.put(auxDatasetMap.getValue(), reportingDataset);
        }
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
  private Map<String, Long> getMapOfDatasetsOnQuery(String query) {
    Map<String, Long> datasetSchamasMap = new HashMap<>();
    String[] words = query.split("\\s+");
    for (String word : words) {
      if (word.contains(DATASET)) {
        try {
          String datasetIdFromotherSchemas =
              word.substring(word.indexOf('_') + 1, word.indexOf('.'));

          datasetSchamasMap.put(
              datasetMetabaseController
                  .findDatasetSchemaIdById(Long.parseLong(datasetIdFromotherSchemas)),
              Long.parseLong(datasetIdFromotherSchemas));
        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
          LOG_ERROR.error("Error validating SQL rule, processing the sentence {}. Message {}",
              query, e.getMessage(), e);
          throw e;
        }
      }
    }
    return datasetSchamasMap;
  }

  /**
   * Gets a list with the id of the query datasets.
   *
   * @param query the query
   * @return Gets a list with the id of the query datasets
   */
  private List<String> getListOfDatasetsOnQuery(String query) {
    List<String> datasetsIdList = new ArrayList<>();
    String[] words = query.split("\\s+");
    for (String word : words) {
      if (word.contains(DATASET)) {
        String datasetId = word.substring(word.indexOf(DATASET) + 8, word.indexOf('.'));
        datasetsIdList.add(datasetId);
      }
    }
    return datasetsIdList;
  }


  /**
   * Check dataset from same dataflow.
   *
   * @param dataSetMetabaseVO the data set metabase VO
   * @param ids the ids
   * @return the list
   */
  private List<String> checkDatasetFromSameDataflow(DataSetMetabaseVO dataSetMetabaseVO,
      List<String> ids) {

    List<String> idsFound = new ArrayList<>();
    Long dataflowId = dataSetMetabaseVO.getDataflowId();
    for (String id : ids) {
      Long idDataFlowFromDatasetOnQuery =
          datasetMetabaseController.findDatasetMetabaseById(Long.parseLong(id)).getDataflowId();
      if (dataflowId.equals(idDataFlowFromDatasetOnQuery)) {
        idsFound.add(id);
      }
    }
    ids.removeAll(idsFound);
    return ids;
  }


  /**
   * Check dataset from reference dataflow.
   *
   * @param ids the ids
   * @return the list
   */
  private List<String> checkDatasetFromReferenceDataflow(List<String> ids) {

    List<String> referenceDatasetsId = new ArrayList<>();
    List<DataFlowVO> referencesDataflow = dataFlowController.findReferenceDataflows();

    for (DataFlowVO referenceDataflow : referencesDataflow) {
      List<ReferenceDatasetVO> referenceDatasets =
          referenceDatasetController.findReferenceDatasetByDataflowId(referenceDataflow.getId());
      for (ReferenceDatasetVO referenceDataset : referenceDatasets) {
        referenceDatasetsId.add(referenceDataset.getId().toString());
      }
    }
    List<String> idsFound = new ArrayList<>();
    for (String id : ids) {
      if (referenceDatasetsId.contains(id)) {
        idsFound.add(id);
      }
    }
    ids.removeAll(idsFound);
    return ids;
  }

}
