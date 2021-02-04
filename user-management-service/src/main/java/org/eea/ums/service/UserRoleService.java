package org.eea.ums.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.DataflowUserRoleVO;
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
   * Gets the user roles.
   *
   * @param dataProviderId the data provider id
   * @return the user roles
   */
  List<DataflowUserRoleVO> getUserRoles(Long dataProviderId);

  /**
   * Gets the provider id.
   *
   * @return the provider id
   * @throws EEAException
   */
  List<Long> getProviderIds() throws EEAException;

}
