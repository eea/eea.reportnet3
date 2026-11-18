package org.eea.validation.util;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.security.jwt.utils.JwtTokenProvider;
import org.eea.utils.LiteralConstants;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.eea.validation.util.model.ValidationProcessVO;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * ATA2-3422 / EEA 310871: reproduces the lock-key mismatches that left dataset 112692 stuck in
 * "Validation in progress".
 *
 * <p>
 * The lock service is backed by an in-memory store keyed by the criteria map, so a removal only
 * succeeds when it builds exactly the same criteria (and therefore the same hash id) as the
 * creation. {@code {preparationCode=null}} and "no preparationCode key" are different locks, just
 * like in {@code LockServiceImpl.generateHashCode}.
 * </p>
 */
public class ValidationHelperLockKeysTest {

  private static final Long DATASET_ID = 112692L;
  private static final String PROCESS_ID = "process-274001";

  private ValidationHelper validationHelper;

  /** In-memory lock table: one entry per lock, identified by its criteria. */
  private final Set<Map<String, Object>> locks = new HashSet<>();

  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;
  private ProcessControllerZuul processControllerZuul;
  private TaskRepository taskRepository;
  private SecurityContext previousContext;

  @Before
  public void setUp() throws EEAException {
    validationHelper = new ValidationHelper(null);

    LockService lockService = Mockito.mock(LockService.class);
    Mockito.when(lockService.createLock(ArgumentMatchers.any(), ArgumentMatchers.any(),
        ArgumentMatchers.any(), ArgumentMatchers.any())).thenAnswer(inv -> {
          Map<String, Object> criteria = new HashMap<>(inv.getArgument(3));
          if (!locks.add(criteria)) {
            throw new EEAException("Already locked: " + criteria);
          }
          return new LockVO(inv.getArgument(0), inv.getArgument(1), LockType.METHOD,
              criteria.hashCode(), criteria);
        });
    Mockito.when(lockService.findByCriteria(ArgumentMatchers.any())).thenAnswer(inv -> {
      Map<String, Object> criteria = inv.getArgument(0);
      return locks.contains(criteria) ? Mockito.mock(LockVO.class) : null;
    });
    Mockito.when(lockService.removeLockByCriteria(ArgumentMatchers.any()))
        .thenAnswer(inv -> locks.remove(new HashMap<>(inv.<Map<String, Object>>getArgument(0))));
    ReflectionTestUtils.setField(validationHelper, "lockService", lockService);

    datasetMetabaseControllerZuul = Mockito.mock(DataSetMetabaseControllerZuul.class);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setId(DATASET_ID);
    dataset.setDataflowId(2090L);
    dataset.setDataProviderId(5L);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(DATASET_ID))
        .thenReturn(dataset);
    ReflectionTestUtils.setField(validationHelper, "datasetMetabaseControllerZuul",
        datasetMetabaseControllerZuul);

    DatasetSchemaControllerZuul datasetSchemaControllerZuul =
        Mockito.mock(DatasetSchemaControllerZuul.class);
    TableSchemaIdNameVO table = new TableSchemaIdNameVO();
    table.setIdTableSchema("tableSchemaId");
    Mockito.when(datasetSchemaControllerZuul.getTableSchemasIds(ArgumentMatchers.any(),
        ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Collections.singletonList(table));
    ReflectionTestUtils.setField(validationHelper, "datasetSchemaControllerZuul",
        datasetSchemaControllerZuul);

    processControllerZuul = Mockito.mock(ProcessControllerZuul.class);
    taskRepository = Mockito.mock(TaskRepository.class);
    ReflectionTestUtils.setField(validationHelper, "processControllerZuul", processControllerZuul);
    ReflectionTestUtils.setField(validationHelper, "taskRepository", taskRepository);
    ReflectionTestUtils.setField(validationHelper, "dataFlowControllerZuul",
        Mockito.mock(DataFlowControllerZuul.class));
    ReflectionTestUtils.setField(validationHelper, "kafkaSenderUtils",
        Mockito.mock(KafkaSenderUtils.class));
    ReflectionTestUtils.setField(validationHelper, "jobProcessControllerZuul",
        Mockito.mock(JobProcessControllerZuul.class));
    ReflectionTestUtils.setField(validationHelper, "jobControllerZuul",
        Mockito.mock(JobControllerZuul.class));
    ReflectionTestUtils.setField(validationHelper, "jwtTokenProvider",
        Mockito.mock(JwtTokenProvider.class));

    previousContext = SecurityContextHolder.getContext();
    SecurityContextHolder.setContext(new SecurityContextImpl(
        new UsernamePasswordAuthenticationToken("reportnet_admin", "token")));
  }

  @After
  public void tearDown() {
    SecurityContextHolder.setContext(previousContext);
  }

  /** EXECUTE_VALIDATION lock exactly as MethodLockAspect builds it for executeValidation(..). */
  private static Map<String, Object> executeValidationLock() {
    Map<String, Object> criteria = new HashMap<>();
    criteria.put(LiteralConstants.SIGNATURE, LockSignature.EXECUTE_VALIDATION.getValue());
    criteria.put(LiteralConstants.DATASETID, DATASET_ID);
    return criteria;
  }

