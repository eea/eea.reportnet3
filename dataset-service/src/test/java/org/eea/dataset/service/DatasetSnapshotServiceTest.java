/*
 * 
 */
package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Optional;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.mapper.SnapshotMapper;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.impl.DatasetSnapshotServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class DatasetSnapshotServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetSnapshotServiceTest {

  /** The dataset metabase service. */
  @InjectMocks
  private DatasetSnapshotServiceImpl datasetSnapshotService;

  /** The data set metabase repository. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The snapshot repository. */
  @Mock
  private SnapshotRepository snapshotRepository;

  /** The data set metabase mapper. */
  @Mock
  private DataSetMetabaseMapper dataSetMetabaseMapper;

  /** The snapshot mapper. */
  @Mock
  private SnapshotMapper snapshotMapper;

  /** The record store controller zull. */
  @Mock
  private RecordStoreControllerZull recordStoreControllerZull;

  /** The partition data set metabase repository. */
  @Mock
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The record repository. */
  @Mock
  private RecordRepository recordRepository;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }



  /**
   * Test get snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSnapshots() throws Exception {

    when(snapshotMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    datasetSnapshotService.getSnapshotsByIdDataset(Mockito.anyLong());
    assertEquals("failed assertion", new ArrayList<>(),
        datasetSnapshotService.getSnapshotsByIdDataset(Mockito.anyLong()));

  }

  /**
   * Test add snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddSnapshots() throws Exception {

    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    doNothing().when(recordStoreControllerZull).createSnapshotData(Mockito.any(), Mockito.any(),
        Mockito.any());
    datasetSnapshotService.addSnapshot(1L, "test");
    Mockito.verify(snapshotRepository, times(1)).save(Mockito.any());

  }

  /**
   * Test delete snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteSnapshots() throws Exception {

    datasetSnapshotService.removeSnapshot(1L, 1L);
    Mockito.verify(snapshotRepository, times(1)).deleteById(Mockito.anyLong());


  }

  /**
   * Test restore snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testRestoreSnapshotsException() throws Exception {

    datasetSnapshotService.restoreSnapshot(1L, 1L);

  }

  /**
   * Test restore snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRestoreSnapshots() throws Exception {

    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    datasetSnapshotService.restoreSnapshot(1L, 1L);
    Mockito.verify(datasetService, times(1)).deleteRecordValuesToRestoreSnapshot(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void releaseSnapshot() throws Exception {

    doNothing().when(snapshotRepository).releaseSnaphot(Mockito.any(), Mockito.any());
    datasetSnapshotService.releaseSnapshot(1L, 1L);
    Mockito.verify(snapshotRepository, times(1)).releaseSnaphot(Mockito.any(), Mockito.any());
  }

}
