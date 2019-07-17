package org.eea.dataset.controller;

import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.eea.dataset.service.DatasetMetabaseService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

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
  public void findDataSetIdByDataflowId() {
    when(datasetMetabaseService.getDataSetIdByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    dataSetMetabaseControllerImpl.findDataSetIdByDataflowId(Mockito.anyLong());
  }
}
