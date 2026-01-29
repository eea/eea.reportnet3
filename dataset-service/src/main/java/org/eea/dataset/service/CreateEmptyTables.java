package org.eea.dataset.service;

import org.eea.datalake.service.model.S3PathResolver;
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

  /**
   * Deletes parquet table if empty to cover the case that the user has added or removed columns (has changed the schema)
   *
   * @param tableSchemaName The table schema name
   * @param tablePathResolver The table path
   * @throws Exception exception
   */
  void deleteTableIfEmpty(String tableSchemaName, S3PathResolver tablePathResolver) throws Exception;

  void runCreationForPreparationDataset(DataSetMetabaseVO parentDataset, String preparationSetName) throws EEAException;

  void runCreationForPreparationDatasetTable(DataSetMetabaseVO parentDataset, String tableSchemaId, String preparationSetName) throws EEAException;

}
