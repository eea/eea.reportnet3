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
   * @param account the account
   * @param dataflowId the dataflow id
   */
  void deleteContributor(Long dataflowId, String account) throws EEAException;

  /**
   * Creates the role user.
   *
   * @param contributorVO the contributor VO
   * @param dataflowId the dataflow id
   */
  void createContributor(Long dataflowId, ContributorVO contributorVO) throws EEAException;

  /**
   * Find role users by id dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<ContributorVO> findContributorsByIdDataflow(Long dataflowId);

  /**
   * Update role user.
   *
   * @param contributorVO the contributor VO
   * @param dataflowId the dataflow id
   */
  void updateContributor(Long dataflowId, ContributorVO contributorVO) throws EEAException;
}
