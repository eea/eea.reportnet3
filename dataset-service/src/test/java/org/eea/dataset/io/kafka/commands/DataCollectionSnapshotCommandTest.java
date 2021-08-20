package org.eea.dataset.io.kafka.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.EUDataset;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.EUDatasetRepository;
import org.eea.dataset.service.DatasetSnapshotService;
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

@RunWith(MockitoJUnitRunner.class)
public class DataCollectionSnapshotCommandTest {


  /** The save statistics command. */
  @InjectMocks
  private DataCollectionSnapshotCommand dataCollectionSnapshotCommand;

  /** The eu dataset repository. */
  @Mock
  private EUDatasetRepository euDatasetRepository;

  /** The data collection repository. */
  @Mock
  private DataCollectionRepository dataCollectionRepository;

  /** The dataset snapshot service. */
  @Mock
  private DatasetSnapshotService datasetSnapshotService;


  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  @Before
  public void initMocks() {
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.ADD_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT);
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testExecute() throws EEAException {

    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("snapshot_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    DataCollection dataCollection = new DataCollection();
    dataCollection.setDueDate(new Date());
    List<EUDataset> euDatasetList = new ArrayList();
    euDatasetList.add(new EUDataset());
    when(dataCollectionRepository.findById(Mockito.any())).thenReturn(Optional.of(dataCollection));
    when(euDatasetRepository.findByDataflowIdAndDatasetSchema(Mockito.any(), Mockito.any()))
        .thenReturn(euDatasetList);
    dataCollectionSnapshotCommand.execute(eeaEventVO);
    Mockito.verify(dataCollectionRepository, times(1)).findById(Mockito.any());

  }


  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.ADD_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT,
        dataCollectionSnapshotCommand.getEventType());
  }
}
