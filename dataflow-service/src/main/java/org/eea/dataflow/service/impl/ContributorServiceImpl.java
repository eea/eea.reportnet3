package org.eea.dataflow.service.impl;


import org.eea.dataflow.service.ContributorService;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.contributor.ContributorVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class ContributorServiceImpl.
 */
@Service("ContributorService")
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
   * Delete role user.
   *
   * @param roleUserVO the role user VO
   * @param dataflowId the dataflow id
   */
  @Override
  public void deleteContributor(ContributorVO contributorVO, Long dataflowId) {
    // dataflowControlleZuul.findById(dataflowId);

    // switch (roleUserVO.getRole()) {
    // case "EDITOR":
    // resourceGroupEnum = Boolean.TRUE.equals(roleUserVO.getPermission())
    // ? resourceGroupEnum.DATASCHEMA_EDITOR_WRITE
    // : resourceGroupEnum.DATASCHEMA_EDITOR_READ;
    // break;
    // case "REPORTER_PARTITIONED":
    // break;
    // case "REPORTER":
    // /*
    // * resourceGroupEnum = Boolean.TRUE.equals(representativeVO.getPermission()) ?
    // * resourceGroupEnum.datas : resourceGroupEnum.DATASCHEMA_EDITOR_READ;
    // */
    // break;
    // }
    //
    // if (TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
    // List<ResourceAssignationVO> resourcesProviders = new ArrayList<>();
    // for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {
    // // quitar resource
    // ResourceAssignationVO resourceDP = fillResourceAssignation(designDatasetVO.getId(),
    // roleUserVO.getAccount(), resourceGroupEnum);
    // resourcesProviders.add(resourceDP);
    // }
    // // enviar a bea resourcesProviders;
    // }
  }



  /**
   * Creates the role user.
   *
   * @param representativeVO the representative VO
   * @param dataflowId the dataflow id
   */
  @Override
  public void createContributor(ContributorVO contributorVO, Long dataflowId) {
    dataflowControlleZuul.findById(dataflowId);

    // switch (roleUserVO.getRole()) {
    // case "EDITOR":
    // securityRoleEnum =
    // Boolean.TRUE.equals(roleUserVO.getPermission()) ? SecurityRoleEnum.EDITOR_WRITE
    // : SecurityRoleEnum.EDITOR_READ;
    // resourceGroupEnum = Boolean.TRUE.equals(roleUserVO.getPermission())
    // ? ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE
    // : ResourceGroupEnum.DATASCHEMA_EDITOR_READ;
    // resourceGroupEnumDataflow = Boolean.TRUE.equals(roleUserVO.getPermission())
    // ? ResourceGroupEnum.DATAFLOW_EDITOR_WRITE
    // : ResourceGroupEnum.DATAFLOW_EDITOR_READ;
    // break;
    // case "REPORTER_PARTITIONED":
    // break;
    // case "REPORTER":
    // /*
    // * resourceGroupEnum = Boolean.TRUE.equals(representativeVO.getPermission()) ?
    // * resourceGroupEnum.datas : resourceGroupEnum.DATASCHEMA_EDITOR_READ;
    // */
    // break;
    // }
    // if (TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
    // final List<ResourceAssignationVO> resourceAssignationVOList = new ArrayList();
    // resourceAssignationVOList.add(
    // fillResourceAssignation(dataflowId, roleUserVO.getAccount(), resourceGroupEnumDataflow));
    //
    // resourceManagementControllerZull
    // .createResource(createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, securityRoleEnum));
    //
    // for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {
    //
    // resourceManagementControllerZull.createResource(
    // createGroup(designDatasetVO.getId(), ResourceTypeEnum.DATA_SCHEMA, securityRoleEnum));
    //
    // resourceAssignationVOList.add(fillResourceAssignation(designDatasetVO.getId(),
    // roleUserVO.getAccount(), resourceGroupEnum));
    // }
    // enviar a bea resourcesProviders;


    // resourceManagementControllerZull
    // .createResource(createGroup(dataflow.getId(), ResourceTypeEnum.DATAFLOW, securityRoleEnum));
    // // we add all datas to contributor
    // userManagementControllerZull.addContributorsToResources(resourceAssignationVOList);
    // }

  }


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
}
