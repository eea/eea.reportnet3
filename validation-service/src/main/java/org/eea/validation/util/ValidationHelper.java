package org.eea.validation.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.interfaces.vo.metabase.TaskType;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.EEADelegatingSecurityContextExecutorService;
import org.eea.utils.LiteralConstants;
import org.eea.validation.kafka.command.Validator;
import org.eea.validation.mapper.TaskMapper;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.model.ValidationProcessVO;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.*;

/**
 * The Class ValidationHelper.
 */
@Component
@RefreshScope
public class ValidationHelper implements DisposableBean {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationHelper.class);

  /** The processes map. */
  private Map<String, ValidationProcessVO> processesMap;

  /** The validation executor service. */
  private ExecutorService validationExecutorService;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The validation service. */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /** The field batch size. */
  @Value("${validation.fieldBatchSize}")
  private int fieldBatchSize;

  /** The record batch size. */
  @Value("${validation.recordBatchSize}")
  private int recordBatchSize;

  /** The task released tax. */
  @Value("${validation.tasks.release.tax}")
  private int taskReleasedTax;

  /** The initial tax. */
  @Value("${validation.tasks.initial.tax}")
  private int initialTax;

  /** The max running tasks. */
  @Value("${validation.tasks.parallelism}")
  private int maxRunningTasks;

  /** The priority days. */
  @Value("${validation.priority.days}")
  private String priorityDays;

  /** The period days. */
  private List<Long> periodDays;
  private List<String> dremioSqlRuleMethods = new ArrayList<>(Arrays.asList("isfieldFK","isUniqueConstraint","checkIntegrityConstraint","isTableEmpty"));

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The reference dataset controller zuul. */
  @Autowired
  private ReferenceDatasetControllerZuul referenceDatasetControllerZuul;

  /** The process controller zuul. */
  @Autowired
  private ProcessControllerZuul processControllerZuul;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The task repository. */
  @Autowired
  private TaskRepository taskRepository;

  /** The data flow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  @Autowired
  private JobControllerZuul jobControllerZuul;

  @Autowired
  private JobProcessControllerZuul jobProcessControllerZuul;

  /** The Constant DATASET: {@value}. */
  private static final String DATASET = "dataset_";

  /** The data set mapper. */
  @Autowired
  private TaskMapper taskMapper;

  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaControllerZuul;

  @Autowired
  private S3Helper s3Helper;

  @Autowired
  DremioHelperService dremioHelperService;

  @Autowired
  S3Service s3Service;


  /**
   * Instantiates a new validation helper.
   */
  public ValidationHelper() {
    super();
    processesMap = new ConcurrentHashMap<>();
  }

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    periodDays = Arrays.asList(priorityDays.split(" ")).stream().map(Long::parseLong)
        .collect(Collectors.toList());
    validationExecutorService = new EEADelegatingSecurityContextExecutorService(
        Executors.newFixedThreadPool(maxRunningTasks));
    validationExecutorService.submit(() -> LOG.info("initializer validation executor service"));
  }

  /**
   * Gets the kie base.
   *
   * @param processId the process id
   * @param datasetId the dataset id
   * @param rule the rule
   * @return the kie base
   * @throws EEAException the EEA exception
   */
  public KieBase getKieBase(String processId, Long datasetId, String rule) throws EEAException {
    KieBase kieBase = null;
    synchronized (processesMap) {
      if (!processesMap.containsKey(processId)) {
        initializeProcess(processId, null);
      }
      if (null == processesMap.get(processId).getKieBase()) {
        processesMap.get(processId)
            .setKieBase(validationService.loadRulesKnowledgeBase(datasetId, rule));
      }
      kieBase = processesMap.get(processId).getKieBase();
    }
    return kieBase;
  }

  /**
   * Finish process in map.
   *
   * @param processId the process id
   * @return true, if successful
   */
  public boolean finishProcessInMap(String processId) {
    boolean result = false;
    synchronized (processesMap) {
      LOG.info("Process {} will be removed from processesMap", processId);
      if (processesMap.containsKey(processId)) {
        LOG.info("ProcessesMap contains processId " + processId);
        ValidationProcessVO removed = processesMap.remove(processId);
        System.gc();
        if (removed != null) {
          LOG.info("Removing process {} from processesMap ", processId);
          result = true;
        } else {
          LOG.info("Process {} not removed from processesMap", processId);
        }
      } else {
        LOG.info("Process {} not found in processesMap" + processId);
        result = true;
      }
    }
    return result;
  }

  /**
   * Initialize process.
   *
   * @param processId the process id
   * @param user the user
   */
  public void initializeProcess(String processId, String user) {
    ValidationProcessVO process = new ValidationProcessVO(null,
        user != null ? user : processControllerZuul.findById(processId).getUser());

    synchronized (processesMap) {
      processesMap.put(processId, process);
    }
  }

  /**
   * Execute validation.
   *
   * @param datasetId the dataset id
   * @param processId the process id
   * @param released the released
   * @param updateViews the update views
   * @throws EEAException the EEA exception
   */
  @Async
  @LockMethod(removeWhenFinish = true, isController = false)
  public void executeValidation(@LockCriteria(name = "datasetId") Long datasetId, String processId,
      boolean released, boolean updateViews) throws EEAException {

    DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    LOG.info(
        "Obtaining dataset metabase from datasetId {} to perform validation. The schema from the metabase is {}",
        datasetId, dataset.getDatasetSchema());
    // In case there's no processId, set a new one (because the processId is set in
    // ValidationControlleriImpl)
    if (StringUtils.isBlank(processId) || "null".equals(processId)) {
      processId = UUID.randomUUID().toString();
      LOG.info("processId is empty. Generating one: {} for validating datasetId {}", processId, datasetId);
    }
    ProcessVO processVO = processControllerZuul.findById(processId);
    if (processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
        ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.VALIDATION, processId,
        processVO.getUser(), 0, released)) {


      // If there's no SQL rules enabled, no need to refresh the views, so directly start the
      // validation
      TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + dataset.getId());
      List<Rule> listSql =
          rulesRepository.findSqlRulesEnabled(new ObjectId(dataset.getDatasetSchema()));
      Boolean hasSqlEnabled = true;
      if (CollectionUtils.isEmpty(listSql)) {
        hasSqlEnabled = false;
      }

      LOG.info("In executeValidation for datasetId {} and processId {} updateViews is {} and hasSqlEnabled is {}", datasetId, processId, updateViews, hasSqlEnabled);
      if (Boolean.FALSE.equals(updateViews) || Boolean.FALSE.equals(hasSqlEnabled)) {
        executeValidationProcess(dataset, processId);
      } else {
        deleteLockToReleaseProcess(datasetId);
        Map<String, Object> values = new HashMap<>();
        values.put(LiteralConstants.DATASET_ID, datasetId);
        values.put("released", released);
        values.put("referencesToRefresh",
            List.copyOf(updateMaterializedViewsOfReferenceDatasetsInSQL(datasetId,
                dataset.getDataflowId(), dataset.getDatasetSchema())));
        values.put("processId", processId);
        kafkaSenderUtils.releaseKafkaEvent(EventType.REFRESH_MATERIALIZED_VIEW_EVENT, values);
      }
    }
    LOG.info("Successfully executed validation for datasetId {}", datasetId);
    dataset = null;
  }

  @LockMethod(removeWhenFinish = true, isController = false)
  public void executeValidationDL(@LockCriteria(name = "datasetId") Long datasetId, String processId, boolean released, S3PathResolver s3PathResolver, boolean createParquetWithSQL) {
    initializeProcess(processId, SecurityContextHolder.getContext().getAuthentication().getName());
    DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    LOG.info("Obtaining dataset metabase from datasetId {} to perform validationDL. The schema from the metabase is {}",
            datasetId, dataset.getDatasetSchema());
    ProcessVO processVO = processControllerZuul.findById(processId);
    if (processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
            ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.VALIDATION, processId,
            processVO.getUser(), 0, released)) {

      //delete previous validation folder
      if (s3Helper.checkFolderExist(s3PathResolver, S3_VALIDATION_TABLE_PATH)) {
        s3Helper.deleteFolder(s3PathResolver, S3_VALIDATION_TABLE_PATH);
      }

      DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));
      List<Rule> rules = rulesRepository.findRulesEnabled(new ObjectId(dataset.getDatasetSchema()));
      for (Rule rule : rules) {
        TableSchema tableSchema = null;
        if (rule.getReferenceFieldSchemaPKId()!=null || rule.getType().equals(EntityTypeEnum.TABLE)) {
          tableSchema = schema.getTableSchemas().stream().filter(t -> t.getIdTableSchema().toString().equals(rule.getReferenceId().toString())).findFirst().get();
        } else {
          for (TableSchema t : schema.getTableSchemas()) {
            List<FieldSchema> fieldSchemas = t.getRecordSchema().getFieldSchema().stream().filter(f -> f.getIdFieldSchema().toString().equals(rule.getReferenceId().toString())).collect(Collectors.toList());
            if (fieldSchemas.size() > 0 || t.getRecordSchema().getIdRecordSchema().toString().equals(rule.getReferenceId().toString())) {
              tableSchema = t;
              break;
            }
          }
        }
        Map<String, Object> value = new HashMap<>();
        value.put(LiteralConstants.DATASET_ID, dataset.getId());
        value.put("uuid", processId);
        value.put("dataflowId", dataset.getDataflowId());
        value.put("user", processesMap.get(processId).getRequestingUser());
        value.put("dataProviderId", dataset.getDataProviderId()!=null ? dataset.getDataProviderId() : 0);
        value.put("datasetSchema", dataset.getDatasetSchema());
        value.put("ruleId", rule.getRuleId().toString());
        value.put("ruleCode", rule.getShortCode());
        value.put("tableName", tableSchema.getNameTableSchema());
        value.put("tableSchemaId", tableSchema.getIdTableSchema().toString());
        value.put("bigData", "true");
        value.put("createParquetWithSQL", createParquetWithSQL);
        if (rule.getSqlSentence()!=null || isDremioSqlRuleMethod(rule.getWhenCondition())) {
          addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_DL_WITH_SQL, value);
        } else if (rule.getWhenCondition().contains("RuleOperators")) {
          addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_EXPRESSION_DL, value);
        } else {
          addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_DL, value);
        }
      }
      if (rules.size()==0) {
        Map<String, Object> value = new HashMap<>();
        value.put(LiteralConstants.DATASET_ID, dataset.getId());
        value.put("uuid", processId);
        value.put("bigData", "true");
        addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_EMPTY_RULE, value);
      }
    }
  }

  private boolean isDremioSqlRuleMethod(String whenCondition) {
    return dremioSqlRuleMethods.stream().anyMatch(method -> whenCondition.contains(method));
  }

  /**
   * Gets the priority.
   *
   * @param dataset the dataset
   * @return the priority
   */
  public int getPriority(DataSetMetabaseVO dataset) {
    int priority = 0;
    DataFlowVO dataflow = dataFlowControllerZuul.getMetabaseById(dataset.getDataflowId());
    dataflow.getDeadlineDate();

    if (dataflow.getDeadlineDate() == null || TypeStatusEnum.DESIGN.equals(dataflow.getStatus())
        ) {
      priority = 70;
    } else {
      final LocalDateTime today = LocalDateTime.now();
      Long days = Duration.between(today,
          LocalDateTime.ofInstant(dataflow.getDeadlineDate().toInstant(), ZoneId.systemDefault()))
          .toDays();
      if (days > periodDays.get(0)) {
        priority = 50;
      } else if (days <= periodDays.get(0) && days > periodDays.get(1)) {
        priority = 40;
      } else if (days <= periodDays.get(2) && days > periodDays.get(3)) {
        priority = 30;
      } else {
        priority = 20;
      }
    }
    return priority;
  }

  /**
   * Update materialized views of reference datasets in SQL.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param datasetSchemaId the dataset schema id
   * @return the sets the
   */
  private Set<Long> updateMaterializedViewsOfReferenceDatasetsInSQL(Long datasetId, Long dataflowId,
      String datasetSchemaId) {
    List<Rule> rules = rulesRepository.findSqlRules(new ObjectId(datasetSchemaId));
    Set<Long> datasetsToRefresh = new HashSet<>();
    Map<String, Long> testDatasetSchemasMap = new HashMap<>();
    Map<Long, Long> datasetIdOldNew = new HashMap<>();
    List<ReferenceDatasetVO> referenceDatasets =
        referenceDatasetControllerZuul.findReferenceDatasetByDataflowId(dataflowId);
    // remove the reference datasets that aren't open for modifying. We asume that these
    // materialized views are refreshed
    referenceDatasets.removeIf(r -> !Boolean.TRUE.equals(r.getUpdatable()));

    for (Rule rule : rules) {
      String query = rule.getSqlSentence();
      if (StringUtils.isNotBlank(query) && query.contains(DATASET) && rule.isEnabled()) {
        Map<String, Long> datasetSchemasMap = getMapOfDatasetsOnQuery(query);

        List<Long> referenceDatasetsId =
            referenceDatasets.stream().map(ReferenceDatasetVO::getId).collect(Collectors.toList());
        for (ReferenceDatasetVO referenceDataset : referenceDatasets) {
          testDatasetSchemasMap.put(referenceDataset.getDatasetSchema(), referenceDataset.getId());
        }
        for (Map.Entry<String, Long> auxDatasetMap : datasetSchemasMap.entrySet()) {
          String key = auxDatasetMap.getKey();
          Long testDatasetId = testDatasetSchemasMap.get(key);
          if (null != testDatasetId) {
            datasetIdOldNew.put(auxDatasetMap.getValue(), testDatasetId);
          }
          if (referenceDatasetsId.contains(testDatasetId)) {
            datasetsToRefresh.add(testDatasetId);
          }
        }

        List<String> datasetsInQuery = getListOfDatasetsOnQuery(query);
        Set<String> datasetsNotChanged = new HashSet<>(datasetsInQuery);
        for (Map.Entry<Long, Long> auxDatasetOldAndNew : datasetIdOldNew.entrySet()) {
          datasetsNotChanged.remove(auxDatasetOldAndNew.getKey().toString());
        }
        datasetsToRefresh.addAll(datasetsNotChanged.stream().map(Long::parseLong).distinct()
            .collect(Collectors.toList()));

      }
    }
    LOG.info("Datasets to refresh present in the sqls to perform: {}", datasetsToRefresh);
    return datasetsToRefresh;
  }

  /**
   * Gets the map of datasets on query.
   *
   * @param query the query
   * @return the map of datasets on query
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
              datasetMetabaseControllerZuul
                  .findDatasetSchemaIdById(Long.parseLong(datasetIdFromotherSchemas)),
              Long.parseLong(datasetIdFromotherSchemas));
        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
          LOG.error("Error validating SQL rule, processing the sentence {}. Message {}",
              query, e.getMessage(), e);
          throw e;
        }
      }
    }
    return datasetSchamasMap;
  }

  /**
   * Gets the list of datasets on query.
   *
   * @param query the query
   * @return the list of datasets on query
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
   * Execute validation process.
   *
   * @param dataset the dataset metabase object
   * @param processId the process id
   */
  public void executeValidationProcess(final DataSetMetabaseVO dataset, String processId) {
    // Initialize process as coordinator
    RulesSchema rules =
        rulesRepository.findByIdDatasetSchema(new ObjectId(dataset.getDatasetSchema()));
    initializeProcess(processId, SecurityContextHolder.getContext().getAuthentication().getName());
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + dataset.getId());
    LOG.info("Deleting all Validations for processId {} and datasetId {}", processId, dataset.getId());
    validationService.deleteAllValidation(dataset.getId());
    LOG.info("Collecting Dataset Validation tasks for processId {} and datasetId {}", processId, dataset.getId());
    releaseDatasetValidation(dataset, processId);
    LOG.info("Collecting Record Validation tasks for processId {} and datasetId {}", processId, dataset.getId());
    if (rules.getRules().stream().anyMatch(rule -> EntityTypeEnum.RECORD.equals(rule.getType()))) {
      releaseRecordsValidation(dataset, processId);
    }
    LOG.info("Collecting Field Validation tasks for processId {} and datasetId {}", processId, dataset.getId());
    releaseFieldsValidation(dataset, processId, !filterEmptyFields(rules.getRules()));
    LOG.info("Collecting Table Validation tasks for processId {} and datasetId {}", processId, dataset.getId());
    releaseTableValidation(dataset, processId);
    datasetMetabaseControllerZuul.updateDatasetRunningStatus(dataset.getId(),
        DatasetRunningStatusEnum.VALIDATING);
    LOG.info("Validation process has been executed for datasetId {} and processId {}", dataset.getId(), processId);
  }


  /**
   * Filter empty fields.
   *
   * @param rules the rules
   * @return true, if successful
   */
  private boolean filterEmptyFields(List<Rule> rules) {
    return rules.stream().anyMatch(rule -> !"isBlank(value)".equals(rule.getWhenCondition())
        && EntityTypeEnum.FIELD.equals(rule.getType()));
  }

  /**
   * Destroy.
   *
   * @throws Exception the exception
   */
  @Override
  public void destroy() throws Exception {
    if (null != validationExecutorService) {
      this.validationExecutorService.shutdown();
    }
  }

  /**
   * Process validation.
   *
   * @param taskId the task id
   * @param eeaEventVO the eea event VO
   * @param processId the process id
   * @param datasetId the dataset id
   * @param validator the validator
   * @param notificationEventType the notification event type
   * @throws EEAException the EEA exception
   */
  public void processValidation(Long taskId, EEAEventVO eeaEventVO, String processId,
      Long datasetId, Validator validator, EventType notificationEventType) throws EEAException {
    String rule = null;
    if (eeaEventVO.getData().get("sqlRule") != null) {
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      rule = (String) (eeaEventVO.getData().get("sqlRule"));
    }
    ValidationTask validationTask;
    if (eeaEventVO.getData().get("bigData")!=null && eeaEventVO.getData().get("bigData").equals("true")) {
       validationTask = new ValidationTask(taskId, eeaEventVO, validator, datasetId,
              null, processId);
    } else {
       validationTask = new ValidationTask(taskId, eeaEventVO, validator, datasetId,
              getKieBase(processId, datasetId, rule), processId);
    }

    // first every task is always queued up to ensure the order

    if (((ThreadPoolExecutor) ((EEADelegatingSecurityContextExecutorService) validationExecutorService)
        .getDelegateExecutorService()).getActiveCount() == maxRunningTasks) {
      LOG.info(
          "Event {} will be queued up as there are no validating threads available at the moment",
          eeaEventVO);
    }

    this.validationExecutorService.submit(new ValidationTasksExecutorThread(validationTask));
  }

  /**
   * Creates the lock with signature.
   *
   * @param lockSignature the lock signature
   * @param mapCriteria the map criteria
   * @param userName the user name
   * @throws EEAException the EEA exception
   */
  public void createLockWithSignature(LockSignature lockSignature, Map<String, Object> mapCriteria,
      String userName) throws EEAException {
    mapCriteria.put("signature", lockSignature.getValue());
    LockVO lockVO = lockService.findByCriteria(mapCriteria);
    if (lockVO == null) {
      lockService.createLock(new Timestamp(System.currentTimeMillis()), userName, LockType.METHOD,
          mapCriteria);
    }
  }


  /**
   * Adds the lock to release process.
   *
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  public void addLockToReleaseProcess(Long datasetId) throws EEAException {
    try {
      DataSetMetabaseVO datasetMetabaseVO =
          datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
      Map<String, Object> mapCriteria = new HashMap<>();
      mapCriteria.put("dataflowId", datasetMetabaseVO.getDataflowId());
      mapCriteria.put("dataProviderId", datasetMetabaseVO.getDataProviderId());

      Map<String, Object> mapCriteriaRestoreSnapshot = new HashMap<>();
      mapCriteriaRestoreSnapshot.put("datasetId", datasetId);
      if (datasetMetabaseVO.getDataProviderId() != null) {
        createLockWithSignature(LockSignature.RELEASE_SNAPSHOTS, mapCriteria,
            SecurityContextHolder.getContext().getAuthentication().getName());
        createLockWithSignature(LockSignature.RESTORE_SNAPSHOT, mapCriteriaRestoreSnapshot,
            SecurityContextHolder.getContext().getAuthentication().getName());
      } else {
        createLockWithSignature(LockSignature.RESTORE_SCHEMA_SNAPSHOT, mapCriteriaRestoreSnapshot,
            SecurityContextHolder.getContext().getAuthentication().getName());
      }

      // Avoid delete the table or dataset data while validating
      Map<String, Object> mapCriteriaDeleteDataset = new HashMap<>();
      mapCriteriaDeleteDataset.put(LiteralConstants.DATASETID, datasetId);
      createLockWithSignature(LockSignature.DELETE_DATASET_VALUES, mapCriteriaDeleteDataset,
          SecurityContextHolder.getContext().getAuthentication().getName());

      TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
      List<TableValue> tableList = tableRepository.findAll();
      for (TableValue table : tableList) {
        Map<String, Object> mapCriteriaDeleteTable = new HashMap<>();
        mapCriteriaDeleteTable.put(LiteralConstants.DATASETID, datasetId);
        mapCriteriaDeleteTable.put(LiteralConstants.TABLESCHEMAID, table.getIdTableSchema());
        createLockWithSignature(LockSignature.DELETE_IMPORT_TABLE, mapCriteriaDeleteTable,
            SecurityContextHolder.getContext().getAuthentication().getName());
      }
      // We add a lock to the validation processs itself
      Map<String, Object> mapCriteriaValidation = new HashMap<>();
      mapCriteriaValidation.put(LiteralConstants.DATASETID, datasetId);
      createLockWithSignature(LockSignature.FORCE_EXECUTE_VALIDATION, mapCriteriaValidation,
          SecurityContextHolder.getContext().getAuthentication().getName());

    } catch (Exception e) {
      LOG.error("There's an error putting the lock to validation process in dataset {}",
          datasetId);
      deleteLockToReleaseProcess(datasetId);
    }

  }

  /**
   * Delete lock to release process.
   *
   * @param datasetId the dataset id
   */
  public void deleteLockToReleaseProcess(Long datasetId) {
    DataSetMetabaseVO datasetMetabaseVO =
        datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    Map<String, Object> mapCriteriaRestoreSnapshot = new HashMap<>();
    mapCriteriaRestoreSnapshot.put(LiteralConstants.SIGNATURE,
        LockSignature.RESTORE_SNAPSHOT.getValue());
    mapCriteriaRestoreSnapshot.put("datasetId", datasetId);
    if (datasetMetabaseVO.getDataProviderId() != null) {
      Map<String, Object> releaseSnapshots = new HashMap<>();
      releaseSnapshots.put(LiteralConstants.SIGNATURE, LockSignature.RELEASE_SNAPSHOTS.getValue());
      releaseSnapshots.put(LiteralConstants.DATAFLOWID, datasetMetabaseVO.getDataflowId());
      releaseSnapshots.put(LiteralConstants.DATAPROVIDERID, datasetMetabaseVO.getDataProviderId());
      lockService.removeLockByCriteria(releaseSnapshots);
      lockService.removeLockByCriteria(mapCriteriaRestoreSnapshot);
    } else {
      mapCriteriaRestoreSnapshot.put(LiteralConstants.SIGNATURE,
          LockSignature.RESTORE_SCHEMA_SNAPSHOT.getValue());
      lockService.removeLockByCriteria(mapCriteriaRestoreSnapshot);
    }

    // Remove the locks to delete data
    Map<String, Object> mapCriteriaDeleteDataset = new HashMap<>();
    mapCriteriaDeleteDataset.put(LiteralConstants.SIGNATURE,
        LockSignature.DELETE_DATASET_VALUES.getValue());
    mapCriteriaDeleteDataset.put(LiteralConstants.DATASETID, datasetId);
    lockService.removeLockByCriteria(mapCriteriaDeleteDataset);

    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
    List<TableValue> tableList = tableRepository.findAll();
    for (TableValue table : tableList) {
      Map<String, Object> mapCriteriaDeleteTable = new HashMap<>();
      mapCriteriaDeleteTable.put(LiteralConstants.SIGNATURE,
          LockSignature.DELETE_IMPORT_TABLE.getValue());
      mapCriteriaDeleteTable.put(LiteralConstants.DATASETID, datasetId);
      mapCriteriaDeleteTable.put(LiteralConstants.TABLESCHEMAID, table.getIdTableSchema());
      lockService.removeLockByCriteria(mapCriteriaDeleteTable);
    }

    Map<String, Object> mapCriteriaValidation = new HashMap<>();
    mapCriteriaValidation.put(LiteralConstants.SIGNATURE,
        LockSignature.EXECUTE_VALIDATION.getValue());
    mapCriteriaValidation.put(LiteralConstants.DATASETID, datasetId);
    lockService.removeLockByCriteria(mapCriteriaValidation);

    Map<String, Object> mapCriteriaValidationDataset = new HashMap<>();
    mapCriteriaValidationDataset.put(LiteralConstants.SIGNATURE,
        LockSignature.FORCE_EXECUTE_VALIDATION.getValue());
    mapCriteriaValidationDataset.put(LiteralConstants.DATASETID, datasetId);
    lockService.removeLockByCriteria(mapCriteriaValidationDataset);
  }

  /**
   * Release fields validation.
   *
   * @param dataset the dataset
   * @param uuId the uu id
   * @param onlyEmptyFields the only empty fields
   */
  private void releaseFieldsValidation(final DataSetMetabaseVO dataset, String uuId,
      boolean onlyEmptyFields) {
    int i = 0;
    if (fieldBatchSize != 0) {
      Integer emptyFieldsDataset = validationService.countEmptyFieldsDataset(dataset.getId());
      Integer countFieldsDataset = validationService.countFieldsDataset(dataset.getId());
      for (Integer totalFields =
          onlyEmptyFields ? emptyFieldsDataset : countFieldsDataset; totalFields >= 0; totalFields =
              totalFields - fieldBatchSize) {
        releaseFieldValidation(dataset, uuId, i++, onlyEmptyFields);
      }
    }
  }

  /**
   * Release records validation.
   *
   * @param dataset the dataset
   * @param uuId the uu id
   */
  private void releaseRecordsValidation(final DataSetMetabaseVO dataset, String uuId) {
    int i = 0;
    if (recordBatchSize != 0) {
      Integer recordsDataset = validationService.countRecordsDataset(dataset.getId());
      for (Integer totalRecords = recordsDataset; totalRecords >= 0; totalRecords =
          totalRecords - recordBatchSize) {
        releaseRecordValidation(dataset, uuId, i++);
      }
    }
  }


  /**
   * Release table validation.
   *
   * @param dataset the dataset
   * @param uuId the uu id
   */
  private void releaseTableValidation(final DataSetMetabaseVO dataset, String uuId) {
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + dataset.getId());

    List<TableValue> tableList = tableRepository.findAll();
    int i = 0;
    List<Rule> rules =
        rulesRepository.findSqlRulesEnabled(new ObjectId(dataset.getDatasetSchema()));
    DataSetSchema datasetSchema =
        schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));
    for (Integer totalTables = tableList.size(); totalTables > 0; totalTables = totalTables - 1) {
      Long idTable = tableList.get(i++).getId();
      releaseTableValidation(dataset, uuId, idTable, null);

    }
    i = 0;
    for (Integer totalTables = tableList.size(); totalTables > 0; totalTables = totalTables - 1) {
      Long idTable = tableList.get(i++).getId();
      for (Rule rule : rules) {
        String idTableSchema = tableList.get(i - 1).getIdTableSchema();
        if (EntityTypeEnum.TABLE.equals(rule.getType())
            && rule.getReferenceId().equals(new ObjectId(idTableSchema))
            || checkSubTable(rule.getReferenceId(),
                datasetSchema.getTableSchemas().stream()
                    .filter(table -> table.getIdTableSchema().equals(new ObjectId(idTableSchema)))
                    .findFirst().orElse(null))) {
          releaseTableValidation(dataset, uuId, idTable, rule);
        }
      }
    }
    tableList.clear();
  }


  /**
   * Check sub table.
   *
   * @param referenceId the reference id
   * @param table the table
   * @return true, if successful
   */
  private boolean checkSubTable(ObjectId referenceId, TableSchema table) {
    boolean found = false;
    if (table != null) {
      if (table.getRecordSchema().getIdRecordSchema().equals(referenceId)) {
        found = true;
      }
      for (FieldSchema fieldSchema : table.getRecordSchema().getFieldSchema()) {
        if (found || fieldSchema.getIdFieldSchema().equals(referenceId)) {
          found = true;
          break;
        }
      }
    }
    return found;
  }

  /**
   * Release dataset validation.
   *
   * @param dataset the dataset
   * @param processId the process id
   */
  private void releaseDatasetValidation(final DataSetMetabaseVO dataset, final String processId) {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, dataset.getId());
    value.put("uuid", processId);
    value.put("user", processesMap.get(processId).getRequestingUser());
    addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_DATASET, value);
  }

  /**
   * Release table validation.
   *
   * @param dataset the dataset
   * @param processId the process id
   * @param idTable the id table
   * @param sqlRule the sql rule
   */
  private void releaseTableValidation(final DataSetMetabaseVO dataset, final String processId,
      Long idTable, Rule sqlRule) {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, dataset.getId());
    value.put("uuid", processId);
    value.put("idTable", idTable);
    value.put("user", processesMap.get(processId).getRequestingUser());
    value.put("dataProviderId", dataset.getDataProviderId());
    value.put("datasetSchema", dataset.getDatasetSchema());
    value.put("sqlRule", sqlRule != null ? sqlRule.getRuleId().toString() : null);
    addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_TABLE, value);
  }

  /**
   * Release record validation.
   *
   * @param dataset the dataset
   * @param processId the process id
   * @param numPag the num pag
   */
  private void releaseRecordValidation(final DataSetMetabaseVO dataset, final String processId,
      int numPag) {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, dataset.getId());
    value.put("uuid", processId);
    value.put("numPag", numPag);
    value.put("user", processesMap.get(processId).getRequestingUser());
    addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_RECORD, value);
  }


  /**
   * Release field validation.
   *
   * @param dataset the dataset
   * @param processId the process id
   * @param numPag the num pag
   * @param onlyEmptyFields the only empty fields
   */
  private void releaseFieldValidation(final DataSetMetabaseVO dataset, final String processId,
      int numPag, boolean onlyEmptyFields) {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, dataset.getId());
    value.put("uuid", processId);
    value.put("numPag", numPag);
    value.put("user", processesMap.get(processId).getRequestingUser());
    value.put("onlyEmptyFields", onlyEmptyFields);
    value.put("dataProviderId", dataset.getDataProviderId());
    value.put("datasetSchema", dataset.getDatasetSchema());
    addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_FIELD, value);


  }

  /**
   * Adds the validation task to process.
   *
   * @param processId the process id
   * @param eventType the event type
   * @param value the value
   */
  @Transactional
  public void addValidationTaskToProcess(final String processId, final EventType eventType,
      final Map<String, Object> value) {
    if (checkStartedProcess(processId)) {
      EEAEventVO eeaEventVO = new EEAEventVO();
      eeaEventVO.setEventType(eventType);
      value.put("processId", processId);
      value.put("user", SecurityContextHolder.getContext().getAuthentication().getName());
      value.put("token",
          String.valueOf(SecurityContextHolder.getContext().getAuthentication().getCredentials()));
      eeaEventVO.setData(value);
      ObjectMapper objectMapper = new ObjectMapper();
      String json = "";
      try {
        json = objectMapper.writeValueAsString(eeaEventVO);
      } catch (JsonProcessingException e) {
        LOG.error("error processing json for processId {}", processId);
      }
      Task task = new Task(null, processId, ProcessStatusEnum.IN_QUEUE, TaskType.VALIDATION_TASK, new Date(), null, null,
          json, 0, null);
      taskRepository.save(task);
    }
  }


  /**
   * Check started process.
   *
   * @param processId the process id
   * @return true, if successful
   */
  private boolean checkStartedProcess(String processId) {
    boolean isProcessStarted = processesMap.containsKey(processId);
    if (!isProcessStarted) {
      LOG.warn("Warning, proces {} has not been initialized or it has been already finished",
          processId);
    }
    return isProcessStarted;
  }

  /**
   * Gets the used execution threads.
   *
   * @return the used execution threads
   */
  public int getUsedExecutionThreads() {
    return ((ThreadPoolExecutor) ((EEADelegatingSecurityContextExecutorService) validationExecutorService)
        .getDelegateExecutorService()).getActiveCount();
  }

  /**
   * Gets the tasks by ids.
   *
   * @param ids the ids
   * @return the tasks by ids
   */
  public List<Task> getTasksByIds(List<Long> ids) {
    return CollectionUtils.isNotEmpty(ids) ? taskRepository.findByIdIn(ids) : new ArrayList<>();
  }

  /**
   * Gets the last low priority task.
   *
   * @param limit the limit
   * @return the last low priority task
   */
  public List<Long> getLastLowPriorityTask(int limit) {
    return taskRepository.findLastLowPriorityTask(limit);
  }

  /**
   * Gets the last high priority task.
   *
   * @param limit the limit
   * @return the last high priority task
   */
  public List<Long> getLastHighPriorityTask(int limit) {
    return taskRepository.findLastTask(limit);
  }

  /**
   * update task status
   * @param taskId
   * @param status
   */
  @Transactional
  public void updateTaskStatus(Long taskId, ProcessStatusEnum status) {
    taskRepository.updateStatus(taskId, status.toString());
  }

  /**
   * Update task.
   *
   * @param taskId the task id
   * @param status the status
   * @param finishDate the finish date
   */
  @Transactional
  public void updateTask(Long taskId, ProcessStatusEnum status, Date finishDate) {
    taskRepository.updateStatusAndFinishDate(taskId, status.toString(), finishDate);
  }

  /**
   * Cancel task.
   *
   * @param taskId the task id
   * @param finishDate the finish date
   */
  @Transactional
  public void cancelTask(Long taskId, Date finishDate) {
    taskRepository.cancelStatusAndFinishDate(taskId, finishDate);
  }

  @Transactional
  public List<BigInteger> getInProgressValidationTasksThatExceedTime(long timeInMinutes) {
     return taskRepository.getInProgressValidationTasksThatExceedTime(timeInMinutes);
  }

  /**
   * Instantiates a new validation task.
   */
  @AllArgsConstructor
  private static class ValidationTask {

    /** The task id. */
    Long taskId;

    /** The eea event VO. */
    EEAEventVO eeaEventVO;

    /** The validator. */
    Validator validator;

    /** The dataset id. */
    Long datasetId;

    /** The kie base. */
    KieBase kieBase;

    /** The process id. */
    String processId;

  }

  /**
   * The Class ValidationTasksExecutorThread.
   */
  private class ValidationTasksExecutorThread implements Runnable {

    /** The Constant MILISECONDS. */
    private static final double MILISECONDS = 1000.0;

    /** The validation task. */
    private ValidationTask validationTask;

    /**
     * Instantiates a new validation tasks executor thread.
     *
     * @param validationTask the validation task
     */
    public ValidationTasksExecutorThread(ValidationTask validationTask) {
      this.validationTask = validationTask;
    }

    /**
     * Run.
     */
    @Override
    public void run() {
      ProcessStatusEnum status = ProcessStatusEnum.FINISHED;
      Long currentTime = System.currentTimeMillis();
      int workingThreads =
          ((ThreadPoolExecutor) ((EEADelegatingSecurityContextExecutorService) validationExecutorService)
              .getDelegateExecutorService()).getActiveCount();

      LOG.info(
          "Executing validation for event {}. Working validating threads {}, Available validating threads {}",
          validationTask.eeaEventVO, workingThreads, maxRunningTasks - workingThreads);

      try {
        validationTask.validator.performValidation(validationTask.eeaEventVO,
            validationTask.datasetId, validationTask.kieBase, validationTask.taskId);
      } catch (Exception e) {
        LOG.error("Error processing validations for dataset {} due to exception {}",
            validationTask.datasetId, e.getMessage(), e);
        status = ProcessStatusEnum.IN_QUEUE;
      } finally {
        try {
          if (ProcessStatusEnum.IN_QUEUE.equals(status)) {
            cancelTask(validationTask.taskId, new Date());
          } else {
            updateTask(validationTask.taskId, status, new Date());
          }
          Double totalTime = (System.currentTimeMillis() - currentTime) / MILISECONDS;
          LOG.info("Validation task {} finished, it has taken taken {} seconds",
              validationTask.eeaEventVO, totalTime);
        } catch (Exception e) {
          LOG.error("Error updating validations for dataset {} due to exception {}",
              validationTask.datasetId, e.getMessage(), e);
        } finally {
          try {
            Thread.sleep(1000);
            LOG.info("Checking status of process {} for dataset {}. taskId {}", validationTask.processId, validationTask.datasetId, validationTask.taskId);
            checkFinishedValidations(validationTask.datasetId, validationTask.processId, validationTask.taskId);
          } catch (EEAException | InterruptedException eeaEx) {
            LOG.error("Error finishing validations for dataset {} due to exception {}",
                validationTask.datasetId, eeaEx.getMessage(), eeaEx);
          }

        }

      }
    }

    /**
     * Check finished validations.
     *
     * @param datasetId the dataset id
     * @param processId the process id
     * @return true, if successful
     * @throws EEAException the EEA exception
     */
    private boolean checkFinishedValidations(Long datasetId, String processId, Long taskId) throws EEAException {
      boolean isFinished = false;
      LOG.info("checkFinishedValidations for processId {}, datasetId {} and taskId {}", processId, datasetId, taskId);
      if (taskRepository.isProcessFinished(processId)) {
        LOG.info("isProcessFinished for processId " + processId);
        if (finishProcessInMap(processId)) {
          LOG.info("Removed process {} from processesMap", processId);
          ProcessVO process = processControllerZuul.findById(processId);
          LOG.info("Process {} finished for dataset {}", processId, datasetId);
          // Release the lock manually
          Map<String, Object> executeValidation = new HashMap<>();
          executeValidation.put(LiteralConstants.SIGNATURE,
                  LockSignature.EXECUTE_VALIDATION.getValue());
          executeValidation.put(LiteralConstants.DATASETID, datasetId);
          lockService.removeLockByCriteria(executeValidation);

          Map<String, Object> forceExecuteValidation = new HashMap<>();
          forceExecuteValidation.put(LiteralConstants.SIGNATURE,
                  LockSignature.FORCE_EXECUTE_VALIDATION.getValue());
          forceExecuteValidation.put(LiteralConstants.DATASETID, datasetId);
          lockService.removeLockByCriteria(forceExecuteValidation);
          datasetMetabaseControllerZuul.updateDatasetRunningStatus(datasetId,
                  DatasetRunningStatusEnum.VALIDATED);

          // after last dataset validations have been saved, an event is sent to notify it
          Map<String, Object> value = new HashMap<>();
          value.put(LiteralConstants.DATASET_ID, datasetId);
          value.put("uuid", processId);
          // Setting as user the requesting one as it is being taken from ThreadPropertiesManager
          // and
          // validation threads inheritances from it. This is a side effect.
          value.put("user", process.getUser());
          DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
          S3PathResolver s3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getDataProviderId() != null ? dataset.getDataProviderId() : 0, datasetId, S3_VALIDATION);
          DataFlowVO dataflow = dataFlowControllerZuul.getMetabaseById(dataset.getDataflowId());
          if (dataflow.getBigData() != null) {
            value.put("bigData", dataflow.getBigData());
          }

          if (dataflow.getBigData() != null && !dataflow.getBigData() || dataflow.getBigData() == null) {
            kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_CLEAN_KYEBASE, value);
          }
          if (processControllerZuul.updateProcess(datasetId, -1L, ProcessStatusEnum.FINISHED,
                  ProcessTypeEnum.VALIDATION, processId,
                  process.getUser(), 0, null)) {

            Long jobId = jobProcessControllerZuul.findJobIdByProcessId(processId);
            value.put("validation_job_id", jobId);

            checkAndPromoteFolder(s3PathResolver, dataflow);

            if (datasetId.equals(process.getDatasetId()) && process.isReleased()) {
              ProcessVO nextProcess = null;
              if (jobId != null) {
                List<String> processes = jobProcessControllerZuul.findProcessesByJobId(jobId);
                processes.remove(process.getProcessId());
                for (String procId : processes) {
                  ProcessVO processVO = processControllerZuul.findById(procId);
                  if (processVO.getStatus().equals(ProcessStatusEnum.IN_QUEUE.toString())) {
                    nextProcess = processVO;
                    break;
                  }
                }
              } else {
                nextProcess = processControllerZuul.getNextProcess(processId);
              }
              if (null != nextProcess) {
                if (dataflow.getBigData() != null && dataflow.getBigData()) {
                  S3PathResolver nextS3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getDataProviderId(), nextProcess.getDatasetId(), S3_VALIDATION);
                  JobVO jobVO = null;
                  Boolean createParquetWithSQL = false;
                  if (jobId != null) {
                    jobVO = jobControllerZuul.findJobById(jobId);
                    if (jobVO != null) {
                      Map<String, Object> parameters = jobVO.getParameters();
                      if (parameters.containsKey("createParquetWithSQL")) {
                        createParquetWithSQL = (Boolean) parameters.get("createParquetWithSQL");
                      }
                    }
                    executeValidationDL(nextProcess.getDatasetId(), nextProcess.getProcessId(), true, nextS3PathResolver, createParquetWithSQL);
                  }
                } else {
                  executeValidation(nextProcess.getDatasetId(), nextProcess.getProcessId(), true,
                          true);
                }
              } else if (processControllerZuul.isProcessFinished(processId)) {
                if (jobId != null) {
                  jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);
                }
                kafkaSenderUtils.releaseKafkaEvent(EventType.VALIDATION_RELEASE_FINISHED_EVENT, value);
                if (taskRepository.hasProcessCanceledTasks(processId)) {
                  kafkaSenderUtils.releaseKafkaEvent(EventType.FINISHED_VALIDATION_WITH_CANCELED_TASKS, value);
                }
              }

            } else {
              // Delete the lock to the Release process
              deleteLockToReleaseProcess(datasetId);
              checkAndPromoteFolder(s3PathResolver, dataflow);
              if (jobId != null) {
                jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);
              }
              kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATION_FINISHED_EVENT,
                      value,
                      NotificationVO.builder().user(process.getUser()).datasetId(datasetId).build());
              if (taskRepository.hasProcessCanceledTasks(processId)) {
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.FINISHED_VALIDATION_WITH_CANCELED_TASKS,
                        value,
                        NotificationVO.builder().user(process.getUser()).datasetId(datasetId).build());
              }
            }
          }
          isFinished = true;
        }
      } else {
        LOG.info("Process {} not finished for dataset {}. TaskId {}", processId, datasetId, taskId);
        if (taskRepository.isProcessEnding(processId)) {
          try {
            LOG.info("Process {} for dataset {} ending", processId, datasetId);
            Thread.sleep(5000);
          } catch (InterruptedException eeaEx) {
            LOG.error("interrupting the sleep because of {}", eeaEx);
          }
          checkFinishedValidations(datasetId, processId, taskId);
        }
        LOG.info("Process {} not ending for dataset {}. TaskId {}", processId, datasetId, taskId);
      }
      return isFinished;
    }
  }

  /**
   * Checks and promotes validation table
   * @param s3PathResolver
   * @param dataflow
   * @throws EEAException
   */
  private void checkAndPromoteFolder(S3PathResolver s3PathResolver, DataFlowVO dataflow) throws EEAException {
    if (dataflow.getBigData()!=null && dataflow.getBigData()) {
      if (s3Helper.checkFolderExist(s3PathResolver, S3_VALIDATION_TABLE_PATH)) {
        try {
          String validateTable = s3Service.getTableAsFolderQueryPath(s3PathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
          String query = "ALTER TABLE " + validateTable + " REFRESH METADATA AUTO PROMOTION";
          String id = dremioHelperService.executeSqlStatement(query);
          dremioHelperService.checkIfDremioProcessFinishedSuccessfully(query, id, null);
        } catch (Exception e) {
          throw new EEAException(e.getCause().getCause().getMessage());
        }
      }
    }
  }

  public List<BigInteger> findTasksByProcessId(String processId) {
     return taskRepository.findByProcessId(processId);
  }

  public Boolean findIfTasksExistByProcessIdAndStatusAndDuration(String processId, ProcessStatusEnum status, Long maxDuration) {
    List<Task> tasks = taskRepository.findByProcessIdAndStatus(processId, status);
    for(Task task: tasks){
      if(task.getStartingDate() != null) {
        Long taskDuration =  new Date().getTime() - task.getStartingDate().getTime();
        if(taskDuration > maxDuration) {
          return true;
        }
      }
    }
    return false;
  }

  @Transactional
  public void updateTaskStatusByProcessIdAndCurrentStatus(ProcessStatusEnum status, String processId, Set<String> currentStatuses){
    taskRepository.updateTaskStatusByProcessIdAndCurrentStatus(status.toString(), new Date(), processId, currentStatuses);
  }

  public Integer findTasksCountByProcessIdAndStatusIn(String processId,List<String> status) {
    return taskRepository.findTasksCountByProcessIdAndStatusIn(processId, status);
  }

  public TaskVO  getTaskThatExceedsTimeByStatusesAndType(String processId, long timeInMinutes, Set<String> statuses, TaskType taskType) {
    Task task = taskRepository.getTaskThatExceedsTimeByStatusesAndType(processId, timeInMinutes, statuses, taskType.getValue());
    if(task == null){
      return null;
    }
    return taskMapper.entityToClass(task);
  }

  public TaskVO findTaskById(Long taskId) {
     Optional<Task> task = taskRepository.findById(taskId);
     if (task.isPresent()) {
       return taskMapper.entityToClass(task.get());
     }
     return null;
  }

  /**
   * Compose list query.
   * @param originsFilter the origins filter
   * @return the string
   */
  public String composeListQuery(String originsFilter) {
    return "('" + originsFilter.replace(",", "','") + "')";
  }

  /**
   * Removes the spaces generated with automatic toString.
   * @param listFormatted the list formatted
   * @return the string
   */
  public String removeSpacesEnum(String listFormatted) {
    return listFormatted.replace(", ", ",").replace("[", "").replace("]", "");
  }

  /**
   * Builds rule validation folder name
   * @param ruleVO
   * @param validationResolver
   * @param fileName
   * @param ruleIdLength
   * @param parquetFile
   * @return
   */
  public String getRuleValidationFolderName(RuleVO ruleVO, S3PathResolver validationResolver, String fileName, int ruleIdLength, String parquetFile) {
    StringBuilder pathBuilder = new StringBuilder();
    return pathBuilder.append(s3Service.getTableAsFolderQueryPath(validationResolver, S3_VALIDATION_TABLE_PATH)).append(SLASH).append(ruleVO.getShortCode())
            .append(DASH).append(ruleVO.getRuleId().substring(ruleIdLength - 3, ruleIdLength)).append(SLASH).append(fileName).toString();
  }

  /**
   * uploads validation parquet to S3
   * @param ruleVO
   * @param validationResolver
   * @param fileName
   * @param ruleIdLength
   * @param parquetFile
   */
  public void uploadValidationParquetToS3(RuleVO ruleVO, S3PathResolver validationResolver, String fileName, int ruleIdLength, String parquetFile) {
    //if the dataset to validate is of reference type, then the validation path should be changed
    String s3FilePath = this.getRuleValidationFolderName(ruleVO, validationResolver, fileName, ruleIdLength, parquetFile);
    s3Helper.uploadFileToBucket(s3FilePath, parquetFile);
  }
}
