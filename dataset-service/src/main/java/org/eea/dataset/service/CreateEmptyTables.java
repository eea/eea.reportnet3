package org.eea.dataset.service;

import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;

public interface CreateEmptyTables {
  /**
   * Create the tables in Dremio if not exists
   *
   * @throws EEAException exception
   */
  void runCreationForAllDatasets(Long datasetId) throws EEAException;

  void runCreationForOneDataset(DataSetMetabaseVO dataset) throws EEAException;
}
