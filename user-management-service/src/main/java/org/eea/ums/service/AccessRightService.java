package org.eea.ums.service;

import org.eea.interfaces.vo.dataflow.RepresentativeVO;

/**
 * The Interface AccessRightService.
 */
public interface AccessRightService {


  /**
   * Delete role user.
   *
   * @param representativeVO the representative VO
   * @param dataflowId the dataflow id
   */
  void deleteRoleUser(RepresentativeVO representativeVO, Long dataflowId);
}
