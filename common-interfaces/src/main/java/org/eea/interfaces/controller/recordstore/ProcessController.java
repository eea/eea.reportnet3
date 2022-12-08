package org.eea.interfaces.controller.recordstore;

import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.ProcessesVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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
   * @param type the type
   * @param processId the process id
   * @param user the user
   * @param priority the priority
   * @param released the released
   */
  @PostMapping(value = "/private/updateProcess")
  boolean updateProcess(@RequestParam("datasetId") Long datasetId,
      @RequestParam(required = false) Long dataflowId,
      @RequestParam("status") ProcessStatusEnum status, @RequestParam("type") ProcessTypeEnum type,
      @RequestParam("processId") String processId, @RequestParam("user") String user,
      @RequestParam("priority") int priority,
      @RequestParam(required = false, value = "released") Boolean released);

  /**
   * Update priority.
   *
   * @param processId the process id
   * @param priority the priority
   */
  @PostMapping(value = "/{processId}/priority/{priority}")
  void updatePriority(@PathVariable("processId") Long processId,
      @PathVariable("priority") int priority);

  /**
   * Find by id.
   *
   * @param processId the process id
   * @return the process VO
   */
  @GetMapping(value = "/private/{processId}", produces = MediaType.APPLICATION_JSON_VALUE)
  ProcessVO findById(@PathVariable("processId") String processId);


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
  @GetMapping(value = "/private/")
  ProcessesVO getPrivateProcesses(
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "asc", defaultValue = "true") boolean asc,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "user", required = false) String user,
      @RequestParam(value = "header", defaultValue = "date_start") String header);

  /**
   * Checks if is process finished.
   *
   * @param processId the process id
   * @return true, if is process finished
   */
  @GetMapping(value = "/private/finished/{processId}")
  boolean isProcessFinished(@PathVariable("processId") String processId);

  /**
   * Gets the next process.
   *
   * @param processId the process id
   * @return the next process
   */
  @GetMapping(value = "/private/next/{processId}")
  ProcessVO getNextProcess(@PathVariable("processId") String processId);

  /**
   * Saves process
   * @param processVO
   */
  @PostMapping(value = "/private/saveProcess")
  ProcessVO saveProcess(@RequestBody ProcessVO processVO);

  /**
   * Updates process
   * @param status
   * @param dateFinish
   * @param processId
   */
  @PutMapping(value = "/private/updateProcessStatus")
  void updateStatusAndFinishedDate(@RequestParam("processId") String processId, @RequestParam("status") String status, @RequestParam("dateFinish") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date dateFinish);
  /**
   * Inserts sagaTransactionId and aggregateId
   * @param sagaTransactionId
   * @param aggregateId
   * @param processId
   */
  @PostMapping(value = "/private/insertSagaInfo")
  void insertSagaTransactionIdAndAggregateId(@RequestParam("sagaTransactionId") String sagaTransactionId,
                                             @RequestParam("aggregateId") String aggregateId, @RequestParam("processId") String processId);

  /**
   * Finds processes by dataflow and dataset with specific status
   * @param dataflowId
   * @param datasetId
   * @param status
   * @return
   */
  @GetMapping(value = "/private/getProcessByDataflowAndDataset/{dataflowId}/{datasetId}")
  List<ProcessVO> getProcessByDataflowAndDataset(@PathVariable("dataflowId") Long dataflowId, @PathVariable("datasetId") Long datasetId, @RequestParam("status") List<String> status);
}
