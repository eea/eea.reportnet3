package org.eea.dataset.io.kafka.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class SaveStatisticsCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutePropagateNewFieldCommandTest {


  /** The save statistics command. */
  @InjectMocks
  private ExecutePropagateNewFieldCommand executePropagateCommand;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /** The update record helper. */
  @Mock
  private UpdateRecordHelper updateRecordHelper;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  /**
   * Test execute.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecute() throws EEAException {

    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_EXECUTE_NEW_DESIGN_FIELD_PROPAGATION);
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("sizeRecords", 1);
    data.put("idTableSchema", "5cf0e9b3b793310e9ceca190");
    data.put("idFieldSchema", "5cf0e9b3b793310e9ceca190");
    data.put("typeField", TypeData.TEXT);
    eeaEventVO.setData(data);

    doNothing().when(datasetService).saveNewFieldPropagation(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());

    ConcurrentHashMap<String, Integer> processesMap = new ConcurrentHashMap<String, Integer>();
    processesMap.put("test", 1);
    when(updateRecordHelper.getProcessesMap()).thenReturn(processesMap);
    executePropagateCommand.execute(eeaEventVO);
    Mockito.verify(datasetService, times(1)).saveNewFieldPropagation(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());

  }


  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.COMMAND_EXECUTE_NEW_DESIGN_FIELD_PROPAGATION,
        executePropagateCommand.getEventType());
  }

}
