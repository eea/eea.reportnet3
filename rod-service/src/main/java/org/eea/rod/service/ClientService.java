package org.eea.rod.service;

import java.util.List;
import org.eea.interfaces.vo.rod.ClientVO;
import org.eea.interfaces.vo.rod.CountryVO;

/**
 * The type Client service.
 */
public interface ClientService {

  /**
   * Find all list.
   *
   * @return the list
   */
  List<ClientVO> findAll();

}
