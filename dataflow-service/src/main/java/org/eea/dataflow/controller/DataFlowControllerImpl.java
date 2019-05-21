package org.eea.dataflow.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The type Data flow controller.
 */
@RestController
@RequestMapping(value = "/dataflow")
public class DataFlowControllerImpl implements DataFlowController {

  @Autowired
  private DataSetControllerZuul datasetController;

  @Override
  @HystrixCommand(fallbackMethod = "errorHandler")
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DataFlowVO findById(@PathVariable("id") final Long id) {
    final DataFlowVO result = new DataFlowVO();
    result.setId(1L);
    final List<DataSetVO> datasets = new ArrayList<>();
    final DataSetVO set = datasetController.findById(1L);
    datasets.add(set);
    result.setDatasets(datasets);
    return result;
  }

  /**
   * Error handler data flow vo.
   *
   * @param id the id
   *
   * @return the data flow vo
   */
  public static DataFlowVO errorHandler(@PathVariable("id") final Long id) {
    final DataFlowVO result = new DataFlowVO();
    result.setId(-1L);
    return result;
  }
}
