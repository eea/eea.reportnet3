package org.eea.dataset.service;

import java.util.List;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.WebformTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.WebformConfigVO;
import org.eea.interfaces.vo.dataset.schemas.WebformMetabaseVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;


/**
 * The Interface WebformService.
 */
public interface WebformService {

  /**
   * Insert webform config.
   *
   * @param name the name
   * @param content the content
   * @param type the type
   * @throws EEAException the EEA exception
   */
  void insertWebformConfig(String name, String content, WebformTypeEnum type) throws EEAException;

  /**
   * Update webform config.
   *
   * @param id the id
   * @param name the name
   * @param content the content
   * @param type the type
   * @throws EEAException the EEA exception
   */
  void updateWebformConfig(Long id, String name, String content, WebformTypeEnum type)
      throws EEAException;

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

  /**
   * Uploads the selected webform to the selected dataset
   *
   * @param webformConfigVO The webform to upload
   * @param datasetId GThe selected datasetId
   * @return The response
   */
  ResponseEntity<?> uploadWebFormConfig(WebformConfigVO webformConfigVO, Long datasetId) ;

  /**
   * Get webform by name and version
   * @param webFormName The webform name
   * @param version The webform version
   * @return The response
   */
  ResponseEntity<?> getWebformConfigHistorySchema(String webFormName, Long version);

  /**
   * Restore a specific version and keep history for that
   * @param webFormName The webForm name
   * @param version The version to restore
   * @param datasetId The dataset id
   */
  void restorePreviousWebFormVersions(String webFormName, Long version, Long datasetId);
}
