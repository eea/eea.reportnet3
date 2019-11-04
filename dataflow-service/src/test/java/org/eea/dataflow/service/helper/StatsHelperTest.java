package org.eea.dataflow.service.helper;

import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;



@RunWith(MockitoJUnitRunner.class)
public class StatsHelperTest {


  @InjectMocks
  private StatsHelper statisticsHelper;

  @Mock
  private DataSetControllerZuul datasetController;

  @Mock
  private DataflowService dataflowService;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGlobalStatistics() throws EEAException {

    List<ReportingDatasetVO> datasets = new ArrayList<>();
    ReportingDatasetVO dataset = new ReportingDatasetVO();
    dataset.setId(1L);
    datasets.add(dataset);
    DataFlowVO df = new DataFlowVO();
    df.setReportingDatasets(datasets);


    when(dataflowService.getReportingDatasetsId(Mockito.anyLong(), Mockito.any())).thenReturn(df);
    when(datasetController.getStatisticsById(Mockito.anyLong())).thenReturn(new StatisticsVO());
    statisticsHelper.executeStatsProcess(1L, "");

  }

}
