package org.eea.dataset.service;

import static org.mockito.Mockito.times;
import org.eea.dataset.mapper.DataCollectionMapper;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.service.impl.DataCollectionServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;



/**
 * The Class DataCollectionServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCollectionServiceTest {

  /** The data collection service. */
  @InjectMocks
  private DataCollectionServiceImpl dataCollectionService;


  /** The data collection repository. */
  @Mock
  private DataCollectionRepository dataCollectionRepository;

  /** The data collection mapper. */
  @Mock
  private DataCollectionMapper dataCollectionMapper;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  /**
   * Gets the data collection id by dataflow id test.
   *
   * @return the data collection id by dataflow id test
   */
  @Test
  public void getDataCollectionIdByDataflowIdTest() {

    dataCollectionService.getDataCollectionIdByDataflowId(Mockito.anyLong());
    Mockito.verify(dataCollectionRepository, times(1)).findByDataflowId(Mockito.any());
  }



}
