package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.mapper.ReportingDatasetMapper;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.impl.ReportingDatasetServiceImpl;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ReportingDatasetServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportingDatasetServiceTest {

  /** The reporting dataset service. */
  @InjectMocks
  private ReportingDatasetServiceImpl reportingDatasetService;

  /** The reporting dataset repository. */
  @Mock
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The reporting dataset mapper. */
  @Mock
  private ReportingDatasetMapper reportingDatasetMapper;

  /** The snapshot repository. */
  @Mock
  private SnapshotRepository snapshotRepository;

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
  public void getDataSetIdByDataflowIdNull() {
    when(reportingDatasetMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(reportingDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    assertEquals("failed assertion", new ArrayList<>(),
        reportingDatasetService.getDataSetIdByDataflowId(Mockito.anyLong()));
  }


  /**
   * Gets the data set id by dataflow id.
   *
   * @return the data set id by dataflow id
   */
  @Test
  public void getDataSetIdByDataflowId() {

    List<ReportingDatasetVO> datasets = new ArrayList<>();
    ReportingDatasetVO dataset = new ReportingDatasetVO();
    dataset.setId(1L);
    datasets.add(dataset);
    List<Long> result = new ArrayList<>();
    result.add(1L);
    when(reportingDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    when(reportingDatasetMapper.entityListToClass(Mockito.any())).thenReturn(datasets);
    when(snapshotRepository.findByReportingDatasetAndRelease(Mockito.any())).thenReturn(result);
    assertEquals("failed assertion", datasets,
        reportingDatasetService.getDataSetIdByDataflowId(Mockito.anyLong()));
  }

}
