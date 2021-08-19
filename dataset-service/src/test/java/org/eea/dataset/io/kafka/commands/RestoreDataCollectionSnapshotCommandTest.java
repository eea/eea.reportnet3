package org.eea.dataset.io.kafka.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.EUDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The Class RestoreDataCollectionSnapshotCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestoreDataCollectionSnapshotCommandTest {

  /** The restore data collection snapshot command. */
  @InjectMocks
  private RestoreDataCollectionSnapshotCommand restoreDataCollectionSnapshotCommand;

  /** The dataset snapshot service. */
  @Mock
  private DatasetSnapshotService datasetSnapshotService;

  /** The eu dataset service. */
  @Mock
  private EUDatasetService euDatasetService;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  private SecurityContext securityContext;

  private Authentication authentication;

  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.RESTORE_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT);
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testExecute() throws EEAException {
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    when(datasetMetabaseService.findDatasetMetabase(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    when(euDatasetService.removeLocksRelatedToPopulateEU(Mockito.any())).thenReturn(Boolean.TRUE);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    restoreDataCollectionSnapshotCommand.execute(eeaEventVO);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }

  @Test
  public void testExecuteSecondRun() throws EEAException {
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    when(datasetMetabaseService.findDatasetMetabase(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    when(euDatasetService.removeLocksRelatedToPopulateEU(Mockito.any())).thenReturn(Boolean.FALSE);
    restoreDataCollectionSnapshotCommand.execute(eeaEventVO);
    Mockito.verify(kafkaSenderUtils, times(0)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }

  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.RESTORE_DATACOLLECTION_SNAPSHOT_COMPLETED_EVENT,
        restoreDataCollectionSnapshotCommand.getEventType());
  }

}
