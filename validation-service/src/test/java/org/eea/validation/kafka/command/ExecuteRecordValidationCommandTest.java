package org.eea.validation.kafka.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.ValidationHelper;
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
 * The Class ExecuteRecordValidationCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteRecordValidationCommandTest {

  /**
   * The execute record validation command.
   */
  @InjectMocks
  private ExecuteRecordValidationCommand executeRecordValidationCommand;

  /**
   * The kafka sender utils.
   */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The validation service.
   */
  @Mock
  private ValidationService validationService;
  @Mock
  private ValidationHelper validationHelper;

  /**
   * The kie base.
   */
  @Mock
  private KieBase kieBase;

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
  private ConcurrentHashMap<String, Integer> processesMap;

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
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.COMMAND_VALIDATE_RECORD, executeRecordValidationCommand.getEventType());
  }

  /**
   * Execute test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTest() throws EEAException {
    processesMap.put("uuid", 1);
    ReflectionTestUtils.setField(executeRecordValidationCommand, "recordBatchSize", 20);
    Mockito.when(validationHelper.getProcessesMap()).thenReturn(new ConcurrentHashMap<>());

    when(validationHelper.getKieBase(Mockito.any(), Mockito.any())).thenReturn(kieBase);

    executeRecordValidationCommand.execute(eeaEventVO);

    Mockito.verify(validationService, times(1)).validateRecord(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Execute exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeExceptionTest() throws EEAException {
    processesMap.put("uuid", 1);
    when(validationHelper.getProcessesMap()).thenReturn(processesMap);
    ReflectionTestUtils.setField(executeRecordValidationCommand, "recordBatchSize", 20);
    doThrow(new EEAException()).when(validationService).validateRecord(Mockito.any(), Mockito.any(),
        Mockito.any());

    executeRecordValidationCommand.execute(eeaEventVO);

    Mockito.verify(validationService, times(1)).validateRecord(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  @Test
  public void executeExceptionSendTest() throws EEAException {
    when(validationHelper.getProcessesMap()).thenReturn(processesMap);
    ReflectionTestUtils.setField(executeRecordValidationCommand, "recordBatchSize", 20);
    doThrow(new EEAException()).when(validationService).validateRecord(Mockito.any(), Mockito.any(),
        Mockito.any());

    executeRecordValidationCommand.execute(eeaEventVO);

    Mockito.verify(validationService, times(1)).validateRecord(Mockito.any(), Mockito.any(),
        Mockito.any());
  }
}
