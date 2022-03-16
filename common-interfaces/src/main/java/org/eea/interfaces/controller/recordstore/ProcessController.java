package org.eea.interfaces.controller.recordstore;

import org.eea.interfaces.vo.recordstore.ProcessesVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface ProcessController.
 */
public interface ProcessController {

  /**
   * The Interface ProcessControllerZuul.
   */
  @FeignClient(value = "recordstore", contextId = "process", path = "/process")
  interface ProcessControllerZuul extends ProcessController {

  }

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
  @GetMapping()
  ProcessesVO getProcesses(
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "asc", defaultValue = "true") boolean asc,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "user", required = false) String user,
      @RequestParam(value = "header", defaultValue = "date_start") String header);

  /**
   * Update process.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param status the status
   * @param processId the process id
   * @param user the user
   */
  @PostMapping(value = "/private/updateProcess")
  void updateProcess(@RequestParam("datasetId") Long datasetId,
      @RequestParam(required = false) Long dataflowId,
      @RequestParam("status") ProcessStatusEnum status, @RequestParam("type") ProcessTypeEnum type,
      @RequestParam("processId") String processId, @RequestParam("threadId") String threadId,
      @RequestParam("user") String user);

  /**
   * Update priority.
   *
   * @param processId the process id
   * @param priority the priority
   */
  @PostMapping(value = "/{processId}/priority/{priority}")
  void updatePriority(@PathVariable("processId") Long processId,
      @PathVariable("priority") int priority);

}
