package org.eea.dataset.controller;

import ch.qos.logback.core.ConsoleAppender;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.micrometer.core.annotation.Timed;
import java.util.Date;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Data set controller.
 */
@RestController
@RequestMapping("/dataset")
public class DataSetControllerImpl implements DatasetController {

  private static final Logger LOG = LoggerFactory.getLogger(DataSetControllerImpl.class);
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Override
  @HystrixCommand
  @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Timed("FIND_BY_ID_TIMER")
  public DataSetVO findById(@PathVariable("id") String id) {
    DataSetVO dataset = new DataSetVO();
    dataset.setId(id);
    Date now = new Date();
    LOG.info("devolviendo datos chulos {}", dataset);
    LOG_ERROR.error("hola  {}", id);
    ConsoleAppender a;
    Date start = new Date();
    boolean fin = false;
    while (!fin) {
      Date actual = new Date();
      Long time = (actual.getTime() - start.getTime()) / 1000;
      if (time > 5) {
        fin = true;
      }
    }
    return dataset;
  }

  public DataSetVO errorHandler(@PathVariable("id") String id) {
    DataSetVO dataset = new DataSetVO();
    dataset.setId("ERROR");
    return dataset;
  }
}
