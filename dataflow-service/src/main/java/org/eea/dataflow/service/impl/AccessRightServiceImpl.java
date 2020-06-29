package org.eea.dataflow.service.impl;


import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.AccessRightService;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.RoleUserVO;
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
import org.springframework.stereotype.Service;

/**
 * The Class AccessRightServiceImpl.
 */
@Service("AccessRightService")
public class AccessRightServiceImpl implements AccessRightService {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AccessRightServiceImpl.class);
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
   * Find role users by id dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  public List<RoleUserVO> findRoleUsersByIdDataflow(Long dataflowId) {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);
    List<RoleUserVO> listRoleUserVO = new ArrayList();

    if (TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
      StringBuilder stringBuilder =
          new StringBuilder("Dataflow-").append(dataflowId).append("-EDITOR_WRITE");
      List<UserRepresentationVO> listUserWrite =
          userManagementControllerZull.getUsersByGroup(stringBuilder.toString());
      listUserWrite.stream().forEach(userWrite -> {
        RoleUserVO roleUserVO = new RoleUserVO();
        roleUserVO.setAccount(userWrite.getEmail());
        roleUserVO.setPermission(true);
        listRoleUserVO.add(roleUserVO);
      });
      stringBuilder = new StringBuilder("Dataflow-").append(dataflowId).append("-EDITOR_READ");
      List<UserRepresentationVO> listUserRead =
          userManagementControllerZull.getUsersByGroup(stringBuilder.toString());
      listUserRead.stream().forEach(userRead -> {
        RoleUserVO roleUserVO = new RoleUserVO();
        roleUserVO.setAccount(userRead.getEmail());
        roleUserVO.setPermission(false);
        roleUserVO.setRole("EDITOR");
        listRoleUserVO.add(roleUserVO);
      });
    }

    return listRoleUserVO;
  }

  /**
   * Delete role user.
   *
   * @param roleUserVO the role user VO
   * @param dataflowId the dataflow id
   */
  @Override
  public void deleteRoleUser(RoleUserVO roleUserVO, Long dataflowId) {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);

    ResourceGroupEnum resourceGroupEnum = null;
    ResourceGroupEnum resourceGroupEnumDataflow = null;

    switch (roleUserVO.getRole()) {
      case "EDITOR":
        resourceGroupEnum = Boolean.TRUE.equals(roleUserVO.getPermission())
            ? ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE
            : ResourceGroupEnum.DATASCHEMA_EDITOR_READ;
        resourceGroupEnumDataflow = Boolean.TRUE.equals(roleUserVO.getPermission())
            ? ResourceGroupEnum.DATAFLOW_EDITOR_WRITE
            : ResourceGroupEnum.DATAFLOW_EDITOR_READ;

        break;
      case "REPORTER_PARTITIONED":
        break;
      case "REPORTER":
        /*
         * resourceGroupEnum = Boolean.TRUE.equals(representativeVO.getPermission()) ?
         * resourceGroupEnum.datas : resourceGroupEnum.DATASCHEMA_EDITOR_READ;
         */
        break;
    }

    if (TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
      List<ResourceAssignationVO> resourcesProviders = new ArrayList<>();
      resourcesProviders.add(
          fillResourceAssignation(dataflowId, roleUserVO.getAccount(), resourceGroupEnumDataflow));
      for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {
        // quitar resources

        resourcesProviders.add(fillResourceAssignation(designDatasetVO.getId(),
            roleUserVO.getAccount(), resourceGroupEnum));
      }
      userManagementControllerZull.removeContributorsFromResources(resourcesProviders);
    }
  }



  /**
   * Creates the role user.
   *
   * @param roleUserVO the role user VO
   * @param dataflowId the dataflow id
   */
  @Override
  public void createRoleUser(RoleUserVO roleUserVO, Long dataflowId) {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);
    SecurityRoleEnum securityRoleEnum = null;
    ResourceGroupEnum resourceGroupEnum = null;
    ResourceGroupEnum resourceGroupEnumDataflow = null;

    switch (roleUserVO.getRole()) {
      case "EDITOR":
        securityRoleEnum =
            Boolean.TRUE.equals(roleUserVO.getPermission()) ? SecurityRoleEnum.EDITOR_WRITE
                : SecurityRoleEnum.EDITOR_READ;
        resourceGroupEnum = Boolean.TRUE.equals(roleUserVO.getPermission())
            ? ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE
            : ResourceGroupEnum.DATASCHEMA_EDITOR_READ;
        resourceGroupEnumDataflow = Boolean.TRUE.equals(roleUserVO.getPermission())
            ? ResourceGroupEnum.DATAFLOW_EDITOR_WRITE
            : ResourceGroupEnum.DATAFLOW_EDITOR_READ;
        break;
      case "REPORTER_PARTITIONED":
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
      resourceAssignationVOList.add(
          fillResourceAssignation(dataflowId, roleUserVO.getAccount(), resourceGroupEnumDataflow));

      resourceManagementControllerZull
          .createResource(createGroup(dataflowId, ResourceTypeEnum.DATAFLOW, securityRoleEnum));

      for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {

        resourceManagementControllerZull.createResource(
            createGroup(designDatasetVO.getId(), ResourceTypeEnum.DATA_SCHEMA, securityRoleEnum));

        resourceAssignationVOList.add(fillResourceAssignation(designDatasetVO.getId(),
            roleUserVO.getAccount(), resourceGroupEnum));
      }


      resourceManagementControllerZull.createResource(
          createGroup(dataflow.getId(), ResourceTypeEnum.DATAFLOW, securityRoleEnum));
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
   * Update role user.
   *
   * @param roleUserVO the role user VO
   * @param dataflowId the dataflow id
   */
  @Override
  public void updateRoleUser(RoleUserVO roleUserVO, Long dataflowId) {
    // TODO Auto-generated method stub

  }



}
