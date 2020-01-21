package org.eea.validation.kafka.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
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
 * The Class ExecuteValidationCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteValidationCommandTest {

  /** The execute validation command. */
  @InjectMocks
  private ExecuteValidationCommand executeValidationCommand;

  /** The validation helper. */
  @Mock
  private ValidationHelper validationHelper;

  /** The kie base. */
  @Mock
  private KieBase kieBase;

  /** The data. */
  private Map<String, Object> data;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    data = new HashMap<>();
    data.put("uuid", "uuid");
    data.put("datasetId", "1L");
    data.put("kieBase", kieBase);
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATED_TABLE_COMPLETED);
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
    assertEquals(EventType.COMMAND_EXECUTE_VALIDATION, executeValidationCommand.getEventType());
  }

  /**
   * Execute test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTest() throws EEAException {
    doNothing().when(validationHelper).executeValidation(Mockito.any(), Mockito.any());

    executeValidationCommand.execute(eeaEventVO);

    Mockito.verify(validationHelper, times(1)).executeValidation(Mockito.any(), Mockito.any());
  }

  /**
   * Execute exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeExceptionTest() throws EEAException {
    executeValidationCommand.execute(eeaEventVO);

    Mockito.verify(validationHelper, times(1)).executeValidation(Mockito.any(), Mockito.any());
  }

}
