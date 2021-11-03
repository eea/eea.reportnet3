package org.eea.recordstore.kafka.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.recordstore.service.RecordStoreService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ExecuteUpdateMaterializedViewCommandTest {

  @InjectMocks
  private ExecuteUpdateMaterializedViewCommand executeUpdateMaterializedViewCommand;

  @Mock
  private RecordStoreService recordStoreService;

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
    data.put("released", true);
    data.put("referencesToRefresh", Arrays.asList(1));
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.UPDATE_MATERIALIZED_VIEW_EVENT);
    eeaEventVO.setData(data);
    MockitoAnnotations.openMocks(this);
  }


  @Test
  public void testGetEventType() throws Exception {
    Assert.assertEquals(EventType.UPDATE_MATERIALIZED_VIEW_EVENT,
        executeUpdateMaterializedViewCommand.getEventType());
  }

  @Test
  public void testExecute() throws Exception {
    executeUpdateMaterializedViewCommand.execute(eeaEventVO);
    Mockito.verify(recordStoreService, Mockito.times(1))
        .updateMaterializedQueryView(Mockito.anyLong(), Mockito.any(), Mockito.anyBoolean());
  }

}
