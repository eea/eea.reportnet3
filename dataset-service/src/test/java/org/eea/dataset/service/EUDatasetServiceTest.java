package org.eea.dataset.service;

import static org.mockito.Mockito.times;
import org.eea.dataset.mapper.EUDatasetMapper;
import org.eea.dataset.persistence.metabase.repository.EUDatasetRepository;
import org.eea.dataset.service.impl.EUDatasetServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EUDatasetServiceTest {


  @InjectMocks
  private EUDatasetServiceImpl euDatasetService;


  @Mock
  private EUDatasetRepository euDatasetRepository;

  @Mock
  private EUDatasetMapper euDatasetMapper;


  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void getEUDatasetByDataflowIdTest() {
    euDatasetService.getEUDatasetByDataflowId(Mockito.anyLong());
    Mockito.verify(euDatasetRepository, times(1)).findByDataflowId(Mockito.any());
  }


}
