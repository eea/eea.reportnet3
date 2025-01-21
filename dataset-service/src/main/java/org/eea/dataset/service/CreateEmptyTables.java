package org.eea.dataset.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;

public interface CreateEmptyTables {
  /**
   * Generates all the tables and datasets for the specific type of dataset
   *
   * @throws EEAException exception
   */
  void runCreationForAllDatasets(DataSetMetabaseVO dataset) throws EEAException;

  /**
   * Generates all tables for the given dataset
   *
   * @param dataset The dataset Object
   * @throws EEAException exception
   */
  void runCreationForOneDataset(DataSetMetabaseVO dataset) throws EEAException;
}
