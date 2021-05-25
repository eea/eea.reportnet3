package org.eea.dataflow.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.contributor.ContributorVO;

/**
 * The Interface ContributorService.
 */
public interface ContributorService {


  /**
   * Delete role user.
   *
   * @param dataflowId the dataflow id
   * @param account the account
   * @param role the role
   * @param dataProviderId the data provider id
   * @throws EEAException the EEA exception
   */
  void deleteContributor(Long dataflowId, String account, String role, Long dataProviderId)
      throws EEAException;

  /**
   * Creates the role user.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param dataProviderId the data provider id
   * @param persistDataflowPermission the persist dataflow permission
   * @throws EEAException the EEA exception
   */
  void createContributor(Long dataflowId, ContributorVO contributorVO, Long dataProviderId,
      Boolean persistDataflowPermission) throws EEAException;

  /**
   * Find role users by id dataflow.
   *
   * @param dataflowId the dataflow id
   * @param dataproviderId the dataprovider id
   * @param role the role
   * @return the list
   */
  List<ContributorVO> findContributorsByResourceId(Long dataflowId, Long dataproviderId,
      String role);

  /**
   * Update role user.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param dataProviderId the data provider id
   * @throws EEAException the EEA exception
   */
  void updateContributor(Long dataflowId, ContributorVO contributorVO, Long dataProviderId)
      throws EEAException;

  /**
   * Creates the associated permissions.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  void createAssociatedPermissions(Long dataflowId, Long datasetId) throws EEAException;

}
