package org.eea.dataflow.service;

import org.eea.interfaces.vo.contributor.ContributorVO;


/**
 * The Interface ContributorService.
 */
public interface ContributorService {


  /**
   * Delete role user.
   *
   * @param roleUserVO the role user VO
   * @param dataflowId the dataflow id
   */
  void deleteContributor(ContributorVO contributorVO, Long dataflowId);

  /**
   * Creates the role user.
   *
   * @param roleUserVO the role user VO
   * @param dataflowId the dataflow id
   */
  void createContributor(ContributorVO contributorVO, Long dataflowId);
}
