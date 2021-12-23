package org.eea.dataflow.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eea.dataflow.persistence.domain.TempUser;
import org.eea.dataflow.persistence.repository.TempUserRepository;
import org.eea.dataflow.service.ContributorService;
import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.TestDatasetController.TestDatasetControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.utils.LiteralConstants;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class ContributorServiceImpl.
 */
@Service
public class ContributorServiceImpl implements ContributorService {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ContributorServiceImpl.class);
  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The resource management controller zull. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The data set metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The data collection controller zuul. */
  @Autowired
  private DataCollectionControllerZuul dataCollectionControllerZuul;

  /** The EU dataset controller zuul. */
  @Autowired
  private EUDatasetControllerZuul eUDatasetControllerZuul;

  /** The test dataset controller zuul. */
  @Autowired
  private TestDatasetControllerZuul testDatasetControllerZuul;

  /** The reference dataset controller zuul. */
  @Autowired
  private ReferenceDatasetControllerZuul referenceDatasetControllerZuul;

  /** The dataflow service. */
  @Autowired
  @Lazy
  private DataflowService dataflowService;

  @Autowired
  private TempUserRepository tempUserRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;


  /**
   * Find contributors by id dataflow.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param role the role
   * @return the list
   */
  @Override
  public List<ContributorVO> findContributorsByResourceId(Long dataflowId, Long providerId,
      String role) {
    List<ContributorVO> contributorVOList = new ArrayList<>();

    if (LiteralConstants.REPORTER.equals(role)) {
      Long referenceId = dataSetMetabaseControllerZuul
          .findReportingDataSetIdByDataflowId(dataflowId).stream()
          .filter(reportingDatasetVO -> providerId.equals(reportingDatasetVO.getDataProviderId()))
          .map(ReportingDatasetVO::getId).findFirst().orElse(null);
      String resource = "Dataset-";

      getContributorList(SecurityRoleEnum.REPORTER_READ.toString(), contributorVOList, referenceId,
          resource);
      getContributorList(SecurityRoleEnum.REPORTER_WRITE.toString(), contributorVOList, referenceId,
          resource);
    }
    if (LiteralConstants.REQUESTER.equals(role)) {
      String resource = "Dataflow-";
      getContributorList(SecurityRoleEnum.EDITOR_READ.toString(), contributorVOList, dataflowId,
          resource);
      getContributorList(SecurityRoleEnum.EDITOR_WRITE.toString(), contributorVOList, dataflowId,
          resource);
      getContributorList(SecurityRoleEnum.DATA_OBSERVER.toString(), contributorVOList, dataflowId,
          resource);
      getContributorList(SecurityRoleEnum.DATA_CUSTODIAN.toString(), contributorVOList, dataflowId,
          resource);
      getContributorList(SecurityRoleEnum.DATA_STEWARD.toString(), contributorVOList, dataflowId,
          resource);
      getContributorList(SecurityRoleEnum.CUSTODIAN_SUPPORT.toString(), contributorVOList,
          dataflowId, resource);
    }

    return contributorVOList;
  }

  /**
   * Find temp user by account and dataflow.
   *
   * @param account the account
   * @param dataflowId the dataflow id
   * @return the contributor VO
   */
  @Override
  public ContributorVO findTempUserByAccountAndDataflow(String account, Long dataflowId,
      Long dataProviderId) {

    TempUser foundUser =
        tempUserRepository.findTempUserByAccountAndDataflow(account, dataflowId, dataProviderId);
    ContributorVO contributor;

    if (foundUser != null) {
      contributor = new ContributorVO();
      contributor.setAccount(foundUser.getEmail());
      contributor.setRole(foundUser.getRole());
      contributor.setDataProviderId(foundUser.getDataProviderId());
    } else {
      contributor = null;
    }

    return contributor;

  }

  /**
   * Find temp user by role and dataflow.
   *
   * @param role the role
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  public List<ContributorVO> findTempUserByRoleAndDataflow(String role, Long dataflowId,
      Long dataProviderId) {

    List<TempUser> foundUsers =
        tempUserRepository.findTempUserByRoleAndDataflow(role, dataflowId, dataProviderId);
    List<ContributorVO> contributors = new ArrayList<>();

    for (TempUser tempuser : foundUsers) {
      ContributorVO newContributor = new ContributorVO();
      newContributor.setAccount(tempuser.getEmail());
      newContributor.setRole(tempuser.getRole());
      newContributor.setDataProviderId(tempuser.getDataProviderId());
      newContributor.setInvalid(true);
      contributors.add(newContributor);
    }

    return contributors;
  }

  /**
   * Gets the contributor list.
   *
   * @param role the role
   * @param contributorVOList the contributor VO list
   * @param referenceId the reference id
   * @param resource the resource
   * @return the contributor list
   */
  private void getContributorList(String role, List<ContributorVO> contributorVOList,
      Long referenceId, String resource) {
    StringBuilder stringBuilder =
        new StringBuilder(resource).append(referenceId).append("-").append(role);
    List<UserRepresentationVO> listUserWrite =
        userManagementControllerZull.getUsersByGroup(stringBuilder.toString());
    if (!CollectionUtils.isEmpty(listUserWrite)) {
      listUserWrite.stream().forEach(userWrite -> {
        ContributorVO contributorVO = new ContributorVO();
        contributorVO.setAccount(userWrite.getEmail());
        contributorVO.setRole(role);
        contributorVOList.add(contributorVO);
      });
    }
  }

  /**
   * Delete contributor.
   *
   * @param dataflowId the dataflow id
   * @param account the account
   * @param role the role
   * @param dataProviderId the data provider id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteContributor(Long dataflowId, String account, String role, Long dataProviderId)
      throws EEAException {

    List<ResourceAssignationVO> resourcesProviders = new ArrayList<>();
    switch (SecurityRoleEnum.valueOf(role)) {
      case EDITOR_READ:
      case EDITOR_WRITE:
        getResourceEditors(dataflowId, account, resourcesProviders);
        break;
      case REPORTER_READ:
      case REPORTER_WRITE:
        getResourceReporters(dataflowId, account, role, dataProviderId, resourcesProviders);
        break;
      case DATA_OBSERVER:
      case CUSTODIAN_SUPPORT:
        getResourceObserverCustodianSupport(dataflowId, account, resourcesProviders, role);
        break;
      case DATA_CUSTODIAN:
      case DATA_STEWARD:
        if (dataflowService.isAdmin()) {
          getResourceCustodianSteward(dataflowId, account, resourcesProviders, role);
          break;
        } else {
          throw new EEAException();
        }
      default:
        break;
    }
    LOG.info("For the account:{} in Dataflow {} the following resources are going to be deleted:{}",
        account, dataflowId, resourcesProviders);
    userManagementControllerZull.removeContributorsFromResources(resourcesProviders);
  }

  /**
   * Gets the resource reporters.
   *
   * @param dataflowId the dataflow id
   * @param account the account
   * @param role the role
   * @param dataProviderId the data provider id
   * @param resourcesProviders the resources providers
   * @return the resource reporters
   */
  private void getResourceReporters(Long dataflowId, String account, String role,
      Long dataProviderId, List<ResourceAssignationVO> resourcesProviders) {
    List<Long> ids = new ArrayList<>();
    List<ContributorVO> contributors =
        findContributorsByResourceId(dataflowId, dataProviderId, LiteralConstants.REPORTER);
    if (contributors != null) {
      if (SecurityRoleEnum.REPORTER_READ.toString().equals(role)) {
        resourcesProviders.add(
            fillResourceAssignation(dataflowId, account, ResourceGroupEnum.DATAFLOW_REPORTER_READ));
      } else if (SecurityRoleEnum.REPORTER_WRITE.toString().equals(role)) {
        resourcesProviders.add(fillResourceAssignation(dataflowId, account,
            ResourceGroupEnum.DATAFLOW_REPORTER_WRITE));
      }
    }
    resourcesProviders.add(
        fillResourceAssignation(dataflowId, account, ResourceGroupEnum.DATASCHEMA_REPORTER_READ));

    ids = dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId).stream()
        .filter(reportingDatasetVO -> dataProviderId.equals(reportingDatasetVO.getDataProviderId()))
        .map(ReportingDatasetVO::getId).collect(Collectors.toList());

    // reference
    addResources(account, resourcesProviders,
        referenceDatasetControllerZuul.findReferenceDatasetByDataflowId(dataflowId).stream()
            .map(ReferenceDatasetVO::getId).collect(Collectors.toList()),
        ResourceGroupEnum.REFERENCEDATASET_CUSTODIAN);

    for (Long id : ids) {
      // remove resources
      resourcesProviders
          .add(fillResourceAssignation(id, account, ResourceGroupEnum.DATASET_REPORTER_WRITE));
      resourcesProviders
          .add(fillResourceAssignation(id, account, ResourceGroupEnum.DATASET_REPORTER_READ));
    }
  }

  /**
   * Gets the resource editors.
   *
   * @param dataflowId the dataflow id
   * @param account the account
   * @param resourcesProviders the resources providers
   * @return the resource editors
   */
  private void getResourceEditors(Long dataflowId, String account,
      List<ResourceAssignationVO> resourcesProviders) {
    List<Long> ids = new ArrayList<>();
    resourcesProviders
        .add(fillResourceAssignation(dataflowId, account, ResourceGroupEnum.DATAFLOW_EDITOR_WRITE));
    resourcesProviders
        .add(fillResourceAssignation(dataflowId, account, ResourceGroupEnum.DATAFLOW_EDITOR_READ));

    ids = dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(dataflowId).stream()
        .map(DesignDatasetVO::getId).collect(Collectors.toList());
    for (Long id : ids) {
      // remove resources
      resourcesProviders
          .add(fillResourceAssignation(id, account, ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE));
      resourcesProviders
          .add(fillResourceAssignation(id, account, ResourceGroupEnum.DATASCHEMA_EDITOR_READ));
    }
  }

  /**
   * Gets the resource observer.
   *
   * @param dataflowId the dataflow id
   * @param account the account
   * @param resourcesProviders the resources providers
   * @return the resource observer
   */
  private void getResourceObserverCustodianSupport(Long dataflowId, String account,
      List<ResourceAssignationVO> resourcesProviders, String role) {
    boolean isObserver = SecurityRoleEnum.DATA_OBSERVER.toString().equals(role);
    resourcesProviders.add(fillResourceAssignation(dataflowId, account,
        isObserver ? ResourceGroupEnum.DATAFLOW_OBSERVER
            : ResourceGroupEnum.DATAFLOW_CUSTODIAN_SUPPORT));
    // dataset
    addResources(account, resourcesProviders,
        dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId).stream()
            .map(ReportingDatasetVO::getId).collect(Collectors.toList()),
        isObserver ? ResourceGroupEnum.DATASET_OBSERVER
            : ResourceGroupEnum.DATASET_CUSTODIAN_SUPPORT);
    // dc
    addResources(account, resourcesProviders,
        dataCollectionControllerZuul.findDataCollectionIdByDataflowId(dataflowId).stream()
            .map(DataCollectionVO::getId).collect(Collectors.toList()),
        isObserver ? ResourceGroupEnum.DATACOLLECTION_OBSERVER
            : ResourceGroupEnum.DATACOLLECTION_CUSTODIAN_SUPPORT);
    // eu
    addResources(account, resourcesProviders,
        eUDatasetControllerZuul.findEUDatasetByDataflowId(dataflowId).stream()
            .map(EUDatasetVO::getId).collect(Collectors.toList()),
        isObserver ? ResourceGroupEnum.EUDATASET_OBSERVER
            : ResourceGroupEnum.EUDATASET_CUSTODIAN_SUPPORT);

    // reference
    addResources(account, resourcesProviders,
        referenceDatasetControllerZuul.findReferenceDatasetByDataflowId(dataflowId).stream()
            .map(ReferenceDatasetVO::getId).collect(Collectors.toList()),
        isObserver ? ResourceGroupEnum.REFERENCEDATASET_OBSERVER
            : ResourceGroupEnum.REFERENCEDATASET_CUSTODIAN);

    if (!isObserver) {
      // testdataset
      addResources(account, resourcesProviders,
          referenceDatasetControllerZuul.findReferenceDatasetByDataflowId(dataflowId).stream()
              .map(ReferenceDatasetVO::getId).collect(Collectors.toList()),
          ResourceGroupEnum.TESTDATASET_CUSTODIAN_SUPPORT);
    }
  }


  /**
   * Gets the resource custodian steward.
   *
   * @param dataflowId the dataflow id
   * @param account the account
   * @param resourcesProviders the resources providers
   * @param role the role
   * @return the resource custodian steward
   */
  private void getResourceCustodianSteward(Long dataflowId, String account,
      List<ResourceAssignationVO> resourcesProviders, String role) {
    resourcesProviders.add(fillResourceAssignation(dataflowId, account,
        SecurityRoleEnum.DATA_CUSTODIAN.toString().equals(role)
            ? ResourceGroupEnum.DATAFLOW_CUSTODIAN
            : ResourceGroupEnum.DATAFLOW_STEWARD));
    // dataset
    addResources(account, resourcesProviders,
        dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId).stream()
            .map(ReportingDatasetVO::getId).collect(Collectors.toList()),
        SecurityRoleEnum.DATA_CUSTODIAN.toString().equals(role)
            ? ResourceGroupEnum.DATASET_CUSTODIAN
            : ResourceGroupEnum.DATASET_STEWARD);
    // dc
    addResources(account, resourcesProviders,
        dataCollectionControllerZuul.findDataCollectionIdByDataflowId(dataflowId).stream()
            .map(DataCollectionVO::getId).collect(Collectors.toList()),
        SecurityRoleEnum.DATA_CUSTODIAN.toString().equals(role)
            ? ResourceGroupEnum.DATACOLLECTION_CUSTODIAN
            : ResourceGroupEnum.DATACOLLECTION_STEWARD);
    // eu
    addResources(account, resourcesProviders,
        eUDatasetControllerZuul.findEUDatasetByDataflowId(dataflowId).stream()
            .map(EUDatasetVO::getId).collect(Collectors.toList()),
        SecurityRoleEnum.DATA_CUSTODIAN.toString().equals(role)
            ? ResourceGroupEnum.EUDATASET_CUSTODIAN
            : ResourceGroupEnum.EUDATASET_STEWARD);

    // reference
    addResources(account, resourcesProviders,
        referenceDatasetControllerZuul.findReferenceDatasetByDataflowId(dataflowId).stream()
            .map(ReferenceDatasetVO::getId).collect(Collectors.toList()),
        SecurityRoleEnum.DATA_CUSTODIAN.toString().equals(role)
            ? ResourceGroupEnum.REFERENCEDATASET_CUSTODIAN
            : ResourceGroupEnum.REFERENCEDATASET_STEWARD);

    // design datasets
    addResources(account, resourcesProviders,
        referenceDatasetControllerZuul.findReferenceDatasetByDataflowId(dataflowId).stream()
            .map(ReferenceDatasetVO::getId).collect(Collectors.toList()),
        SecurityRoleEnum.DATA_CUSTODIAN.toString().equals(role)
            ? ResourceGroupEnum.REFERENCEDATASET_OBSERVER
            : ResourceGroupEnum.REFERENCEDATASET_STEWARD);
  }

  /**
   * Adds the resources.
   *
   * @param account the account
   * @param resourcesProviders the resources providers
   * @param ids the ids
   * @param resourceGroup the resource group
   */
  private void addResources(String account, List<ResourceAssignationVO> resourcesProviders,
      List<Long> ids, ResourceGroupEnum resourceGroup) {
    for (Long id : ids) {
      // remove resources
      resourcesProviders.add(fillResourceAssignation(id, account, resourceGroup));
    }
  }



  /**
   * Creates the contributor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param dataProviderId the data provider id
   * @param persistDataflowPermission the persist dataflow permission
   * @throws EEAException the EEA exception
   */
  @Override
  public void createContributor(Long dataflowId, ContributorVO contributorVO, Long dataProviderId,
      Boolean persistDataflowPermission) throws EEAException {
    LOG.info("Initiating the creation of a contributing user for the following account:{}",
        contributorVO.getAccount());
    final List<ResourceAssignationVO> resourceAssignationVOList = new ArrayList<>();
    List<ResourceInfoVO> resourceInfoVOs = new ArrayList<>();

    switch (SecurityRoleEnum.valueOf(contributorVO.getRole())) {
      case EDITOR_READ:
        createEditorGroupsResources(dataflowId, contributorVO,
            ResourceGroupEnum.DATASCHEMA_EDITOR_READ, ResourceGroupEnum.DATAFLOW_EDITOR_READ,
            resourceAssignationVOList);
        break;
      case EDITOR_WRITE:
        createEditorGroupsResources(dataflowId, contributorVO,
            ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE, ResourceGroupEnum.DATAFLOW_EDITOR_WRITE,
            resourceAssignationVOList);
        break;
      case REPORTER_READ:
        createReporterGroupsResources(dataflowId, contributorVO, dataProviderId,
            SecurityRoleEnum.REPORTER_READ, ResourceGroupEnum.DATASCHEMA_REPORTER_READ,
            ResourceGroupEnum.DATAFLOW_REPORTER_READ, ResourceGroupEnum.DATASET_REPORTER_READ,
            resourceAssignationVOList, resourceInfoVOs, persistDataflowPermission);
        break;
      case REPORTER_WRITE:
        createReporterGroupsResources(dataflowId, contributorVO, dataProviderId,
            SecurityRoleEnum.REPORTER_WRITE, ResourceGroupEnum.DATASCHEMA_REPORTER_READ,
            ResourceGroupEnum.DATAFLOW_REPORTER_WRITE, ResourceGroupEnum.DATASET_REPORTER_WRITE,
            resourceAssignationVOList, resourceInfoVOs, persistDataflowPermission);
        break;
      case DATA_OBSERVER:
        createRequesterGroupsResources(dataflowId, contributorVO, dataProviderId,
            resourceAssignationVOList, resourceInfoVOs, SecurityRoleEnum.DATA_OBSERVER,
            ResourceGroupEnum.DATAFLOW_OBSERVER, ResourceGroupEnum.DATASET_OBSERVER,
            ResourceGroupEnum.DATACOLLECTION_OBSERVER, ResourceGroupEnum.EUDATASET_OBSERVER, null,
            null, ResourceGroupEnum.REFERENCEDATASET_OBSERVER);
        break;
      case DATA_CUSTODIAN:
        createRequesterGroupsResources(dataflowId, contributorVO, dataProviderId,
            resourceAssignationVOList, resourceInfoVOs, SecurityRoleEnum.DATA_CUSTODIAN,
            ResourceGroupEnum.DATAFLOW_CUSTODIAN, ResourceGroupEnum.DATASET_CUSTODIAN,
            ResourceGroupEnum.DATACOLLECTION_CUSTODIAN, ResourceGroupEnum.EUDATASET_CUSTODIAN,
            ResourceGroupEnum.TESTDATASET_CUSTODIAN, ResourceGroupEnum.DATASCHEMA_CUSTODIAN,
            ResourceGroupEnum.REFERENCEDATASET_CUSTODIAN);
        break;
      case DATA_STEWARD:
        createRequesterGroupsResources(dataflowId, contributorVO, dataProviderId,
            resourceAssignationVOList, resourceInfoVOs, SecurityRoleEnum.DATA_STEWARD,
            ResourceGroupEnum.DATAFLOW_STEWARD, ResourceGroupEnum.DATASET_STEWARD,
            ResourceGroupEnum.DATACOLLECTION_STEWARD, ResourceGroupEnum.EUDATASET_STEWARD,
            ResourceGroupEnum.TESTDATASET_STEWARD, ResourceGroupEnum.DATASCHEMA_STEWARD,
            ResourceGroupEnum.REFERENCEDATASET_STEWARD);
        break;
      case CUSTODIAN_SUPPORT:
        createRequesterGroupsResources(dataflowId, contributorVO, dataProviderId,
            resourceAssignationVOList, resourceInfoVOs, SecurityRoleEnum.CUSTODIAN_SUPPORT,
            ResourceGroupEnum.DATAFLOW_CUSTODIAN_SUPPORT,
            ResourceGroupEnum.DATASET_CUSTODIAN_SUPPORT,
            ResourceGroupEnum.DATACOLLECTION_CUSTODIAN_SUPPORT,
            ResourceGroupEnum.EUDATASET_CUSTODIAN_SUPPORT,
            ResourceGroupEnum.TESTDATASET_CUSTODIAN_SUPPORT, null,
            ResourceGroupEnum.REFERENCEDATASET_CUSTODIAN);
        break;
      default:
        break;
    }

    // we add data to contributor
    userManagementControllerZull.addContributorsToResources(resourceAssignationVOList);
  }


  /**
   * Creates the editor groups resources.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param resourceGroupEnum the resource group enum
   * @param resourceGroupEnumDataflow the resource group enum dataflow
   * @param resourceAssignationVOList the resource assignation VO list
   */
  private void createEditorGroupsResources(Long dataflowId, ContributorVO contributorVO,
      ResourceGroupEnum resourceGroupEnum, ResourceGroupEnum resourceGroupEnumDataflow,
      final List<ResourceAssignationVO> resourceAssignationVOList) {
    resourceAssignationVOList.add(
        fillResourceAssignation(dataflowId, contributorVO.getAccount(), resourceGroupEnumDataflow));
    for (DesignDatasetVO designDatasetVO : dataSetMetabaseControllerZuul
        .findDesignDataSetIdByDataflowId(dataflowId)) {

      resourceAssignationVOList.add(fillResourceAssignation(designDatasetVO.getId(),
          contributorVO.getAccount(), resourceGroupEnum));
    }
  }

  /**
   * Creates the reporter groups resources.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param dataProviderId the data provider id
   * @param securityRoleEnum the security role enum
   * @param resourceGroupEnum the resource group enum
   * @param resourceGroupEnumDataflow the resource group enum dataflow
   * @param resourceGroupEnumDataset the resource group enum dataset
   * @param resourceAssignationVOList the resource assignation VO list
   * @param resourceInfoVOs the resource info V os
   * @param persistDataflowPermission the persist dataflow permission
   */
  private void createReporterGroupsResources(Long dataflowId, ContributorVO contributorVO,
      Long dataProviderId, SecurityRoleEnum securityRoleEnum, ResourceGroupEnum resourceGroupEnum,
      ResourceGroupEnum resourceGroupEnumDataflow, ResourceGroupEnum resourceGroupEnumDataset,
      final List<ResourceAssignationVO> resourceAssignationVOList,
      List<ResourceInfoVO> resourceInfoVOs, Boolean persistDataflowPermission) {

    contributorVO.setAccount(contributorVO.getAccount().toLowerCase());

    ResourceInfoVO resourceDataflow =
        resourceManagementControllerZull.getResourceDetail(dataflowId, resourceGroupEnumDataflow);
    LOG.info("Dataflow {} resources:¨{}", dataflowId, resourceDataflow);
    if (null == resourceDataflow.getName()) {
      resourceInfoVOs.add(createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, securityRoleEnum));
      LOG.info("Do dataflow {} permissions need to persist?: {}", dataflowId,
          persistDataflowPermission);
      if (Boolean.TRUE.equals(persistDataflowPermission)) {
        resourceInfoVOs.add(createGroup(dataflowId, ResourceTypeEnum.DATAFLOW,
            SecurityRoleEnum.REPORTER_READ.equals(securityRoleEnum) ? SecurityRoleEnum.REPORTER_READ
                : SecurityRoleEnum.REPORTER_WRITE));
      }
    }
    resourceAssignationVOList.add(
        fillResourceAssignation(dataflowId, contributorVO.getAccount(), resourceGroupEnumDataflow));

    for (Long reportingDatasetId : dataSetMetabaseControllerZuul
        .findReportingDataSetIdByDataflowId(dataflowId).stream()
        .filter(reportingDatasetVO -> dataProviderId.equals(reportingDatasetVO.getDataProviderId()))
        .map(ReportingDatasetVO::getId).collect(Collectors.toList())) {
      ResourceInfoVO resourceDataSchema =
          resourceManagementControllerZull.getResourceDetail(reportingDatasetId, resourceGroupEnum);
      if (null == resourceDataSchema.getName()) {
        resourceInfoVOs
            .add(createGroup(reportingDatasetId, ResourceTypeEnum.DATASET, securityRoleEnum));
      }

      createGroupList(dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs,
          ResourceGroupEnum.REFERENCEDATASET_CUSTODIAN, SecurityRoleEnum.DATA_CUSTODIAN,
          referenceDatasetControllerZuul.findReferenceDatasetByDataflowId(dataflowId).stream()
              .map(ReferenceDatasetVO::getId).collect(Collectors.toList()),
          ResourceTypeEnum.REFERENCE_DATASET);

      resourceAssignationVOList.add(fillResourceAssignation(reportingDatasetId,
          contributorVO.getAccount(), resourceGroupEnum));
      resourceAssignationVOList.add(fillResourceAssignation(reportingDatasetId,
          contributorVO.getAccount(), resourceGroupEnumDataset));
    }


    // Resources creation
    resourceManagementControllerZull.createResources(resourceInfoVOs);
  }

  /**
   * Creates the requester groups resources.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param dataProviderId the data provider id
   * @param resourceAssignationVOList the resource assignation VO list
   * @param resourceInfoVOs the resource info V os
   * @param securityRole the security role
   * @param resourceGroupDataflow the resource group dataflow
   * @param resourceGroupDataset the resource group dataset
   * @param resourceGroupDC the resource group DC
   * @param resourceGroupEU the resource group EU
   * @param resourceGroupTest the resource group test
   * @param resourceGroupSchema the resource group schema
   * @param resourceGroupReference the resource group reference
   */
  private void createRequesterGroupsResources(Long dataflowId, ContributorVO contributorVO,
      Long dataProviderId, final List<ResourceAssignationVO> resourceAssignationVOList,
      List<ResourceInfoVO> resourceInfoVOs, SecurityRoleEnum securityRole,
      ResourceGroupEnum resourceGroupDataflow, ResourceGroupEnum resourceGroupDataset,
      ResourceGroupEnum resourceGroupDC, ResourceGroupEnum resourceGroupEU,
      ResourceGroupEnum resourceGroupTest, ResourceGroupEnum resourceGroupSchema,
      ResourceGroupEnum resourceGroupReference) {

    contributorVO.setAccount(contributorVO.getAccount().toLowerCase());

    ResourceInfoVO resourceDataflow =
        resourceManagementControllerZull.getResourceDetail(dataflowId, resourceGroupDataflow);
    if (null == resourceDataflow.getName()) {
      resourceInfoVOs.add(createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, securityRole));
    }
    resourceAssignationVOList.add(
        fillResourceAssignation(dataflowId, contributorVO.getAccount(), resourceGroupDataflow));

    // dataset
    createGroupList(
        dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs, resourceGroupDataset,
        securityRole, dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId)
            .stream().map(ReportingDatasetVO::getId).collect(Collectors.toList()),
        ResourceTypeEnum.DATASET);
    // DC
    createGroupList(dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs,
        resourceGroupDC, securityRole,
        dataCollectionControllerZuul.findDataCollectionIdByDataflowId(dataflowId).stream()
            .map(DataCollectionVO::getId).collect(Collectors.toList()),
        ResourceTypeEnum.DATA_COLLECTION);
    // EU
    createGroupList(dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs,
        resourceGroupEU, securityRole, eUDatasetControllerZuul.findEUDatasetByDataflowId(dataflowId)
            .stream().map(EUDatasetVO::getId).collect(Collectors.toList()),
        ResourceTypeEnum.EU_DATASET);

    // REFERENCE
    createGroupList(dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs,
        resourceGroupReference, securityRole,
        referenceDatasetControllerZuul.findReferenceDatasetByDataflowId(dataflowId).stream()
            .map(ReferenceDatasetVO::getId).collect(Collectors.toList()),
        ResourceTypeEnum.REFERENCE_DATASET);

    if (SecurityRoleEnum.DATA_CUSTODIAN.equals(securityRole)
        || SecurityRoleEnum.DATA_STEWARD.equals(securityRole)
        || SecurityRoleEnum.CUSTODIAN_SUPPORT.equals(securityRole)) {
      // test
      createGroupList(dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs,
          resourceGroupTest, securityRole,
          testDatasetControllerZuul.findTestDatasetByDataflowId(dataflowId).stream()
              .map(TestDatasetVO::getId).collect(Collectors.toList()),
          ResourceTypeEnum.TEST_DATASET);
      if (!SecurityRoleEnum.CUSTODIAN_SUPPORT.equals(securityRole)) {
        // schemas
        createGroupList(dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs,
            resourceGroupSchema, securityRole,
            dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(dataflowId).stream()
                .map(DesignDatasetVO::getId).collect(Collectors.toList()),
            ResourceTypeEnum.DATA_SCHEMA);
      }
    }

    // Resources creation
    resourceManagementControllerZull.createResources(resourceInfoVOs);
  }

  /**
   * Creates the group list.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param resourceAssignationVOList the resource assignation VO list
   * @param resourceInfoVOs the resource info V os
   * @param resourceGroupEnum the resource group enum
   * @param securityRoleEnum the security role enum
   * @param idList the id list
   * @param resourceTypeEnum the resource type enum
   */
  private void createGroupList(Long dataflowId, ContributorVO contributorVO,
      final List<ResourceAssignationVO> resourceAssignationVOList,
      List<ResourceInfoVO> resourceInfoVOs, ResourceGroupEnum resourceGroupEnum,
      SecurityRoleEnum securityRoleEnum, List<Long> idList, ResourceTypeEnum resourceTypeEnum) {
    for (Long reportingDatasetId : idList) {
      ResourceInfoVO resourceData =
          resourceManagementControllerZull.getResourceDetail(reportingDatasetId, resourceGroupEnum);
      if (null == resourceData || null == resourceData.getName()) {
        resourceInfoVOs.add(createGroup(reportingDatasetId, resourceTypeEnum, securityRoleEnum));
      }
      resourceAssignationVOList.add(fillResourceAssignation(reportingDatasetId,
          contributorVO.getAccount(), resourceGroupEnum));
    }
  }

  /**
   * Fill resource assignation.
   *
   * @param id the id
   * @param email the email
   * @param group the group
   * @return the resource assignation VO
   */
  private ResourceAssignationVO fillResourceAssignation(Long id, String email,
      ResourceGroupEnum group) {

    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setResourceId(id);
    resource.setEmail(email);
    resource.setResourceGroup(group);

    return resource;
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
    LOG.info("Group created with the following resources:{}", resourceInfoVO);
    return resourceInfoVO;
  }

  /**
   * Update contributor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param dataProviderId the data provider id
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateContributor(Long dataflowId, ContributorVO contributorVO, Long dataProviderId)
      throws EEAException {

    // we delete the contributor and after that we create it to update
    if (SecurityRoleEnum.EDITOR_READ.toString().equals(contributorVO.getRole())
        || SecurityRoleEnum.EDITOR_WRITE.toString().equals(contributorVO.getRole())
        || SecurityRoleEnum.REPORTER_READ.toString().equals(contributorVO.getRole())
        || SecurityRoleEnum.REPORTER_WRITE.toString().equals(contributorVO.getRole())
        || SecurityRoleEnum.DATA_CUSTODIAN.toString().equals(contributorVO.getRole())
        || SecurityRoleEnum.CUSTODIAN_SUPPORT.toString().equals(contributorVO.getRole())
        || SecurityRoleEnum.DATA_STEWARD.toString().equals(contributorVO.getRole())
        || SecurityRoleEnum.DATA_OBSERVER.toString().equals(contributorVO.getRole())) {
      contributorVO.setAccount(contributorVO.getAccount().toLowerCase());
      Boolean persistDataflowPermission = null;
      // avoid delete if it's a new contributor
      List<ResourceAccessVO> resourceAccessVOs =
          userManagementControllerZull.getResourcesByUserEmail(contributorVO.getAccount());
      if (null != resourceAccessVOs && !resourceAccessVOs.isEmpty()) {
        ResourceAccessVO resourceAccess = null;
        List<Long> reportings = new ArrayList<>();
        if (null != dataProviderId) {
          reportings =
              dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId).stream()
                  .filter(reportingDatasetVO -> dataProviderId
                      .equals(reportingDatasetVO.getDataProviderId()))
                  .map(ReportingDatasetVO::getId).collect(Collectors.toList());
        }
        for (ResourceAccessVO resource : resourceAccessVOs) {
          if (reportings != null && !reportings.isEmpty()
              && contributorVO.getRole().contains(LiteralConstants.REPORTER + "_")
              && resource.getId().equals(reportings.get(0))
              && resource.getResource().equals(ResourceTypeEnum.DATASET)
              && SecurityRoleEnum.LEAD_REPORTER.equals(resource.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                new StringBuilder("Role ").append(contributorVO.getRole())
                    .append(" cannot be added to that user").toString());
          }
          if (resource.getId().equals(dataflowId)
              && resource.getResource().equals(ResourceTypeEnum.DATAFLOW)) {
            if (contributorVO.getRole().contains(LiteralConstants.REPORTER + "_")
                && resource.getRole().toString().contains(LiteralConstants.REPORTER + "_")) {
              resourceAccess = resource;
            } else if (!contributorVO.getRole().contains(LiteralConstants.REPORTER + "_")
                && !resource.getRole().toString().contains(LiteralConstants.REPORTER + "_")) {
              resourceAccess = resource;
              break;
            }
          }
        }
        LOG.info("Checking for account:{} in Dataflow {} and Role:{}, Access resources:{}",
            contributorVO.getAccount(), dataflowId, contributorVO.getRole(), resourceAccess);
        if (null != resourceAccess) {
          try {
            persistDataflowPermission =
                deleteContributor(dataflowId, contributorVO, dataProviderId, resourceAccess);
          } catch (EEAException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
          }
        }
      }
      try {
        createContributor(dataflowId, contributorVO, dataProviderId, persistDataflowPermission);
      } catch (EEAException e) {
        LOG_ERROR.error("Error creating contributor with the account: {} in the dataflow {} ",
            contributorVO.getAccount(), dataflowId);
        throw new EEAException(e);
      }
    } else {
      LOG_ERROR.error(
          "Error creating contributor with the account: {} in the dataflow {}  because the role not avaliable {}",
          contributorVO.getAccount(), dataflowId, contributorVO.getRole());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, new StringBuilder("Role ")
          .append(contributorVO.getRole()).append(" doesn't exist").toString());
    }

  }

  /**
   * Update temporal user.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param dataProviderId the data provider id
   */
  @Override
  public void updateTemporaryUser(Long dataflowId, ContributorVO contributorVO,
      Long dataProviderId) {

    TempUser userToUpdate = tempUserRepository
        .findTempUserByAccountAndDataflow(contributorVO.getAccount(), dataflowId, dataProviderId);
    LOG.info("Updated temporary user:{} in Dataflow {} and Role:{} to Role:{}",
        contributorVO.getAccount(), dataflowId, userToUpdate.getRole(), contributorVO.getRole());

    userToUpdate.setRole(contributorVO.getRole());

    tempUserRepository.save(userToUpdate);
  }

  @Override
  public void deleteTemporaryUser(Long dataflowId, String email, String role, Long dataProviderId) {

    TempUser userToUpdate =
        tempUserRepository.findTempUserByAccountAndDataflow(email, dataflowId, dataProviderId);
    LOG.info("Deleting temporary user:{} in Dataflow {} with dataproviderId {} and role {}.", email,
        dataflowId, dataProviderId, role);

    tempUserRepository.delete(userToUpdate);
  }

  /**
   * Delete contributor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param dataProviderId the data provider id
   * @param resourceAccess the resource access
   * @return the boolean
   * @throws EEAException the EEA exception
   */
  private Boolean deleteContributor(Long dataflowId, ContributorVO contributorVO,
      Long dataProviderId, ResourceAccessVO resourceAccess) throws EEAException {
    Boolean persistDataflowPermission;
    persistDataflowPermission =
        checkDataflowPrevPermission(contributorVO.getRole(), resourceAccess);
    LOG.info("Permissions to be maintained:{} for the user:{} in Dataflow {}.",
        persistDataflowPermission, contributorVO.getAccount(), dataflowId);
    try {
      if (checkReporterDelete(contributorVO.getRole(), resourceAccess)) {
        deleteContributor(dataflowId, contributorVO.getAccount(),
            resourceAccess.getRole().toString(), dataProviderId);
      }
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error deleting contributor with the account: {} in the dataflow {} with role {} ",
          contributorVO.getAccount(), dataflowId, resourceAccess.getRole());
      throw new EEAException(e);
    }
    return persistDataflowPermission;
  }

  /**
   * Check reporter delete.
   *
   * @param role the role
   * @param resourceAccess the resource access
   * @return true, if successful
   */
  private boolean checkReporterDelete(String role, ResourceAccessVO resourceAccess) {
    boolean delete = true;
    if ((SecurityRoleEnum.REPORTER_READ.toString().equals(role)
        || SecurityRoleEnum.REPORTER_WRITE.toString().equals(role))
        && !resourceAccess.toString().contains(LiteralConstants.REPORTER)) {
      delete = false;
    }
    return delete;
  }

  /**
   * Check dataflow prev permission.
   *
   * @param role the role
   * @param resourceAccess the resource access
   * @return the boolean
   */
  private Boolean checkDataflowPrevPermission(String role, ResourceAccessVO resourceAccess) {
    Boolean result = null;
    if (SecurityRoleEnum.REPORTER_READ.toString().equals(role)) {
      SecurityRoleEnum roleEnumToCreate = SecurityRoleEnum.REPORTER_READ;
      if (!resourceAccess.getRole().equals(roleEnumToCreate)) {
        result = true;
      }
    } else if (SecurityRoleEnum.REPORTER_WRITE.toString().equals(role)) {
      SecurityRoleEnum roleEnumToCreate = SecurityRoleEnum.REPORTER_WRITE;
      if (!resourceAccess.getRole().equals(roleEnumToCreate)) {
        result = true;
      }
    }
    return result;
  }

  /**
   * Creates the associated permissions.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  @Override
  public void createAssociatedPermissions(Long dataflowId, Long datasetId) throws EEAException {

    List<ResourceAssignationVO> resources = new ArrayList<>();

    // It finds all users that have dataflow-xx-editor-read
    List<UserRepresentationVO> usersEditorRead = userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_READ.getGroupName(dataflowId));

    // It finds all users that have dataflow-xx-editor-write
    List<UserRepresentationVO> usersEditorWrite = userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_WRITE.getGroupName(dataflowId));

    // It finds all users that have dataflow-xx-data-custodian
    List<UserRepresentationVO> usersCustodian = userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(dataflowId));


    List<UserRepresentationVO> usersDataSteward = userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_STEWARD.getGroupName(dataflowId));

    if (!CollectionUtils.isEmpty(usersDataSteward)) {
      for (UserRepresentationVO userDataSteward : usersDataSteward) {
        resources.add(fillResourceAssignation(datasetId, userDataSteward.getEmail(),
            ResourceGroupEnum.DATASCHEMA_STEWARD));

      }
    }
    // find custodian and add custodian if we create since a editor
    createCustodian(datasetId, resources, usersCustodian);
    // we create resources for any users to add the new resource associated with the new
    // datasetSchema
    if (!CollectionUtils.isEmpty(usersEditorRead) || !CollectionUtils.isEmpty(usersEditorWrite)) {
      if (!CollectionUtils.isEmpty(usersEditorRead)) {
        for (UserRepresentationVO userEditorRead : usersEditorRead) {
          resources.add(fillResourceAssignation(datasetId, userEditorRead.getEmail(),
              ResourceGroupEnum.DATASCHEMA_EDITOR_READ));

        }
      }
      if (!CollectionUtils.isEmpty(usersEditorWrite)) {
        for (UserRepresentationVO userEditorWrite : usersEditorWrite) {
          resources.add(fillResourceAssignation(datasetId, userEditorWrite.getEmail(),
              ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE));
        }
      }
      LOG.info("Create role editor for dataflow {} with the dataset id {}", dataflowId, datasetId);
    } else {
      LOG.info(
          "Didn't create role editor for dataflow {} with the dataset id {}, because it hasn't editors associated",
          dataflowId, datasetId);
    }
    // we add all contributors to all users
    userManagementControllerZull.addContributorsToResources(resources);
  }


  /**
   * Creates the temp user.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param dataproviderId the dataprovider id
   */
  @Override
  public void createTempUser(Long dataflowId, ContributorVO contributorVO, Long dataproviderId) {
    TempUser tempUser = new TempUser();

    tempUser.setDataflowId(dataflowId);
    tempUser.setEmail(contributorVO.getAccount());
    tempUser.setRole(contributorVO.getRole());
    tempUser.setRegisteredDate(LocalDateTime.now().toDate());
    tempUser.setDataProviderId(dataproviderId);

    tempUserRepository.save(tempUser);
  }

  /**
   * Validate reporters.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param contributorVOList the contributor VO list
   * @throws EEAException
   */
  @Override
  @Async
  public void validateReporters(Long dataflowId, Long dataProviderId, boolean sendNotification)
      throws EEAException {

    List<ContributorVO> tempReporterWrite = findTempUserByRoleAndDataflow(
        SecurityRoleEnum.REPORTER_WRITE.toString(), dataflowId, dataProviderId);
    List<ContributorVO> tempReporterRead = findTempUserByRoleAndDataflow(
        SecurityRoleEnum.REPORTER_READ.toString(), dataflowId, dataProviderId);
    List<ContributorVO> reportersList =
        findContributorsByResourceId(dataflowId, dataProviderId, LiteralConstants.REPORTER);
    reportersList.addAll(tempReporterWrite);
    reportersList.addAll(tempReporterRead);


    for (ContributorVO contributor : reportersList) {
      try {
        if (contributor.isInvalid()
            && userManagementControllerZull.getUserByEmail(contributor.getAccount()) != null) {
          deleteTemporaryUser(dataflowId, contributor.getAccount(), contributor.getRole(),
              dataProviderId);
          updateContributor(dataflowId, contributor, dataProviderId);
        }
      } catch (Exception e) {
        LOG_ERROR.error(
            "Error creating contributor with the account: {} in the dataflow {} with role {}.",
            contributor.getAccount(), dataflowId, contributor.getRole());

        if (sendNotification) {
          NotificationVO notificationVO = NotificationVO.builder()
              .user(SecurityContextHolder.getContext().getAuthentication().getName())
              .dataflowId(dataflowId).providerId(dataProviderId).build();

          kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATE_REPORTERS_FAILED_EVENT,
              null, notificationVO);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
      }
    }

    if (sendNotification) {
      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName())
          .dataflowId(dataflowId).providerId(dataProviderId).build();

      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATE_REPORTERS_COMPLETED_EVENT,
          null, notificationVO);
    }

  }

  /**
   * Creates the custodian.
   *
   * @param datasetId the dataset id
   * @param resources the resources
   * @param usersCustodian the users custodian
   */
  private void createCustodian(Long datasetId, List<ResourceAssignationVO> resources,
      List<UserRepresentationVO> usersCustodian) {
    if (!CollectionUtils.isEmpty(usersCustodian)) {
      for (UserRepresentationVO userRepresentationVO : usersCustodian) {
        List<ResourceAccessVO> resourceAccessVOs =
            userManagementControllerZull.getResourcesByUserEmail(userRepresentationVO.getEmail());
        if (null != resourceAccessVOs
            && !resourceAccessVOs.stream().anyMatch(resource -> resource.getId().equals(datasetId)
                && resource.getResource().equals(ResourceTypeEnum.DATA_SCHEMA))) {
          resources.add(fillResourceAssignation(datasetId, userRepresentationVO.getEmail(),
              ResourceGroupEnum.DATASCHEMA_CUSTODIAN));
        }
      }
    }
  }

}
