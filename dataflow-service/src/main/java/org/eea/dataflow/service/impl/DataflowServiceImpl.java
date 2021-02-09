package org.eea.dataflow.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.eea.dataflow.mapper.DataflowMapper;
import org.eea.dataflow.mapper.DataflowNoContentMapper;
import org.eea.dataflow.persistence.domain.Contributor;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.DataflowStatusDataset;
import org.eea.dataflow.persistence.domain.UserRequest;
import org.eea.dataflow.persistence.repository.ContributorRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository;
import org.eea.dataflow.persistence.repository.DataflowRepository.IDatasetStatus;
import org.eea.dataflow.persistence.repository.RepresentativeRepository;
import org.eea.dataflow.persistence.repository.UserRequestRepository;
import org.eea.dataflow.service.DataflowService;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.controller.rod.ObligationController;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * The Class DataflowServiceImpl.
 */
@Service("dataflowService")
public class DataflowServiceImpl implements DataflowService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowServiceImpl.class);

  /** The max message length. */
  @Value("${spring.health.db.check.frequency}")
  private int maxMessageLength;

  /** The dataset metabase controller. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The resource management controller zull. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The dataset schema controller zuul. */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaControllerZuul;

  /** The document controller zuul. */
  @Autowired
  private DocumentControllerZuul documentControllerZuul;

  /** The data collection controller zuul. */
  @Autowired
  private DataCollectionControllerZuul dataCollectionControllerZuul;

  /** The obligation controller. */
  @Autowired
  private ObligationController obligationController;

  /** The eu dataset controller zuul. */
  @Autowired
  private EUDatasetControllerZuul euDatasetControllerZuul;

  /** The representative repository. */
  @Autowired
  private RepresentativeRepository representativeRepository;

  /** The dataflow repository. */
  @Autowired
  private DataflowRepository dataflowRepository;

  /** The user request repository. */
  @Autowired
  private UserRequestRepository userRequestRepository;

  /** The contributor repository. */
  @Autowired
  private ContributorRepository contributorRepository;

  /** The dataflow mapper. */
  @Autowired
  private DataflowMapper dataflowMapper;

  /** The dataflow no content mapper. */
  @Autowired
  private DataflowNoContentMapper dataflowNoContentMapper;

  /** The representative service. */
  @Autowired
  private RepresentativeService representativeService;

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
    return getByIdWithCondition(id, true);
  }

  /**
   * Gets the by id with representatives filtered by user email.
   *
   * @param id the id
   * @return the by id with representatives filtered by user email
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataFlowVO getByIdWithRepresentativesFilteredByUserEmail(Long id) throws EEAException {
    return getByIdWithCondition(id, false);
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
   * Gets the dataflows.
   *
   * @param userId the user id
   * @return the dataflows
   * @throws EEAException the EEA exception
   */
  @Override
  public List<DataFlowVO> getDataflows(String userId) throws EEAException {

    List<DataFlowVO> dataflowVOs = new ArrayList<>();

    // Get user's datasets
    Map<Long, List<DataflowStatusDataset>> map = getDatasetsStatus();

    // Get user's dataflows sorted by status and creation date
    dataflowRepository.findByIdInOrderByStatusDescCreationDateDesc(
        userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATAFLOW).stream()
            .map(ResourceAccessVO::getId).collect(Collectors.toList()))
        .forEach(dataflow -> {
          DataFlowVO dataflowVO = dataflowNoContentMapper.entityToClass(dataflow);
          dataflowVO.setUserRequestStatus(TypeRequestEnum.ACCEPTED);
          List<DataflowStatusDataset> datasetsStatusList = map.get(dataflowVO.getId());
          if (!map.isEmpty() && null != datasetsStatusList) {
            setReportingDatasetStatus(datasetsStatusList, dataflowVO);
          }
          dataflowVOs.add(dataflowVO);
        });

    getOpenedObligations(dataflowVOs);

    return dataflowVOs;
  }

  /**
   * Sets the reporting dataset status.
   *
   * @param map the map
   * @param dataflowVO the dataflow VO
   */
  private void setReportingDatasetStatus(List<DataflowStatusDataset> datasetsStatusList,
      DataFlowVO dataflowVO) {
    boolean containsPending = false;
    int releasedCount = 0;
    int techAcceptedCount = 0;
    boolean containsCorrectionR = false;
    boolean containsFinalFeedback = false;
    for (int i = 0; i < datasetsStatusList.size() && !containsPending; i++) {
      switch (datasetsStatusList.get(i).getStatus()) {
        case PENDING:
          containsPending = true;
          break;
        case RELEASED:
          releasedCount++;
          break;
        case CORRECTION_REQUESTED:
          containsCorrectionR = true;
          break;
        case TECHNICALLY_ACCEPTED:
          techAcceptedCount++;
          break;
        case FINAL_FEEDBACK:
          containsFinalFeedback = true;
          break;
        default:
          containsPending = true;
          break;
      }
    }
    if (containsPending) {
      dataflowVO.setReportingStatus(DatasetStatusEnum.PENDING);
    } else {
      if (releasedCount == datasetsStatusList.size()) {
        dataflowVO.setReportingStatus(DatasetStatusEnum.RELEASED);
      } else if (techAcceptedCount == datasetsStatusList.size()) {
        dataflowVO.setReportingStatus(DatasetStatusEnum.TECHNICALLY_ACCEPTED);
      } else if (containsCorrectionR) {
        dataflowVO.setReportingStatus(DatasetStatusEnum.CORRECTION_REQUESTED);
      } else if (containsFinalFeedback) {
        dataflowVO.setReportingStatus(DatasetStatusEnum.FINAL_FEEDBACK);
      }
    }
  }

  /**
   * Gets the datasets status.
   *
   * @return the datasets status
   */
  private Map<Long, List<DataflowStatusDataset>> getDatasetsStatus() {
    Map<Long, List<DataflowStatusDataset>> map = new HashMap<>();

    List<Long> listDatasets =
        userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATASET).stream()
            .map(ResourceAccessVO::getId).collect(Collectors.toList());
    if (!listDatasets.isEmpty()) {
      List<IDatasetStatus> queryResult = dataflowRepository.getDatasetsStatus(listDatasets);
      for (IDatasetStatus object : queryResult) {
        List<DataflowStatusDataset> list2 = new ArrayList<>();
        DataflowStatusDataset dataflowStatusDataset = new DataflowStatusDataset();
        dataflowStatusDataset.setId(object.getId());
        dataflowStatusDataset.setStatus(DatasetStatusEnum.valueOf(object.getStatus()));
        list2.add(dataflowStatusDataset);
        if (map.get(dataflowStatusDataset.getId()) != null) {
          map.get(dataflowStatusDataset.getId()).addAll(list2);
        } else {
          map.put(dataflowStatusDataset.getId(), list2);
        }
      }
    }
    return map;
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
   * @return the pending by user
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
        userManagementControllerZull.addUserToResource(dataflowId,
            ResourceGroupEnum.DATAFLOW_LEAD_REPORTER);
        LOG.info("The dataflow {} has been added into keycloak", dataflowId);
      }
    }
  }

  /**
   * Adds the contributor to dataflow.
   *
   * @param idDataflow the id dataflow
   * @param idContributor the id contributor
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
    if (dataflowRepository.findByNameIgnoreCase(dataflowVO.getName()).isPresent()) {
      LOG.info("The dataflow: {} already exists.", dataflowVO.getName());
      throw new EEAException(EEAErrorMessage.DATAFLOW_EXISTS_NAME);
    } else {
      dataflowVO.setCreationDate(new Date());
      dataflowVO.setStatus(TypeStatusEnum.DESIGN);
      dataFlowSaved = dataflowRepository.save(dataflowMapper.classToEntity(dataflowVO));
      LOG.info("The dataflow {} has been created.", dataFlowSaved.getName());
    }
    // With that method we create the group in keycloack
    resourceManagementControllerZull.createResource(createGroup(dataFlowSaved.getId(),
        ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.DATA_CUSTODIAN));

    resourceManagementControllerZull.createResource(createGroup(dataFlowSaved.getId(),
        ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.LEAD_REPORTER));

    resourceManagementControllerZull.createResource(createGroup(dataFlowSaved.getId(),
        ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.EDITOR_READ));

    resourceManagementControllerZull.createResource(createGroup(dataFlowSaved.getId(),
        ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.EDITOR_WRITE));

    userManagementControllerZull.addUserToResource(dataFlowSaved.getId(),
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

    Optional<Dataflow> dataflow = dataflowRepository.findByNameIgnoreCase(dataflowVO.getName());
    // we find if the name of this dataflow exist
    if (dataflow.isPresent() && !dataflow.get().getId().equals(dataflowVO.getId())) {
      LOG.info("The dataflow: {} already exists.", dataflowVO.getName());
      throw new EEAException(EEAErrorMessage.DATAFLOW_EXISTS_NAME);
    } else {
      Optional<Dataflow> dataflowSave = dataflowRepository.findById(dataflowVO.getId());
      if (dataflowSave.isPresent()) {
        dataflowSave.get().setName(dataflowVO.getName());
        dataflowSave.get().setDescription(dataflowVO.getDescription());
        dataflowSave.get().setObligationId(dataflowVO.getObligation().getObligationId());
        dataflowSave.get().setReleasable(dataflowVO.isReleasable());
        dataflowRepository.save(dataflowSave.get());
        LOG.info("The dataflow {} has been updated.", dataflowSave.get().getName());
      }
    }
  }

  /**
   * Gets the reporting datasets id.
   *
   * @param dataschemaId the dataschema id
   * @return the reporting datasets id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataFlowVO getReportingDatasetsId(String dataschemaId) throws EEAException {

    if (dataschemaId == null) {
      throw new EEAException(EEAErrorMessage.SCHEMA_NOT_FOUND);
    }

    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setReportingDatasets(
        datasetMetabaseControllerZuul.getReportingsIdBySchemaId(dataschemaId));

    return dataflowVO;
  }

  /**
   * Gets the metabase by id.
   *
   * @param id the id
   * @return the metabase by id
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

    LOG.info("Get the dataflow metabase with id {}", id);

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
    // take the jpa entity
    DataFlowVO dataflowVO = getById(idDataflow);
    // use it to take all datasets Desing

    LOG.info("Get the dataflow metabase with id {}", idDataflow);

    // // PART DELETE DOCUMENTS
    if (null != dataflowVO.getDocuments() && !dataflowVO.getDocuments().isEmpty()) {
      deleteDocuments(idDataflow, dataflowVO);
    }

    // PART OF DELETE ALL THE DATASETSCHEMA we have in the dataflow
    if (null != dataflowVO.getDesignDatasets() && !dataflowVO.getDesignDatasets().isEmpty()) {
      deleteDatasetSchemas(idDataflow, dataflowVO);
    }

    // PART OF DELETE ALL THE REPRESENTATIVE we have in the dataflow
    if (null != dataflowVO.getRepresentatives() && !dataflowVO.getRepresentatives().isEmpty()) {
      deleteRepresentatives(dataflowVO);
    }
    try {
      // Delete the dataflow metabase info. Also by the foreign keys of the database, entities like
      // weblinks are also deleted
      dataflowRepository.deleteNativeDataflow(idDataflow);
      LOG.info("Delete full dataflow with id: {}", idDataflow);
    } catch (Exception e) {
      LOG.error("Error deleting dataflow: {}", idDataflow, e);
      throw new EEAException("Error Deleting dataflow ", e);
    }

    // add resource to delete(DATAFLOW PART)
    try {
      List<ResourceInfoVO> resourceCustodian = resourceManagementControllerZull
          .getGroupsByIdResourceType(idDataflow, ResourceTypeEnum.DATAFLOW);
      resourceManagementControllerZull.deleteResource(resourceCustodian);

      LOG.info("Delete full keycloack data to dataflow with id: {}", idDataflow);
    } catch (Exception e) {
      LOG.error("Error deleting resources in keycloack, group with the id: {}", idDataflow, e);
      throw new EEAException("Error deleting resource in keycloack ", e);
    }
  }

  /**
   * Update data flow status.
   *
   * @param id the id
   * @param status the status
   * @param deadlineDate the deadline date
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateDataFlowStatus(Long id, TypeStatusEnum status, Date deadlineDate)
      throws EEAException {
    Optional<Dataflow> dataflow = dataflowRepository.findById(id);
    if (dataflow.isPresent()) {
      dataflow.get().setStatus(status);
      dataflow.get().setDeadlineDate(deadlineDate);
      dataflowRepository.save(dataflow.get());
      LOG.info("The dataflow {} has been saved.", dataflow.get().getName());
    } else {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
  }

  /**
   * Delete documents.
   *
   * @param idDataflow the id dataflow
   * @param dataflowVO the dataflow VO
   * @throws Exception the exception
   */
  private void deleteDocuments(Long idDataflow, DataFlowVO dataflowVO) throws Exception {
    for (DocumentVO document : dataflowVO.getDocuments()) {
      try {
        documentControllerZuul.deleteDocument(document.getId(), Boolean.TRUE);
      } catch (EEAException e) {
        LOG.error("Error deleting document with id {}", document.getId());
        throw new EEAException(new StringBuilder().append("Error Deleting document ")
            .append(document.getName()).append(" with ").append(document.getId()).toString(), e);
      }
    }
    LOG.info("Documents deleted to dataflow with id: {}", idDataflow);
  }

  /**
   * Delete dataset schemas.
   *
   * @param idDataflow the id dataflow
   * @param dataflowVO the dataflow VO
   * @throws EEAException the EEA exception
   */
  private void deleteDatasetSchemas(Long idDataflow, DataFlowVO dataflowVO) throws EEAException {
    for (DesignDatasetVO designDatasetVO : dataflowVO.getDesignDatasets()) {
      try {
        datasetSchemaControllerZuul.deleteDatasetSchema(designDatasetVO.getId(), true);
      } catch (Exception e) {
        LOG.error("Error deleting DesignDataset with id {}", designDatasetVO.getId(), e);
        throw new EEAException(new StringBuilder().append("Error Deleting dataset ")
            .append(designDatasetVO.getDataSetName()).append(" with ")
            .append(designDatasetVO.getId()).toString(), e);
      }
    }
    LOG.info("Delete full datasetSchemas with dataflow id: {}", idDataflow);
  }

  /**
   * Delete representatives.
   *
   * @param dataflowVO the dataflow VO
   * @throws EEAException the EEA exception
   */
  private void deleteRepresentatives(DataFlowVO dataflowVO) throws EEAException {
    for (RepresentativeVO representative : dataflowVO.getRepresentatives()) {
      try {
        representativeRepository.deleteById(representative.getId());
      } catch (Exception e) {
        LOG.error("Error deleting representative with id {}", representative.getId(), e);
        throw new EEAException(new StringBuilder().append("Error Deleting representative")
            .append(" with id ").append(representative.getId()).toString(), e);
      }
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
   * Gets the obligation.
   *
   * @param dataflow the dataflow
   * @return the obligation
   */
  private void getObligation(DataFlowVO dataflow) {
    // Get the obligationVO from ROD and Set in dataflow VO
    // We check that the field is not empty to avoid the call to rod due to maintain backward
    // compatibility concerns
    if (dataflow.getObligation() != null && dataflow.getObligation().getObligationId() != null) {
      dataflow.setObligation(
          obligationController.findObligationById(dataflow.getObligation().getObligationId()));
    }
  }

  /**
   * Gets the by id with condition.
   *
   * @param id the id
   * @param includeAllRepresentatives the include all representatives
   * @return the by id with condition
   * @throws EEAException the EEA exception
   */
  private DataFlowVO getByIdWithCondition(Long id, boolean includeAllRepresentatives)
      throws EEAException {

    if (id == null) {
      throw new EEAException(EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    Dataflow result = dataflowRepository.findById(id).orElse(null);
    // filter datasets showed to the user depending on permissions
    List<ResourceAccessVO> datasets =
        userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATASET);
    // add to the filter the design datasets (data schemas) too
    datasets.addAll(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATA_SCHEMA));
    // also, add to the filter the data collection
    datasets
        .addAll(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.DATA_COLLECTION));
    // and the eu datasets
    datasets.addAll(userManagementControllerZull.getResourcesByUser(ResourceTypeEnum.EU_DATASET));
    List<Long> datasetsIds =
        datasets.stream().map(ResourceAccessVO::getId).collect(Collectors.toList());
    DataFlowVO dataflowVO = dataflowMapper.entityToClass(result);
    dataflowVO.setReportingDatasets(
        datasetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(id).stream()
            .filter(dataset -> datasetsIds.contains(dataset.getId())).collect(Collectors.toList()));
    // Add the design datasets
    dataflowVO.setDesignDatasets(
        datasetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(id).stream()
            .filter(dataset -> datasetsIds.contains(dataset.getId())).collect(Collectors.toList()));

    // Add the data collections
    dataflowVO.setDataCollections(
        dataCollectionControllerZuul.findDataCollectionIdByDataflowId(id).stream()
            .filter(dataset -> datasetsIds.contains(dataset.getId())).collect(Collectors.toList()));

    // Add the EU datasets
    dataflowVO.setEuDatasets(euDatasetControllerZuul.findEUDatasetByDataflowId(id).stream()
        .filter(dataset -> datasetsIds.contains(dataset.getId())).collect(Collectors.toList()));

    // Add the representatives
    if (includeAllRepresentatives) {
      dataflowVO.setRepresentatives(representativeService.getRepresetativesByIdDataFlow(id));
    } else {
      String userId = ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication()
          .getDetails()).get(AuthenticationDetails.USER_ID);
      UserRepresentationVO user = userManagementControllerZull.getUserByUserId(userId);
      dataflowVO.setRepresentatives(
          representativeService.getRepresetativesByDataflowIdAndEmail(id, user.getEmail()));
    }

    getObligation(dataflowVO);

    // we sort the weblinks and documents
    if (!CollectionUtils.isEmpty(dataflowVO.getWeblinks())) {
      dataflowVO.getWeblinks()
          .sort(Comparator.comparing(WeblinkVO::getDescription, String.CASE_INSENSITIVE_ORDER));
    }
    if (!CollectionUtils.isEmpty(dataflowVO.getDocuments())) {
      dataflowVO.getDocuments()
          .sort(Comparator.comparing(DocumentVO::getName, String.CASE_INSENSITIVE_ORDER));
    }
    LOG.info("Get the dataflow information with id {}", id);

    return dataflowVO;
  }

  /**
   * Gets the opened obligations.
   *
   * @param dataflowVOs the dataflow V os
   * @return the opened obligations
   */
  private void getOpenedObligations(List<DataFlowVO> dataflowVOs) {

    // Get all opened obligations from ROD
    List<ObligationVO> obligations =
        obligationController.findOpenedObligations(null, null, null, null, null);

    Map<Integer, ObligationVO> obligationMap = obligations.stream()
        .collect(Collectors.toMap(ObligationVO::getObligationId, obligation -> obligation));

    for (DataFlowVO dataFlowVO : dataflowVOs) {
      if (dataFlowVO.getObligation() != null
          && dataFlowVO.getObligation().getObligationId() != null) {
        dataFlowVO.setObligation(obligationMap.get(dataFlowVO.getObligation().getObligationId()));
      }
    }
  }
}
