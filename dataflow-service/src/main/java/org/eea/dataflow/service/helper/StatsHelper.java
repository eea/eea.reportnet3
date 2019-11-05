package org.eea.dataflow.service.helper;


import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class StatsHelper.
 */
@Component
public class StatsHelper {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StatsHelper.class);


  /** The dataflow service. */
  @Autowired
  private DataflowService dataflowService;

  /** The dataset controller. */
  @Autowired
  private DataSetControllerZuul datasetController;


  /**
   * Instantiates a new stats helper.
   */
  public StatsHelper() {
    super();
  }



  /**
   * Execute stats process.
   *
   * @return the list
   * @throws EEAException the EEA exception
   */
  public List<StatisticsVO> executeStatsProcess(String dataschemaId) throws EEAException {

    List<StatisticsVO> statistics = new ArrayList<>();
    DataFlowVO dfVO = dataflowService.getReportingDatasetsId(dataschemaId);
    LOG.info("Retrieving all the datasets' stats from the dataschema: {}", dataschemaId);
    dfVO.getReportingDatasets().parallelStream().forEach(d -> {
      LOG.info(d.getId().toString());
      statistics.add(datasetController.getStatisticsById(d.getId()));

    });


    return statistics;
  }


}
