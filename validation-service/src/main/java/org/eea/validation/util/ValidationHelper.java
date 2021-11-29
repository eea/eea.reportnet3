package org.eea.validation.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.kafka.domain.ConsumerGroupVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaAdminUtils;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.EEADelegatingSecurityContextExecutorService;
import org.eea.utils.LiteralConstants;
import org.eea.validation.kafka.command.Validator;
import org.eea.validation.persistence.data.domain.TableValue;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

/**
 * The Class ValidationHelper.
 */
@Component
public class ValidationHelper implements DisposableBean {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationHelper.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;

  /** The kafka admin utils. */
  @Autowired
  private KafkaAdminUtils kafkaAdminUtils;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;


  /** The reference dataset controller zuul. */
  @Autowired
  private ReferenceDatasetControllerZuul referenceDatasetControllerZuul;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;


  /** The Constant DATASET_: {@value}. */
  private static final String DATASET = "dataset_";

  /** The Constant Rule: {@value}. */
  private static final String Rule = null;

  /**
   * Instantiates a new file loader helper.
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
    validationExecutorService = new EEADelegatingSecurityContextExecutorService(
        Executors.newFixedThreadPool(maxRunningTasks));
  }

  /**
   * Gets kie base for given process. If the processId is not registered as an active process then a
   * new process entry is created as worker (coordinator=false). This will occurr when a
   * microservice receives a validation command for the first time for the given process If kie base
   * does not exist then it is created.
   *
   * Throws exception if process has not been initialized
   *
   * @param processId the process id
   * @param datasetId the dataset id
   * @param rule the rule
   * @return the kie base
   * @throws EEAException the eea exception
   */
  public KieBase getKieBase(String processId, Long datasetId, Rule rule) throws EEAException {
    KieBase kieBase = null;
    synchronized (processesMap) {
      if (!processesMap.containsKey(processId)) {
        initializeProcess(processId, false, false);
      }
      if (null == rule) {
        if (null == processesMap.get(processId).getKieBase()) {
          processesMap.get(processId)
              .setKieBase(validationService.loadRulesKnowledgeBase(datasetId, null));
        } else {
          kieBase = validationService.loadRulesKnowledgeBase(datasetId, null);
        }
      } else {
        kieBase = validationService.loadRulesKnowledgeBase(datasetId, rule);
      }
    }
    return kieBase;
  }

  /**
   * Return true if the a validation process was launched from this service instance. false
   * otherwise
   *
   * @param processId the process id
   *
   * @return the boolean
   */
  public boolean isProcessCoordinator(final String processId) {
    boolean isProcessCoordinator = false;
    if (checkStartedProcess(processId)) {
      isProcessCoordinator = processesMap.get(processId).isCoordinatorProcess();
    }
    return isProcessCoordinator;
  }

  /**
   * Finish process removing all the data related to the given processId .
   *
   * @param processId the process id
   */
  public void finishProcess(String processId) {
    LOG.info("Process {} finished ", processId);
    processesMap.remove(processId);
    System.gc();
  }

  /**
   * Initialize process control structure for the given processId as coordinator or as a worker.
   *
   * @param processId the process id
   * @param isCoordinator the is coordinator
   * @param released the released
   */
  public void initializeProcess(String processId, boolean isCoordinator, boolean released) {
    ValidationProcessVO process = new ValidationProcessVO(0, new ConcurrentLinkedDeque<>(), null,
        isCoordinator, SecurityContextHolder.getContext().getAuthentication().getName(), released);

    synchronized (processesMap) {
      processesMap.put(processId, process);
    }
  }

