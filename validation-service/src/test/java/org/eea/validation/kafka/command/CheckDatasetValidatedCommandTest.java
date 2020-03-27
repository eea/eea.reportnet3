package org.eea.validation.kafka.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
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
   * The kafka sender utils.
   */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

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
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATED_DATASET_COMPLETED);
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
    // self uuid
    processesMap.put("uuid", 1);
    when(validationHelper.getProcessesMap()).thenReturn(processesMap);
    when(validationHelper.getProcessesMap()).thenReturn(processesMap);
    doNothing().when(validationHelper).checkFinishedValidations(Mockito.any(), Mockito.any());
    checkDatasetValidatedCommand.execute(eeaEventVO);

    Mockito.verify(validationHelper, times(1)).checkFinishedValidations(Mockito.any(),
        Mockito.any());
  }

  /**
   * Execute throw test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeThrowTest() throws EEAException {
    when(validationHelper.getProcessesMap()).thenReturn(processesMap);
    checkDatasetValidatedCommand.execute(eeaEventVO);
    Mockito.verify(validationHelper, times(1)).getProcessesMap();
  }

}
