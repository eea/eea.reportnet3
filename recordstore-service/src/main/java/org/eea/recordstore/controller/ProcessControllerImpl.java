package org.eea.recordstore.controller;


import java.util.Arrays;
import java.util.List;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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

  List<String> validHeaders = Arrays.asList("name", "dataset_name", "username", "status",
      "queued_date", "date_start", "date_finish", "priority");


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
      responseContainer = "List", hidden = false)
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ProcessesVO getProcesses(Integer pageNum, Integer pageSize, boolean asc, String status,
      Long dataflowId, String user, String header) {
    Pageable pageable = PageRequest.of(pageNum, pageSize);

    ProcessTypeEnum type = ProcessTypeEnum.VALIDATION;

    if (!validHeaders.contains(header)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong sorting header provided.");
    }

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
      @RequestParam("user") String user, @RequestParam("priority") int priority) {
    processService.updateProcess(datasetId, dataflowId, status, type, processId, threadId, user,
        priority);
  }

  /**
   * Update priority.
   *
   * @param processId the process id
   * @param priority the priority
   */
  @Override
  @PostMapping(value = "/{processId}/priority/{priority}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ApiOperation(value = "Updates the process priority in the process table", hidden = false)
  public void updatePriority(@PathVariable("processId") Long processId,
      @PathVariable("priority") int priority) {
    if (priority > 100 || priority < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong priority range.");
    }
    processService.updatePriority(processId, priority);
  }

}
