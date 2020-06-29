package org.eea.dataflow.service;

import java.util.List;
import org.eea.interfaces.vo.dataflow.RoleUserVO;

/**
 * The Interface AccessRightService.
 */
public interface AccessRightService {


  /**
   * Delete role user.
   *
   * @param roleUserVO the role user VO
   * @param dataflowId the dataflow id
   */
  void deleteRoleUser(RoleUserVO roleUserVO, Long dataflowId);

  /**
   * Creates the role user.
   *
   * @param roleUserVO the role user VO
   * @param dataflowId the dataflow id
   */
  void createRoleUser(RoleUserVO roleUserVO, Long dataflowId);

  /**
   * Find role users by id dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<RoleUserVO> findRoleUsersByIdDataflow(Long dataflowId);

  /**
   * Update role user.
   *
   * @param roleUserVO the role user VO
   * @param dataflowId the dataflow id
   */
  void updateRoleUser(RoleUserVO roleUserVO, Long dataflowId);
}
