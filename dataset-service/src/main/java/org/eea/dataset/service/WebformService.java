package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;



/**
 * The Interface WebformService.
 */
public interface WebformService {


  /**
   * Gets the list webforms by dataset id.
   *
   * @param datasetId the dataset id
   * @return the list webforms by dataset id
   * @throws EEAException the EEA exception
   */
  List<WebformMetabaseVO> getListWebforms() throws EEAException;
}
