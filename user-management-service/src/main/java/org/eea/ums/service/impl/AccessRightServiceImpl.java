package org.eea.ums.service.impl;


import java.util.ArrayList;
import java.util.List;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.ums.service.AccessRightService;
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

  @Autowired
  private DataFlowControllerZuul dataflowControlleZuul;

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
      for (DesignDatasetVO designDatasetVO : dataflow.getDesignDatasets()) {
        // quitar resource
        List<ResourceAssignationVO> resourcesProviders = new ArrayList<>();
        ResourceAssignationVO resourceDP = fillResourceAssignation(designDatasetVO.getId(),
            representativeVO.getAccount(), resourceGroupEnum);
        resourcesProviders.add(resourceDP);
      }
      // enviar a bea resourcesProviders;
    }
  }

  private ResourceAssignationVO fillResourceAssignation(Long id, String email,
      ResourceGroupEnum group) {

    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setResourceId(id);
    resource.setEmail(email);
    resource.setResourceGroup(group);

    return resource;
  }

}
