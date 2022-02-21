package org.eea.recordstore.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.core.JsonProcessingException;



/**
 * The Class ProcessServiceImpl.
 */
@Service("ProcessService")
public class ProcessServiceImpl implements ProcessService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ProcessServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The process repository. */
  @Autowired
  private ProcessRepository processRepository;

  /** The process mapper. */
  @Autowired
  private ProcessMapper processMapper;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;


  /**
   * Gets the processes.
   *
   * @param pageable the pageable
   * @param asc the asc
   * @param status the status
   * @param dataflowId the dataflow id
   * @param user the user
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
   * @param threadId the thread id
   * @param user the user
   */
  @Override
  public void updateProcess(Long datasetId, Long dataflowId, ProcessStatusEnum status,
      ProcessTypeEnum type, String processId, String threadId, String user) {

    EEAProcess processToUpdate =
        processRepository.findOneByProcessId(processId).orElse(new EEAProcess());

    if (processToUpdate.getDatasetId() == null) {
      processToUpdate.setDatasetId(datasetId);
    }

    processToUpdate.setProcessId(processId.equals(threadId) ? processId : threadId);
    processToUpdate.setProcessType(type);
    processToUpdate.setStatus(status);
    processToUpdate.setDataflowId(dataflowId != -1L ? dataflowId
        : datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId).getDataflowId());
    processToUpdate.setUser(user);

    switch (status) {
      case IN_QUEUE:
        processToUpdate.setQueuedDate(new Date());
        break;
      case IN_PROGRESS:
        processToUpdate.setProcessStartingDate(new Date());
        break;
      case FINISHED:
      case CANCELED:
        processToUpdate.setProcessFinishingDate(new Date());
        break;
    }

    LOG.info(String.format(
        "Adding or updating process for datasetId %s, dataflowId %s: %s %s with processId %s made by user %s",
        datasetId, processToUpdate.getDataflowId(), type, status, processId, user));
    processRepository.save(processToUpdate);
  }
}
