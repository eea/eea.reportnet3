package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;



/**
 * The Interface WebformService.
 */
public interface WebformService {


  /**
   * Gets the list webforms by dataset id.
   *
   * @return the list webforms by dataset id
   */
  List<WebformMetabaseVO> getListWebforms();
}
