package org.eea.validation.kafka.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
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
    MockitoAnnotations.openMocks(this);
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
    executeTableValidationCommand.execute(eeaEventVO);
    Mockito.verify(validationHelper, times(1)).processValidation(Mockito.eq(eeaEventVO),
        Mockito.eq("uuid"), Mockito.eq(1l), Mockito.any(),
        Mockito.eq(EventType.COMMAND_VALIDATED_TABLE_COMPLETED));
  }

}
