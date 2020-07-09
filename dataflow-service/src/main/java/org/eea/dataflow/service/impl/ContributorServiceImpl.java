package org.eea.dataflow.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eea.dataflow.service.ContributorService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
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

  /** The dataflow controlle zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The resource management controller zull. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZull;

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
      DataFlowVO dataflow = dataflowControllerZuul.findById(dataflowId);
      Long referenceId = EDITOR.equals(role) ? dataflowId
          : dataflow.getReportingDatasets().stream()
              .filter(
                  reportingDatasetVO -> reportingDatasetVO.getDataProviderId().equals(providerId))
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
    DataFlowVO dataflow = dataflowControllerZuul.findById(dataflowId);

    ResourceGroupEnum resourceGroupEnumWrite = null;
    ResourceGroupEnum resourceGroupEnumRead = null;
    ResourceGroupEnum resourceGroupEnumDataflowWrite = null;
    ResourceGroupEnum resourceGroupEnumDataflowRead = null;

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
        resourceGroupEnumDataflowWrite = ResourceGroupEnum.DATASCHEMA_REPORTER_READ;
        resourceGroupEnumDataflowRead = ResourceGroupEnum.DATAFLOW_REPORTER_READ;
        break;
      default:
        break;
    }

    if (EDITOR.equals(role) || REPORTER.equals(role)) {
      List<ResourceAssignationVO> resourcesProviders = new ArrayList<>();
      resourcesProviders
          .add(fillResourceAssignation(dataflowId, account, resourceGroupEnumDataflowWrite));
      resourcesProviders
          .add(fillResourceAssignation(dataflowId, account, resourceGroupEnumDataflowRead));
      List<Long> ids = EDITOR.equals(role)
          ? dataflow.getDesignDatasets().stream().map(DesignDatasetVO::getId)
              .collect(Collectors.toList())
          : dataflow.getReportingDatasets().stream().filter(
              reportingDatasetVO -> reportingDatasetVO.getDataProviderId().equals(dataProviderId))
              .map(ReportingDatasetVO::getId).collect(Collectors.toList());
      for (Long id : ids) {
        // remove resources
        resourcesProviders.add(fillResourceAssignation(id, account, resourceGroupEnumWrite));
        resourcesProviders.add(fillResourceAssignation(id, account, resourceGroupEnumRead));
      }
      userManagementControllerZull.removeContributorsFromResources(resourcesProviders);
    }
  }


  /**
   * Creates the contributor.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param role the role
   * @param dataProviderId the data provider id
   * @throws EEAException the EEA exception
   */
  @Override
  public void createContributor(Long dataflowId, ContributorVO contributorVO, String role,
      Long dataProviderId) throws EEAException {
    DataFlowVO dataflow = dataflowControllerZuul.findById(dataflowId);
    SecurityRoleEnum securityRoleEnum = null;
    ResourceGroupEnum resourceGroupEnum = null;
    ResourceGroupEnum resourceGroupEnumDataflow = null;
    ResourceGroupEnum resourceGroupEnumDataset = null;

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
        resourceGroupEnumDataflow = ResourceGroupEnum.DATAFLOW_REPORTER_READ;
        resourceGroupEnumDataset = Boolean.TRUE.equals(contributorVO.getWritePermission())
            ? ResourceGroupEnum.DATASET_REPORTER_WRITE
            : ResourceGroupEnum.DATASET_REPORTER_READ;
        break;
      default:
        break;
    }
    final List<ResourceAssignationVO> resourceAssignationVOList = new ArrayList<>();
    List<ResourceInfoVO> resourceInfoVOs = new ArrayList<>();
    if (EDITOR.equals(role)) {
      resourceAssignationVOList.add(fillResourceAssignation(dataflowId, contributorVO.getAccount(),
          resourceGroupEnumDataflow));
      for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {

        resourceAssignationVOList.add(fillResourceAssignation(designDatasetVO.getId(),
            contributorVO.getAccount(), resourceGroupEnum));
      }
    } else if (REPORTER.equals(role)) {
      ResourceInfoVO resourceDataflow =
          resourceManagementControllerZull.getResourceDetail(dataflowId, resourceGroupEnumDataflow);
      if (null == resourceDataflow.getName()) {
        resourceInfoVOs.add(
            createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.REPORTER_READ));
      }
      resourceAssignationVOList.add(fillResourceAssignation(dataflowId, contributorVO.getAccount(),
          resourceGroupEnumDataflow));

      for (Long reportingDatasetId : dataflow.getReportingDatasets().stream()
          .filter(
              reportingDatasetVO -> reportingDatasetVO.getDataProviderId().equals(dataProviderId))
          .map(ReportingDatasetVO::getId).collect(Collectors.toList())) {
        ResourceInfoVO resourceDataSchema = resourceManagementControllerZull
            .getResourceDetail(reportingDatasetId, resourceGroupEnum);
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

    // we add data to contributor
    userManagementControllerZull.addContributorsToResources(resourceAssignationVOList);
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
      // avoid delete if it's a new contributor
      List<ResourceAccessVO> resourceAccessVOs =
          userManagementControllerZull.getResourcesByUserEmail(contributorVO.getAccount());
      if (resourceAccessVOs != null && !resourceAccessVOs.isEmpty()
          && resourceAccessVOs.stream().anyMatch(resource -> resource.getId().equals(dataflowId)
              && resource.getResource().equals(ResourceTypeEnum.DATAFLOW))) {
        try {
          deleteContributor(dataflowId, contributorVO.getAccount(), role, dataProviderId);
        } catch (EEAException e) {
          LOG_ERROR.error("Error deleting contributor with the account: {} in the dataflow {} ",
              contributorVO.getAccount(), dataflowId);
          throw new EEAException(e);
        }
      }
      try {
        createContributor(dataflowId, contributorVO, role, dataProviderId);
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

    // find custodian and add custodian if we create since a editor
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

}
