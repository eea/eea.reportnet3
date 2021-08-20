package org.eea.dataset.io.kafka.commands;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.EmailController.EmailControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
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
public class ReleaseDataSnapshotsCommandTest {

  /** The restore data collection snapshot command. */
  @InjectMocks
  private ReleaseDataSnapshotsCommand releaseDataSnapshotsCommand;

  /** The dataset snapshot service. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The eu dataset service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private DatasetSnapshotService datasetSnapshotService;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Mock
  private EmailControllerZuul emailControllerZuul;

  @Mock
  private UserManagementControllerZull userManagementControllerZuul;


  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  private SecurityContext securityContext;

  private Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.RELEASE_ONEBYONE_COMPLETED_EVENT);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test execute without blockers.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteFinish() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
    dataSetMetabaseVO.setId(1L);
    dataSetMetabaseVO.setDataflowId(1L);
    dataSetMetabaseVO.setDataProviderId(1L);
    List<Long> datasetsId = new ArrayList<>();
    datasetsId.add(1L);
    datasetsId.add(2L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(1L)).thenReturn(dataSetMetabaseVO);
    Mockito.when(datasetMetabaseService.getLastDatasetValidationForRelease(1L)).thenReturn(null);

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setName("dataflowName");
    dataflowVO.setShowPublicInfo(false);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    releaseDataSnapshotsCommand.execute(eeaEventVO);
    Mockito.verify(datasetMetabaseService, times(1)).getLastDatasetValidationForRelease(1L);

  }

  @Test
  public void testExecuteFinish2Datas() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
    dataSetMetabaseVO.setId(1L);
    dataSetMetabaseVO.setDataflowId(1L);
    dataSetMetabaseVO.setDataProviderId(1L);
    List<Long> datasetsId = new ArrayList<>();
    datasetsId.add(1L);
    datasetsId.add(2L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(1L)).thenReturn(dataSetMetabaseVO);
    Mockito.when(datasetMetabaseService.getLastDatasetValidationForRelease(1L)).thenReturn(null);

    List<Long> idsLong = new ArrayList();
    idsLong.add(1L);
    idsLong.add(2L);
    Mockito.when(datasetMetabaseService
        .getDatasetIdsByDataflowIdAndDataProviderId(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(idsLong);
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setName("dataflowName");
    dataflowVO.setId(1L);
    dataflowVO.setShowPublicInfo(false);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    releaseDataSnapshotsCommand.execute(eeaEventVO);
    Mockito.verify(datasetMetabaseService, times(1)).getLastDatasetValidationForRelease(1L);

  }

  /**
   * Test execute with blockers.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecuteContinue() throws EEAException {
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("user", "user1");
    eeaEventVO.setData(data);
    DataSetMetabase datasetMetabase = new DataSetMetabase();
    datasetMetabase.setId(1L);
    datasetMetabase.setDataflowId(1L);
    datasetMetabase.setDataProviderId(1L);
    List<Long> datasetsId = new ArrayList<>();
    datasetsId.add(1L);
    datasetsId.add(2L);
    releaseDataSnapshotsCommand.execute(eeaEventVO);
    Mockito.verify(datasetMetabaseService, times(1)).getLastDatasetValidationForRelease(1L);

  }

}
