package org.eea.interfaces.controller.dataflow;


import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The interface Data flow controller.
 */

public interface DataFlowController {

  @FeignClient(value = "dataflow", path = "/dataflow")
  public interface DataFlowControllerZuul extends DataFlowController {

  }

  /**
   * Find by id data flow vo.
   *
   * @param id the id
   *
   * @return the data flow vo
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DataFlowVO findById(@PathVariable("id") Long id);
}
