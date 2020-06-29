package org.eea.dataflow.service;

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
}
