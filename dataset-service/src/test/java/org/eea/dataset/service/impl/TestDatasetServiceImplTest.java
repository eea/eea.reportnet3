package org.eea.dataset.service.impl;

import static org.mockito.Mockito.times;
import org.eea.dataset.mapper.TestDatasetMapper;
import org.eea.dataset.persistence.metabase.repository.TestDatasetRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


public class TestDatasetServiceImplTest {

  @InjectMocks
  private TestDatasetServiceImpl testDatasetService;

  @Mock
  private TestDatasetRepository testDatasetRepository;

  @Mock
  private TestDatasetMapper testDatasetMapper;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }


  @Test
  public void testGetTestDatasetByDataflowId() throws Exception {
    testDatasetService.getTestDatasetByDataflowId(Mockito.anyLong());
    Mockito.verify(testDatasetRepository, times(1)).findByDataflowId(Mockito.any());
  }

}
