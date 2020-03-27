package org.eea.rod.service;

import java.util.List;
import org.eea.interfaces.vo.rod.ObligationVO;

/**
 * The interface Obligation service.
 */
public interface ObligationService {

  /**
   * Find opened obligation list.
   *
   * @return the list
   */
  List<ObligationVO> findOpenedObligation();

  /**
   * Find obligation by id obligation vo.
   *
   * @param obligationId the obligation id
   *
   * @return the obligation vo
   */
  ObligationVO findObligationById(Integer obligationId);
}
