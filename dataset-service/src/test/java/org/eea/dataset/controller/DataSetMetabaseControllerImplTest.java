package org.eea.dataset.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataSetMetabaseControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSetMetabaseControllerImplTest {

  /** The data set metabase controller impl. */
  @InjectMocks
  private DataSetMetabaseControllerImpl dataSetMetabaseControllerImpl;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The reporting dataset service. */
  @Mock
  private ReportingDatasetService reportingDatasetService;



  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Find data set id by dataflow id.
   */
  @Test
  public void testFindDataSetIdByDataflowId() {
    when(reportingDatasetService.getDataSetIdByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    dataSetMetabaseControllerImpl.findDataSetIdByDataflowId(Mockito.anyLong());
    Mockito.verify(reportingDatasetService, times(1)).getDataSetIdByDataflowId(Mockito.any());
  }


  /**
   * Test get snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSnapshots() throws Exception {
    when(datasetMetabaseService.getSnapshotsByIdDataset(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    dataSetMetabaseControllerImpl.getSnapshotsByIdDataset(Mockito.anyLong());
    Mockito.verify(datasetMetabaseService, times(1)).getSnapshotsByIdDataset(Mockito.any());
  }

  /**
   * Test add snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddSnapshots() throws Exception {

    dataSetMetabaseControllerImpl.createSnapshot(1L, "test");
    Mockito.verify(datasetMetabaseService, times(1)).addSnapshot(Mockito.any(), Mockito.any());
  }

  /**
   * Test delete snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteSnapshots() throws Exception {

    dataSetMetabaseControllerImpl.deleteSnapshot(1L, 1L);
    Mockito.verify(datasetMetabaseService, times(1)).removeSnapshot(Mockito.any(), Mockito.any());
  }

  /**
   * Test get snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testGetSnapshotsException() throws Exception {

    dataSetMetabaseControllerImpl.getSnapshotsByIdDataset(null);
  }

  /**
   * Test add snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testAddSnapshotsException() throws Exception {

    dataSetMetabaseControllerImpl.createSnapshot(null, "test");
  }

  /**
   * Test delete snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteSnapshotsException() throws Exception {

    dataSetMetabaseControllerImpl.deleteSnapshot(null, 1L);
  }

  /**
   * Test get snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetMetabaseService).getSnapshotsByIdDataset(Mockito.any());
    dataSetMetabaseControllerImpl.getSnapshotsByIdDataset(1L);
    Mockito.verify(datasetMetabaseService, times(1)).getSnapshotsByIdDataset(Mockito.any());
  }

  /**
   * Test add snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testAddSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetMetabaseService).addSnapshot(Mockito.any(),
        Mockito.any());
    dataSetMetabaseControllerImpl.createSnapshot(1L, "test");
  }

  /**
   * Test delete snapshots exception 2.
   *
   * @throws Exception the exception
   */
  @Test(expected = ResponseStatusException.class)
  public void testDeleteSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetMetabaseService).removeSnapshot(Mockito.any(),
        Mockito.any());
    dataSetMetabaseControllerImpl.deleteSnapshot(1L, 1L);
  }

  @Test
  public void testRestoreSnapshots() throws Exception {

    dataSetMetabaseControllerImpl.restoreSnapshot(1L, 1L);
    Mockito.verify(datasetMetabaseService, times(1)).restoreSnapshot(Mockito.any(), Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testRestsoreSnapshotsException() throws Exception {

    dataSetMetabaseControllerImpl.restoreSnapshot(null, 1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void testRestoreSnapshotsException2() throws Exception {

    doThrow(new EEAException()).when(datasetMetabaseService).restoreSnapshot(Mockito.any(),
        Mockito.any());
    dataSetMetabaseControllerImpl.restoreSnapshot(1L, 1L);

  }

}