  /** FORCE_EXECUTE_VALIDATION lock as built by the aspect / addLockToReleaseProcess. */
  private static Map<String, Object> forceExecuteValidationLock(String preparationCode) {
    Map<String, Object> criteria = new HashMap<>();
    criteria.put(LiteralConstants.SIGNATURE, LockSignature.FORCE_EXECUTE_VALIDATION.getValue());
    criteria.put(LiteralConstants.DATASETID, DATASET_ID);
    criteria.put(LiteralConstants.PREPARATION_CODE, preparationCode);
    return criteria;
  }

  private boolean hasLock(String signature) {
    return locks.stream().anyMatch(l -> signature.equals(l.get(LiteralConstants.SIGNATURE)));
  }

  /**
   * Bug 1 (job 274001): release validation of a reporting dataset with SQL rules. The Kafka
   * COMMAND_EXECUTE_VALIDATION handler re-adds FORCE_EXECUTE_VALIDATION with preparationCode=null
   * (no timer); when the process finishes, checkFinishedValidations must remove that same lock.
   */
  @Test
  public void releasedValidationFinishRemovesForceLockReAddedByKafkaHandler() throws Exception {
    validationHelper.addLockToReleaseProcess(DATASET_ID, null);
    Assert.assertTrue("precondition: FORCE lock created",
        locks.contains(forceExecuteValidationLock(null)));

    ProcessVO process = new ProcessVO();
    process.setProcessId(PROCESS_ID);
    process.setDatasetId(DATASET_ID);
    process.setReleased(true);
    process.setUser("reportnet_admin");
    Mockito.when(processControllerZuul.findById(PROCESS_ID)).thenReturn(process);
    Mockito.when(processControllerZuul.updateProcess(ArgumentMatchers.any(), ArgumentMatchers.any(),
        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
        ArgumentMatchers.any(), ArgumentMatchers.anyInt(), ArgumentMatchers.any()))
        .thenReturn(true);
    Mockito.when(taskRepository.isProcessFinished(PROCESS_ID)).thenReturn(true);
    DataFlowControllerZuul dataFlowControllerZuul =
        (DataFlowControllerZuul) ReflectionTestUtils.getField(validationHelper,
            "dataFlowControllerZuul");
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setBigData(false);
    Mockito.when(dataFlowControllerZuul.getMetabaseById(ArgumentMatchers.any()))
        .thenReturn(dataflow);
    @SuppressWarnings("unchecked")
    Map<String, ValidationProcessVO> processesMap = (Map<String, ValidationProcessVO>) ReflectionTestUtils
        .getField(validationHelper, "processesMap");
    processesMap.put(PROCESS_ID, Mockito.mock(ValidationProcessVO.class));

    invokeCheckFinishedValidations(null);

    Assert.assertFalse("FORCE_EXECUTE_VALIDATION lock leaked after a released validation finished: "
        + locks, hasLock(LockSignature.FORCE_EXECUTE_VALIDATION.getValue()));
  }

  /**
   * Bug 2 (job 274246): the orchestrator safety net (deleteLocksToReleaseProcess ->
   * deleteLockToReleaseProcess(datasetId, null)) must remove an orphan EXECUTE_VALIDATION lock,
   * which the aspect creates with datasetId only.
   */
  @Test
  public void deleteLockToReleaseProcessRemovesOrphanExecuteValidationLock() throws EEAException {
    locks.add(executeValidationLock());

    validationHelper.deleteLockToReleaseProcess(DATASET_ID, null);

    Assert.assertFalse("EXECUTE_VALIDATION lock survived the safety-net cleanup: " + locks,
        hasLock(LockSignature.EXECUTE_VALIDATION.getValue()));
  }

  /** Same as above for a preparation dataset: the aspect lock still has no preparationCode. */
  @Test
  public void deleteLockToReleaseProcessRemovesExecuteLockForPreparationDataset() {
    locks.add(executeValidationLock());
    locks.add(forceExecuteValidationLock("PREP1"));

    validationHelper.deleteLockToReleaseProcess(DATASET_ID, "PREP1");

    Assert.assertFalse("EXECUTE_VALIDATION lock survived the cleanup: " + locks,
        hasLock(LockSignature.EXECUTE_VALIDATION.getValue()));
    Assert.assertFalse("FORCE_EXECUTE_VALIDATION lock survived the cleanup: " + locks,
        hasLock(LockSignature.FORCE_EXECUTE_VALIDATION.getValue()));
  }

  /** Non-release path keeps working: FORCE created by the controller aspect is removed. */
  @Test
  public void deleteLockToReleaseProcessRemovesControllerForceLock() {
    locks.add(forceExecuteValidationLock(null));

    validationHelper.deleteLockToReleaseProcess(DATASET_ID, null);

    Assert.assertFalse(hasLock(LockSignature.FORCE_EXECUTE_VALIDATION.getValue()));
  }

  /** checkFinishedValidations lives in the private inner ValidationTasksExecutorThread. */
  private void invokeCheckFinishedValidations(String preparationCode) throws Exception {
    Class<?> threadClass =
        Class.forName(ValidationHelper.class.getName() + "$ValidationTasksExecutorThread");
    Class<?> taskClass = Class.forName(ValidationHelper.class.getName() + "$ValidationTask");
    Constructor<?> constructor = threadClass.getDeclaredConstructor(ValidationHelper.class, taskClass);
    constructor.setAccessible(true);
    Object executorThread = constructor.newInstance(validationHelper, null);
    ReflectionTestUtils.invokeMethod(executorThread, "checkFinishedValidations", DATASET_ID,
        PROCESS_ID, 1L, preparationCode);
  }
}
