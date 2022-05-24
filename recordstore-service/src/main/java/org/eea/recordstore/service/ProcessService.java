package org.eea.recordstore.service;

import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.ProcessesVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.springframework.data.domain.Pageable;



/**
 * The Interface ProcessService.
 */
public interface ProcessService {

  /**
   * Gets the processes.
   *
   * @param pageable the pageable
   * @param asc the asc
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
   * @param type the type
   * @param header the header
   * @return the processes
   */
  ProcessesVO getProcesses(Pageable pageable, boolean asc, String status, Long dataflowId,
      String user, ProcessTypeEnum type, String header);

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
  boolean updateProcess(Long datasetId, Long dataflowId, ProcessStatusEnum status,
      ProcessTypeEnum type, String processId, String user, int priority, Boolean released);

  /**
   * Update priority.
   *
   * @param processId the process id
   * @param priority the priority
   */
  void updatePriority(Long processId, int priority);

  /**
   * Gets the by process id.
   *
   * @param processId the process id
   * @return the by process id
   */
  ProcessVO getByProcessId(String processId);

  /**
   * Checks if is process finished.
   *
   * @param processId the process id
   * @return true, if is process finished
   */
  boolean isProcessFinished(String processId);

  /**
   * Find next process.
   *
   * @param processId the process id
   * @return the process VO
   */
  ProcessVO findNextProcess(String processId);
}
