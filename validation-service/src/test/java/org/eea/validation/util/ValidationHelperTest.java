package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.ConsumerGroupVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.MemberDescriptionVO;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaAdminUtils;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.model.ValidationProcessVO;
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
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class ValidationHelperTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationHelperTest {

  /** The validation helper. */
  @InjectMocks
  private ValidationHelper validationHelper;

  /** The validation service. */
  @Mock
  private ValidationService validationService;

  /**
   * The kie base.
   */
  @Mock
  private KieBase kieBase;

  /** The kafka admin utils. */
  @Mock
  private KafkaAdminUtils kafkaAdminUtils;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The table repository. */
  @Mock
  private TableRepository tableRepository;

  /** The lock service. */
  @Mock
  private LockService lockService;
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
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the kie base.
   *
   * @return the kie base
   * @throws EEAException the EEA exception
   */
  @Test
  public void getKieBase() throws EEAException {
    Mockito.when(validationService.loadRulesKnowledgeBase(Mockito.eq(1l))).thenReturn(kieBase);
    KieBase result = validationHelper.getKieBase("", 1l);
    Assert.assertNotNull(result);
  }

  /**
   * Gets the kie base exception.
   *
   * @return the kie base exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getKieBaseException() throws EEAException {
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
    processesMap.put("1", new ValidationProcessVO(0, null, null, true));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);

    Assert.assertTrue(validationHelper.isProcessCoordinator("1"));
  }

  /**
   * Checks if is process coordinator exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void isProcessCoordinatorException() throws EEAException {
    validationHelper.isProcessCoordinator("1");
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
    validationHelper.initializeProcess("1", true);
    Assert.assertNotNull(processesMap.get("1"));
    Assert.assertTrue(processesMap.get("1").isCoordinatorProcess());
  }

  /**
   * Initialize process as worker.
   */
  @Test
  public void initializeProcessAsWorker() {
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    validationHelper.initializeProcess("1", false);
    Assert.assertNotNull(processesMap.get("1"));
    Assert.assertFalse(processesMap.get("1").isCoordinatorProcess());
  }

  /**
   * Execute validation.
   */
  @Test
  public void executeValidation() {
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

    Mockito.when(validationService.countRecordsDataset(Mockito.eq(1l))).thenReturn(1);
    Mockito.when(validationService.countFieldsDataset(Mockito.eq(1l))).thenReturn(1);

    validationHelper.executeValidation(1l, "1");
    Mockito.verify(validationService, Mockito.times(1)).deleteAllValidation(Mockito.eq(1l));
    Mockito.verify(kafkaSenderUtils, Mockito.times(2))
        .releaseKafkaEvent(Mockito.any(EEAEventVO.class));
  }


  /**
   * Reduce pending tasks error.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void reducePendingTasksError() throws EEAException {
    validationHelper.reducePendingTasks(1l, "1");
  }

  /**
   * Reduce pending tasks finishing process.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void reducePendingTasksFinishingProcess() throws EEAException {

    Deque<EEAEventVO> pendingValidations = new ConcurrentLinkedDeque<>();
    processesMap.put("1", new ValidationProcessVO(1, pendingValidations, null, true));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);

    validationHelper.reducePendingTasks(1l, "1");

    Mockito.verify(kafkaSenderUtils, Mockito.times(1))
        .releaseKafkaEvent(Mockito.eq(EventType.COMMAND_CLEAN_KYEBASE), Mockito.anyMap());

    Mockito.verify(kafkaSenderUtils, Mockito.times(1)).releaseNotificableKafkaEvent(
        Mockito.eq(EventType.VALIDATION_FINISHED_EVENT), Mockito.anyMap(),
        Mockito.any(NotificationVO.class));
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
    processesMap.put("1", new ValidationProcessVO(1, pendingValidations, null, true));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);

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

    processesMap.put("1", new ValidationProcessVO(2, pendingValidations, null, true));
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
    processesMap.put("1", new ValidationProcessVO(2, pendingValidations, null, true));
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

}
