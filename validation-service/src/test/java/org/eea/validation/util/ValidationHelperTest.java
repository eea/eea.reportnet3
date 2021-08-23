package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.kafka.domain.ConsumerGroupVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.MemberDescriptionVO;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaAdminUtils;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.EEADelegatingSecurityContextExecutorService;
import org.eea.validation.kafka.command.Validator;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.model.ValidationProcessVO;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class ValidationHelperTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationHelperTest {

  /**
   * The validation helper.
   */
  @InjectMocks
  private ValidationHelper validationHelper;

  /**
   * The validation service.
   */
  @Mock
  private ValidationService validationService;

  /**
   * The kie base.
   */
  @Mock
  private KieBase kieBase;

  /**
   * The kafka admin utils.
   */
  @Mock
  private KafkaAdminUtils kafkaAdminUtils;

  /**
   * The kafka sender utils.
   */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The table repository.
   */
  @Mock
  private TableRepository tableRepository;

  /**
   * The lock service.
   */
  @Mock
  private LockService lockService;

  /**
   * The dataset metabase controller zuul.
   */
  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /**
   * The data.
   */
  private Map<String, Object> data;

  /**
   * The eea event VO.
   */
  private EEAEventVO eeaEventVO;

  /**
   * The processes map.
   */
  private Map<String, ValidationProcessVO> processesMap;

  /**
   * The executor service.
   */
  private ExecutorService executorService;

  @Mock
  private SecurityContext securityContext;
  @Mock
  private Authentication authentication;

  @Mock
  private RulesRepository rulesRepository;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    data = new HashMap<>();
    data.put("uuid", "uuid");
    data.put("dataset_id", "1");
    data.put("kieBase", kieBase);
    data.put("numPag", 1);
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATE_RECORD);
    eeaEventVO.setData(data);
    processesMap = new ConcurrentHashMap<>();
    executorService =
        new EEADelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(2));
    MockitoAnnotations.openMocks(this);
    SecurityContextHolder.setContext(securityContext);
  }

  /**
   * Finish tasks.
   */
  @After
  public void finishTasks() {
    executorService.shutdown();
  }

  /**
   * Gets the kie base.
   *
   * @return the kie base
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getKieBase() throws EEAException {
    Mockito.when(validationService.loadRulesKnowledgeBase(Mockito.eq(1l))).thenReturn(kieBase);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    KieBase result = validationHelper.getKieBase("", 1l);
    Assert.assertNotNull(result);
  }

  /**
   * Gets the kie base exception.
   *
   * @return the kie base exception
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getKieBaseException() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Mockito.doThrow(new EEAException("error")).when(validationService)
        .loadRulesKnowledgeBase(Mockito.eq(1l));
    validationHelper.getKieBase("", 1l);
  }

  /**
   * Checks if is process coordinator.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void isProcessCoordinator() throws EEAException {
    processesMap.put("1", new ValidationProcessVO(0, null, null, true, "user1", false));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);

    Assert.assertTrue(validationHelper.isProcessCoordinator("1"));
  }


  /**
   * Finish process.
   */
  @Test
  public void finishProcess() {
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    validationHelper.finishProcess("1");
    Assert.assertNull(processesMap.get("1"));
  }

  /**
   * Initialize process as coordinator.
   */
  @Test
  public void initializeProcessAsCoordinator() {
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    validationHelper.initializeProcess("1", true, false);
    Assert.assertNotNull(processesMap.get("1"));
    Assert.assertTrue(processesMap.get("1").isCoordinatorProcess());
  }

  /**
   * Initialize process as worker.
   */
  @Test
  public void initializeProcessAsWorker() {
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    validationHelper.initializeProcess("1", false, false);
    Assert.assertNotNull(processesMap.get("1"));
    Assert.assertFalse(processesMap.get("1").isCoordinatorProcess());
  }

  /**
   * Execute validation.
   *
   * @throws EEAException
   */
  @Test
  public void executeValidation() throws EEAException {
    ReflectionTestUtils.setField(validationHelper, "fieldBatchSize", 20);
    ReflectionTestUtils.setField(validationHelper, "recordBatchSize", 20);
    ReflectionTestUtils.setField(validationHelper, "initialTax", 2);
    List<TableValue> tables = new ArrayList<>();
    TableValue table = new TableValue();
    table.setId(1l);
    tables.add(table);
    Mockito.when(tableRepository.findAll()).thenReturn(tables);

    ConsumerGroupVO consumerGroups = new ConsumerGroupVO();
    Collection<MemberDescriptionVO> members = new ArrayList<>();

    MemberDescriptionVO member = new MemberDescriptionVO();
    members.add(member);
    consumerGroups.setMembers(members);
    Mockito.when(kafkaAdminUtils.getConsumerGroupInfo()).thenReturn(consumerGroups);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Mockito.when(datasetMetabaseControllerZuul.getType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setId(1L);
    datasetMetabase.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    RulesSchema rules = new RulesSchema();
    Rule rule = new Rule();
    rule.setType(EntityTypeEnum.RECORD);
    rules.setRules(Arrays.asList(rule));
    Mockito.when(rulesRepository.findByIdDatasetSchema(Mockito.any())).thenReturn(rules);
    Mockito.when(validationService.countRecordsDataset(Mockito.eq(1l))).thenReturn(1);

    validationHelper.executeValidation(1l, "1", false, false);
    Mockito.verify(validationService, Mockito.times(1)).deleteAllValidation(Mockito.eq(1l));
    Mockito.verify(kafkaSenderUtils, Mockito.times(2))
        .releaseKafkaEvent(Mockito.any(EEAEventVO.class));
  }


  /**
   * Reduce pending tasks finishing process.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void reducePendingTasksFinishingProcess() throws EEAException {

    Deque<EEAEventVO> pendingValidations = new ConcurrentLinkedDeque<>();
    processesMap.put("1",
        new ValidationProcessVO(1, pendingValidations, null, true, "user1", true));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    Mockito.when(datasetMetabaseControllerZuul.getLastDatasetValidationForRelease(Mockito.eq(1l)))
        .thenReturn(null);
    validationHelper.reducePendingTasks(1l, "1");

    Mockito.verify(kafkaSenderUtils, Mockito.times(1))
        .releaseKafkaEvent(Mockito.eq(EventType.COMMAND_CLEAN_KYEBASE), Mockito.anyMap());

    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseKafkaEvent(
        Mockito.eq(EventType.VALIDATION_RELEASE_FINISHED_EVENT), Mockito.anyMap());
    Assert.assertNull(processesMap.get("1"));
  }

  /**
   * Reduce pending tasks finishing process abnormal situation.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void reducePendingTasksFinishingProcessAbnormalSituation() throws EEAException {

    Deque<EEAEventVO> pendingValidations = new ConcurrentLinkedDeque<>();
    pendingValidations.add(eeaEventVO);
    processesMap.put("1",
        new ValidationProcessVO(1, pendingValidations, null, true, "user1", false));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);

    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    validationHelper.reducePendingTasks(1l, "1");

    Mockito.verify(kafkaSenderUtils, Mockito.times(1))
        .releaseKafkaEvent(Mockito.eq(EventType.COMMAND_CLEAN_KYEBASE), Mockito.anyMap());

    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseNotificableKafkaEvent(
        Mockito.eq(EventType.VALIDATION_FINISHED_EVENT), Mockito.anyMap(),
        Mockito.any(NotificationVO.class));
    Assert.assertNull(processesMap.get("1"));
  }

  /**
   * Reduce pending tasks process not finished.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void reducePendingTasksProcessNotFinished() throws EEAException {
    Deque<EEAEventVO> pendingValidations = new ConcurrentLinkedDeque<>();
    pendingValidations.add(eeaEventVO);

    processesMap.put("1",
        new ValidationProcessVO(2, pendingValidations, null, true, "user1", false));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    ReflectionTestUtils.setField(validationHelper, "taskReleasedTax", 2);

    validationHelper.reducePendingTasks(1l, "1");
    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseKafkaEvent(Mockito.eq(eeaEventVO));
    // Checking that no VALIDATION_FINISHED_EVENT message has been sent since process is not over
    // yet
    Mockito.verify(kafkaSenderUtils, Mockito.times(0)).releaseNotificableKafkaEvent(
        Mockito.eq(EventType.VALIDATION_FINISHED_EVENT), Mockito.anyMap(),
        Mockito.any(NotificationVO.class));
    Assert.assertNotNull(processesMap.get("1"));
  }

  /**
   * Reduce pending tasks process not finished send several tasks.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void reducePendingTasksProcessNotFinishedSendSeveralTasks() throws EEAException {
    Deque<EEAEventVO> pendingValidations = new ConcurrentLinkedDeque<>();
    pendingValidations.add(eeaEventVO);
    pendingValidations.add(eeaEventVO);
    processesMap.put("1",
        new ValidationProcessVO(2, pendingValidations, null, true, "user1", false));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    ReflectionTestUtils.setField(validationHelper, "taskReleasedTax", 2);

    validationHelper.reducePendingTasks(1l, "1");
    Mockito.verify(kafkaSenderUtils, Mockito.times(2)).releaseKafkaEvent(Mockito.eq(eeaEventVO));
    // Checking that no VALIDATION_FINISHED_EVENT message has been sent since process is not over
    // yet
    Mockito.verify(kafkaSenderUtils, Mockito.times(0)).releaseNotificableKafkaEvent(
        Mockito.eq(EventType.VALIDATION_FINISHED_EVENT), Mockito.anyMap(),
        Mockito.any(NotificationVO.class));
    Assert.assertNotNull(processesMap.get("1"));
  }

  /**
   * Process validation exceding maximum parallelism.
   *
   * @throws EEAException the EEA exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void processValidationExcedingMaximumParallelism()
      throws EEAException, InterruptedException {
    ReflectionTestUtils.setField(validationHelper, "validationExecutorService",
        new EEADelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(2)));
    ReflectionTestUtils.setField(validationHelper, "maxRunningTasks", 2);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    eeaEventVO.getData().put("user", "user");
    eeaEventVO.getData().put("token", "credentials");
    Validator validator = (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase) -> {
      try {
        // Thiss counter will be usefull to verify how many threads has been executed simultaneously
        // before the test ends
        if (eeaEventVO.getData().containsKey("counter")) {
          Integer counter = Integer.valueOf(eeaEventVO.getData().get("counter").toString()) + 1;
          eeaEventVO.getData().put("counter", counter);
        } else {
          eeaEventVO.getData().put("counter", new Integer(1));
        }
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    };
    validationHelper.processValidation(eeaEventVO, "1", 0l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);
    validationHelper.processValidation(eeaEventVO, "1", 1l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);
    validationHelper.processValidation(eeaEventVO, "1", 2l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);
    validationHelper.processValidation(eeaEventVO, "1", 2l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);
    validationHelper.processValidation(eeaEventVO, "1", 2l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);
    validationHelper.processValidation(eeaEventVO, "1", 2l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);

    Thread.sleep(1000);// giving some time to the threads to update the counter

    Assert.assertTrue(eeaEventVO.getData().containsKey("counter"));
    Assert.assertTrue(Integer.valueOf(eeaEventVO.getData().get("counter").toString()) <= 2);
  }


  /**
   * Process validation one task as not coordinator.
   *
   * @throws EEAException the EEA exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void processValidationOneTaskAsNotCoordinator() throws EEAException, InterruptedException {
    Deque<EEAEventVO> pendingValidations = new ConcurrentLinkedDeque<>();
    eeaEventVO.getData().put("user", "user");
    eeaEventVO.getData().put("token", "credentials");
    pendingValidations.add(eeaEventVO);
    pendingValidations.add(eeaEventVO);

    processesMap.put("1",
        new ValidationProcessVO(2, pendingValidations, null, false, "user1", false));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    ReflectionTestUtils.setField(validationHelper, "taskReleasedTax", 2);

    ReflectionTestUtils.setField(validationHelper, "validationExecutorService", executorService);
    Validator validator = (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase) -> {
      // Thiss counter will be usefull to verify how many threads has been executed simultaneously
      // before the test ends
      eeaEventVO.getData().put("counter", 0);
    };
    validationHelper.processValidation(eeaEventVO, "1", 0l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);

    Thread.sleep(1000);// giving some time to the threads to update the counter

    Assert.assertTrue(eeaEventVO.getData().containsKey("counter"));
    Assert.assertTrue(Integer.valueOf(eeaEventVO.getData().get("counter").toString()) == 0);
    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseKafkaEvent(
        Mockito.eq(EventType.COMMAND_VALIDATED_RECORD_COMPLETED), Mockito.eq(eeaEventVO.getData()));
  }

  /**
   * Process validation one task as coordinator.
   *
   * @throws EEAException the EEA exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void processValidationOneTaskAsCoordinator() throws EEAException, InterruptedException {
    Deque<EEAEventVO> pendingValidations = new ConcurrentLinkedDeque<>();
    pendingValidations.add(eeaEventVO);
    pendingValidations.add(eeaEventVO);
    eeaEventVO.getData().put("user", "user");
    eeaEventVO.getData().put("token", "credentials");
    processesMap.put("1",
        new ValidationProcessVO(2, pendingValidations, null, true, "user1", false));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    ReflectionTestUtils.setField(validationHelper, "taskReleasedTax", 1);

    ReflectionTestUtils.setField(validationHelper, "validationExecutorService", executorService);

    Validator validator = (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase) -> {
      // This counter will be usefull to verify how many threads has been executed simultaneously
      // before the test ends
      eeaEventVO.getData().put("counter", 0);
    };
    validationHelper.processValidation(eeaEventVO, "1", 0l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);

    Thread.sleep(1000);// giving some time to the threads to update the counter

    Assert.assertTrue(eeaEventVO.getData().containsKey("counter"));
    Assert.assertTrue(Integer.valueOf(eeaEventVO.getData().get("counter").toString()) == 0);
    Mockito.verify(kafkaSenderUtils, Mockito.times(1))
        .releaseKafkaEvent(Mockito.any(EEAEventVO.class));
  }

}
