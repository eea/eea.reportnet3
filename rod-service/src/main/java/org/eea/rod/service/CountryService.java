package org.eea.rod.service;

import java.util.List;
import org.eea.interfaces.vo.rod.CountryVO;

/**
 * The type Country service.
 */
public interface CountryService {

  /**
   * Find all list.
   *
   * @return the list
   */
  List<CountryVO> findAll();

}
