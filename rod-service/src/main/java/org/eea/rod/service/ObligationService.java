package org.eea.rod.service;

import java.util.Date;
import org.eea.interfaces.vo.rod.ObligationListVO;
import org.eea.interfaces.vo.rod.ObligationVO;

/**
 * The interface Obligation service.
 */
public interface ObligationService {

  /**
   * Find opened obligation.
   *
   * @param clientId the client id
   * @param spatialId the spatial id
   * @param issueId the issue id
   * @param deadlineDateFrom the deadline date from
   * @param deadlineDateTo the deadline date to
   * @return the obligation list VO
   */
  ObligationListVO findOpenedObligation(Integer clientId, Integer spatialId, Integer issueId,
      Date deadlineDateFrom, Date deadlineDateTo);

  /**
   * Find obligation by id obligation vo.
   *
   * @param obligationId the obligation id
   *
   * @return the obligation vo
   */
  ObligationVO findObligationById(Integer obligationId);
}
