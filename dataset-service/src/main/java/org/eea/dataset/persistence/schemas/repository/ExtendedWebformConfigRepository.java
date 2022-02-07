package org.eea.dataset.persistence.schemas.repository;

import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;


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

}
