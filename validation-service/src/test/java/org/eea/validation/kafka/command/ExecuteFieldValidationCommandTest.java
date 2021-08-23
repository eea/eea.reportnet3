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
 * The Class ExecuteFieldValidationCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteFieldValidationCommandTest {

  /**
   * The execute field validation command.
   */
  @InjectMocks
  private ExecuteFieldValidationCommand executeFieldValidationCommand;

  /**
   * The validation helper
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
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATE_FIELD);
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
    assertEquals(EventType.COMMAND_VALIDATE_FIELD, executeFieldValidationCommand.getEventType());
  }

  @Test
  public void getNotificationEventType() {
    assertEquals(EventType.COMMAND_VALIDATED_FIELD_COMPLETED,
        executeFieldValidationCommand.getNotificationEventType());
  }

  /**
   * Execute test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTest() throws EEAException {
    executeFieldValidationCommand.execute(eeaEventVO);

    Mockito.verify(validationHelper, times(1)).processValidation(Mockito.eq(eeaEventVO),
        Mockito.eq("uuid"), Mockito.eq(1l), Mockito.any(),
        Mockito.eq(EventType.COMMAND_VALIDATED_FIELD_COMPLETED));
  }

}
