package org.eea.dataset.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;

public interface CreateEmptyTables {

  /**
   * Generates all tables for the given dataset
   *
   * @param dataset The dataset Object
   * @throws EEAException exception
   */
  void runCreationForOneDataset(DataSetMetabaseVO dataset) throws EEAException;

  /***
   * Given the dataset and table schemaId recreate the table if needed
   *
   * @param dataset the datasetId
   * @param tableSchemaId The table schemaId
   * @throws EEAException Eea exception
   */
  void runCreationForSpecificTableSchema(DataSetMetabaseVO dataset, String tableSchemaId) throws EEAException;
}
