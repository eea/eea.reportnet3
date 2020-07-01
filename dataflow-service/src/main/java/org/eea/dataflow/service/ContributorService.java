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
   * @throws EEAException the EEA exception
   */
  void deleteContributor(Long dataflowId, String account, String role) throws EEAException;

  /**
   * Creates the role user.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @throws EEAException the EEA exception
   */
  void createContributor(Long dataflowId, ContributorVO contributorVO, String role)
      throws EEAException;

  /**
   * Find role users by id dataflow.
   *
   * @param dataflowId the dataflow id
   * @param role
   * @return the list
   */
  List<ContributorVO> findContributorsByIdDataflow(Long dataflowId, String role);

  /**
   * Update role user.
   *
   * @param dataflowId the dataflow id
   * @param contributorVO the contributor VO
   * @param role the role
   * @throws EEAException the EEA exception
   */
  void updateContributor(Long dataflowId, ContributorVO contributorVO, String role)
      throws EEAException;


  /**
   * Creates the associated permissions.
   *
   * @param dataflowId the dataflow id
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  void createAssociatedPermissions(Long dataflowId, Long datasetId) throws EEAException;
}
