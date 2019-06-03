package org.eea.validation.service;


import org.springframework.stereotype.Service;

/**
 * The Class ValidationService.
 */
@Service
public interface ValidationService {

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   */
  void validateDataSetData(Long datasetId);

}
