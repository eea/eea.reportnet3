package org.eea.recordstore.controller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eea.interfaces.controller.recordstore.ProcessController;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * The Class ProcessControllerImpl.
 */
@RestController
@RequestMapping("/process")
@Api(tags = "Processes : Processes Manager")
public class ProcessControllerImpl implements ProcessController {

  // @Autowired
  // private ProcessService processService;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ProcessControllerImpl.class);


  /**
   * Gets the processes.
   *
   * @param pageNum the page num
   * @param pageSize the page size
   * @param asc the asc
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
   * @return the processes
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/")
  @ApiOperation(value = "Gets all the system processes", response = ProcessVO.class,
      responseContainer = "List", hidden = false)
  @PreAuthorize("hasAnyRole('ADMIN')")
  public List<ProcessVO> getProcesses(Integer pageNum, Integer pageSize, boolean asc, String status,
      Long dataflowId, String user) {
    List<ProcessVO> mockProcesses = new ArrayList<>();
    ProcessVO process1 = new ProcessVO();
    ProcessVO process2 = new ProcessVO();
    process1.setId(1L);
    process1.setDataflowId(dataflowId);
    process1.setDataflowName("dataflowName");
    process1.setDatasetId(11L);
    process1.setDatasetName("datasetName");
    process1.setStatus("validated");
    process1.setQueuedDate(new Date());
    process1.setProcessFinishingDate(new Date());
    process1.setProcessStartingDate(new Date());
    process1.setUser("user");
    process2.setId(1L);
    process2.setDataflowId(dataflowId);
    process2.setDataflowName("dataflowName2");
    process2.setDatasetId(11L);
    process2.setDatasetName("datasetName2");
    process2.setStatus("validating");
    process2.setQueuedDate(new Date());
    process2.setProcessFinishingDate(new Date());
    process2.setProcessStartingDate(new Date());
    process2.setUser("user2");
    mockProcesses.add(process1);
    mockProcesses.add(process2);
    return mockProcesses;
  }

}
