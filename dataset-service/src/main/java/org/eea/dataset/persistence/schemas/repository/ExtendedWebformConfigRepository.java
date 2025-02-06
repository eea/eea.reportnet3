package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfigHistory;


/**
 * The Interface ExtendedWebformConfigRepository.
 */
public interface ExtendedWebformConfigRepository {

  /**
   * Update web form config.
   *
   * @param webform the webform
   */
  void updateWebFormConfig(WebformConfig webform);

  /**
   * Keep history for webFormConfig
   * @param webFormMongo the webform to keep
   */
  void saveWebFormConfigHistory(WebformConfig webFormMongo);

  /***
   * Get webFormSchema by version and id
   * @param webFormId the webForm id
   * @param version The version
   * @return The webForm schema object
   */
  WebformConfigHistory getWebFormConfigHistory(ObjectId webFormId, Long version);

}
