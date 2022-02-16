package org.eea.interfaces.controller.recordstore;

import java.util.List;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface ProcessController.
 */
public interface ProcessController {

  /**
   * The Interface ProcessControllerZuul.
   */
  @FeignClient(value = "recordstore", contextId = "processes", path = "/processes")
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
   * @return the processes
   */
  @GetMapping("/")
  List<ProcessVO> getProcesses(
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "asc", defaultValue = "true") boolean asc,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "user", required = false) String user);
}
