package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
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

  /** The data set metabase mapper. */
  @Mock
  private DataSetMetabaseMapper dataSetMetabaseMapper;

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
  public void getDataSetIdByDataflowId() {
    when(dataSetMetabaseMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(dataSetMetabaseRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    datasetMetabaseService.getDataSetIdByDataflowId(Mockito.anyLong());
    assertEquals("failed assertion", new ArrayList<>(),
        datasetMetabaseService.getDataSetIdByDataflowId(Mockito.anyLong()));
  }

}
