package org.eea.recordstore.kafka.commands;

import java.util.HashMap;
import java.util.Map;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.recordstore.util.ViewHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FinishViewProccesCommandTest {

  @InjectMocks
  private FinishViewProccesCommand finishViewProccesCommand;

  @Mock
  private ViewHelper viewHelper;


  /**
   * The data.
   */
  private Map<String, Object> data;

  /**
   * The eea event VO.
   */
  private EEAEventVO eeaEventVO;

  @Before
  public void initMocks() {
    data = new HashMap<>();
    data.put("user", "user");
    data.put("dataset_id", "1");
    data.put("isMaterialized", true);
    data.put("checkSQL", false);
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.FINISH_VIEW_PROCCES_EVENT);
    eeaEventVO.setData(data);
    MockitoAnnotations.openMocks(this);
  }


  @Test
  public void testGetEventType() throws Exception {
    Assert.assertEquals(EventType.FINISH_VIEW_PROCCES_EVENT,
        finishViewProccesCommand.getEventType());
  }

  @Test
  public void testExecute() throws Exception {
    finishViewProccesCommand.execute(eeaEventVO);
    Mockito.verify(viewHelper, Mockito.times(1)).finishProcces(Mockito.anyLong(),
        Mockito.anyBoolean(), Mockito.anyBoolean());
  }

}
