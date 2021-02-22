package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eea.dataset.mapper.ReportingDatasetMapper;
import org.eea.dataset.mapper.ReportingDatasetPublicMapper;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
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

  /** The design dataset repository. */
  @Mock
  private DesignDatasetRepository designDatasetRepository;

  /** The reporting dataset public mapper. */
  @Mock
  private ReportingDatasetPublicMapper reportingDatasetPublicMapper;

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
    DesignDataset designDataset = new DesignDataset();
    ArrayList<DesignDataset> designs = new ArrayList<>();
    designDataset.setDatasetSchema("");
    designs.add(designDataset);
    dataset.setId(1L);
    datasets.add(dataset);
    Snapshot snap = new Snapshot();
    snap.setId(1L);
    snap.setDcReleased(false);
    ReportingDataset reporting = new ReportingDataset();
    reporting.setId(1L);
    snap.setReportingDataset(reporting);
    when(reportingDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    when(reportingDatasetMapper.entityListToClass(Mockito.any())).thenReturn(datasets);
    when(snapshotRepository.findByReportingDatasetAndRelease(Mockito.any(), Mockito.any()))
        .thenReturn(Arrays.asList(snap));
    when(designDatasetRepository.findbyDatasetSchemaList(Mockito.any())).thenReturn(designs);
    assertEquals("failed assertion", datasets,
        reportingDatasetService.getDataSetIdByDataflowId(Mockito.anyLong()));
  }


  /**
   * Test get data set id by id dataschema.
   */
  @Test
  public void testGetDataSetIdByIdDataschema() {

    List<ReportingDatasetVO> datasets = new ArrayList<>();
    ReportingDatasetVO dataset = new ReportingDatasetVO();
    dataset.setId(1L);
    datasets.add(dataset);
    List<Long> result = new ArrayList<>();
    result.add(1L);
    when(reportingDatasetRepository.findByDatasetSchema(Mockito.any()))
        .thenReturn(new ArrayList<>());
    when(reportingDatasetMapper.entityListToClass(Mockito.any())).thenReturn(datasets);

    reportingDatasetService.getDataSetIdBySchemaId("5ce524fad31fc52540abae73");


    assertEquals("failed assertion", datasets,
        reportingDatasetService.getDataSetIdBySchemaId(Mockito.any()));
  }


  @Test
  public void testUpdateReportingMetabase() {
    ReportingDataset dataset = new ReportingDataset();
    dataset.setId(1L);
    dataset.setReleasing(false);

    ReportingDatasetVO datasetVO = new ReportingDatasetVO();
    datasetVO.setId(1L);
    datasetVO.setReleasing(true);

    Mockito.when(reportingDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(dataset));
    reportingDatasetService.updateReportingDatasetMetabase(datasetVO);
    Mockito.verify(reportingDatasetRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void getDataSetPublicByDataflowTest() {
    reportingDatasetService.getDataSetPublicByDataflow(1L);
    Mockito.verify(reportingDatasetPublicMapper, times(1)).entityListToClass(Mockito.any());
  }

}
