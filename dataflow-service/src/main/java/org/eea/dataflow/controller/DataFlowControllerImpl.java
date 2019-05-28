package org.eea.dataflow.controller;

import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Find by id.
   *
   * @param id the id
   * @return the data flow VO
   */
  @Override
  @HystrixCommand(fallbackMethod = "errorHandler")
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DataFlowVO findById(@PathVariable("id") final Long id) {
    final DataFlowVO result = new DataFlowVO();
    result.setId(id);
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
    final String errorMessage = String.format("Dataflow with id: %d has a problem", id);
    final DataFlowVO result = new DataFlowVO();
    result.setId(-1L);
    LOG_ERROR.error(errorMessage);
    return result;
  }
}
