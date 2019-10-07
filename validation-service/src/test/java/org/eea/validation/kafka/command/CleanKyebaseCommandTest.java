package org.eea.validation.kafka.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
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

@RunWith(MockitoJUnitRunner.class)
public class CleanKyebaseCommandTest {

  @InjectMocks
  private CleanKyebaseCommand cleanKyebaseCommand;

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
    data.put("datasetId", "1L");
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_CLEAN_KYEBASE);
    eeaEventVO.setData(data);
  }

  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.COMMAND_CLEAN_KYEBASE, cleanKyebaseCommand.getEventType());
  }

  @Test
  public void testData() throws EEAException {
    doNothing().when(validationHelper).removeKieBase(Mockito.anyString());
    cleanKyebaseCommand.execute(eeaEventVO);;
  }

}
