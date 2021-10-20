package org.eea.dataset.service;

import java.util.List;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.core.JsonProcessingException;



/**
 * The Interface WebformService.
 */
public interface WebformService {

  /**
   * Insert webform config.
   *
   * @param id the id
   * @param name the name
   * @param content the content
   * @throws ParseException the parse exception
   */
  void insertWebformConfig(Long id, String name, String content) throws ParseException;

  /**
   * Find webform config content by id.
   *
   * @param id the id
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  String findWebformConfigContentById(Long id) throws JsonProcessingException;

  /**
   * Gets the list webforms.
   *
   * @return the list webforms
   */
  List<WebformMetabaseVO> getListWebforms();
}
