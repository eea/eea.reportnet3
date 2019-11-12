package org.eea.dataset.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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

    dataSetSnapshotControllerImpl.createSnapshot(1L, "test");
    Mockito.verify(datasetSnapshotService, times(1)).addSnapshot(Mockito.any(), Mockito.any());
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
   * Test add snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testAddSnapshotsException() throws Exception {

    dataSetSnapshotControllerImpl.createSnapshot(null, "test");
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
   * Test get snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService).getSnapshotsByIdDataset(Mockito.any());
    dataSetSnapshotControllerImpl.getSnapshotsByIdDataset(1L);
    Mockito.verify(datasetSnapshotService, times(1)).getSnapshotsByIdDataset(Mockito.any());
  }

  /**
   * Test add snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testAddSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService).addSnapshot(Mockito.any(),
        Mockito.any());
    dataSetSnapshotControllerImpl.createSnapshot(1L, "test");
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
   * Test restore snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRestoreSnapshots() throws Exception {

    dataSetSnapshotControllerImpl.restoreSnapshot(1L, 1L);
    Mockito.verify(datasetSnapshotService, times(1)).restoreSnapshot(Mockito.any(), Mockito.any());
  }

  /**
   * Test restsore snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testRestsoreSnapshotsException() throws Exception {

    dataSetSnapshotControllerImpl.restoreSnapshot(null, 1L);
  }

  /**
   * Test restore snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testRestoreSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService).restoreSnapshot(Mockito.any(),
        Mockito.any());
    dataSetSnapshotControllerImpl.restoreSnapshot(1L, 1L);

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
   * Test release snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testReleaseSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService).releaseSnapshot(Mockito.any(),
        Mockito.any());
    dataSetSnapshotControllerImpl.releaseSnapshot(1L, 1L);
  }


  @Test
  public void testGetSchemaSnapshots() throws Exception {
    when(datasetSnapshotService.getSchemaSnapshotsByIdDataset(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    dataSetSnapshotControllerImpl.getSchemaSnapshotsByIdDataset(Mockito.anyLong());
    Mockito.verify(datasetSnapshotService, times(1)).getSchemaSnapshotsByIdDataset(Mockito.any());
  }


  @Test
  public void testAddSchemaSnapshots() throws Exception {

    dataSetSnapshotControllerImpl.createSchemaSnapshot(1L, "5db99d0bb67ca68cb8fa7053", "test");
    Mockito.verify(datasetSnapshotService, times(1)).addSchemaSnapshot(Mockito.any(), Mockito.any(),
        Mockito.any());
  }


  @Test
  public void testRestoreSchemaSnapshots() throws Exception {

    dataSetSnapshotControllerImpl.restoreSchemaSnapshot(1L, 1L);
    Mockito.verify(datasetSnapshotService, times(1)).restoreSchemaSnapshot(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void testDeleteSchemaSnapshots() throws Exception {

    dataSetSnapshotControllerImpl.deleteSchemaSnapshot(1L, 1L);
    Mockito.verify(datasetSnapshotService, times(1)).removeSchemaSnapshot(Mockito.any(),
        Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testGetSchemaSnapshotsException() throws Exception {

    dataSetSnapshotControllerImpl.getSchemaSnapshotsByIdDataset(null);
  }


  @Test
  public void testGetSchemaSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService)
        .getSchemaSnapshotsByIdDataset(Mockito.any());
    dataSetSnapshotControllerImpl.getSchemaSnapshotsByIdDataset(1L);
    Mockito.verify(datasetSnapshotService, times(1)).getSchemaSnapshotsByIdDataset(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testDeleteSchemaSnapshotsException() throws Exception {

    dataSetSnapshotControllerImpl.deleteSchemaSnapshot(null, 1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void testDeleteSchemaSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService).removeSchemaSnapshot(Mockito.any(),
        Mockito.any());
    dataSetSnapshotControllerImpl.deleteSchemaSnapshot(1L, 1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void testAddSchemaSnapshotsException() throws Exception {

    dataSetSnapshotControllerImpl.createSchemaSnapshot(null, "5db99d0bb67ca68cb8fa7053", "test");
  }

  @Test(expected = ResponseStatusException.class)
  public void testAddSchemaSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService).addSchemaSnapshot(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataSetSnapshotControllerImpl.createSchemaSnapshot(1L, "5db99d0bb67ca68cb8fa7053", "test");
  }


  @Test(expected = ResponseStatusException.class)
  public void testRestoreSchemaSnapshotsException() throws Exception {

    dataSetSnapshotControllerImpl.restoreSchemaSnapshot(null, 1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void testRestoreSchemaSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetSnapshotService).restoreSchemaSnapshot(Mockito.any(),
        Mockito.any());
    dataSetSnapshotControllerImpl.restoreSchemaSnapshot(1L, 1L);

  }


}
