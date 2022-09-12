package org.eea.validation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.kafka.domain.ConsumerGroupVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.MemberDescriptionVO;
import org.eea.kafka.utils.KafkaAdminUtils;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.EEADelegatingSecurityContextExecutorService;
import org.eea.validation.kafka.command.Validator;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
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

  /** The validation helper. */
  @InjectMocks
  private ValidationHelper validationHelper;

  /** The validation service. */
  @Mock
  private ValidationService validationService;

  /** The kie base. */
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

  /** The dataset metabase controller zuul. */
  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The reference dataset controller zuul. */
  @Mock
  private ReferenceDatasetControllerZuul referenceDatasetControllerZuul;

  /** The data flow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataFlowControllerZuul;

  /** The data. */
  private Map<String, Object> data;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The processes map. */
  private Map<String, ValidationProcessVO> processesMap;

  /** The executor service. */
  private ExecutorService executorService;

  /** The security context. */
  @Mock
  private SecurityContext securityContext;

  /** The authentication. */
  @Mock
  private Authentication authentication;

  /** The rules repository. */
  @Mock
  private RulesRepository rulesRepository;

  /** The schemas repository. */
  @Mock
  private SchemasRepository schemasRepository;

  /** The process controller zuul. */
  @Mock
  private ProcessControllerZuul processControllerZuul;

  /** The task repository. */
  @Mock
  private TaskRepository taskRepository;

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
    List<Long> periodDays = new ArrayList<>();
    periodDays.add(1L);
    periodDays.add(2L);
    periodDays.add(3L);
    periodDays.add(4L);
    ReflectionTestUtils.setField(validationHelper, "periodDays", periodDays);
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
   * @throws EEAException the EEA exception
   */
  @Test
  public void getKieBase() throws EEAException {
    Mockito.when(validationService.loadRulesKnowledgeBase(Mockito.eq(1l), Mockito.any()))
        .thenReturn(kieBase);
    Mockito.when(processControllerZuul.findById(Mockito.any())).thenReturn(new ProcessVO());
    KieBase result = validationHelper.getKieBase("", 1l, "idRule");
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
    Mockito.when(processControllerZuul.findById(Mockito.any())).thenReturn(new ProcessVO());

    Mockito.doThrow(new EEAException("error")).when(validationService)
        .loadRulesKnowledgeBase(Mockito.eq(1l), Mockito.any());
    validationHelper.getKieBase("", 1l, null);
  }



  /**
   * Finish process.
   */
  @Test
  public void finishProcess() {
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    validationHelper.finishProcessInMap("1");
    Assert.assertNull(processesMap.get("1"));
  }


  /**
   * Execute validation.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeValidation() throws EEAException {
    ReflectionTestUtils.setField(validationHelper, "fieldBatchSize", 20);
    ReflectionTestUtils.setField(validationHelper, "recordBatchSize", 20);
    ReflectionTestUtils.setField(validationHelper, "initialTax", 2);
    ConsumerGroupVO consumerGroups = new ConsumerGroupVO();
    Collection<MemberDescriptionVO> members = new ArrayList<>();

    MemberDescriptionVO member = new MemberDescriptionVO();
    members.add(member);
    consumerGroups.setMembers(members);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDatasetTypeEnum(DatasetTypeEnum.REPORTING);
    dataset.setId(1L);
    dataset.setDataflowId(1L);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setId(1L);
    datasetMetabase.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    datasetMetabase.setDataflowId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    RulesSchema rules = new RulesSchema();
    Rule rule = new Rule();
    rule.setType(EntityTypeEnum.RECORD);
    rules.setRules(Arrays.asList(rule));
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setDeadlineDate(new Date());

    Mockito
        .when(processControllerZuul.updateProcess(Mockito.anyLong(), Mockito.anyLong(),
            Mockito.any(ProcessStatusEnum.class), Mockito.any(ProcessTypeEnum.class),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyBoolean()))
        .thenReturn(true);
    Mockito.when(rulesRepository.findByIdDatasetSchema(Mockito.any())).thenReturn(rules);
    Mockito.when(validationService.countRecordsDataset(Mockito.eq(1l))).thenReturn(1);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any()))
        .thenReturn(new DataSetSchema());

    validationHelper.executeValidation(1l, "1", false, false);
    Mockito.verify(validationService, Mockito.times(1)).deleteAllValidation(Mockito.eq(1l));
  }


  /**
   * Execute validation 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeValidation2() throws EEAException {
    ReflectionTestUtils.setField(validationHelper, "fieldBatchSize", 20);
    ReflectionTestUtils.setField(validationHelper, "recordBatchSize", 20);
    ReflectionTestUtils.setField(validationHelper, "initialTax", 2);
    List<TableValue> tables = new ArrayList<>();
    TableValue table = new TableValue();
    table.setId(1l);
    tables.add(table);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    ConsumerGroupVO consumerGroups = new ConsumerGroupVO();
    Collection<MemberDescriptionVO> members = new ArrayList<>();

    MemberDescriptionVO member = new MemberDescriptionVO();
    members.add(member);
    consumerGroups.setMembers(members);

    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDatasetTypeEnum(DatasetTypeEnum.REPORTING);
    dataset.setId(1L);
    dataset.setDataflowId(1L);
    DataSetMetabaseVO datasetMetabase = new DataSetMetabaseVO();
    datasetMetabase.setId(1L);
    datasetMetabase.setDatasetSchema("5cf0e9b3b793310e9ceca190");
    datasetMetabase.setDataflowId(1L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabase);
    RulesSchema rules = new RulesSchema();
    Rule rule = new Rule();
    rule.setSqlSentence("SELECT * from dataset_1.\"t1\"");
    rule.setType(EntityTypeEnum.RECORD);
    rule.setEnabled(true);
    rules.setRules(Arrays.asList(rule));

    ReferenceDatasetVO reference = new ReferenceDatasetVO();
    reference.setId(23L);
    reference.setDatasetSchema(new ObjectId().toString());

    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setDeadlineDate(new Date());
    Mockito
        .when(processControllerZuul.updateProcess(Mockito.anyLong(), Mockito.anyLong(),
            Mockito.any(ProcessStatusEnum.class), Mockito.any(ProcessTypeEnum.class),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyBoolean()))
        .thenReturn(true);
    Mockito.when(rulesRepository.findSqlRules(Mockito.any())).thenReturn(Arrays.asList(rule));
    Mockito.when(rulesRepository.findSqlRulesEnabled(Mockito.any()))
        .thenReturn(Arrays.asList(rule));
    validationHelper.executeValidation(1l, "1", false, true);
    Mockito.verify(referenceDatasetControllerZuul, Mockito.times(1))
        .findReferenceDatasetByDataflowId(Mockito.any());

  }


  /**
   * Process validation exceding maximum parallelism.
   *
   * @throws EEAException the EEA exception
   * @throws InterruptedException the interrupted exception
   */
  // @Test
  public void processValidationExcedingMaximumParallelism()
      throws EEAException, InterruptedException {
    ReflectionTestUtils.setField(validationHelper, "validationExecutorService",
        new EEADelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(2)));
    ReflectionTestUtils.setField(validationHelper, "maxRunningTasks", 2);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    eeaEventVO.getData().put("user", "user");
    eeaEventVO.getData().put("token", "credentials");
    Validator validator = (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase, Long taskId) -> {
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
    validationHelper.processValidation(1L, eeaEventVO, "1", 0l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);
    validationHelper.processValidation(1L, eeaEventVO, "1", 1l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);
    validationHelper.processValidation(1L, eeaEventVO, "1", 2l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);
    validationHelper.processValidation(1L, eeaEventVO, "1", 2l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);
    validationHelper.processValidation(1L, eeaEventVO, "1", 2l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);
    validationHelper.processValidation(1L, eeaEventVO, "1", 2l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);

    Thread.sleep(1000);// giving some time to the threads to update the counter

    Assert.assertTrue(eeaEventVO.getData().containsKey("counter"));
    Assert.assertTrue(Integer.valueOf(eeaEventVO.getData().get("counter").toString()) <= 2);
  }


  /**
   * Process validation one task as coordinator.
   *
   * @throws EEAException the EEA exception
   * @throws InterruptedException the interrupted exception
   */
  // @Test
  public void processValidationOneTaskAsCoordinator() throws EEAException, InterruptedException {
    Deque<EEAEventVO> pendingValidations = new ConcurrentLinkedDeque<>();
    pendingValidations.add(eeaEventVO);
    pendingValidations.add(eeaEventVO);
    eeaEventVO.getData().put("user", "user");
    eeaEventVO.getData().put("token", "credentials");
    processesMap.put("1", new ValidationProcessVO(null, "user1"));
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    ReflectionTestUtils.setField(validationHelper, "taskReleasedTax", 1);

    ReflectionTestUtils.setField(validationHelper, "validationExecutorService", executorService);

    Validator validator = (EEAEventVO eeaEventVO, Long datasetId, KieBase kieBase, Long taskId) -> {
      // This counter will be useful to verify how many threads has been executed simultaneously
      // before the test ends
      eeaEventVO.getData().put("counter", 0);
    };
    validationHelper.processValidation(1L, eeaEventVO, "1", 0l, validator,
        EventType.COMMAND_VALIDATED_RECORD_COMPLETED);

    Thread.sleep(1000);// giving some time to the threads to update the counter

    Assert.assertTrue(eeaEventVO.getData().containsKey("counter"));
    Assert.assertTrue(Integer.valueOf(eeaEventVO.getData().get("counter").toString()) == 0);
    Mockito.verify(kafkaSenderUtils, Mockito.times(1))
        .releaseKafkaEvent(Mockito.any(EEAEventVO.class));
  }

}
