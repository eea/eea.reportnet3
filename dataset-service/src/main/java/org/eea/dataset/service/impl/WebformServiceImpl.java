package org.eea.dataset.service.impl;

import java.util.List;
import org.eea.dataset.mapper.WebformMetabaseMapper;
import org.eea.dataset.persistence.metabase.repository.WebformRepository;
import org.eea.dataset.service.WebformService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



/**
 * The Class WebformServiceImpl.
 */
@Service
public class WebformServiceImpl implements WebformService {

  /** The webform repository. */
  @Autowired
  WebformRepository webformRepository;


  /** The webform mapper. */
  @Autowired
  private WebformMetabaseMapper webformMetabaseMapper;


  /**
   * Gets the list webforms by dataset id.
   *
   * @param datasetId the dataset id
   * @return the list webforms by dataset id
   * @throws EEAException the EEA exception
   */
  @Override
  public List<WebformMetabaseVO> getListWebforms() throws EEAException {


    return webformMetabaseMapper.entityListToClass(webformRepository.findAll());
  }
}
