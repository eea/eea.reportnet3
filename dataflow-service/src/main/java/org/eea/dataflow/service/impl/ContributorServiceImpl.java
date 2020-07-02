package org.eea.dataflow.service.impl;


import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.ContributorService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
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

  /** The dataflow controlle zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControlleZuul;


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
   * @param role the role
   * @return the list
   */
  @Override
  public List<ContributorVO> findContributorsByIdDataflow(Long dataflowId, String role) {
    dataflowControlleZuul.findById(dataflowId);
    List<ContributorVO> listRoleUserVO = new ArrayList<>();

    if ("EDITOR".equals(role)) {
      StringBuilder stringBuilder =
          new StringBuilder("Dataflow-").append(dataflowId).append("-EDITOR_WRITE");
      List<UserRepresentationVO> listUserWrite =
          userManagementControllerZull.getUsersByGroup(stringBuilder.toString());
      listUserWrite.stream().forEach(userWrite -> {
        ContributorVO contributorVO = new ContributorVO();
        contributorVO.setAccount(userWrite.getEmail());
        contributorVO.setWritePermission(true);
        contributorVO.setRole("EDITOR");
        listRoleUserVO.add(contributorVO);
      });
      stringBuilder = new StringBuilder("Dataflow-").append(dataflowId).append("-EDITOR_READ");
      List<UserRepresentationVO> listUserRead =
          userManagementControllerZull.getUsersByGroup(stringBuilder.toString());
      listUserRead.stream().forEach(userRead -> {
        ContributorVO contributorVO = new ContributorVO();
        contributorVO.setAccount(userRead.getEmail());
        contributorVO.setWritePermission(false);
        contributorVO.setRole("EDITOR");
        listRoleUserVO.add(contributorVO);
      });
    }

    return listRoleUserVO;
  }

  /**
   * Delete contributor.
   *
   * @param dataflowId the dataflow id
   * @param account the account
   * @param role the role
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteContributor(Long dataflowId, String account, String role) throws EEAException {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);

    ResourceGroupEnum resourceGroupEnumWrite = null;
    ResourceGroupEnum resourceGroupEnumRead = null;
    ResourceGroupEnum resourceGroupEnumDataflowWrite = null;
    ResourceGroupEnum resourceGroupEnumDataflowRead = null;

    switch (role) {
      case "EDITOR":
        resourceGroupEnumWrite = ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE;
        resourceGroupEnumRead = ResourceGroupEnum.DATASCHEMA_EDITOR_READ;
        resourceGroupEnumDataflowWrite = ResourceGroupEnum.DATAFLOW_EDITOR_WRITE;
        resourceGroupEnumDataflowRead = ResourceGroupEnum.DATAFLOW_EDITOR_READ;

        break;
      case "REPORTER":
        break;
      default:
        break;
    }

    if ("EDITOR".equals(role)) {
      List<ResourceAssignationVO> resourcesProviders = new ArrayList<>();
      resourcesProviders
          .add(fillResourceAssignation(dataflowId, account, resourceGroupEnumDataflowWrite));
      resourcesProviders
          .add(fillResourceAssignation(dataflowId, account, resourceGroupEnumDataflowRead));
      for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {
        // quitar resources

        resourcesProviders
            .add(fillResourceAssignation(designDatasetVO.getId(), account, resourceGroupEnumWrite));
        resourcesProviders
            .add(fillResourceAssignation(designDatasetVO.getId(), account, resourceGroupEnumRead));
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
   * @throws EEAException the EEA exception
   */
  @Override
  public void createContributor(Long dataflowId, ContributorVO contributorVO, String role)
      throws EEAException {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);
    SecurityRoleEnum securityRoleEnum = null;
    ResourceGroupEnum resourceGroupEnum = null;
    ResourceGroupEnum resourceGroupEnumDataflow = null;
    ResourceGroupEnum resourceGroupEnumDataset = null;

    switch (role) {
      case "EDITOR":
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
      case "REPORTER":
        securityRoleEnum = Boolean.TRUE.equals(contributorVO.getWritePermission())
            ? SecurityRoleEnum.REPORTER_WRITE
            : SecurityRoleEnum.REPORTER_READ;
        resourceGroupEnum = ResourceGroupEnum.DATASCHEMA_REPORTER;
        resourceGroupEnumDataflow = ResourceGroupEnum.DATAFLOW_REPORTER;
        resourceGroupEnumDataset = Boolean.TRUE.equals(contributorVO.getWritePermission())
            ? ResourceGroupEnum.DATASET_REPORTER_WRITE
            : ResourceGroupEnum.DATASET_REPORTER_READ;
        break;
      default:
        break;
    }
    final List<ResourceAssignationVO> resourceAssignationVOList = new ArrayList<>();
    if ("EDITOR".equals(role)) {

      ResourceInfoVO resourceDataflow =
          resourceManagementControllerZull.getResourceDetail(dataflowId, resourceGroupEnumDataflow);
      if (null == resourceDataflow.getName()) {
        resourceManagementControllerZull
            .createResource(createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, securityRoleEnum));
      }
      resourceAssignationVOList.add(fillResourceAssignation(dataflowId, contributorVO.getAccount(),
          resourceGroupEnumDataflow));
      for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {
        ResourceInfoVO resourceDataSchema = resourceManagementControllerZull
            .getResourceDetail(designDatasetVO.getId(), resourceGroupEnum);
        if (null == resourceDataSchema.getName()) {
          resourceManagementControllerZull.createResource(
              createGroup(designDatasetVO.getId(), ResourceTypeEnum.DATA_SCHEMA, securityRoleEnum));
        }
        resourceAssignationVOList.add(fillResourceAssignation(designDatasetVO.getId(),
            contributorVO.getAccount(), resourceGroupEnum));
      }
    } else if ("REPORTER".equals(role)) {
      ResourceInfoVO resourceDataflow =
          resourceManagementControllerZull.getResourceDetail(dataflowId, resourceGroupEnumDataflow);
      if (null == resourceDataflow.getName()) {
        resourceManagementControllerZull.createResource(
            createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, SecurityRoleEnum.REPORTER_READ));
      }
      resourceAssignationVOList.add(fillResourceAssignation(dataflowId, contributorVO.getAccount(),
          resourceGroupEnumDataflow));

      for (ReportingDatasetVO reportingDatasetVO : dataflow.getReportingDatasets()) {
        ResourceInfoVO resourceDataSchema = resourceManagementControllerZull
            .getResourceDetail(reportingDatasetVO.getId(), resourceGroupEnum);
        if (null == resourceDataSchema.getName()) {
          resourceManagementControllerZull.createResource(
              createGroup(reportingDatasetVO.getId(), ResourceTypeEnum.DATASET, securityRoleEnum));
          resourceManagementControllerZull.createResource(createGroup(dataflowId,
              ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.REPORTER_READ));
        }
        resourceAssignationVOList.add(fillResourceAssignation(reportingDatasetVO.getId(),
            contributorVO.getAccount(), resourceGroupEnum));
        resourceAssignationVOList.add(fillResourceAssignation(reportingDatasetVO.getId(),
            contributorVO.getAccount(), resourceGroupEnumDataset));
      }

    }
    // we add all data to contributor
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
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateContributor(Long dataflowId, ContributorVO contributorVO, String role)
      throws EEAException {
    dataflowControlleZuul.findById(dataflowId);
    // we delete the contributor and after we create to update it
    if ("EDITOR".equals(role) || "REPORTER".equals(role)) {
      try {
        deleteContributor(dataflowId, contributorVO.getAccount(), role);
      } catch (EEAException e) {
        LOG_ERROR.error("Error deleting contributor with the account: {} in the dataflow {} ",
            contributorVO.getAccount(), dataflowId);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
      }
      try {
        createContributor(dataflowId, contributorVO, role);
      } catch (EEAException e) {
        LOG_ERROR.error("Error creating contributor with the account: {} in the dataflow {} ",
            contributorVO.getAccount(), dataflowId);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
      }
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

    // It find all users that have dataflow-xx-editor-read
    List<UserRepresentationVO> usersEditorRead = userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_READ.getGroupName(dataflowId));

    // It find all users that have dataflow-xx-editor-write
    List<UserRepresentationVO> usersEditorWrite = userManagementControllerZull
        .getUsersByGroup(ResourceGroupEnum.DATAFLOW_EDITOR_WRITE.getGroupName(dataflowId));


    List<ResourceAssignationVO> resources = new ArrayList();

    // we create resources for any of users to add the new resource associated with the new
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
      userManagementControllerZull.addContributorsToResources(resources);

      LOG.info("Create role editor for dataflow {} with the dataset id {}", dataflowId, datasetId);
    } else {
      LOG.info(
          "Didn't create role editor for dataflow {} with the dataset id {}, because it hasn't editors associated",
          dataflowId, datasetId);
    }
  }



}
