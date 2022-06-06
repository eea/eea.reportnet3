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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class CleanKyebaseCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CleanKyebaseCommandTest {

  /**
   * The clean kyebase command.
   */
  @InjectMocks
  private CleanKyebaseCommand cleanKyebaseCommand;

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
    eeaEventVO.setEventType(EventType.COMMAND_CLEAN_KYEBASE);
    eeaEventVO.setData(data);
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.COMMAND_CLEAN_KYEBASE, cleanKyebaseCommand.getEventType());
  }

  /**
   * Test data.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testData() throws EEAException {
    Mockito.when(validationHelper.finishProcessInMap(Mockito.anyString())).thenReturn(true);
    cleanKyebaseCommand.execute(eeaEventVO);
    Mockito.verify(validationHelper, times(1)).finishProcessInMap(Mockito.any());
  }

}
