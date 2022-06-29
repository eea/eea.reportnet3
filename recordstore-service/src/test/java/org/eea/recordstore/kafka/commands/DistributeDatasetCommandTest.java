package org.eea.recordstore.kafka.commands;

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

public class DistributeDatasetCommandTest {

  @InjectMocks
  private DistributeDatasetCommand distributeDatasetCommand;

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
    data.put("dataset_id", "1");
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.DISTRIBUTE_DATASET_EVENT);
    eeaEventVO.setData(data);
    MockitoAnnotations.openMocks(this);
  }


  @Test
  public void testGetEventType() throws Exception {
    Assert.assertEquals(EventType.DISTRIBUTE_DATASET_EVENT,
        distributeDatasetCommand.getEventType());
  }

  @Test
  public void testExecute() throws Exception {
    distributeDatasetCommand.execute(eeaEventVO);
    Mockito.verify(recordStoreService, Mockito.times(1)).distributeTablesJob(Mockito.any());
  }

}
