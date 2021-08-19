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

public class DeleteViewProccesCommandTest {

  @InjectMocks
  private DeleteViewProccesCommand deleteViewProccesCommand;

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
    data.put("dataset_id", "1");
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.DELETE_VIEW_PROCCES_EVENT);
    eeaEventVO.setData(data);
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGetEventType() throws Exception {
    Assert.assertEquals(EventType.DELETE_VIEW_PROCCES_EVENT,
        deleteViewProccesCommand.getEventType());
  }

  @Test
  public void testExecute() throws Exception {
    deleteViewProccesCommand.execute(eeaEventVO);
    Mockito.verify(viewHelper, Mockito.times(1)).deleteProccesList(Mockito.anyLong());
  }

}
