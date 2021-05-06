package org.eea.dataflow.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eea.dataflow.service.ContributorService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.TestDatasetController.TestDatasetControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    }

    return contributorVOList;
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

    ResourceGroupEnum resourceGroupEnumWrite = null;
    ResourceGroupEnum resourceGroupEnumRead = null;
    ResourceGroupEnum resourceGroupEnumDataflowWrite = null;
    ResourceGroupEnum resourceGroupEnumDataflowRead = null;
    ResourceGroupEnum resourceGroupEnumDataschemaRead = null;

    switch (SecurityRoleEnum.valueOf(role)) {
      case EDITOR_READ:
      case EDITOR_WRITE:
        resourceGroupEnumWrite = ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE;
        resourceGroupEnumRead = ResourceGroupEnum.DATASCHEMA_EDITOR_READ;
        resourceGroupEnumDataflowWrite = ResourceGroupEnum.DATAFLOW_EDITOR_WRITE;
        resourceGroupEnumDataflowRead = ResourceGroupEnum.DATAFLOW_EDITOR_READ;

        break;
      case REPORTER_READ:
      case REPORTER_WRITE:
        resourceGroupEnumWrite = ResourceGroupEnum.DATASET_REPORTER_WRITE;
        resourceGroupEnumRead = ResourceGroupEnum.DATASET_REPORTER_READ;
        resourceGroupEnumDataschemaRead = ResourceGroupEnum.DATASCHEMA_REPORTER_READ;
        resourceGroupEnumDataflowRead = ResourceGroupEnum.DATAFLOW_REPORTER_READ;
        resourceGroupEnumDataflowWrite = ResourceGroupEnum.DATAFLOW_REPORTER_WRITE;
        break;
      case DATA_OBSERVER:

      default:
        break;
    }

    List<ResourceAssignationVO> resourcesProviders = new ArrayList<>();
    List<Long> ids = new ArrayList<>();
    if (SecurityRoleEnum.REPORTER_READ.toString().equals(role)
        || SecurityRoleEnum.REPORTER_WRITE.toString().equals(role)) {
      List<ContributorVO> contributors =
          findContributorsByResourceId(dataflowId, dataProviderId, LiteralConstants.REPORTER);
      if (contributors != null) {
        if (SecurityRoleEnum.REPORTER_READ.toString().equals(role)) {
          resourcesProviders
              .add(fillResourceAssignation(dataflowId, account, resourceGroupEnumDataflowRead));
        } else if (SecurityRoleEnum.REPORTER_WRITE.toString().equals(role)) {
          resourcesProviders
              .add(fillResourceAssignation(dataflowId, account, resourceGroupEnumDataflowWrite));
        }
      }
      resourcesProviders
          .add(fillResourceAssignation(dataflowId, account, resourceGroupEnumDataschemaRead));

      ids = dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId).stream()
          .filter(
              reportingDatasetVO -> dataProviderId.equals(reportingDatasetVO.getDataProviderId()))
          .map(ReportingDatasetVO::getId).collect(Collectors.toList());
    }


    if (SecurityRoleEnum.EDITOR_READ.toString().equals(role)
        || SecurityRoleEnum.EDITOR_WRITE.toString().equals(role)) {
      resourcesProviders
          .add(fillResourceAssignation(dataflowId, account, resourceGroupEnumDataflowWrite));
      resourcesProviders
          .add(fillResourceAssignation(dataflowId, account, resourceGroupEnumDataflowRead));

      ids = dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(dataflowId).stream()
          .map(DesignDatasetVO::getId).collect(Collectors.toList());
    }
    for (Long id : ids) {
      // remove resources
      resourcesProviders.add(fillResourceAssignation(id, account, resourceGroupEnumWrite));
      resourcesProviders.add(fillResourceAssignation(id, account, resourceGroupEnumRead));
    }
    userManagementControllerZull.removeContributorsFromResources(resourcesProviders);
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
            null);
        break;
      case DATA_CUSTODIAN:
        createRequesterGroupsResources(dataflowId, contributorVO, dataProviderId,
            resourceAssignationVOList, resourceInfoVOs, SecurityRoleEnum.DATA_CUSTODIAN,
            ResourceGroupEnum.DATAFLOW_CUSTODIAN, ResourceGroupEnum.DATASET_CUSTODIAN,
            ResourceGroupEnum.DATACOLLECTION_CUSTODIAN, ResourceGroupEnum.EUDATASET_CUSTODIAN,
            ResourceGroupEnum.TESTDATASET_CUSTODIAN, ResourceGroupEnum.DATASCHEMA_CUSTODIAN);
        break;
      case DATA_STEWARD:
        createRequesterGroupsResources(dataflowId, contributorVO, dataProviderId,
            resourceAssignationVOList, resourceInfoVOs, SecurityRoleEnum.DATA_STEWARD,
            ResourceGroupEnum.DATAFLOW_STEWARD, ResourceGroupEnum.DATASET_STEWARD,
            ResourceGroupEnum.DATACOLLECTION_STEWARD, ResourceGroupEnum.EUDATASET_STEWARD,
            ResourceGroupEnum.TESTDATASET_STEWARD, ResourceGroupEnum.DATASCHEMA_STEWARD);
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
    if (null == resourceDataflow.getName()) {
      resourceInfoVOs.add(createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, securityRoleEnum));
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
   * @param ResourceGroupDataflow the resource group dataflow
   * @param ResourceGroupDataset the resource group dataset
   * @param ResourceGroupDC the resource group DC
   * @param ResourceGroupEU the resource group EU
   * @param ResourceGroupTest the resource group test
   * @param ResourceGroupSchema the resource group schema
   */
  private void createRequesterGroupsResources(Long dataflowId, ContributorVO contributorVO,
      Long dataProviderId, final List<ResourceAssignationVO> resourceAssignationVOList,
      List<ResourceInfoVO> resourceInfoVOs, SecurityRoleEnum securityRole,
      ResourceGroupEnum ResourceGroupDataflow, ResourceGroupEnum ResourceGroupDataset,
      ResourceGroupEnum ResourceGroupDC, ResourceGroupEnum ResourceGroupEU,
      ResourceGroupEnum ResourceGroupTest, ResourceGroupEnum ResourceGroupSchema) {

    contributorVO.setAccount(contributorVO.getAccount().toLowerCase());

    ResourceInfoVO resourceDataflow =
        resourceManagementControllerZull.getResourceDetail(dataflowId, ResourceGroupDataflow);
    if (null == resourceDataflow.getName()) {
      resourceInfoVOs.add(createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, securityRole));
    }
    resourceAssignationVOList.add(
        fillResourceAssignation(dataflowId, contributorVO.getAccount(), ResourceGroupDataflow));

    // dataset
    createGroupList(
        dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs, ResourceGroupDataset,
        securityRole, dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId)
            .stream().map(ReportingDatasetVO::getId).collect(Collectors.toList()),
        ResourceTypeEnum.DATASET);
    // DC
    createGroupList(dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs,
        ResourceGroupDC, securityRole,
        dataCollectionControllerZuul.findDataCollectionIdByDataflowId(dataflowId).stream()
            .map(DataCollectionVO::getId).collect(Collectors.toList()),
        ResourceTypeEnum.DATA_COLLECTION);
    // EU
    createGroupList(dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs,
        ResourceGroupEU, securityRole, eUDatasetControllerZuul.findEUDatasetByDataflowId(dataflowId)
            .stream().map(EUDatasetVO::getId).collect(Collectors.toList()),
        ResourceTypeEnum.EU_DATASET);

    if (SecurityRoleEnum.DATA_CUSTODIAN.equals(securityRole)
        || SecurityRoleEnum.DATA_STEWARD.equals(securityRole)) {
      // schemas
      createGroupList(dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs,
          ResourceGroupSchema, securityRole,
          dataSetMetabaseControllerZuul.findDesignDataSetIdByDataflowId(dataflowId).stream()
              .map(DesignDatasetVO::getId).collect(Collectors.toList()),
          ResourceTypeEnum.DATA_SCHEMA);
      // test
      createGroupList(dataflowId, contributorVO, resourceAssignationVOList, resourceInfoVOs,
          ResourceGroupTest, securityRole,
          testDatasetControllerZuul.findTestDatasetByDataflowId(dataflowId).stream()
              .map(TestDatasetVO::getId).collect(Collectors.toList()),
          ResourceTypeEnum.TEST_DATASET);
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
        || SecurityRoleEnum.DATA_STEWARD.toString().equals(contributorVO.getRole())
        || SecurityRoleEnum.DATA_OBSERVER.toString().equals(contributorVO.getRole())) {
      contributorVO.setAccount(contributorVO.getAccount().toLowerCase());
      Boolean persistDataflowPermission = null;
      // avoid delete if it's a new contributor
      List<ResourceAccessVO> resourceAccessVOs =
          userManagementControllerZull.getResourcesByUserEmail(contributorVO.getAccount());
      if (resourceAccessVOs != null && !resourceAccessVOs.isEmpty()) {
        ResourceAccessVO resourceAccess =
            resourceAccessVOs.stream()
                .filter(resource -> resource.getId().equals(dataflowId)
                    && ResourceTypeEnum.DATAFLOW.equals(resource.getResource()))
                .findAny().orElse(null);
        if (resourceAccess != null) {
          persistDataflowPermission =
              deleteContributor(dataflowId, contributorVO, dataProviderId, resourceAccess);
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
    try {
      deleteContributor(dataflowId, contributorVO.getAccount(), contributorVO.getRole(),
          dataProviderId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting contributor with the account: {} in the dataflow {} ",
          contributorVO.getAccount(), dataflowId);
      throw new EEAException(e);
    }
    return persistDataflowPermission;
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
