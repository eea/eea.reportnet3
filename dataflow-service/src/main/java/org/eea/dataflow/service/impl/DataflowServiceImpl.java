package org.eea.dataflow.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.mapper.DataflowNoContentMapper;
import org.eea.dataflow.persistence.domain.Contributor;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.DataflowWithRequestType;
import org.eea.dataflow.persistence.domain.UserRequest;
import org.eea.dataflow.persistence.repository.ContributorRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.UserRequestRepository;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DataSetSchemaControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
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


  /**
   * The dataflow repository.
   */
  @Autowired
  private DataflowRepository dataflowRepository;

  /**
   * The user request repository.
   */
  @Autowired
  private UserRequestRepository userRequestRepository;

  /**
   * The contributor repository.
   */
  @Autowired
  private ContributorRepository contributorRepository;

  /**
   * The dataflow mapper.
   */
  @Autowired
  private DataflowMapper dataflowMapper;

  /**
   * The dataflow no content mapper.
   */
  @Autowired
  private DataflowNoContentMapper dataflowNoContentMapper;

  /**
   * The dataset metabase controller.
   */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  /** The dataset controller. */
  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;
  /**
   * The user management controller zull.
   */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;


  /** The resource management controller zull. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The data set schema controller zuul. */
  @Autowired
  private DataSetSchemaControllerZuul dataSetSchemaControllerZuul;

  /** The document controller zuul. */
  @Autowired
  private DocumentControllerZuul documentControllerZuul;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowServiceImpl.class);


  /**
   * Gets the by id.
   *
   * @param id the id
   *
   * @return the by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataFlowVO getById(Long id) throws EEAException {

    if (id == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Dataflow result = dataflowRepository.findById(id).orElse(null);
    // filter datasets showed to the user depending on permissions
    List<ResourceAccessVO> datasets =
        userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATASET);
    // add to the filter the design datasets (data schemas) too
    datasets.addAll(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATA_SCHEMA));
    List<Long> datasetsIds =
        datasets.stream().map(ResourceAccessVO::getId).collect(Collectors.toList());
    DataFlowVO dataflowVO = dataflowMapper.entityToClass(result);
    dataflowVO.setReportingDatasets(
        datasetMetabaseController.findReportingDataSetIdByDataflowId(id).stream()
            .filter(dataset -> datasetsIds.contains(dataset.getId())).collect(Collectors.toList()));
    // Add the design datasets
    dataflowVO
        .setDesignDatasets(datasetMetabaseController.findDesignDataSetIdByDataflowId(id).stream()
            .filter(dataset -> datasetsIds.contains(dataset.getId())).collect(Collectors.toList()));

    LOG.info("Get the dataflow information with id {}", id);

    return dataflowVO;
  }

  /**
   * Gets the by status.
   *
   * @param status the status
   *
   *
   *
   * @return the by status
   *
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
   *
   * @return the pending accepted
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getPendingAccepted(String userId) throws EEAException {

    // get pending
    List<DataflowWithRequestType> dataflows = dataflowRepository.findPending(userId);
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

    // Get user's dataflows
    dataflowRepository
        .findAllById(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW)
            .stream().map(ResourceAccessVO::getId).collect(Collectors.toList()))
        .forEach(dataflow -> {
          DataFlowVO dataflowVO = dataflowNoContentMapper.entityToClass(dataflow);
          dataflowVO.setUserRequestStatus(TypeRequestEnum.ACCEPTED);
          dataflowVOs.add(dataflowVO);
        });

    return dataflowVOs;
  }


  /**
   * Gets the completed.
   *
   * @param userId the user id
   * @param pageable the pageable
   *
   * @return the completed
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getCompleted(String userId, Pageable pageable) throws EEAException {

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
   *
   * @return the pending by user
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getPendingByUser(String userId, TypeRequestEnum type)
      throws EEAException {

    List<Dataflow> dataflows = dataflowRepository.findByStatusAndUserRequester(type, userId);
    LOG.info("Get the dataflows of the user id: {} with the status {}", userId, type);
    return dataflowNoContentMapper.entityListToClass(dataflows);

  }

  /**
   * Update user request status.
   *
   * @param userRequestId the user request id
   * @param type the type
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateUserRequestStatus(Long userRequestId, TypeRequestEnum type)
      throws EEAException {

    userRequestRepository.updateUserRequestStatus(userRequestId, type.name());
    LOG.info("Update the Metabase request status of the requestId: {}. New status: {}",
        userRequestId, type);
    if (TypeRequestEnum.ACCEPTED.equals(type)) {
      // add the resource to the user id in keycloak
      Long dataflowId = 0L;
      UserRequest ur = userRequestRepository.findById(userRequestId).orElse(new UserRequest());
      if (ur.getDataflows() != null) {
        for (Dataflow df : ur.getDataflows()) {
          dataflowId = df.getId();
        }
        userManagementControllerZull.addContributorToResource(dataflowId,
            ResourceGroupEnum.DATAFLOW_PROVIDER);
        LOG.info("The dataflow {} has been added into keycloak", dataflowId);
      }
    }
  }

  /**
   * Adds the contributor to dataflow.
   *
   * @param idDataflow the id dataflow
   * @param idContributor the id contributor
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void addContributorToDataflow(Long idDataflow, String idContributor) throws EEAException {

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
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void removeContributorFromDataflow(Long idDataflow, String idContributor)
      throws EEAException {

    contributorRepository.removeContributorFromDataset(idDataflow, idContributor);

  }


  /**
   * Creates the data flow.
   *
   * @param dataflowVO the dataflow VO
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void createDataFlow(DataFlowVO dataflowVO) throws EEAException {
    Dataflow dataFlowSaved;
    // we find if the name of this dataflow exist
    if (dataflowRepository.findByName(dataflowVO.getName()).isPresent()) {
      LOG.info("The dataflow: {} already exists.", dataflowVO.getName());
      throw new EEAException(EEAErrorMessage.DATAFLOW_EXISTS_NAME);
    } else {
      dataflowVO.setCreationDate(new Date());
      dataFlowSaved = dataflowRepository.save(dataflowMapper.classToEntity(dataflowVO));
      LOG.info("The dataflow {} has been saved.", dataFlowSaved.getName());
    }
    // With that method we create the group in keycloack
    resourceManagementControllerZull.createResource(createGroup(dataFlowSaved.getId(),
        ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.DATA_CUSTODIAN));

    // // with that service we assing the group created to a user who create it
    userManagementControllerZull.addContributorToResource(dataFlowSaved.getId(),
        ResourceGroupEnum.DATAFLOW_CUSTODIAN);
  }


  /**
   * Update data flow.
   *
   * @param dataflowVO the dataflow VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateDataFlow(DataFlowVO dataflowVO) throws EEAException {

    // we find if the name of this dataflow exist
    if (dataflowRepository.findByName(dataflowVO.getName()).isPresent()) {
      LOG.info("The dataflow: {} already exists.", dataflowVO.getName());
      throw new EEAException(EEAErrorMessage.DATAFLOW_EXISTS_NAME);
    } else {
      Dataflow dataFlowSaved = dataflowRepository.save(dataflowMapper.classToEntity(dataflowVO));
      LOG.info("The dataflow {} has been saved.", dataFlowSaved.getName());
    }
  }

  /**
   * Creates the group.
   *
   * @param datasetId the dataset id
   * @param type the type
   * @param role the role
   * @return the resource info VO
   */
  private ResourceInfoVO createGroup(Long datasetId, ResourceTypeEnum type, SecurityRoleEnum role) {
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(datasetId);
    resourceInfoVO.setResourceTypeEnum(type);
    resourceInfoVO.setSecurityRoleEnum(role);
    return resourceInfoVO;
  }

  /**
   * Gets the datasets id.
   *
   * @param dataschemaId the dataschema id
   * @return the datasets id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataFlowVO getReportingDatasetsId(String dataschemaId) throws EEAException {

    if (dataschemaId == null) {
      throw new EEAException(EEAErrorMessage.SCHEMA_NOT_FOUND);
    }

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO
        .setReportingDatasets(datasetMetabaseController.getReportingsIdBySchemaId(dataschemaId));

    return dataflowVO;
  }


  /**
   * Gets the metabase by id.
   *
   * @param id the id
   *
   * @return the metabase by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataFlowVO getMetabaseById(Long id) throws EEAException {

    if (id == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Dataflow result = dataflowRepository.findById(id).orElse(null);
    DataFlowVO dataflowVO = dataflowNoContentMapper.entityToClass(result);

    LOG.info("Get the dataflow metabaser with id {}", id);

    return dataflowVO;
  }

  /**
   * Delete data flow.
   *
   * @param idDataflow the id dataflow
   * @throws Exception the exception
   */
  @Override
  @Transactional
  public void deleteDataFlow(Long idDataflow) throws Exception {

    // LOAD DATAFLOW DATA
    final DataFlowVO dataflow = getById(idDataflow);
    LOG.info("Get the dataflow metabaser with id {}", idDataflow);

    // // PART DELETE DOCUMENTS
    // for (DocumentVO document : dataflow.getDocuments()) {
    // documentControllerZuul.deleteDocument(document.getId());
    // }
    // LOG.info("Documents deleted to dataflow with id: {}", idDataflow);


    // PART OF DELETE ALL THE DATASETSCHEMA we have in the dataflow
    // if (null != dataflow.getDesignDatasets() || !dataflow.getDesignDatasets().isEmpty()) {
    // for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {
    // dataSetSchemaControllerZuul.deleteDatasetSchema(designDatasetVO.getId());
    // }
    // }
    // LOG.info("Delete full datasetSchemas with dataflow id: {}", idDataflow);


    // add resource to delete(DATAFLOW PART)
    List<ResourceInfoVO> resourceCustodian = resourceManagementControllerZull
        .getGroupsByIdResourceType(idDataflow, ResourceTypeEnum.DATAFLOW);

    // PART OF DELETE ALL THE DATASET we have in the dataflow
    if (null != dataflow.getReportingDatasets() || !dataflow.getReportingDatasets().isEmpty()) {
      dataflow.getReportingDatasets().stream().forEach(datasets -> {
        dataSetControllerZuul.deleteDataset(datasets.getId());
        // add more resource to delete(DATASET PART)
        resourceCustodian.addAll(resourceManagementControllerZull
            .getGroupsByIdResourceType(datasets.getId(), ResourceTypeEnum.DATASET));
      });
    }
    // resourceManagementControllerZull.deleteResource(resourceCustodian);
    // LOG.info("Delete full keycloack data to dataflow with id: {}", idDataflow);



    // we delete the dataflow in metabase at the end
    // dataflowRepository.deleteById(idDataflow);
    // LOG.info("Delete full dataflow with id: {}", idDataflow);

  }



}
