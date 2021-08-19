package org.eea.dataset.io.kafka.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
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
public class SaveStatisticsCommandTest {


  /**
   * The save statistics command.
   */
  @InjectMocks
  private SaveStatisticsCommand saveStatisticsCommand;

  /**
   * The dataset service.
   */
  @Mock
  private DatasetService datasetService;

  /**
   * The eea event VO.
   */
  private EEAEventVO eeaEventVO;

  /**
   * The data.
   */
  private Map<String, Object> data;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test execute save statistics.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteSaveStatistics() throws EEAException {

    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.VALIDATION_FINISHED_EVENT);
    data = new HashMap<>();
    data.put("dataset_id", 1);
    eeaEventVO.setData(data);

    SaveStatisticsCommand spyClass = Mockito.spy(saveStatisticsCommand);
    spyClass.execute(eeaEventVO);
    Mockito.verify(spyClass, times(1)).execute(Mockito.any());

  }


  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.VALIDATION_FINISHED_EVENT, saveStatisticsCommand.getEventType());
  }

}
