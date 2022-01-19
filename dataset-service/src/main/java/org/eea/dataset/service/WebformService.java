package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import com.fasterxml.jackson.core.JsonProcessingException;



/**
 * The Interface WebformService.
 */
public interface WebformService {

  /**
   * Insert webform config.
   *
   * @param name the name
   * @param content the content
   * @throws EEAException the EEA exception
   */
  void insertWebformConfig(String name, String content) throws EEAException;

  /**
   * Update webform config.
   *
   * @param id the id
   * @param name the name
   * @param content the content
   */
  void updateWebformConfig(Long id, String name, String content) throws EEAException;

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

  /**
   * Delete webform config.
   *
   * @param id the id
   * @throws EEAException the EEA exception
   */
  void deleteWebformConfig(Long id) throws EEAException;
}
