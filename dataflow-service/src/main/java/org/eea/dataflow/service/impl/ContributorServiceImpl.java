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
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
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
   * @return the list
   */
  @Override
  public List<ContributorVO> findContributorsByIdDataflow(Long dataflowId) {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);
    List<ContributorVO> listRoleUserVO = new ArrayList();

    if (TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
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
   */
  @Override
  public void deleteContributor(Long dataflowId, String account) throws EEAException {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);

    ResourceGroupEnum resourceGroupEnumWrite = null;
    ResourceGroupEnum resourceGroupEnumRead = null;
    ResourceGroupEnum resourceGroupEnumDataflowWrite = null;
    ResourceGroupEnum resourceGroupEnumDataflowRead = null;

    switch (dataflow.getStatus().toString()) {
      case "DESIGN":
        resourceGroupEnumWrite = ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE;
        resourceGroupEnumRead = ResourceGroupEnum.DATASCHEMA_EDITOR_READ;
        resourceGroupEnumDataflowWrite = ResourceGroupEnum.DATAFLOW_EDITOR_WRITE;
        resourceGroupEnumDataflowRead = ResourceGroupEnum.DATAFLOW_EDITOR_READ;

        break;
      case "DRAFT":
        break;
      default:
        break;
    }

    if (TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
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
   * @param contributorVO the contributor VO
   * @param dataflowId the dataflow id
   */
  @Override
  public void createContributor(Long dataflowId, ContributorVO contributorVO) throws EEAException {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);
    SecurityRoleEnum securityRoleEnum = null;
    ResourceGroupEnum resourceGroupEnum = null;
    ResourceGroupEnum resourceGroupEnumDataflow = null;

    switch (contributorVO.getRole()) {
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
        /*
         * resourceGroupEnum = Boolean.TRUE.equals(representativeVO.getPermission()) ?
         * resourceGroupEnum.datas : resourceGroupEnum.DATASCHEMA_EDITOR_READ;
         */
        break;
    }
    if (TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
      final List<ResourceAssignationVO> resourceAssignationVOList = new ArrayList();
      resourceAssignationVOList.add(fillResourceAssignation(dataflowId, contributorVO.getAccount(),
          resourceGroupEnumDataflow));

      ResourceInfoVO resourceDataflow =
          resourceManagementControllerZull.getResourceDetail(dataflowId, resourceGroupEnumDataflow);
      if (null == resourceDataflow.getName()) {
        resourceManagementControllerZull
            .createResource(createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, securityRoleEnum));
      }
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
      // we add all datas to contributor
      userManagementControllerZull.addContributorsToResources(resourceAssignationVOList);
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
   * @param contributorVO the contributor VO
   * @param dataflowId the dataflow id
   */
  @Override
  public void updateContributor(Long dataflowId, ContributorVO contributorVO) throws EEAException {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);
    // we delete the contributor and after we create to update it
    if (TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
      try {
        deleteContributor(dataflowId, contributorVO.getAccount());
      } catch (EEAException e) {
        LOG_ERROR.error("Error deleting contributor with the account: {} in the dataflow {} ",
            contributorVO.getAccount(), dataflowId);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
      }
      try {
        createContributor(dataflowId, contributorVO);
      } catch (EEAException e) {
        LOG_ERROR.error("Error creating contributor with the account: {} in the dataflow {} ",
            contributorVO.getAccount(), dataflowId);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
      }
    }

  }



}
