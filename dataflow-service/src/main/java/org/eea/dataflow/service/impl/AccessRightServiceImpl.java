package org.eea.dataflow.service.impl;


import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.service.AccessRightService;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
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
   * Delete role user.
   *
   * @param representativeVO the representative VO
   * @param dataflowId the dataflow id
   */
  @Override
  public void deleteRoleUser(RepresentativeVO representativeVO, Long dataflowId) {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);

    ResourceGroupEnum resourceGroupEnum = null;

    switch (representativeVO.getRole()) {
      case "EDITOR":
        resourceGroupEnum = Boolean.TRUE.equals(representativeVO.getPermission())
            ? resourceGroupEnum.DATASCHEMA_EDITOR_WRITE
            : resourceGroupEnum.DATASCHEMA_EDITOR_READ;
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
      for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {
        // quitar resource
        ResourceAssignationVO resourceDP = fillResourceAssignation(designDatasetVO.getId(),
            representativeVO.getAccount(), resourceGroupEnum);
        resourcesProviders.add(resourceDP);
      }
      // enviar a bea resourcesProviders;
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
   * Creates the role user.
   *
   * @param representativeVO the representative VO
   * @param dataflowId the dataflow id
   */
  @Override
  public void createRoleUser(RepresentativeVO representativeVO, Long dataflowId) {
    DataFlowVO dataflow = dataflowControlleZuul.findById(dataflowId);
    SecurityRoleEnum securityRoleEnum = null;
    ResourceGroupEnum resourceGroupEnum = null;
    switch (representativeVO.getRole()) {
      case "EDITOR":
        securityRoleEnum =
            Boolean.TRUE.equals(representativeVO.getPermission()) ? securityRoleEnum.EDITOR_WRITE
                : securityRoleEnum.EDITOR_READ;
        resourceGroupEnum = Boolean.TRUE.equals(representativeVO.getPermission())
            ? resourceGroupEnum.DATASCHEMA_EDITOR_WRITE
            : resourceGroupEnum.DATASCHEMA_EDITOR_READ;
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

    resourceManagementControllerZull
        .createResource(createGroup(dataflow.getId(), ResourceTypeEnum.DATAFLOW, securityRoleEnum));
    userManagementControllerZull.addUserToResource(dataflowId, resourceGroupEnum);

    if (TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
      List<ResourceAssignationVO> resourcesProviders = new ArrayList<>();
      for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {

        resourceManagementControllerZull.createResource(
            createGroup(designDatasetVO.getId(), ResourceTypeEnum.DATA_SCHEMA, securityRoleEnum));

        // Add user to new group Dataschema-X-DATA_CUSTODIAN
        userManagementControllerZull.addUserToResource(designDatasetVO.getId(), resourceGroupEnum);
      }
      // enviar a bea resourcesProviders;
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
}
