package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.eea.dataset.mapper.ReportingDatasetMapper;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.service.impl.ReportingDatasetServiceImpl;
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
    when(reportingDatasetMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(reportingDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    assertEquals("failed assertion", new ArrayList<>(),
        reportingDatasetService.getDataSetIdByDataflowId(Mockito.anyLong()));
  }

}
