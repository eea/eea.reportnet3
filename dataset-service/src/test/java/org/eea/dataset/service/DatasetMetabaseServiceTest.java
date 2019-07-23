package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.mapper.SnapshotMapper;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.impl.DatasetMetabaseServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class DatasetMetabaseServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetMetabaseServiceTest {


  /** The dataset metabase service. */
  @InjectMocks
  private DatasetMetabaseServiceImpl datasetMetabaseService;

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

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the data set id by dataflow id.
   *
   * @return the data set id by dataflow id
   */
  @Test
  public void testGetDataSetIdByDataflowId() {
    when(dataSetMetabaseMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(dataSetMetabaseRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    datasetMetabaseService.getDataSetIdByDataflowId(Mockito.anyLong());
    assertEquals("failed assertion", new ArrayList<>(),
        datasetMetabaseService.getDataSetIdByDataflowId(Mockito.anyLong()));
  }


  /**
   * Test get snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSnapshots() throws Exception {

    when(snapshotMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    datasetMetabaseService.getSnapshotsByIdDataset(Mockito.anyLong());
    assertEquals("failed assertion", new ArrayList<>(),
        datasetMetabaseService.getSnapshotsByIdDataset(Mockito.anyLong()));

  }

  /**
   * Test add snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddSnapshots() throws Exception {

    datasetMetabaseService.addSnapshot(1L, "test");
    Mockito.verify(snapshotRepository, times(1)).save(Mockito.any());

  }

  /**
   * Test delete snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteSnapshots() throws Exception {

    datasetMetabaseService.removeSnapshot(1L, 1L);
    Mockito.verify(snapshotRepository, times(1)).removeSnaphot(Mockito.any(), Mockito.any());

  }

}
