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
    data.put("task_id", 1);
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATE_RECORD);
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
    assertEquals(EventType.COMMAND_VALIDATE_RECORD, executeRecordValidationCommand.getEventType());
  }

  /**
   * Gets notification event type.
   */
  @Test
  public void getNotificationEventType() {
    assertEquals(EventType.COMMAND_VALIDATED_RECORD_COMPLETED,
        executeRecordValidationCommand.getNotificationEventType());
  }

  /**
   * Execute test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTest() throws EEAException {
    ReflectionTestUtils.setField(executeRecordValidationCommand, "recordBatchSize", 20);

    executeRecordValidationCommand.execute(eeaEventVO);
    Mockito.verify(validationHelper, times(1)).processValidation(Mockito.anyLong(),
        Mockito.eq(eeaEventVO), Mockito.eq("uuid"), Mockito.eq(1l), Mockito.any(),
        Mockito.eq(EventType.COMMAND_VALIDATED_RECORD_COMPLETED));
  }


}
