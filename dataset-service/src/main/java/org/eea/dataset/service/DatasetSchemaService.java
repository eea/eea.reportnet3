package org.eea.dataset.service;

import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;

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
   * @param datasetSchemaId the dataset schema id
   * @param idTableSchema the id table schema
   * @throws EEAException the EEA exception
   */
  void deleteTableSchema(String datasetSchemaId, String idTableSchema) throws EEAException;

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
   * @param datasetSchemaid the dataset schemaid
   * @param tableSchema the table schema
   * @throws EEAException the EEA exception
   */
  void updateTableSchema(String datasetSchemaid, TableSchemaVO tableSchema) throws EEAException;

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
   * Delete group and remove user.
   *
   * @param datasetId the dataset id
   * @param role the role
   */
  void deleteGroup(Long datasetId, ResourceGroupEnum... role);

  /**
   * Creates the field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @param fieldSchemaVO the field schema VO
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  boolean createFieldSchema(String datasetSchemaId, String tableSchemaId,
      FieldSchemaVO fieldSchemaVO) throws EEAException;

  /**
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return true, if 1 and only 1 fieldSchema has been removed
   * @throws EEAException the EEA exception
   */
  boolean deleteFieldSchema(String datasetSchemaId, String fieldSchemaId) throws EEAException;

  /**
   * Update field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @return The fieldSchema type if the operation worked, null if not.
   * @throws EEAException the EEA exception
   */
  String updateFieldSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO) throws EEAException;

  /**
   * Order.
   *
   * @param schema the schema
   * @param newPosition the new position
   * @return the boolean
   */
  Boolean order(String idDatasetSchema, Object schema, int newPosition) throws EEAException;
}
