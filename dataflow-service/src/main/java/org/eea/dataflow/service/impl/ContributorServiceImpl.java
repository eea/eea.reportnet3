package org.eea.dataflow.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eea.dataflow.service.ContributorService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.ums.ResourceAccessVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
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

  /** The Constant EDITOR: {@value}. */
  private static final String EDITOR = "EDITOR";

  /** The Constant REPORTER: {@value}. */
  private static final String REPORTER = "REPORTER";

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The resource management controller zull. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The data set metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

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

    if (EDITOR.equals(role) || REPORTER.equals(role)) {
      Long referenceId = EDITOR.equals(role) ? dataflowId
          : dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId).stream()
              .filter(
                  reportingDatasetVO -> providerId.equals(reportingDatasetVO.getDataProviderId()))
              .map(ReportingDatasetVO::getId).findFirst().orElse(null);
      String resource = EDITOR.equals(role) ? "Dataflow-" : "Dataset-";

      StringBuilder stringBuilder =
          new StringBuilder(resource).append(referenceId).append("-").append(role).append("_WRITE");
      List<UserRepresentationVO> listUserWrite =
          userManagementControllerZull.getUsersByGroup(stringBuilder.toString());
      if (!CollectionUtils.isEmpty(listUserWrite)) {
        listUserWrite.stream().forEach(userWrite -> {
          ContributorVO contributorVO = new ContributorVO();
          contributorVO.setAccount(userWrite.getEmail());
          contributorVO.setWritePermission(true);
          contributorVO.setRole(role);
          contributorVOList.add(contributorVO);
        });
      }
      stringBuilder =
          new StringBuilder(resource).append(referenceId).append("-").append(role).append("_READ");
      List<UserRepresentationVO> listUserRead =
          userManagementControllerZull.getUsersByGroup(stringBuilder.toString());
      if (!CollectionUtils.isEmpty(listUserRead)) {
        listUserRead.stream().forEach(userRead -> {
          ContributorVO contributorVO = new ContributorVO();
          contributorVO.setAccount(userRead.getEmail());
          contributorVO.setWritePermission(false);
          contributorVO.setRole(role);
          contributorVOList.add(contributorVO);
        });
      }
    }

    return contributorVOList;
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

    switch (role) {
      case EDITOR:
        resourceGroupEnumWrite = ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE;
        resourceGroupEnumRead = ResourceGroupEnum.DATASCHEMA_EDITOR_READ;
        resourceGroupEnumDataflowWrite = ResourceGroupEnum.DATAFLOW_EDITOR_WRITE;
        resourceGroupEnumDataflowRead = ResourceGroupEnum.DATAFLOW_EDITOR_READ;

        break;
      case REPORTER:
        resourceGroupEnumWrite = ResourceGroupEnum.DATASET_REPORTER_WRITE;
        resourceGroupEnumRead = ResourceGroupEnum.DATASET_REPORTER_READ;
        resourceGroupEnumDataschemaRead = ResourceGroupEnum.DATASCHEMA_REPORTER_READ;
        resourceGroupEnumDataflowRead = ResourceGroupEnum.DATAFLOW_REPORTER_READ;
        resourceGroupEnumDataflowWrite = ResourceGroupEnum.DATAFLOW_REPORTER_WRITE;
        break;
      default:
        break;
    }

    List<ResourceAssignationVO> resourcesProviders = new ArrayList<>();
    List<Long> ids = new ArrayList<>();
    if (REPORTER.equals(role)) {
      List<ContributorVO> contributors =
          findContributorsByResourceId(dataflowId, dataProviderId, REPORTER);
      if (contributors != null) {
        resourcesProviders.add(fillResourceAssignation(dataflowId, account,
            contributors.stream().filter(contributor -> account.equals(contributor.getAccount()))
                .findFirst().map(ContributorVO::getWritePermission).orElse(false)
                    ? resourceGroupEnumDataflowWrite
                    : resourceGroupEnumDataflowRead));
      }
      resourcesProviders
          .add(fillResourceAssignation(dataflowId, account, resourceGroupEnumDataschemaRead));

      ids = dataSetMetabaseControllerZuul.findReportingDataSetIdByDataflowId(dataflowId).stream()
          .filter(
              reportingDatasetVO -> dataProviderId.equals(reportingDatasetVO.getDataProviderId()))
          .map(ReportingDatasetVO::getId).collect(Collectors.toList());
    }
    if (EDITOR.equals(role)) {
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
   * @param role the role
   * @param dataProviderId the data provider id
   * @param persistDataflowPermission the persist dataflow permission
   * @throws EEAException the EEA exception
   */
  @Override
  public void createContributor(Long dataflowId, ContributorVO contributorVO, String role,
      Long dataProviderId, Boolean persistDataflowPermission) throws EEAException {
    SecurityRoleEnum securityRoleEnum = null;
    ResourceGroupEnum resourceGroupEnum = null;
    ResourceGroupEnum resourceGroupEnumDataflow = null;
    ResourceGroupEnum resourceGroupEnumDataset = null;

    contributorVO.setAccount(contributorVO.getAccount().toLowerCase());
    switch (role) {
      case EDITOR:
        securityRoleEnum =
            Boolean.TRUE.equals(contributorVO.getWritePermission()) ? SecurityRoleEnum.EDITOR_WRITE
                : SecurityRoleEnum.EDITOR_READ;
        resourceGroupEnum = Boolean.TRUE.equals(contributorVO.getWritePermission())
            ? ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE
            : ResourceGroupEnum.DATASCHEMA_EDITOR_READ;
        resourceGroupEnumDataflow = Boolean.TRUE.equals(contributorVO.getWritePermission())
            ? ResourceGroupEnum.DATAFLOW_EDITOR_WRITE
            : ResourceGroupEnum.DATAFLOW_EDITOR_READ;
        break;
      case REPORTER:
        securityRoleEnum = Boolean.TRUE.equals(contributorVO.getWritePermission())
            ? SecurityRoleEnum.REPORTER_WRITE
            : SecurityRoleEnum.REPORTER_READ;
        resourceGroupEnum = ResourceGroupEnum.DATASCHEMA_REPORTER_READ;
        resourceGroupEnumDataflow = Boolean.TRUE.equals(contributorVO.getWritePermission())
            ? ResourceGroupEnum.DATAFLOW_REPORTER_WRITE
            : ResourceGroupEnum.DATAFLOW_REPORTER_READ;
        resourceGroupEnumDataset = Boolean.TRUE.equals(contributorVO.getWritePermission())
            ? ResourceGroupEnum.DATASET_REPORTER_WRITE
            : ResourceGroupEnum.DATASET_REPORTER_READ;
        break;
      default:
        break;
    }
    List<ResourceAssignationVO> resourceAssignationVOList = fillResourceAssignationList(dataflowId,
        contributorVO, role, dataProviderId, persistDataflowPermission, securityRoleEnum,
        resourceGroupEnum, resourceGroupEnumDataflow, resourceGroupEnumDataset);

    // we add data to contributor
    userManagementControllerZull.addContributorsToResources(resourceAssignationVOList);
  }

  /**
   * Fill resource assignation list.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param role the role
   * @param dataProviderId the data provider id
   * @param persistDataflowPermission the persist dataflow permission
   * @param securityRoleEnum the security role enum
   * @param resourceGroupEnum the resource group enum
   * @param resourceGroupEnumDataflow the resource group enum dataflow
   * @param resourceGroupEnumDataset the resource group enum dataset
   * @return the list
   */
  private List<ResourceAssignationVO> fillResourceAssignationList(Long dataflowId,
      ContributorVO contributorVO, String role, Long dataProviderId,
      Boolean persistDataflowPermission, SecurityRoleEnum securityRoleEnum,
      ResourceGroupEnum resourceGroupEnum, ResourceGroupEnum resourceGroupEnumDataflow,
      ResourceGroupEnum resourceGroupEnumDataset) {
    final List<ResourceAssignationVO> resourceAssignationVOList = new ArrayList<>();
    List<ResourceInfoVO> resourceInfoVOs = new ArrayList<>();
    if (EDITOR.equals(role)) {
      resourceAssignationVOList.add(fillResourceAssignation(dataflowId, contributorVO.getAccount(),
          resourceGroupEnumDataflow));
      for (DesignDatasetVO designDatasetVO : dataSetMetabaseControllerZuul
          .findDesignDataSetIdByDataflowId(dataflowId)) {

        resourceAssignationVOList.add(fillResourceAssignation(designDatasetVO.getId(),
            contributorVO.getAccount(), resourceGroupEnum));
      }
    } else if (REPORTER.equals(role)) {
      createReporterGroupsResources(dataflowId, contributorVO, dataProviderId, securityRoleEnum,
          resourceGroupEnum, resourceGroupEnumDataflow, resourceGroupEnumDataset,
          resourceAssignationVOList, resourceInfoVOs, persistDataflowPermission);

    }
    return resourceAssignationVOList;
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
   * @param role the role
   * @param dataProviderId the data provider id
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateContributor(Long dataflowId, ContributorVO contributorVO, String role,
      Long dataProviderId) throws EEAException {

    // we delete the contributor and after that we create it to update
    if (EDITOR.equals(role) || REPORTER.equals(role)) {
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
              deleteContributor(dataflowId, contributorVO, role, dataProviderId, resourceAccess);
        }
      }
      try {
        createContributor(dataflowId, contributorVO, role, dataProviderId,
            persistDataflowPermission);
      } catch (EEAException e) {
        LOG_ERROR.error("Error creating contributor with the account: {} in the dataflow {} ",
            contributorVO.getAccount(), dataflowId);
        throw new EEAException(e);
      }
    } else {
      LOG_ERROR.error(
          "Error creating contributor with the account: {} in the dataflow {}  because the role not avaliable {}",
          contributorVO.getAccount(), dataflowId, role);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          new StringBuilder("Role ").append(role).append(" doesn't exist").toString());
    }

  }

  /**
   * Delete contributor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param role the role
   * @param dataProviderId the data provider id
   * @param resourceAccess the resource access
   * @return the boolean
   * @throws EEAException the EEA exception
   */
  private Boolean deleteContributor(Long dataflowId, ContributorVO contributorVO, String role,
      Long dataProviderId, ResourceAccessVO resourceAccess) throws EEAException {
    Boolean persistDataflowPermission;
    persistDataflowPermission =
        checkDataflowPrevPermission(role, contributorVO.getWritePermission(), resourceAccess);
    try {
      deleteContributor(dataflowId, contributorVO.getAccount(), role, dataProviderId);
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
   * @param writePermission the write permission
   * @param resourceAccess the resource access
   * @return the boolean
   */
  private Boolean checkDataflowPrevPermission(String role, Boolean writePermission,
      ResourceAccessVO resourceAccess) {
    Boolean result = null;
    if (REPORTER.equals(role)) {
      SecurityRoleEnum roleEnumToCreate =
          Boolean.TRUE.equals(writePermission) ? SecurityRoleEnum.REPORTER_WRITE
              : SecurityRoleEnum.REPORTER_READ;
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
