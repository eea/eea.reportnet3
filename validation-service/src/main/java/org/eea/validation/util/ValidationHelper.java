package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.lock.enums.LockSignature;
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
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.model.ValidationProcessVO;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationHelper.
 */
@Component
public class ValidationHelper {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationHelper.class);
  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * The kafka sender utils.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The lock service.
   */
  @Autowired
  private LockService lockService;

  /**
   * The validation service.
   */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /**
   * The field batch size.
   */
  @Value("${validation.fieldBatchSize}")
  private int fieldBatchSize;

  /**
   * The record batch size.
   */
  @Value("${validation.recordBatchSize}")
  private int recordBatchSize;

  @Value("${validation.tasks.release.tax}")
  private int taskReleasedTax;

  @Value("${validation.tasks.initial.tax}")
  private int initialTax;

  /**
   * The table repository.
   */
  @Autowired
  private TableRepository tableRepository;

  /**
   * the kafka admin utils
   */
  @Autowired
  private KafkaAdminUtils kafkaAdminUtils;

  /**
   * The processes map.
   */
  private Map<String, ValidationProcessVO> processesMap;


  /**
   * Instantiates a new file loader helper.
   */
  public ValidationHelper() {
    super();
    processesMap = new ConcurrentHashMap<>();
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
   *
   * @return the kie base
   *
   * @throws EEAException the eea exception
   */
  public KieBase getKieBase(String processId, Long datasetId) throws EEAException {
    KieBase kieBase = null;
    synchronized (processesMap) {
      if (!processesMap.containsKey(processId)) {
        initializeProcess(processId, false);
      }
      if (null == processesMap.get(processId).getKieBase()) {
        processesMap.get(processId).setKieBase(validationService.loadRulesKnowledgeBase(datasetId));
      }
      kieBase = processesMap.get(processId).getKieBase();
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
   *
   * @throws EEAException the eea exception
   */
  public boolean isProcessCoordinator(final String processId) throws EEAException {
    checkStartedProcess(processId);
    return processesMap.get(processId).isCoordinatorProcess();
  }

  /**
   * Finish process removing all the data related to the given processId .
   *
   * @param processId the process id
   */
  public void finishProcess(String processId) {
    LOG.info("Process {} finished ", processId);
    processesMap.remove(processId);
  }

  /**
   * Initialize process control structure for the given processId as coordinator or as a worker
   *
   * @param processId the process id
   * @param isCoordinator the is coordinator
   */
  public void initializeProcess(String processId, boolean isCoordinator) {
    ValidationProcessVO process = new ValidationProcessVO(0, new ConcurrentLinkedDeque<>(), null,
        isCoordinator);
    synchronized (processesMap) {
      processesMap.put(processId, process);
    }
  }

  /**
   * Execute validation. The lock would be released on ValidationHelper.checkFinishedValidations(..)
   *
   * @param datasetId the dataset id
   * @param processId the uu id
   *
   * @throws EEAException the EEA exception
   */
  @Async
  @LockMethod(removeWhenFinish = false, isController = false)
  public void executeValidation(@LockCriteria(name = "datasetId") final Long datasetId,
      String processId) {
    //Initialize process as coordinator
    initializeProcess(processId, true);
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
    LOG.info("Deleting all Validations");
    validationService.deleteAllValidation(datasetId);
    LOG.info("Collecting Dataset Validation tasks");
    releaseDatasetValidation(datasetId, processId);
    LOG.info("Collecting Table Validation tasks");
    releaseTableValidation(datasetId, processId);
    LOG.info("Collecting Record Validation tasks");
    releaseRecordsValidation(datasetId, processId);
    LOG.info("Collecting Field Validation tasks");
    releaseFieldsValidation(datasetId, processId);
    try {
      startProcess(processId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error starting validation process for process id {}", processId, e);
      this.finishProcess(processId);
    }
  }

  private void startProcess(final String processId) throws EEAException {
    checkStartedProcess(processId);
    ConsumerGroupVO consumerGroupVO = kafkaAdminUtils.getConsumerGroupInfo();
    //get the number of validation instances in the system.
    //at least there should be one, the coordinator node :)
    Integer initialTasks = consumerGroupVO.getMembers().size() * this.initialTax;
    synchronized (processesMap) {

      //Sending initial tasks, 1 per validation instance in the system
      //Due to this initial work load there will be always more pendingOks to be received than pendingValidation to be sent
      //This will affect to the reducePendingTasks method since it will need to know whether to send more tasks or not
      Deque pendingTasks = processesMap.get(processId).getPendingValidations();
      LOG.info(
          "started proces {}. Pending Ok's to be received: {}, pending tasks to be sent: {} initial workload: {}",
          processId,
          processesMap.get(processId).getPendingOks(), pendingTasks.size() - initialTasks,
          initialTasks);
      while (initialTasks > 0) {
        EEAEventVO event = (EEAEventVO) pendingTasks.poll();
        if (null != event) {
          kafkaSenderUtils.releaseKafkaEvent(event);
        }
        initialTasks--;
      }

    }
  }

  /**
   * Release fields validation.
   *
   * @param datasetId the dataset id
   * @param uuId the uu id
   */
  private void releaseFieldsValidation(Long datasetId, String uuId) {
    Integer totalFields = validationService.countFieldsDataset(datasetId);
    if (fieldBatchSize != 0) {
      for (int i = 0; totalFields >= 0; totalFields = totalFields - fieldBatchSize) {
        releaseFieldValidation(datasetId, uuId, i);
        i++;
      }
    }
  }

  /**
   * Release records validation.
   *
   * @param datasetId the dataset id
   * @param uuId the uu id
   */
  private void releaseRecordsValidation(Long datasetId, String uuId) {
    Integer totalRecords = validationService.countRecordsDataset(datasetId);
    if (recordBatchSize != 0) {
      for (int i = 0; totalRecords >= 0; totalRecords = totalRecords - recordBatchSize) {
        releaseRecordValidation(datasetId, uuId, i);
        i++;
      }
    }
  }


  /**
   * Release table validation.
   *
   * @param datasetId the dataset id
   * @param uuId the uu id
   */
  private void releaseTableValidation(Long datasetId, String uuId) {
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);

    List<TableValue> tableList = tableRepository.findAll();
    Integer totalTables = tableList.size();
    for (int i = 0; totalTables > 0; totalTables = totalTables - 1) {
      Long idTable = tableList.get(i).getId();
      releaseTableValidation(datasetId, uuId, idTable);
      i++;
    }
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
    checkStartedProcess(processId);
    synchronized (processesMap) {
      Integer pendingOk = processesMap.get(processId).getPendingOks();
      pendingOk--;
      processesMap.get(processId).setPendingOks(pendingOk);
      if (!this.checkFinishedValidations(datasetId, processId)) {
        //process is not over, but still it could happen that there is no task to be sent
        //remember pendingOks > pendingValidations.size()
        Integer pendingValidations = processesMap.get(processId).getPendingValidations().size();
        if (pendingValidations > 0) {
          //there are more tasks to be sent, just send them out, at least, one more task
          int tasksToBeSent = this.taskReleasedTax;
          while (tasksToBeSent > 0) {
            if (processesMap.get(processId).getPendingValidations().size() >= 1) {
              this.kafkaSenderUtils
                  .releaseKafkaEvent(processesMap.get(processId).getPendingValidations().poll());
            }
            tasksToBeSent--;
          }
          LOG.info(
              "Sent next tasks for process {}. There are still {} tasks to be sent and {} pending Ok's to be received",
              processId,
              processesMap.get(processId).getPendingValidations().size(), pendingOk);
        }
      }
    }
  }


  /**
   * Release dataset validation.
   *
   * @param datasetId the dataset id
   * @param processId the uuid
   */
  private void releaseDatasetValidation(final Long datasetId, final String processId) {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    value.put("uuid", processId);

    try {
      addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_DATASET, value);
    } catch (EEAException e) {
      LOG_ERROR.error("Error trying to add task {} of type {} to process {}", value,
          EventType.COMMAND_VALIDATE_FIELD.getKey(), processId, e);
    }

  }

  /**
   * Release table validation.
   *
   * @param datasetId the dataset id
   * @param processId the processId
   * @param Tablenum the tablenum
   */
  private void releaseTableValidation(final Long datasetId, final String processId, Long Tablenum) {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    value.put("uuid", processId);
    value.put("idTable", Tablenum);

    try {
      addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_TABLE, value);
    } catch (EEAException e) {
      LOG_ERROR.error("Error trying to add task {} of type {} to process {}", value,
          EventType.COMMAND_VALIDATE_FIELD.getKey(), processId, e);
    }

  }

  /**
   * Release record validation.
   *
   * @param datasetId the dataset id
   * @param processId the processId
   * @param numPag the numPag
   */
  private void releaseRecordValidation(final Long datasetId, final String processId, int numPag) {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    value.put("uuid", processId);
    value.put("numPag", numPag);

    try {
      addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_RECORD, value);
    } catch (EEAException e) {
      LOG_ERROR.error("Error trying to add task {} of type {} to process {}", value,
          EventType.COMMAND_VALIDATE_FIELD.getKey(), processId, e);
    }
  }


  /**
   * Release field validation.
   *
   * @param datasetId the dataset id
   * @param processId the uuid
   * @param numPag the numPag
   */
  private void releaseFieldValidation(final Long datasetId, final String processId, int numPag) {
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    value.put("uuid", processId);
    value.put("numPag", numPag);
    try {
      addValidationTaskToProcess(processId, EventType.COMMAND_VALIDATE_FIELD, value);
    } catch (EEAException e) {
      LOG_ERROR.error("Error trying to add task {} of type {} to process {}", value,
          EventType.COMMAND_VALIDATE_FIELD.getKey(), processId, e);
    }


  }

  /**
   * Check finished validations. If process is finished then it releases kafka notifications and
   * finishes the process Returns true if process is over. false otherwise
   *
   * @param datasetId the dataset id
   * @param processId the uuid
   *
   * @throws EEAException the EEA exception
   */
  private boolean checkFinishedValidations(final Long datasetId, final String processId)
      throws EEAException {
    boolean isFinished = false;
    //remember pendingOks > pendingValidations.size()
    if (processesMap.get(processId).getPendingOks() == 0) {
      // Release the lock manually
      List<Object> criteria1 = new ArrayList<>();
      List<Object> criteria2 = new ArrayList<>();
      criteria1.add(LockSignature.EXECUTE_VALIDATION.getValue());
      criteria1.add(datasetId);
      criteria2.add(LockSignature.FORCE_EXECUTE_VALIDATION.getValue());
      criteria2.add(datasetId);
      lockService.removeLockByCriteria(criteria1);
      lockService.removeLockByCriteria(criteria2);

      // after last dataset validations have been saved, an event is sent to notify it
      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, datasetId);
      value.put("uuid", processId);
      Integer pendingValidations = processesMap.get(processId).getPendingValidations().size();
      if (pendingValidations > 0) {
        //this is just a warning messages to show an abnormal situation finishing validation process
        LOG.warn(
            "There are still {} pending tasks to be sent, they will not be sent as process is finished",
            pendingValidations
        );
      }
      this.finishProcess(processId);

      kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_CLEAN_KYEBASE, value);
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATION_FINISHED_EVENT, value,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .datasetId(datasetId).build());
      isFinished = true;
    }
    return isFinished;
  }

  private void addValidationTaskToProcess(final String processId, final EventType eventType,
      final Map<String, Object> value)
      throws EEAException {
    checkStartedProcess(processId);
    synchronized (processesMap) {
      Integer pendingOk = processesMap.get(processId).getPendingOks();
      if (null == pendingOk) {
        pendingOk = 1;
      } else {
        pendingOk++;
      }
      processesMap.get(processId).setPendingOks(pendingOk);
      EEAEventVO eeaEventVO = new EEAEventVO();
      eeaEventVO.setEventType(eventType);
      eeaEventVO.setData(value);
      processesMap.get(processId).getPendingValidations().add(eeaEventVO);
    }
  }

  private void checkStartedProcess(String processId) throws EEAException {
    if (!processesMap.containsKey(processId)) {
      LOG_ERROR.error("Error, proces {} has not been initialized or it has been already finished",
          processId);
      throw new EEAException(
          String.format("Error, proces %s has not been initialized or it has been already finished",
              processId));
    }
  }
}
