package org.eea.validation.kafka.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class DatasetValidatedCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckDatasetValidatedCommandTest {

  /**
   * The dataset validated command.
   */
  @InjectMocks
  private CheckDatasetValidatedCommand checkDatasetValidatedCommand;

  /**
   * The validation helper.
   */
  @Mock
  private ValidationHelper validationHelper;

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
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATED_DATASET_COMPLETED);
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
    assertEquals(EventType.COMMAND_VALIDATED_DATASET_COMPLETED,
        checkDatasetValidatedCommand.getEventType());
  }

  /**
   * Execute self test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeSelfTest() throws EEAException {
    Mockito.when(validationHelper.isProcessCoordinator(Mockito.eq("uuid"))).thenReturn(true);

    checkDatasetValidatedCommand.execute(eeaEventVO);

    Mockito.verify(validationHelper, times(1)).reducePendingTasks(Mockito.any(),
        Mockito.any());
  }

  /**
   * Execute throw test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void executeThrowTest() throws EEAException {
    Mockito.when(validationHelper.isProcessCoordinator(Mockito.eq("uuid"))).thenReturn(true);
    doThrow(new EEAException("Error")).when(validationHelper)
        .reducePendingTasks(Mockito.eq(1l), Mockito.anyString());
    checkDatasetValidatedCommand.execute(eeaEventVO);
  }

}
