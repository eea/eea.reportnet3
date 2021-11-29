package org.eea.dataset.io.kafka.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
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

/**
 * The Class ReferenceDatasetSnapshotCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReferenceDatasetSnapshotCommandTest {



  @InjectMocks
  private ReferenceDatasetSnapshotCommand referenceDatasetSnapshotCommand;


  @Mock
  private DesignDatasetRepository designDatasetRepository;


  @Mock
  private ReferenceDatasetRepository referenceDatasetRepository;

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
    eeaEventVO.setEventType(EventType.COPY_REFERENCE_DATASET_SNAPSHOT_COMPLETED_EVENT);
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testExecute() throws EEAException {

    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("snapshot_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    DesignDataset designDataset = new DesignDataset();

    List<ReferenceDataset> referenceDatasets = new ArrayList<>();
    referenceDatasets.add(new ReferenceDataset());
    when(designDatasetRepository.findById(Mockito.any())).thenReturn(Optional.of(designDataset));
    when(referenceDatasetRepository.findByDataflowIdAndDatasetSchema(Mockito.any(), Mockito.any()))
        .thenReturn(referenceDatasets);
    referenceDatasetSnapshotCommand.execute(eeaEventVO);
    Mockito.verify(designDatasetRepository, times(1)).findById(Mockito.any());

  }


  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.COPY_REFERENCE_DATASET_SNAPSHOT_COMPLETED_EVENT,
        referenceDatasetSnapshotCommand.getEventType());
  }
}
