package org.eea.dataset.io.kafka.commands;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

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

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  /** The lock service. */
  @Mock
  private LockService lockService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(executePropagateCommand, "fieldBatchSize", 1);
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
    data.put("uuId", "1");
    data.put("typeField", DataType.TEXT);
    data.put("numPag", 1);
    List<Integer> pages = new ArrayList<>();
    pages.add(0);
    data.put("pages", pages);
    eeaEventVO.setData(data);


    executePropagateCommand.execute(eeaEventVO);
    Mockito.verify(datasetService, Mockito.times(1)).saveNewFieldPropagation(Mockito.anyLong(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

  }

  @Test
  public void testExecute2() throws EEAException {

    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_EXECUTE_NEW_DESIGN_FIELD_PROPAGATION);
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("sizeRecords", 1);
    data.put("idTableSchema", "5cf0e9b3b793310e9ceca190");
    data.put("idFieldSchema", "5cf0e9b3b793310e9ceca190");
    data.put("typeField", DataType.TEXT);
    data.put("uuId", "1");
    data.put("numPag", 0);
    List<Integer> pages = new ArrayList<>();
    pages.add(0);
    data.put("pages", pages);
    eeaEventVO.setData(data);

    executePropagateCommand.execute(eeaEventVO);
    Mockito.verify(datasetService, Mockito.times(1)).saveNewFieldPropagation(Mockito.anyLong(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
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
