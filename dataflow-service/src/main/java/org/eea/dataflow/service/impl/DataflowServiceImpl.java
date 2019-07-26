package org.eea.dataflow.service.impl;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.mapper.DataflowNoContentMapper;
import org.eea.dataflow.persistence.domain.Contributor;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.DataflowWithRequestType;
import org.eea.dataflow.persistence.domain.Document;
import org.eea.dataflow.persistence.repository.ContributorRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DocumentRepository;
import org.eea.dataflow.persistence.repository.UserRequestRepository;
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

  /** The user request repository. */
  @Autowired
  private UserRequestRepository userRequestRepository;

  /** The contributor repository. */
  @Autowired
  private ContributorRepository contributorRepository;

  /** The document repository. */
  @Autowired
  private DocumentRepository documentRepository;

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

    List<DataflowWithRequestType> dataflows = dataflowRepository.findPendingAccepted(userId);
    List<Dataflow> dfs = new ArrayList<>();
    LOG.info("Get the dataflows pending and accepted of the user id: {}", userId);
    for (DataflowWithRequestType df : dataflows) {
      dfs.add(df.getDataflow());
    }
    List<DataFlowVO> dataflowVOs = dataflowNoContentMapper.entityListToClass(dfs);
    // Adding the user request type to the VO (pending/accepted/rejected)
    for (DataflowWithRequestType df : dataflows) {
      for (int i = 0; i < dataflowVOs.size(); i++) {
        if (df.getDataflow().getId().equals(dataflowVOs.get(i).getId())) {
          dataflowVOs.get(i).setUserRequestStatus(df.getTypeRequestEnum());
          dataflowVOs.get(i).setRequestId(df.getRequestId());
        }
      }
    }
    return dataflowVOs;
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

  /**
   * Update user request status.
   *
   * @param userRequestId the user request id
   * @param type the type
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateUserRequestStatus(Long userRequestId, TypeRequestEnum type)
      throws EEAException {

    userRequestRepository.updateUserRequestStatus(userRequestId, type.name());
    LOG.info("Update the request status of the requestId: {}. New status: {}", userRequestId, type);
  }

  /**
   * Adds the contributor to dataflow.
   *
   * @param idDataflow the id dataflow
   * @param idContributor the id contributor
   * @throws EEAException the EEA exception
   */
  @Override
  public void addContributorToDataflow(Long idDataflow, Long idContributor) throws EEAException {

    Contributor contributor = new Contributor();
    contributor.setUserId(idContributor);
    Dataflow dataflow = dataflowRepository.findById(idDataflow).orElse(new Dataflow());
    contributor.setDataflow(dataflow);

    contributorRepository.save(contributor);
  }

  /**
   * Removes the contributor from dataflow.
   *
   * @param idDataflow the id dataflow
   * @param idContributor the id contributor
   * @throws EEAException the EEA exception
   */
  @Override
  public void removeContributorFromDataflow(Long idDataflow, Long idContributor)
      throws EEAException {

    contributorRepository.removeContributorFromDataset(idDataflow, idContributor);

  }

  /**
   * Insert document.
   *
   * @param dataflowId the dataflow id
   * @param filename the filename
   * @param language the language
   * @param description the description
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void insertDocument(Long dataflowId, String filename, String language, String description)
      throws EEAException {
    if (dataflowId == null || filename == null || language == null || description == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Dataflow dataflow = dataflowRepository.findById(dataflowId).orElse(null);
    if (dataflow != null) {
      Document document = new Document();
      document.setDescription(description);
      document.setLanguage(language);
      document.setName(filename);
      document.setDataflow(dataflow);
      documentRepository.save(document);
    } else {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
  }

  /**
   * Delete document.
   *
   * @param dataflowId the dataflow id
   * @param filename the filename
   * @param language the language
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteDocument(Long dataflowId, String filename, String language)
      throws EEAException {
    if (dataflowId == null || filename == null || language == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Document document =
        documentRepository.findFirstByDataflowIdAndNameAndLanguage(dataflowId, filename, language);
    if (document == null) {
      throw new EEAException(EEAErrorMessage.DOCUMENT_NOT_FOUND);
    } else {
      documentRepository.delete(document);
    }
  }

}
