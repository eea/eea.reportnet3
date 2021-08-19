package org.eea.ums.service;

import java.util.List;
import org.eea.interfaces.vo.ums.UserRoleVO;

/**
 * The Interface BackupManagmentControlerService.
 */
public interface UserRoleService {

  /**
   * Gets the user roles by dataflow country.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the user roles by dataflow country
   */
  List<UserRoleVO> getUserRolesByDataflowCountry(Long dataflowId, Long dataProviderId);

  /**
   * Gets the user roles by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the user roles by dataflow
   */
  List<UserRoleVO> getUserRolesByDataflow(Long dataflowId);


}
