package org.eea.recordstore.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.ProcessesVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.recordstore.mapper.ProcessMapper;
import org.eea.recordstore.persistence.domain.EEAProcess;
import org.eea.recordstore.persistence.repository.ProcessRepository;
import org.eea.recordstore.service.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



/**
 * The Class ProcessServiceImpl.
 */
@Service("ProcessService")
public class ProcessServiceImpl implements ProcessService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ProcessServiceImpl.class);

  /** The process repository. */
  @Autowired
  private ProcessRepository processRepository;

  /** The process mapper. */
  @Autowired
  private ProcessMapper processMapper;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;


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
  @Override
  public ProcessesVO getProcesses(Pageable pageable, boolean asc, String status, Long dataflowId,
      String user, ProcessTypeEnum type, String header) {
    List<EEAProcess> processList;
    try {
      processList = processRepository.getProcessesPaginated(pageable, asc, status, dataflowId, user,
          type, header);
    } catch (JsonProcessingException e) {
      LOG.info("Error processing processes list from json. {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
          "Error retrieving processes list.");
    }
    List<ProcessVO> processVOList = new ArrayList<>();

    if (!CollectionUtils.isEmpty(processList)) {
      processVOList = processMapper.entityListToClass(processList);
    }
    ProcessesVO processes = new ProcessesVO();
    processes.setTotalRecords(processRepository.countProcesses());
    processes.setFilteredRecords(
        processRepository.countProcessesPaginated(asc, status, dataflowId, user, type, header));
    processes.setProcessList(processVOList);

    return processes;
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
  @Transactional
  public boolean updateProcess(Long datasetId, Long dataflowId, ProcessStatusEnum status,
      ProcessTypeEnum type, String processId, String user, int priority, Boolean released) {
    boolean updated = true;
    EEAProcess processToUpdate = processRepository.findOneByProcessId(processId);

    if (processToUpdate == null) {
      processToUpdate = new EEAProcess();
    }

    switch (status) {
      case IN_QUEUE:
        processToUpdate.setQueuedDate(new Date());
        break;
      case IN_PROGRESS:
        if (!ProcessStatusEnum.IN_PROGRESS.equals(processToUpdate.getStatus())) {
          processToUpdate.setProcessStartingDate(new Date());
        }
        break;
      case FINISHED:
      case CANCELED:
        if (!ProcessStatusEnum.FINISHED.equals(processToUpdate.getStatus())
            && !ProcessStatusEnum.CANCELED.equals(processToUpdate.getStatus())) {
          processToUpdate.setProcessFinishingDate(new Date());
        } else {
          updated = false;
        }
        break;
    }
    if (updated) {
      if (processToUpdate.getDatasetId() == null) {
        processToUpdate.setDatasetId(datasetId);
      }
      if (null != released) {
        processToUpdate.setReleased(released);
      }
      processToUpdate.setProcessId(processId);
      processToUpdate.setProcessType(type);
      processToUpdate.setStatus(status);
      processToUpdate.setDataflowId(dataflowId != -1L ? dataflowId
          : datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId).getDataflowId());
      processToUpdate.setUser(user);

      if (priority != 0) {
        processToUpdate.setPriority(priority);
      }
      try {

        processRepository.save(processToUpdate);
        processRepository.flush();
      } catch (ObjectOptimisticLockingFailureException e) {
        updated = false;
      }
    }
    return updated;
  }

  /**
   * Update priority.
   *
   * @param processId the process id
   * @param priority the priority
   */
  @Override
  @Transactional
  public void updatePriority(Long processId, int priority) {
    EEAProcess process = processRepository.findById(processId).orElse(null);
    if (process != null) {
      process.setPriority(priority);
      processRepository.save(process);
      processRepository.flush();
    }
  }

  /**
   * Gets the by process id.
   *
   * @param processId the process id
   * @return the by process id
   */
  @Override
  public ProcessVO getByProcessId(String processId) {
    return processMapper.entityToClass(processRepository.findOneByProcessId(processId));
  }

  /**
   * Checks if is process finished.
   *
   * @param processId the process id
   * @return true, if is process finished
   */
  @Override
  public boolean isProcessFinished(String processId) {
    EEAProcess processToUpdate = processRepository.findOneByProcessId(processId);
    DataSetMetabaseVO dataset =
        datasetMetabaseControllerZuul.findDatasetMetabaseById(processToUpdate.getDatasetId());

    // check if for that dataflow and data provider id are not finished processes
    return processRepository.isProcessFinished(processToUpdate.getDataflowId(),
        dataset.getDataProviderId());
  }

  /**
   * Find next process.
   *
   * @param processId the process id
   * @return the process VO
   */
  @Override
  public ProcessVO findNextProcess(String processId) {
    // load process and dataset
    EEAProcess processToUpdate = processRepository.findOneByProcessId(processId);
    DataSetMetabaseVO dataset =
        datasetMetabaseControllerZuul.findDatasetMetabaseById(processToUpdate.getDatasetId());

    // return next in_queue process with the same dataflow and dataset+dataprovider as the previous
    return processMapper.entityToClass(processRepository.findNextValidationProcess(
        processToUpdate.getDataflowId(), dataset.getDataProviderId(), dataset.getId()));
  }

  /**
   * Finds processId by datasetId and status
   * @param datasetId
   * @param status
   * @return
   */
  @Override
  public List<String> findProcessIdByDatasetAndStatusIn(Long datasetId, String processType, List<String> status) {
     return processRepository.findProcessIdsByDatasetIdAndProcessTypeAndStatusIn(datasetId, processType, status);
  }

  /**
   * Finds processId by type and status
   * @param type
   * @param status
   * @return
   */
  @Override
  public List<ProcessVO> findProcessIdByTypeInAndStatusThatExceedTime(List<String> type, String status, long timeInMinutes) {
    List<EEAProcess> eeaProcesses = processRepository.findProcessIdsByProcessTypeInAndStatus(type, status, timeInMinutes);
    return processMapper.entityListToClass(eeaProcesses);
  }

  /**
   * Finds process ids of processes with type and status and taskStatus
   * @param type
   * @param status
   * @param taskStatus
   * @return
   */
  @Override
  public List<String> findProcessIdsByTypeAndStatusAndTaskStatus(String type, String status, String taskStatus) {
      return processRepository.findProcessIdsByTypeAndStatusAndTaskStatus(type, status, taskStatus);
  }
  /**
   * Deletes process by processId
   * @param processId
   * @return
   */
  @Override
  public void deleteProcessByProcessId(String processId){
    processRepository.deleteByProcessId(processId);
  }

}
