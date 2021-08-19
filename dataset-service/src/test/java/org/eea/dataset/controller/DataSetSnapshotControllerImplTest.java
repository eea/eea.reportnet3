package org.eea.dataset.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataSetSnapshotControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSetSnapshotControllerImplTest {


  /** The data set snapshot controller impl. */
  @InjectMocks
  private DataSetSnapshotControllerImpl dataSetSnapshotControllerImpl;

  /** The dataset metabase service. */
  @Mock
  private DatasetSnapshotService datasetSnapshotService;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /** The data collection service. */
  @Mock
  private DataCollectionService dataCollectionService;

  /** The reporting dataset repository. */
  @Mock
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The dataflow controller zull. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZull;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

  /** The snapshot VO. */
  private SnapshotVO snapshotVO;

  /** The datasets. */
  private List<ReportingDataset> datasets;



  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    snapshotVO = new SnapshotVO();
    snapshotVO.setId(1L);
    datasets = new ArrayList<>();
    ReportingDataset reportingDataset = new ReportingDataset();
    reportingDataset.setId(1L);
    reportingDataset.setDataProviderId(1L);
    datasets.add(reportingDataset);
    ReportingDataset reportingDataset2 = new ReportingDataset();
    reportingDataset2.setId(2L);
    reportingDataset2.setDataProviderId(1L);
    datasets.add(reportingDataset2);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test get snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSnapshots() throws Exception {
    when(datasetSnapshotService.getSnapshotsByIdDataset(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    dataSetSnapshotControllerImpl.getSnapshotsByIdDataset(Mockito.anyLong());
    Mockito.verify(datasetSnapshotService, times(1)).getSnapshotsByIdDataset(Mockito.any());
  }

  /**
   * Test add snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddSnapshots() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetSnapshotControllerImpl.createSnapshot(1L, new CreateSnapshotVO());
    Mockito.verify(datasetSnapshotService, times(1)).addSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Test delete snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteSnapshots() throws Exception {

    dataSetSnapshotControllerImpl.deleteSnapshot(1L, 1L);
    Mockito.verify(datasetSnapshotService, times(1)).removeSnapshot(Mockito.any(), Mockito.any());
  }

  /**
   * Test get snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetSnapshotsException() throws Exception {

    dataSetSnapshotControllerImpl.getSnapshotsByIdDataset(null);
  }

  /**
   * Test delete snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteSnapshotsException() throws Exception {

    dataSetSnapshotControllerImpl.deleteSnapshot(null, 1L);
  }

  /**
   * Test delete snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService).removeSnapshot(Mockito.any(),
        Mockito.any());
    dataSetSnapshotControllerImpl.deleteSnapshot(1L, 1L);
  }

  /**
   * Test restore snapshot.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRestoreSnapshot() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetSnapshotControllerImpl.restoreSnapshot(1L, 1L);
    Mockito.verify(datasetSnapshotService, times(1)).restoreSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Test restsore snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testRestoreSnapshotsException() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    try {
      dataSetSnapshotControllerImpl.restoreSnapshot(null, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, e.getReason());
      throw e;
    }
  }

  /**
   * Test restore snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testRestoreSnapshotsException2() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException()).when(datasetSnapshotService).restoreSnapshot(Mockito.any(),
        Mockito.any(), Mockito.anyBoolean());
    try {
      dataSetSnapshotControllerImpl.restoreSnapshot(1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, e.getReason());
      throw e;
    }
  }

  /**
   * Test release snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReleaseSnapshot() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetSnapshotControllerImpl.releaseSnapshot(1L, 1L, new Date().toString());
    Mockito.verify(datasetSnapshotService, times(1)).releaseSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Test release snapshots exception 1.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testReleaseSnapshotException1() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    try {
      dataSetSnapshotControllerImpl.releaseSnapshot(null, 1L, new Date().toString());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, e.getReason());
      throw e;
    }
  }

  /**
   * Test release snapshot exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testReleaseSnapshotException2() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException(EEAErrorMessage.EXECUTION_ERROR)).when(datasetSnapshotService)
        .releaseSnapshot(Mockito.any(), Mockito.any(), Mockito.any());
    try {
      dataSetSnapshotControllerImpl.releaseSnapshot(1L, 1L, new Date().toString());
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.EXECUTION_ERROR, e.getReason());
      throw e;
    }
  }


  /**
   * Test get schema snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSchemaSnapshots() throws Exception {
    when(datasetSnapshotService.getSchemaSnapshotsByIdDataset(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    dataSetSnapshotControllerImpl.getSchemaSnapshotsByIdDataset(Mockito.anyLong());
    Mockito.verify(datasetSnapshotService, times(1)).getSchemaSnapshotsByIdDataset(Mockito.any());
  }

  /**
   * Test add schema snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddSchemaSnapshots() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetSnapshotControllerImpl.createSchemaSnapshot(1L, "5db99d0bb67ca68cb8fa7053", "test");
    Mockito.verify(datasetSnapshotService, times(1)).addSchemaSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Test restore schema snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRestoreSchemaSnapshots() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetSnapshotControllerImpl.restoreSchemaSnapshot(1L, 1L);
    Mockito.verify(datasetSnapshotService, times(1)).restoreSchemaSnapshot(Mockito.any(),
        Mockito.any());
  }

  /**
   * Test delete schema snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteSchemaSnapshots() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetSnapshotControllerImpl.deleteSchemaSnapshot(1L, 1L);
    Mockito.verify(datasetSnapshotService, times(1)).removeSchemaSnapshot(Mockito.any(),
        Mockito.any());
  }

  /**
   * Test get schema snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetSchemaSnapshotsException() throws Exception {

    dataSetSnapshotControllerImpl.getSchemaSnapshotsByIdDataset(null);
  }


  /**
   * Test get schema snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSchemaSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService)
        .getSchemaSnapshotsByIdDataset(Mockito.any());
    dataSetSnapshotControllerImpl.getSchemaSnapshotsByIdDataset(1L);
    Mockito.verify(datasetSnapshotService, times(1)).getSchemaSnapshotsByIdDataset(Mockito.any());
  }

  /**
   * Test delete schema snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteSchemaSnapshotsException() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetSnapshotControllerImpl.deleteSchemaSnapshot(null, 1L);
  }

  /**
   * Test delete schema snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteSchemaSnapshotsException2() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException()).when(datasetSnapshotService).removeSchemaSnapshot(Mockito.any(),
        Mockito.any());
    dataSetSnapshotControllerImpl.deleteSchemaSnapshot(1L, 1L);
  }

  /**
   * Restore snapshot test.
   */
  @Test(expected = ResponseStatusException.class)
  public void restoreSnapshotTest() {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetSnapshotControllerImpl.restoreSnapshot(null, 1L);
  }

  /**
   * Test restore schema snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testRestoreSchemaSnapshotsException2() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException()).when(datasetSnapshotService).restoreSchemaSnapshot(Mockito.any(),
        Mockito.any());
    dataSetSnapshotControllerImpl.restoreSchemaSnapshot(1L, 1L);
  }

  /**
   * Gets the released and updated status test.
   *
   * @return the released and updated status test
   */
  @Test
  public void getReleasedAndUpdatedStatusTest() {
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Mockito.doNothing().when(response).setContentType(Mockito.anyString());
    Mockito.doNothing().when(response).setHeader(Mockito.anyString(), Mockito.anyString());

    dataSetSnapshotControllerImpl.createReceiptPDF(response, 1L, 1L);
    Mockito.verify(response, times(1)).setContentType(Mockito.anyString());
  }

  /**
   * Test get snapshot exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSnapshotException() throws Exception {
    doThrow(new EEAException("error")).when(datasetSnapshotService).getById(Mockito.anyLong());
    assertNull("not null", dataSetSnapshotControllerImpl.getById(1L));
  }

  /**
   * Test get snapshot.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSnapshot() throws Exception {
    when(datasetSnapshotService.getById(Mockito.anyLong())).thenReturn(snapshotVO);
    assertEquals("not equals", dataSetSnapshotControllerImpl.getById(1L), snapshotVO);
  }

  /**
   * Historic releases reporting success test.
   *
   * @throws Exception the exception
   */
  @Test
  public void historicReleasesReportingSuccessTest() throws Exception {
    when(datasetSnapshotService.getReleases(Mockito.anyLong())).thenReturn(new ArrayList<>());
    assertEquals("not equals", dataSetSnapshotControllerImpl.historicReleases(1L, null),
        new ArrayList<>());
  }

  /**
   * Historic releases exception test.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void historicReleasesExceptionTest() throws Exception {
    doThrow(new EEAException(EEAErrorMessage.DATASET_NOTFOUND)).when(datasetSnapshotService)
        .getReleases(Mockito.anyLong());
    try {
      dataSetSnapshotControllerImpl.historicReleases(1L, null);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }

  }

  /**
   * Gets the schema by id success test.
   *
   * @return the schema by id success test
   * @throws Exception the exception
   */
  @Test
  public void getSchemaByIdSuccessTest() throws Exception {
    when(datasetSnapshotService.getSchemaById(Mockito.anyLong())).thenReturn(snapshotVO);
    assertEquals("not equals", dataSetSnapshotControllerImpl.getSchemaById(1L), snapshotVO);
  }

  /**
   * Gets the schema by id exception test.
   *
   * @return the schema by id exception test
   * @throws Exception the exception
   */
  @Test
  public void getSchemaByIdExceptionTest() throws Exception {
    doThrow(new EEAException()).when(datasetSnapshotService).getSchemaById(Mockito.anyLong());
    assertNull("not equals", dataSetSnapshotControllerImpl.getSchemaById(1L));
  }

  /**
   * Historic releases by respresentative success test.
   *
   * @throws Exception the exception
   */
  @Test
  public void historicReleasesByRespresentativeSuccessTest() throws Exception {
    when(reportingDatasetRepository.findByDataflowId(Mockito.anyLong())).thenReturn(datasets);
    when(datasetSnapshotService.getSnapshotsReleasedByIdDataset(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    assertEquals("not equals",
        dataSetSnapshotControllerImpl.historicReleasesByRepresentative(1L, 1L), new ArrayList<>());
  }

  /**
   * Update snapshot EU release success test.
   *
   * @throws Exception the exception
   */
  @Test
  public void updateSnapshotEUReleaseSuccessTest() throws Exception {
    dataSetSnapshotControllerImpl.updateSnapshotEURelease(Mockito.anyLong());
    Mockito.verify(datasetSnapshotService, times(1)).updateSnapshotEURelease(Mockito.anyLong());
  }

  /**
   * Creates the release snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void createReleaseSnapshots() throws Exception {
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setReleasable(true);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflow);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetSnapshotControllerImpl.createReleaseSnapshots(1L, 1L, false);
    Mockito.verify(datasetSnapshotService, times(1)).createReleaseSnapshots(1L, 1L, false);
  }

  /**
   * Creates the release snapshots throw.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createReleaseSnapshotsThrow() throws Exception {
    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setReleasable(true);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(dataflowControllerZull.getMetabaseById(Mockito.any())).thenReturn(dataflow);
    Mockito.when(authentication.getName()).thenReturn("user");
    doThrow(new EEAException()).when(datasetSnapshotService).createReleaseSnapshots(1L, 1L, true);
    try {
      dataSetSnapshotControllerImpl.createReleaseSnapshots(1L, 1L, true);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.EXECUTION_ERROR, e.getReason());
      throw e;
    }
  }

  /**
   * Test releasing snapshots lock.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReleasingSnapshotsLock() throws Exception {
    dataSetSnapshotControllerImpl.releaseLocksFromReleaseDatasets(1L, 1L);
    Mockito.verify(datasetSnapshotService, times(1)).releaseLocksRelatedToRelease(1L, 1L);
  }

  /**
   * Test releasing snapshots lock exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testReleasingSnapshotsLockException() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService).releaseLocksRelatedToRelease(1L, 1L);
    try {
      dataSetSnapshotControllerImpl.releaseLocksFromReleaseDatasets(1L, 1L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }
}
