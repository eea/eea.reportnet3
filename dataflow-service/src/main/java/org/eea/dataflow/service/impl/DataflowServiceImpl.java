package org.eea.dataflow.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.mapper.DataflowNoContentMapper;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * The Class DataflowServiceImpl.
 */
@Service("dataflowService")
public class DataflowServiceImpl implements DataflowService {


  /** The dataflow repository. */
  @Autowired
  private DataflowRepository dataflowRepository;

  /** The dataflow mapper. */
  @Autowired
  private DataflowMapper dataflowMapper;

  /** The dataflow no content mapper. */
  @Autowired
  private DataflowNoContentMapper dataflowNoContentMapper;


  /** The dataset metabase controller. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseController;


  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowServiceImpl.class);


  /**
   * Gets the by id.
   *
   * @param id the id
   * @return the by id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataFlowVO getById(Long id) throws EEAException {

    if (id == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Dataflow result = dataflowRepository.findById(id).orElse(null);

    DataFlowVO dataflowVO = dataflowMapper.entityToClass(result);

    dataflowVO.setDatasets(datasetMetabaseController.findDataSetIdByDataflowId(id));
    LOG.info("Get the dataflow information with id {}", id);

    return dataflowVO;
  }

  /**
   * Gets the by status.
   *
   * @param status the status
   * @return the by status
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getByStatus(TypeStatusEnum status) throws EEAException {

    List<Dataflow> dataflows = dataflowRepository.findByStatus(status);
    return dataflowMapper.entityListToClass(dataflows);
  }


  /**
   * Gets the pending accepted.
   *
   * @param userId the user id
   * @return the pending accepted
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getPendingAccepted(Long userId) throws EEAException {

    List<Dataflow> dataflows = dataflowRepository.findPendingAccepted(userId);
    LOG.info("Get the dataflows pending and accepted of the user id: {}", userId);
    return dataflowNoContentMapper.entityListToClass(dataflows);

  }


  /**
   * Gets the completed.
   *
   * @param userId the user id
   * @param pageable the pageable
   * @return the completed
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getCompleted(Long userId, Pageable pageable) throws EEAException {

    List<Dataflow> dataflows = dataflowRepository.findCompleted(userId, pageable);
    List<DataFlowVO> dataflowVOs = new ArrayList<>();
    if (!dataflows.isEmpty()) {
      dataflowVOs = dataflowNoContentMapper.entityListToClass(dataflows);
    }
    LOG.info("Get the dataflows completed of the user id: {}. {} per page, current page {}", userId,
        pageable.getPageSize(), pageable.getPageNumber());

    return dataflowVOs;
  }

  /**
   * Gets the pending by user.
   *
   * @param userId the user id
   * @param type the type
   * @return the pending by user
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getPendingByUser(Long userId, TypeRequestEnum type) throws EEAException {

    List<Dataflow> dataflows = dataflowRepository.findByStatusAndUserRequester(type, userId);
    LOG.info("Get the dataflows of the user id: {} with the status {}", userId, type);
    return dataflowNoContentMapper.entityListToClass(dataflows);

  }


}
