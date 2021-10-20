package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;



/**
 * The Interface WebformService.
 */
public interface WebformService {


  /**
   * Gets the webforms list.
   *
   * @return the webforms list
   */
  List<WebformMetabaseVO> getListWebforms();
}
