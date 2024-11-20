package org.eea.dataset.service;

import org.springframework.http.ResponseEntity;

public interface TableDataRetriever {
  /**
   * After release, we need to know if the table have been changed or not
   *
   * @param dpDatasetId The dataset It
   * @return A hashmap of table schema id and a boolean true if table has been changed
   */
  ResponseEntity<?> getTablesUpdatedAfterRelease(Long dpDatasetId);
}
