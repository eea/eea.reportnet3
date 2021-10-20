package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;



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
  List<WebformVO> getListWebformsByDatasetId(Long datasetId) throws EEAException;

  void insertWebformConfig(Long id, String name, String content) throws ParseException;

  String findWebformConfigContentById(Long id) throws JsonProcessingException;
}
