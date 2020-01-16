package org.eea.dataset.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
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

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.initMocks(this);
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
    dataSetSnapshotControllerImpl.createSnapshot(1L, "test", false);
    Mockito.verify(datasetSnapshotService, times(1)).addSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any());
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
   * Test restsore snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testRestsoreSnapshotsException() throws Exception {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    dataSetSnapshotControllerImpl.restoreSnapshot(null, 1L);
  }

  /**
   * Test release snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReleaseSnapshots() throws Exception {

    dataSetSnapshotControllerImpl.releaseSnapshot(1L, 1L);
    Mockito.verify(datasetSnapshotService, times(1)).releaseSnapshot(Mockito.any(), Mockito.any());
  }

  /**
   * Test release snapshots exception 1.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testReleaseSnapshotsException1() throws Exception {

    dataSetSnapshotControllerImpl.releaseSnapshot(null, 1L);
    Mockito.verify(datasetSnapshotService, times(1)).releaseSnapshot(Mockito.any(), Mockito.any());
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
}
