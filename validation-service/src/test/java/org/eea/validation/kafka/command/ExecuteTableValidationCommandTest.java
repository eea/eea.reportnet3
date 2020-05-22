package org.eea.validation.kafka.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

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

/**
 * The Class ExecuteTableValidationCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteTableValidationCommandTest {

  /**
   * The execute table validation command.
   */
  @InjectMocks
  private ExecuteTableValidationCommand executeTableValidationCommand;

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
  /**
   * The Validation helper
   */
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
    data.put("idTable", 1);
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATE_TABLE);
    eeaEventVO.setData(data);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.COMMAND_VALIDATE_TABLE, executeTableValidationCommand.getEventType());
  }

  /**
   * Gets notification event type.
   */
  @Test
  public void getNotificationEventType() {
    assertEquals(EventType.COMMAND_VALIDATED_TABLE_COMPLETED,
        executeTableValidationCommand.getNotificationEventType());
  }

  /**
   * Execute test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTest() throws EEAException {
    doNothing().when(validationService).validateTable(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.when(validationHelper.isProcessCoordinator(Mockito.anyString())).thenReturn(false);
    executeTableValidationCommand.execute(eeaEventVO);

    Mockito.verify(validationService, times(1)).validateTable(Mockito.any(), Mockito.any(),
        Mockito.any());
    Mockito.verify(kafkaSenderUtils, times(1))
        .releaseKafkaEvent(Mockito.eq(EventType.COMMAND_VALIDATED_TABLE_COMPLETED), Mockito.any());
  }

  /**
   * Execute exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(validationService).validateTable(Mockito.any(), Mockito.any(),
        Mockito.any());
    Mockito.when(validationHelper.isProcessCoordinator(Mockito.anyString())).thenReturn(false);
    executeTableValidationCommand.execute(eeaEventVO);

    Mockito.verify(validationService, times(1)).validateTable(Mockito.any(), Mockito.any(),
        Mockito.any());
    Mockito.verify(kafkaSenderUtils, times(1))
        .releaseKafkaEvent(Mockito.eq(EventType.COMMAND_VALIDATED_TABLE_COMPLETED), Mockito.any());
  }

  /**
   * Execute test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTestContainsKey() throws EEAException {
    doNothing().when(validationService).validateTable(Mockito.any(), Mockito.any(), Mockito.any());

    Mockito.when(validationHelper.isProcessCoordinator(Mockito.anyString())).thenReturn(true);
    executeTableValidationCommand.execute(eeaEventVO);

    Mockito.verify(validationService, times(1)).validateTable(Mockito.any(), Mockito.any(),
        Mockito.any());
    Mockito.verify(validationHelper, times(1)).reducePendingTasks(Mockito.any(),
        Mockito.any());
  }
}
