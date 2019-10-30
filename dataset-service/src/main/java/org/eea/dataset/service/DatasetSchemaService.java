package org.eea.dataset.service;

import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;

/**
 * The Interface DataschemaService.
 */
public interface DatasetSchemaService {

  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   */
  void createDataSchema(Long datasetId, Long dataflowId);

  /**
   * Creates the empty data set schema.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchemaName the dataset schema name
   * @return the object id
   * @throws EEAException the EEA exception
   */
  ObjectId createEmptyDataSetSchema(Long dataflowId, String datasetSchemaName) throws EEAException;

  /**
   * Gets dataschema by id.
   *
   * @param dataschemaId the dataschema id
   *
   * @return the data schema by id
   */
  DataSetSchemaVO getDataSchemaById(String dataschemaId);

  /**
   * Gets dataschema by id. If addRules is true, the whole schema including rules will be retrieved
   * Otherwise only the schema (table, records, fields and dataset) will be retrieved
   *
   * @param idFlow the id flow
   * @param addRules the add rules
   *
   * @return the data schema by id flow
   */
  DataSetSchemaVO getDataSchemaByIdFlow(Long idFlow, Boolean addRules);

  /**
   * Delete table schema.
   *
   * @param idTableSchema the id table schema
   */
  void deleteTableSchema(String idTableSchema);

  /**
   * Delete dataset schema.
   *
   * @param datasetId the dataset id
   * @param schemaId the schema id
   */
  void deleteDatasetSchema(Long datasetId, String schemaId);

  /**
   * Update name table schema.
   *
   * @param id the id
   * @param tableSchema the table schema
   * @throws EEAException the EEA exception
   */
  void updateTableSchema(String id, TableSchemaVO tableSchema) throws EEAException;

  /**
   * Creates the table schema.
   *
   * @param id the id
   * @param tableSchema the table schema
   * @param datasetId the dataset id
   */
  void createTableSchema(String id, TableSchemaVO tableSchema, Long datasetId);

  /**
   * Creates the group and add user.
   *
   * @param datasetId the dataset id
   */
  void createGroupAndAddUser(Long datasetId);

  /**
   * Creates the field schema.
   *
   * @param idTableSchema the id table schema
   * @param fieldSchema the field schema
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  void createFieldSchema(String idTableSchema, FieldSchemaVO fieldSchema, Long datasetId)
      throws EEAException;

  /**
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return true, if successful
   */
  boolean deleteFieldSchema(String datasetSchemaId, String fieldSchemaId);
}
