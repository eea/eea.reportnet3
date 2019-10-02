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
import org.kie.api.KieBase;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableValidatedCommandTest {

  @InjectMocks
  private TableValidatedCommand tableValidatedCommand;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private ValidationHelper validationHelper;

  @Mock
  private KieBase kieBase;

  private Map<String, Object> data;

  private EEAEventVO eeaEventVO;

  private ConcurrentHashMap<String, Integer> processesMap;

  @Before
  public void initMocks() {
    data = new HashMap<>();
    data.put("uuid", "uuid");
    data.put("datasetId", "1L");
    data.put("kieBase", kieBase);
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATED_TABLE_COMPLETED);
    eeaEventVO.setData(data);
    processesMap = new ConcurrentHashMap<>();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.COMMAND_VALIDATED_TABLE_COMPLETED, tableValidatedCommand.getEventType());
  }

  @Test
  public void executeSelfTest() throws EEAException {
    // self uuid
    processesMap.put("uuid", 1);
    when(validationHelper.getProcessesMap()).thenReturn(processesMap);
    when(validationHelper.getProcessesMap()).thenReturn(processesMap);
    doNothing().when(validationHelper).checkFinishedValidations(Mockito.any(), Mockito.any(),
        Mockito.any());
    tableValidatedCommand.execute(eeaEventVO);

    Mockito.verify(validationHelper, times(1)).checkFinishedValidations(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void executeThrowTest() throws EEAException {
    when(validationHelper.getProcessesMap()).thenReturn(processesMap);
    tableValidatedCommand.execute(eeaEventVO);
    Mockito.verify(validationHelper, times(1)).getProcessesMap();
  }

}
