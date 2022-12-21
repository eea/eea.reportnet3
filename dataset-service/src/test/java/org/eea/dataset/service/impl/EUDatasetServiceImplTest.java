package org.eea.dataset.service.impl;

import org.eea.dataset.mapper.EUDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.EUDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.EUDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.lock.service.LockService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * The Class EUDatasetServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class EUDatasetServiceImplTest {


  /** The eu dataset service. */
  @InjectMocks
  private EUDatasetServiceImpl euDatasetService;

  /** The eu dataset repository. */
  @Mock
  private EUDatasetRepository euDatasetRepository;

  /** The eu dataset mapper. */
  @Mock
  private EUDatasetMapper euDatasetMapper;

  /** The reporting dataset service. */
  @Mock
  private ReportingDatasetService reportingDatasetService;

  /** The lock service. */
  @Mock
  private LockService lockService;

  /** The data collection repository. */
  @Mock
  private DataCollectionRepository dataCollectionRepository;

  /** The partition data set metabase repository. */
  @Mock
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The dataset snapshot service. */
  @Mock
  private DatasetSnapshotService datasetSnapshotService;

  @Mock
  private ProcessControllerZuul processControllerZuul;

  @Mock
  private JobProcessControllerZuul jobProcessControllerZuul;

  /** The reportings. */
  private List<ReportingDatasetVO> reportings;

  /** The data collection list. */
  private List<DataCollection> dataCollectionList;

  /** The eu dataset list. */
  private List<EUDataset> euDatasetList;

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    reportings = new ArrayList<>();
    ReportingDatasetVO reportingDatasetVO = new ReportingDatasetVO();
    reportingDatasetVO.setId(1L);
    reportings.add(reportingDatasetVO);

    dataCollectionList = new ArrayList<>();
    DataCollection dataCollection = new DataCollection();
    dataCollection.setId(1L);
    dataCollection.setDatasetSchema("es");
    dataCollectionList.add(dataCollection);

    euDatasetList = new ArrayList<>();
    EUDataset euDataset = new EUDataset();
    euDataset.setId(1L);
    euDataset.setDatasetSchema("es");
    euDatasetList.add(euDataset);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    MockitoAnnotations.openMocks(this);
  }


  /**
   * Gets the EU dataset by dataflow id test.
   *
   * @return the EU dataset by dataflow id test
   */
  @Test
  public void getEUDatasetByDataflowIdTest() {
    euDatasetService.getEUDatasetByDataflowId(Mockito.anyLong());
    Mockito.verify(euDatasetRepository, times(1)).findByDataflowId(Mockito.any());
  }

  /**
   * Populate EU dataset with data collection test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void populateEUDatasetWithDataCollectionTest() throws EEAException {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user");
    when(reportingDatasetService.getDataSetIdByDataflowId(Mockito.any())).thenReturn(reportings);
    when(lockService.createLock(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new LockVO());
    when(dataCollectionRepository.findByDataflowId(Mockito.any())).thenReturn(dataCollectionList);
    when(euDatasetRepository.findByDataflowId(Mockito.any())).thenReturn(euDatasetList);
    PartitionDataSetMetabase partitionDataset = new PartitionDataSetMetabase();
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.any(),
        Mockito.any())).thenReturn(Optional.of(partitionDataset));
    doNothing().when(datasetSnapshotService).addSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any());
    Mockito.when(processControllerZuul.updateProcess(anyLong(), anyLong(), any(ProcessStatusEnum.class), any(ProcessTypeEnum.class), anyString(), anyString(), anyInt(), anyBoolean())).thenReturn(true);
    JobProcessVO jobProcessVO = new JobProcessVO(1L, 1L, "fdsakljl");
    Mockito.when(jobProcessControllerZuul.save(any(JobProcessVO.class))).thenReturn(jobProcessVO);
    euDatasetService.populateEUDatasetWithDataCollection(1L, null);
    Mockito.verify(datasetSnapshotService, times(1)).addSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any());
  }

  /**
   * Populate EU dataset with data collection different sizes test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = IllegalArgumentException.class)
  public void populateEUDatasetWithDataCollectionDifferentSizesTest() throws EEAException {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user");
    when(reportingDatasetService.getDataSetIdByDataflowId(Mockito.any())).thenReturn(reportings);
    when(lockService.createLock(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new LockVO());
    DataCollection dc = new DataCollection();
    dc.setId(25L);
    dataCollectionList.add(dc);
    when(dataCollectionRepository.findByDataflowId(Mockito.any())).thenReturn(dataCollectionList);
    when(euDatasetRepository.findByDataflowId(Mockito.any())).thenReturn(euDatasetList);
    try {
      euDatasetService.populateEUDatasetWithDataCollection(1L, null);
    } catch (IllegalArgumentException e) {
      assertEquals("Cannot combine lists with dissimilar sizes", e.getMessage());
      throw e;
    }
  }

  /**
   * Populate EU dataset with data collection partition exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void populateEUDatasetWithDataCollectionPartitionExceptionTest() throws EEAException {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user");
    when(reportingDatasetService.getDataSetIdByDataflowId(Mockito.any())).thenReturn(reportings);
    when(lockService.createLock(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new LockVO());
    when(dataCollectionRepository.findByDataflowId(Mockito.any())).thenReturn(dataCollectionList);
    when(euDatasetRepository.findByDataflowId(Mockito.any())).thenReturn(euDatasetList);
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.any(),
        Mockito.any())).thenReturn(Optional.empty());
    Mockito.when(processControllerZuul.updateProcess(anyLong(), anyLong(), any(ProcessStatusEnum.class), any(ProcessTypeEnum.class), anyString(), anyString(), anyInt(), anyBoolean())).thenReturn(true);
    JobProcessVO jobProcessVO = new JobProcessVO(1L, 1L, "fdsakljl");
    Mockito.when(jobProcessControllerZuul.save(any(JobProcessVO.class))).thenReturn(jobProcessVO);
    try {
      euDatasetService.populateEUDatasetWithDataCollection(1L, null);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.PARTITION_ID_NOTFOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Removes the locks related to populate EU test.
   */
  @Test
  public void removeLocksRelatedToPopulateEUTest() {
    when(reportingDatasetService.getDataSetIdByDataflowId(Mockito.any())).thenReturn(reportings);
    when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(Boolean.TRUE);
    assertTrue(euDatasetService.removeLocksRelatedToPopulateEU(1L));
    Mockito.verify(lockService, times(3)).removeLockByCriteria(Mockito.any());
  }

}
