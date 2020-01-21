package org.eea.validation.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eea.exception.EEAException;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.service.ValidationService;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class ValidationHelperTest.
 */
public class ValidationHelperTest {

  /** The validation helper. */
  @InjectMocks
  public ValidationHelper validationHelper;

  /** The validation service. */
  @Mock
  private ValidationService validationService;

  /** The drools active sessions. */
  @Mock
  private Map<String, KieBase> droolsActiveSessions;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The table repository. */
  @Mock
  private TableRepository tableRepository;

  /** The kie base. */
  @Mock
  private KieBase kieBase;

  /** The lock service. */
  @Mock
  private LockService lockService;



  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    ThreadPropertiesManager.setVariable("user", "user");
    ReflectionTestUtils.setField(validationHelper, "recordBatchSize", 1);
    ReflectionTestUtils.setField(validationHelper, "fieldBatchSize", 1);
  }



  /**
   * Test get kie base.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetKieBase() throws EEAException {
    Map<String, KieBase> droolsActiveSessions = new ConcurrentHashMap<>();
    ReflectionTestUtils.setField(validationHelper, "droolsActiveSessions", droolsActiveSessions);
    Mockito.when(validationService.loadRulesKnowledgeBase(Mockito.any())).thenReturn(kieBase);
    assertEquals(kieBase, validationHelper.getKieBase("", 1L));
  }

  /**
   * Test remove kie base.
   */
  @Test
  public void testRemoveKieBase() {
    Mockito.when(droolsActiveSessions.containsKey(Mockito.any())).thenReturn(true);
    validationHelper.removeKieBase("");
    Mockito.verify(droolsActiveSessions).remove(Mockito.any());
  }

  /**
   * Test execute validation.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteValidation() throws EEAException {
    List<TableValue> tables = new ArrayList<>();
    tables.add(new TableValue());
    tables.add(new TableValue());
    doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(), Mockito.any());
    Mockito.when(tableRepository.findAllTables()).thenReturn(tables);
    Mockito.when(tableRepository.findAll()).thenReturn(tables);
    Mockito.when(validationService.countRecordsDataset(Mockito.any())).thenReturn(1);
    validationHelper.executeValidation(1L, "");
  }

  /**
   * Test get processes map.
   */
  @Test
  public void testGetProcessesMap() {
    ConcurrentHashMap<String, Integer> processesMap = new ConcurrentHashMap<>();;
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    assertEquals(processesMap, validationHelper.getProcessesMap());
  }

  /**
   * Test check finished validations.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testCheckFinishedValidations() throws EEAException {
    ConcurrentHashMap<String, Integer> processesMap = new ConcurrentHashMap<>();;
    processesMap.put("uuid", 0);
    ReflectionTestUtils.setField(validationHelper, "processesMap", processesMap);
    validationHelper.checkFinishedValidations(1L, "uuid");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseKafkaEvent(Mockito.any(), Mockito.any());
  }

}
