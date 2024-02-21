package org.eea.ums.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.ums.UserNationalCoordinatorVO;

import java.util.List;

/**
 * The Interface UserNationalCoordinatorService.
 */
public interface UserNationalCoordinatorService {

  /**
   * Gets the national coordinators.
   *
   * @return the national coordinators
   */
  List<UserNationalCoordinatorVO> getNationalCoordinators();

  /**
   * Creates the national coordinator.
   *
   * @param userNationalCoordinatorVO the user national coordinator VO
   * @throws EEAException
   */
  void createNationalCoordinator(UserNationalCoordinatorVO userNationalCoordinatorVO)
      throws EEAException;

  /**
   * Delete national coordinator.
   *
   * @param userNationalCoordinatorVO the user national coordinator VO
   * @throws EEAException the EEA exception
   */
  void deleteNationalCoordinator(UserNationalCoordinatorVO userNationalCoordinatorVO)
      throws EEAException;

}
