package org.eea.recordstore.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.orchestrator.AdminProcessInfoVO;
import org.eea.interfaces.vo.orchestrator.AdminTaskInfoAnalytics;
import org.eea.interfaces.vo.orchestrator.AdminTaskInfoVO;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.ProcessesVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.recordstore.mapper.ProcessMapper;
import org.eea.recordstore.persistence.domain.EEAProcess;
import org.eea.recordstore.persistence.repository.ProcessRepository;
import org.eea.recordstore.service.ProcessService;
import org.eea.recordstore.service.TaskService;
import org.eea.utils.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


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


  @Autowired
  private TaskService taskService;

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
      } catch (Exception e) {
          LOG.error("Error updating process {} ", processId, e);
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
   * Gets the by process id.
   *
   * @param processIds the process id
   * @return the by process id
   */
  @Override
  public List<ProcessVO> getByProcessIds(List<String> processIds) {
    return processMapper.entityListToClass(processRepository.findOneByProcessIds(processIds));
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

  @Override
  public List<AdminProcessInfoVO> findProcessesAndRelatedTasks(List<String> processIds) {
    List<AdminProcessInfoVO> adminJobInfoVOResponse = new ArrayList<>();

    List<ProcessVO> processVOList = getByProcessIds(processIds);
    List<TaskVO> taskVOList = taskService.findByProcessIds(processIds);

    for (ProcessVO processVO : processVOList) {
      List<TaskVO> relatedTasks = taskVOList.stream().filter(taskVO -> taskVO.getProcessId().equals(processVO.getProcessId())).collect(Collectors.toList());

      Duration processDuration = UtilityClass.calculateDuration(processVO.getProcessStartingDate().toInstant(), processVO.getProcessFinishingDate().toInstant());
      Long processDurationMs = processDuration.toMillis();
      String processDurationFormatted = UtilityClass.formatDuration(processDuration);

      AdminProcessInfoVO adminProcessInfoVO = new AdminProcessInfoVO();
      adminProcessInfoVO.setProcessVO(processVO);
      adminProcessInfoVO.setProcessDurationMs(processDurationMs);
      adminProcessInfoVO.setProcessDurationFormatted(processDurationFormatted);
      adminProcessInfoVO.setAdminTaskInfoVOS(new ArrayList<>());
      adminProcessInfoVO.setAdminTaskInfoAnalytics(new AdminTaskInfoAnalytics());

      relatedTasks.forEach(taskVO -> {

        Duration taskDuration = UtilityClass.calculateDuration(taskVO.getStartingDate().toInstant(), taskVO.getFinishDate().toInstant());
        Long taskDurationMs = taskDuration.toMillis();
        String taskDurationFormatted = UtilityClass.formatDuration(taskDuration);

        AdminTaskInfoVO adminTaskInfoVO = new AdminTaskInfoVO();
        adminTaskInfoVO.setTaskVO(taskVO);
        adminTaskInfoVO.setTaskDurationMs(taskDurationMs);
        adminTaskInfoVO.setTaskDurationFormatted(taskDurationFormatted);

        adminProcessInfoVO.getAdminTaskInfoVOS().add(adminTaskInfoVO);
      });
      AdminTaskInfoAnalytics adminTaskInfoAnalytics = calculateAllTaskAnalytics(adminProcessInfoVO);
      adminProcessInfoVO.setAdminTaskInfoAnalytics(adminTaskInfoAnalytics);
      adminJobInfoVOResponse.add(adminProcessInfoVO);
    }
    return adminJobInfoVOResponse;
  }

  private AdminTaskInfoAnalytics calculateAllTaskAnalytics(AdminProcessInfoVO adminProcessInfoVO) {

    AdminTaskInfoAnalytics analytics = new AdminTaskInfoAnalytics();
    ProcessVO process = adminProcessInfoVO.getProcessVO();
    analytics.setProcessId(process.getProcessId());
    analytics.setProcessType(process.getProcessType());
    analytics.setDataflowId(process.getDataflowId());
    analytics.setDatasetId(process.getDatasetId());
    analytics.setStatus(process.getStatus());

    int totalTasks = 0;
    int queuedTasks = 0;
    int inProgressTasks = 0;
    int canceledTasks = 0;
    int finishedTasks = 0;
    long minFinishedDuration = Long.MAX_VALUE;
    long maxFinishedDuration = 0L;
    long finishedDurationSum = 0L;
    long minInProgressDuration = Long.MAX_VALUE;
    long maxInProgressDuration = 0L;
    int maxVersion = 0;

    List<AdminTaskInfoVO> tasks = adminProcessInfoVO.getAdminTaskInfoVOS();

    if (tasks != null) {

      for (AdminTaskInfoVO adminTask : tasks) {

        TaskVO task = adminTask.getTaskVO();

        if (task == null) {
          continue;
        }

        totalTasks++;

        ProcessStatusEnum status = task.getStatus();

        long duration = adminTask.getTaskDurationMs() != null
                ? adminTask.getTaskDurationMs()
                : 0L;

        int version = task.getVersion();
        if (version > maxVersion) {
          maxVersion = version;
        }

        switch (status) {

          case IN_QUEUE:
            queuedTasks++;
            break;
          case IN_PROGRESS:
            inProgressTasks++;
            if (duration < minInProgressDuration) minInProgressDuration = duration;
            if (duration > maxInProgressDuration) maxInProgressDuration = duration;
            break;
          case CANCELED:
            canceledTasks++;
            break;
          case FINISHED:
            finishedTasks++;
            if (duration < minFinishedDuration) minFinishedDuration = duration;
            if (duration > maxFinishedDuration) maxFinishedDuration = duration;
            finishedDurationSum += duration;
            break;
          default:
            break;
        }
      }
    }

    long averageFinishedDuration = finishedTasks > 0
            ? finishedDurationSum / finishedTasks
            : 0;

    if (minFinishedDuration == Long.MAX_VALUE) minFinishedDuration = 0;
    if (minInProgressDuration == Long.MAX_VALUE) minInProgressDuration = 0;

    analytics.setTotalTasks(totalTasks);
    analytics.setQueuedTasks(queuedTasks);
    analytics.setInProgressTasks(inProgressTasks);
    analytics.setCanceledTasks(canceledTasks);
    analytics.setFinishedTasks(finishedTasks);

    analytics.setMinimumFinishedTaskDurationMs(minFinishedDuration);
    analytics.setMaximumFinishedTaskDurationMs(maxFinishedDuration);
    analytics.setAverageFinishedTaskDurationMs(averageFinishedDuration);

    analytics.setMinimumInProgressTaskDurationMs(minInProgressDuration);
    analytics.setMaximumInProgressTaskDurationMs(maxInProgressDuration);

    analytics.setMaximumVersionOfAnyTask(maxVersion);

    return analytics;
  }

}
