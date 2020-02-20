package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eea.dataset.mapper.DesignDatasetMapper;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.impl.DesignDatasetServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * The Class DesignDatasetServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DesignDatasetServiceTest {


  /** The design dataset service. */
  @InjectMocks
  private DesignDatasetServiceImpl designDatasetService;


  /** The design dataset repository. */
  @Mock
  private DesignDatasetRepository designDatasetRepository;


  /** The design dataset mapper. */
  @Mock
  private DesignDatasetMapper designDatasetMapper;

  /** The file common. */
  @Mock
  private FileCommonUtils fileCommon;

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

  @Test
  public void getFileNameDesignTest1() throws EEAException {
    DesignDataset dataset = new DesignDataset();
    when(designDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(dataset));
    when(fileCommon.getDataSetSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    when(fileCommon.getTableName(Mockito.any(), Mockito.any())).thenReturn("test");
    assertEquals("not equals", "test.csv",
        designDatasetService.getFileNameDesign("csv", "test", 1L));
  }

  @Test
  public void getFileNameDesignTest2() throws EEAException {
    when(designDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new DesignDataset()));
    when(fileCommon.getDataSetSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    when(fileCommon.getFieldSchemas(Mockito.any(), Mockito.any())).thenReturn(null);
    assertEquals("null.csv", designDatasetService.getFileNameDesign("csv", "test", 1L));
  }
}
