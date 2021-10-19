package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.service.WebformService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
import org.springframework.stereotype.Service;



/**
 * The Class WebformServiceImpl.
 */
@Service
public class WebformServiceImpl implements WebformService {


  /**
   * Gets the list webforms by dataset id.
   *
   * @param datasetId the dataset id
   * @return the list webforms by dataset id
   * @throws EEAException the EEA exception
   */
  @Override
  public List<WebformVO> getListWebformsByDatasetId(Long datasetId) throws EEAException {

    List<WebformVO> webformsList = new ArrayList<>();

    return webformsList;
  }
}
