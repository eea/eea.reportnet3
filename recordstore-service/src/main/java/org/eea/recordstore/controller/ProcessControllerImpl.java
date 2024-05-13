package org.eea.recordstore.controller;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ProcessControllerImpl.class);

  /** The valid headers. */
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
   * @param header the header
   * @return the processes
   */
  @Override
  @HystrixCommand
  @GetMapping
  @ApiOperation(value = "Gets all the system processes", response = ProcessVO.class,
      responseContainer = "List", hidden = false)
  @PreAuthorize("hasAnyRole('ADMIN','DATA_CUSTODIAN')")
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
   * @param user the user
   * @param priority the priority
   * @param released the released
   * @return true, if successful
   */
  @Override
  @PostMapping(value = "/private/updateProcess")
  @ApiOperation(value = "Updates or creates the process in the process table", hidden = true)
  public boolean updateProcess(@RequestParam("datasetId") Long datasetId,
      @RequestParam(required = false) Long dataflowId,
      @RequestParam("status") ProcessStatusEnum status, @RequestParam("type") ProcessTypeEnum type,
      @RequestParam("processId") String processId, @RequestParam("user") String user,
      @RequestParam("priority") int priority,
      @RequestParam(required = false, value = "released") Boolean released) {
    try {
      return processService.updateProcess(datasetId, dataflowId, status, type, processId, user,
              priority, released);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error updating process with id {} for datasetId {} to status {} Message: {}", processId, datasetId, status.toString(), e.getMessage());
      throw e;
    }
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
    try {
      processService.updatePriority(processId, priority);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error updating priority of process with id {} to value {} Message: {}", processId, priority, e.getMessage());
      throw e;
    }
  }

  /**
   * Find by id.
   *
   * @param processId the process id
   * @return the process VO
   */
  @Override
  @GetMapping(value = "/private/{processId}")
  public ProcessVO findById(@PathVariable("processId") String processId) {
    return processService.getByProcessId(processId);
  }

  /**
   * Gets the private processes.
   *
   * @param pageNum the page num
   * @param pageSize the page size
   * @param asc the asc
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
   * @param header the header
   * @return the private processes
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/")
  @ApiOperation(value = "Gets all the system processes", response = ProcessVO.class,
      responseContainer = "List", hidden = true)
  public ProcessesVO getPrivateProcesses(Integer pageNum, Integer pageSize, boolean asc,
      String status, Long dataflowId, String user, String header) {
    Pageable pageable = PageRequest.of(pageNum, pageSize);

    ProcessTypeEnum type = ProcessTypeEnum.VALIDATION;
    return processService.getProcesses(pageable, asc, status, dataflowId, user, type, header);
  }


  /**
   * Checks if is process finished.
   *
   * @param processId the process id
   * @return true, if is process finished
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/finished/{processId}")
  public boolean isProcessFinished(String processId) {
    try {
      return processService.isProcessFinished(processId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error checking if process with id {} is finished. Message: {}", processId, e.getMessage());
      throw e;
    }
  }


  /**
   * Gets the next process.
   *
   * @param processId the process id
   * @return the next process
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/next/{processId}")
  public ProcessVO getNextProcess(String processId) {
    try {
      return processService.findNextProcess(processId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error finding next process with id {} Message: {}", processId, e.getMessage());
      throw e;
    }
  }

  /**
   * Lists the process ids of processes with type and status that are in progress for more than the specified period of time
   * @param timeInMinutes
   * @return
   */
  @Override
  @GetMapping(value = "/private/listProcessesExceedingTime")
  @ApiOperation(value = "Lists the processes with type that are in progress for more than the specified period of time", hidden = true)
  public List<ProcessVO> listProcessesThatExceedTime(@ApiParam(value = "The type of processes") @RequestParam("type") List<String> type,
          @ApiParam(value = "The status of processes") @RequestParam("status") String status, @ApiParam(
          value = "Time limit in minutes that in progress validation processes exceed",
          example = "15") @RequestParam("timeInMinutes") long timeInMinutes) {
    LOG.info("Finding in progress validation processes that exceed {}", timeInMinutes);
    try {
      return processService.findProcessIdByTypeInAndStatusThatExceedTime(Arrays.asList(ProcessTypeEnum.VALIDATION.toString(),ProcessTypeEnum.RELEASE.toString()), ProcessStatusEnum.IN_PROGRESS.toString(), timeInMinutes);
    } catch (Exception e) {
      LOG.error("Error while finding in progress tasks that exceed " + timeInMinutes + " minutes " + e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Finds process ids of processes with type and status and taskStatus
   * @param type
   * @param status
   * @param taskStatus
   * @return
   */
  @GetMapping(value = "/private/findProcessIdsByTypeAndStatusAndTaskStatus")
  @Override
  public List<String> findProcessIdsByTypeAndStatusAndTaskStatus(String type, String status, String taskStatus) {
    return processService.findProcessIdsByTypeAndStatusAndTaskStatus(type, status, taskStatus);
  }

  /**
   * Deletes process by processId
   * @param processId
   * @return
   */
  @Override
  @DeleteMapping(value = "/private/deleteProcess")
  public void deleteProcessByProcessId(@RequestParam("processId") String processId){
    processService.deleteProcessByProcessId(processId);
  }

}
