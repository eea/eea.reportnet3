package org.eea.validation.util;

import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class ValidationRuleDrools.
 */
@Component("foreingKeyDrools")
public class ForeingKeyDrools {

  private static DatasetRepository datasetRepository;

  @Autowired
  private void setDatasetRepository(DatasetRepository datasetRepository) {
    ForeingKeyDrools.datasetRepository = datasetRepository;
  }

  public static Boolean query(String value) {
    // List<DatasetValue> dataset = datasetRepository.findAll();
    if (value.equals("2019")) {
      System.err.println("ENTRA PRIMO");
      return false;
    }
    return true;
  }


}