  /**
   * Execute validation. The lock would be released on ValidationHelper.checkFinishedValidations(..)
   *
   * @param datasetId the dataset id
   * @param processId the uu id
   * @param released the released
   * @param updateViews the update views
   * @throws EEAException the EEA exception
   */
  @Async
  @LockMethod(removeWhenFinish = true, isController = false)
  public void executeValidation(@LockCriteria(name = "datasetId") final Long datasetId,
      String processId, boolean released, boolean updateViews) throws EEAException {

    DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    DatasetTypeEnum type = dataset.getDatasetTypeEnum();

    if (DatasetTypeEnum.DESIGN.equals(type)) {
      executeValidationProcess(datasetId, processId, released);
    } else if (Boolean.FALSE.equals(updateViews)) {
      executeValidationProcess(datasetId, processId, released);
    } else {
      Map<String, Object> values = new HashMap<>();
      values.put(LiteralConstants.DATASET_ID, datasetId);
      values.put("released", released);
      values.put("referencesToRefresh",
          List.copyOf(updateMaterializedViewsOfReferenceDatasetsInSQL(datasetId,
              dataset.getDataflowId(), dataset.getDatasetSchema())));
      kafkaSenderUtils.releaseKafkaEvent(EventType.REFRESH_MATERIALIZED_VIEW_EVENT, values);
    }
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
        datasetsToRefresh
            .addAll(datasetsNotChanged.stream().map(Long::parseLong).collect(Collectors.toList()));

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
          LOG_ERROR.error("Error validating SQL rule, processing the sentence {}. Message {}",
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
   * @param datasetId the dataset id
   * @param processId the process id
   * @param released the released
   */
  public void executeValidationProcess(final Long datasetId, String processId, boolean released) {
    // Initialize process as coordinator
    DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    RulesSchema rules =
        rulesRepository.findByIdDatasetSchema(new ObjectId(dataset.getDatasetSchema()));
    initializeProcess(processId, true, released);
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
    LOG.info("Deleting all Validations");
    validationService.deleteAllValidation(datasetId);
    LOG.info("Collecting Dataset Validation tasks");
    releaseDatasetValidation(dataset, processId);
    LOG.info("Collecting Record Validation tasks");
    if (rules.getRules().stream().anyMatch(rule -> EntityTypeEnum.RECORD.equals(rule.getType()))) {
      releaseRecordsValidation(dataset, processId);
    }
    LOG.info("Collecting Field Validation tasks");
    releaseFieldsValidation(dataset, processId, !filterEmptyFields(rules.getRules()));
    LOG.info("Collecting Table Validation tasks");
    releaseTableValidation(dataset, processId);
    startProcess(processId);
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
   * Reduce pending tasks for the given processId.
   *
   * If as a consequence of the reduction the number of the pending tasks reaches 0 then the process
   * is finished and notifications are sent via Kafka
   *
   * If there are more task after the reduction then more tasks are sent
   *
   * @param datasetId the dataset id
   * @param processId the process id
   *
   * @throws EEAException the eea exception
   */
  public void reducePendingTasks(final Long datasetId, final String processId) throws EEAException {
    if (checkStartedProcess(processId)) {
      synchronized (processesMap) {
        Integer pendingOk = processesMap.get(processId).getPendingOks();
        processesMap.get(processId).setPendingOks(--pendingOk);
        if (!this.checkFinishedValidations(datasetId, processId)) {
          // process is not over, but still it could happen that there is no task to be sent
          // remember pendingOks > pendingValidations.size()
          Integer pendingValidations = processesMap.get(processId).getPendingValidations().size();
          if (pendingValidations > 0) {
            pendingValidationProcess(processId);
          }
          LOG.info(
              "There are still {} tasks to be sent and {} pending Ok's to be received for process {}",
              processesMap.get(processId).getPendingValidations().size(), pendingOk, processId);
        }
      }
    }
  }

  /**
   * Pending validation process.
   *
   * @param processId the process id
   */
  private void pendingValidationProcess(final String processId) {
    // there are more tasks to be sent, just send them out, at least, one more task
    int tasksToBeSent = this.taskReleasedTax;
    int sentTasks = 0;
    while (tasksToBeSent > 0) {
      if (!processesMap.get(processId).getPendingValidations().isEmpty()) {
        this.kafkaSenderUtils
            .releaseKafkaEvent(processesMap.get(processId).getPendingValidations().poll());
        sentTasks++;
      } else {
        break;
      }
      tasksToBeSent--;
    }
    LOG.info("Sent next {} tasks for process {}", sentTasks, processId);
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
   * Submits the validation task to the validation executor thread pool. If any thread is available
   * the task will start automatically. Otherwise it will wait in a FIFO queue
   *
   * @param eeaEventVO the eea event vo
   * @param processId the process id
   * @param datasetId the dataset id
   * @param validator the validator
   * @param notificationEventType the notification event type
   *
   * @throws EEAException the eea exception
   */
  public void processValidation(EEAEventVO eeaEventVO, String processId, Long datasetId,
      Validator validator, EventType notificationEventType) throws EEAException {
    Rule rule = null;
    if (eeaEventVO.getData().get("sqlRule") != null) {
      ObjectMapper mapper =
          new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      rule = mapper.convertValue(eeaEventVO.getData().get("sqlRule"), Rule.class);
    }
    ValidationTask validationTask = new ValidationTask(eeaEventVO, validator, datasetId,
        getKieBase(processId, datasetId, rule), processId, notificationEventType);

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
   *
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
   *
   * @throws EEAException the EEA exception
   */
  public void addLockToReleaseProcess(Long datasetId) throws EEAException {
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
  }

  /**
   * Delete lock to release process.
   *
   * @param datasetId the dataset id
   */
  private void deleteLockToReleaseProcess(Long datasetId) {
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
  }

  /**
   * Start process.
   *
   * @param processId the process id
   */
  private void startProcess(final String processId) {
    if (checkStartedProcess(processId)) {
      ConsumerGroupVO consumerGroupVO = kafkaAdminUtils.getConsumerGroupInfo();
      // get the number of validation instances in the system.
      // at least there should be one, the coordinator node :)
      Integer initialTasks = consumerGroupVO.getMembers().size() * this.initialTax;
      synchronized (processesMap) {

        // Sending initial tasks, 1 per validation instance in the system
        // Due to this initial work load there will be always more pendingOks to be received than
        // pendingValidation to be sent
        // This will affect to the reducePendingTasks method since it will need to know whether to
        // send more tasks or not
        Deque pendingTasks = processesMap.get(processId).getPendingValidations();
        LOG.info(
            "started proces {}. Pending Ok's to be received: {}, pending tasks to be sent: {} initial workload: {}",
            processId, processesMap.get(processId).getPendingOks(),
            pendingTasks.size() - initialTasks, initialTasks);
        while (initialTasks > 0) {
          EEAEventVO event = (EEAEventVO) pendingTasks.poll();
          if (null != event) {
            kafkaSenderUtils.releaseKafkaEvent(event);
          }
          initialTasks--;
        }

      }
    }
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
      for (Integer totalFields =
          onlyEmptyFields ? validationService.countEmptyFieldsDataset(dataset.getId())
              : validationService
                  .countFieldsDataset(dataset.getId()); totalFields >= 0; totalFields =
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
      for (Integer totalRecords =
          validationService.countRecordsDataset(dataset.getId()); totalRecords >= 0; totalRecords =
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
    List<Rule> rules = rulesRepository.findSqlRules(new ObjectId(dataset.getDatasetSchema()));
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
   * @param processId the uuid
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
   * @param processId the processId
   * @param idTable the idTable
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
    value.put("sqlRule", sqlRule);
    addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_TABLE, value);
  }

  /**
   * Release record validation.
   *
   * @param dataset the dataset
   * @param processId the processId
   * @param numPag the numPag
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
   * @param processId the uuid
   * @param numPag the numPag
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
   * Check finished validations. If process is finished then it releases kafka notifications and
   * finishes the process Returns true if process is over. false otherwise
   *
   * @param datasetId the dataset id
   * @param processId the uuid
   *
   * @return true, if successful
   *
   * @throws EEAException the EEA exception
   */
  private boolean checkFinishedValidations(final Long datasetId, final String processId)
      throws EEAException {
    boolean isFinished = false;
    // remember pendingOks > pendingValidations.size()
    if (processesMap.get(processId).getPendingOks() == 0) {
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

      // after last dataset validations have been saved, an event is sent to notify it
      String notificationUser = processesMap.get(processId).getRequestingUser();
      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, datasetId);
      value.put("uuid", processId);
      // Setting as user the requesting one as it is being taken from ThreadPropertiesManager and
      // validation threads inheritances from it. This is a side effect.
      value.put("user", notificationUser);
      Integer pendingValidations = processesMap.get(processId).getPendingValidations().size();
      if (pendingValidations > 0) {
        // this is just a warning messages to show an abnormal situation finishing validation
        // process
        LOG.warn(
            "There are still {} pending tasks to be sent for process {}, they will not be sent as process is finished",
            pendingValidations, processId);
      }

      boolean isRelease = processesMap.get(processId).isReleased();
      this.finishProcess(processId);

      kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_CLEAN_KYEBASE, value);
      if (isRelease) {
        Long nextDatasetId =
            datasetMetabaseControllerZuul.getLastDatasetValidationForRelease(datasetId);
        if (null != nextDatasetId) {
          this.executeValidation(nextDatasetId, UUID.randomUUID().toString(), true, false);
        } else {
          kafkaSenderUtils.releaseKafkaEvent(EventType.VALIDATION_RELEASE_FINISHED_EVENT, value);
        }

      } else {
        // Delete the lock to the Release process
        deleteLockToReleaseProcess(datasetId);

        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATION_FINISHED_EVENT, value,
            NotificationVO.builder().user(notificationUser).datasetId(datasetId).build());
      }

      isFinished = true;
    }
    return isFinished;
  }

  /**
   * Adds the validation task to process.
   *
   * @param processId the process id
   * @param eventType the event type
   * @param value the value
   */
  private void addValidationTaskToProcess(final String processId, final EventType eventType,
      final Map<String, Object> value) {
    if (checkStartedProcess(processId)) {
      synchronized (processesMap) {
        Integer pendingOk = processesMap.get(processId).getPendingOks();
        if (null == pendingOk) {
          pendingOk = 0;
        }

        processesMap.get(processId).setPendingOks(++pendingOk);
        EEAEventVO eeaEventVO = new EEAEventVO();
        eeaEventVO.setEventType(eventType);
        value.put("processId", processId);
        eeaEventVO.setData(value);

        processesMap.get(processId).getPendingValidations().add(eeaEventVO);
      }
    }
  }


  /**
   * Check started process.
   *
   * @param processId the process id
   *
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
   * Instantiates a new validation task.
   *
   * @param eeaEventVO the eea event VO
   * @param validator the validator
   * @param datasetId the dataset id
   * @param kieBase the kie base
   * @param processId the process id
   * @param notificationEventType the notification event type
   */
  @AllArgsConstructor
  private static class ValidationTask {

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

    /** The notification event type. */
    EventType notificationEventType;
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

      Long currentTime = System.currentTimeMillis();
      int workingThreads =
          ((ThreadPoolExecutor) ((EEADelegatingSecurityContextExecutorService) validationExecutorService)
              .getDelegateExecutorService()).getActiveCount();

      LOG.info(
          "Executing validation for event {}. Working validating threads {}, Available validating threads {}",
          validationTask.eeaEventVO, workingThreads, maxRunningTasks - workingThreads);

      try {
        validationTask.validator.performValidation(validationTask.eeaEventVO,
            validationTask.datasetId, validationTask.kieBase);
      } catch (EEAException e) {
        LOG_ERROR.error("Error processing validations for dataset {} due to exception {}",
            validationTask.datasetId, e.getMessage(), e);
        validationTask.eeaEventVO.getData().put("error", e);
      } finally {
        // if this is the coordinator validation instance, then no need to send message, just to
        // update
        // expected pending ok's and verify if process is finished
        if (isProcessCoordinator(validationTask.processId)) {
          // if it's not finished a message with the next task will be sent as part of the
          // reducePendingTasks execution
          try {
            reducePendingTasks(validationTask.datasetId, validationTask.processId);
          } catch (EEAException e) {
            LOG_ERROR.error("Error trying to reduce pending tasks due to {}", e.getMessage(), e);
          }
        } else {
          // send the message to coordinator validation instance
          kafkaSenderUtils.releaseKafkaEvent(validationTask.notificationEventType,
              validationTask.eeaEventVO.getData());
        }
        Double totalTime = (System.currentTimeMillis() - currentTime) / MILISECONDS;
        LOG.info("Validation task {} finished, it has taken taken {} seconds",
            validationTask.eeaEventVO, totalTime);
      }
    }
  }
}
