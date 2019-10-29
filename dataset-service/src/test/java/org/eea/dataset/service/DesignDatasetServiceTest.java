package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.mapper.DesignDatasetMapper;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.service.impl.DesignDatasetServiceImpl;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DesignDatasetServiceTest {


  @InjectMocks
  private DesignDatasetServiceImpl designDatasetService;


  @Mock
  private DesignDatasetRepository designDatasetRepository;


  @Mock
  private DesignDatasetMapper designDatasetMapper;



  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void testGetDesignDataSetIdByDataflowIdNull() {
    when(designDatasetMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(designDatasetRepository.findByDataflowId(Mockito.anyLong())).thenReturn(new ArrayList<>());
    assertEquals("failed assertion", new ArrayList<>(),
        designDatasetService.getDesignDataSetIdByDataflowId(Mockito.anyLong()));
  }


  @Test
  public void testGetDesignDataSetIdByDataflowId() {

    List<DesignDatasetVO> datasets = new ArrayList<>();
    DesignDatasetVO dataset = new DesignDatasetVO();
    dataset.setId(1L);
    datasets.add(dataset);
    List<Long> result = new ArrayList<>();
    result.add(1L);
    when(designDatasetRepository.findByDataflowId(Mockito.anyLong())).thenReturn(new ArrayList<>());
    when(designDatasetMapper.entityListToClass(Mockito.any())).thenReturn(datasets);
    assertEquals("failed assertion", datasets,
        designDatasetService.getDesignDataSetIdByDataflowId(Mockito.anyLong()));
  }

}
