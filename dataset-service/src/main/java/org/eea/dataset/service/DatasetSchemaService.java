package org.eea.dataset.service;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
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
   * Gets the data schema by id dataset.
   *
   * @param addRules the add rules
   * @param datasetId the dataset id
   * @return the data schema by id flow
   * @throws EEAException the EEA exception
   */
  DataSetSchemaVO getDataSchemaByDatasetId(Boolean addRules, Long datasetId) throws EEAException;

  /**
   * Delete dataset schema.
   *
   * @param datasetId the dataset id
   * @param schemaId the schema id
   */
  void deleteDatasetSchema(Long datasetId, String schemaId);

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
   * Replace schema.
   *
   * @param idSchema the id schema
   * @param schema the schema
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   */
  void replaceSchema(String idSchema, DataSetSchema schema, Long idDataset, Long idSnapshot);


  /**
   * Gets the dataset schema id.
   *
   * @param datasetId the dataset id
   * @return the dataset schema id
   * @throws EEAException the EEA exception
   */
  String getDatasetSchemaId(Long datasetId) throws EEAException;

  /**
   * Creates the table schema.
   *
   * @param id the id
   * @param tableSchema the table schema
   * @param datasetId the dataset id
   * @return the table schema VO
   */
  TableSchemaVO createTableSchema(String id, TableSchemaVO tableSchema, Long datasetId);

  /**
   * Update name table schema.
   *
   * @param datasetSchemaid the dataset schemaid
   * @param tableSchema the table schema
   * @throws EEAException the EEA exception
   */
  void updateTableSchema(String datasetSchemaid, TableSchemaVO tableSchema) throws EEAException;

  /**
   * Delete table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param idTableSchema the id table schema
   * @throws EEAException the EEA exception
   */
  void deleteTableSchema(String datasetSchemaId, String idTableSchema) throws EEAException;

  /**
   * Order table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @param position the position
   * @return the boolean
   * @throws EEAException the EEA exception
   */
  Boolean orderTableSchema(String datasetSchemaId, String tableSchemaId, Integer position)
      throws EEAException;

  /**
   * Creates the field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  String createFieldSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO) throws EEAException;

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
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return true, if 1 and only 1 fieldSchema has been removed
   * @throws EEAException the EEA exception
   */
  boolean deleteFieldSchema(String datasetSchemaId, String fieldSchemaId) throws EEAException;

  /**
   * Order field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param position the position
   * @return the boolean
   * @throws EEAException the EEA exception
   */
  Boolean orderFieldSchema(String datasetSchemaId, String fieldSchemaId, Integer position)
      throws EEAException;
}
