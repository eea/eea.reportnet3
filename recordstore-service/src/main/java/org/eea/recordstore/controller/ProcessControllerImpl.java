package org.eea.recordstore.controller;


import org.eea.interfaces.controller.recordstore.ProcessController;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.ProcessesVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.recordstore.service.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  /** The process service. */
  @Autowired
  private ProcessService processService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
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
  @GetMapping
  @ApiOperation(value = "Gets all the system processes", response = ProcessVO.class,
      responseContainer = "List", hidden = true)
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ProcessesVO getProcesses(Integer pageNum, Integer pageSize, boolean asc, String status,
      Long dataflowId, String user) {
    Pageable pageable = PageRequest.of(pageNum, pageSize);

    ProcessTypeEnum type = ProcessTypeEnum.VALIDATION;
    String header = "date_start";

    return processService.getProcesses(pageable, asc, status, dataflowId, user, type, header);
  }


  /**
   * Update process.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param status the status
   * @param type the type
   * @param processId the process id
   * @param threadId the thread id
   * @param user the user
   */
  @Override
  @PostMapping(value = "/private/updateProcess")
  @ApiOperation(value = "Updates or creates the process in the process table", hidden = true)
  public void updateProcess(@RequestParam("datasetId") Long datasetId,
      @RequestParam(required = false) Long dataflowId,
      @RequestParam("status") ProcessStatusEnum status, @RequestParam("type") ProcessTypeEnum type,
      @RequestParam("processId") String processId, @RequestParam("threadId") String threadId,
      @RequestParam("user") String user) {
    processService.updateProcess(datasetId, dataflowId, status, type, processId, threadId, user);
  }

}
