package org.eea.rod.service;

import java.util.List;
import org.eea.interfaces.vo.rod.IssueVO;

/**
 * The type Issue service.
 */
public interface IssueService {

  /**
   * Find all list.
   *
   * @return the list
   */
  List<IssueVO> findAll();

}
