package org.eea.dataset.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.SimpleDatasetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;

/**
 * The Interface DataschemaService.
 */
public interface DatasetSchemaService {

  /**
   * Creates the empty data set schema.
   *
   * @param dataflowId the dataflow id
   *
   * @return the object id
   *
   * @throws EEAException the EEA exception
   */
  ObjectId createEmptyDataSetSchema(Long dataflowId) throws EEAException;

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
   *
   * @return the data schema by id flow
   *
   * @throws EEAException the EEA exception
   */
  DataSetSchemaVO getDataSchemaByDatasetId(Boolean addRules, Long datasetId) throws EEAException;

  /**
   * Delete dataset schema.
   *
   * @param schemaId the schema id
   * @param datasetId the dataset id
   */
  void deleteDatasetSchema(String schemaId, Long datasetId);

  /**
   * Delete group and remove user.
   *
   * @param datasetId the dataset id
   * @param resourceTypeEnum the resource type enum
   */
  void deleteGroup(Long datasetId, ResourceTypeEnum resourceTypeEnum);

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
   *
   * @return the dataset schema id
   *
   * @throws EEAException the EEA exception
   */
  String getDatasetSchemaId(Long datasetId) throws EEAException;

  /**
   * Creates the table schema.
   *
   * @param id the id
   * @param tableSchema the table schema
   * @param datasetId the dataset id
   *
   * @return the table schema VO
   */
  TableSchemaVO createTableSchema(String id, TableSchemaVO tableSchema, Long datasetId);

  /**
   * Update name table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   * @throws EEAException the EEA exception
   */
  void updateTableSchema(Long datasetId, TableSchemaVO tableSchema) throws EEAException;

  /**
   * Delete table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the id table schema
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  void deleteTableSchema(String datasetSchemaId, String tableSchemaId, Long datasetId)
      throws EEAException;

  /**
   * Order table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @param position the position
   *
   * @return the boolean
   *
   * @throws EEAException the EEA exception
   */
  Boolean orderTableSchema(String datasetSchemaId, String tableSchemaId, Integer position)
      throws EEAException;

  /**
   * Creates the field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   *
   * @return true, if successful
   *
   * @throws EEAException the EEA exception
   */
  String createFieldSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO) throws EEAException;

  /**
   * Update field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @param datasetId the dataset id
   * @param cloningOrImporting the cloning or importing
   * @return the type data
   * @throws EEAException the EEA exception
   */
  DataType updateFieldSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO, Long datasetId,
      boolean cloningOrImporting) throws EEAException;

  /**
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param datasetId the dataset id
   * @return true, if 1 and only 1 fieldSchema has been removed
   * @throws EEAException the EEA exception
   */
  boolean deleteFieldSchema(String datasetSchemaId, String fieldSchemaId, Long datasetId)
      throws EEAException;

  /**
   * Order field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param position the position
   *
   * @return the boolean
   *
   * @throws EEAException the EEA exception
   */
  Boolean orderFieldSchema(String datasetSchemaId, String fieldSchemaId, Integer position)
      throws EEAException;

  /**
   * Update dataset schema description.
   *
   * @param datasetSchemaId the dataset schema id
   * @param description the description
   *
   */
  void updateDatasetSchemaDescription(String datasetSchemaId, String description);

  /**
   * Gets the table schema name.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   *
   * @return the table schema name
   */
  String getTableSchemaName(String datasetSchemaId, String tableSchemaId);


  /**
   * Validate schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param dataflowType the dataflow type
   * @return the boolean
   */
  Boolean validateSchema(String datasetSchemaId, TypeDataflowEnum dataflowType);


  /**
   * Propagate rules after update schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @param type the type
   * @param datasetId the dataset id
   */
  void propagateRulesAfterUpdateSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO,
      DataType type, Long datasetId);

  /**
   * Check pk allow update.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   *
   * @return the boolean
   */
  Boolean checkPkAllowUpdate(String datasetSchemaId, FieldSchemaVO fieldSchemaVO);


  /**
   * Adds the to pk catalogue.
   *
   * @param fieldSchemaVO the field schema VO
   * @param datasetId the dataset id
   */
  void addToPkCatalogue(FieldSchemaVO fieldSchemaVO, Long datasetId);

  /**
   * Check existing pk referenced.
   *
   * @param fieldSchemaVO the field schema VO
   *
   * @return the boolean
   */
  Boolean checkExistingPkReferenced(FieldSchemaVO fieldSchemaVO);

  /**
   * Adds the foreign relation.
   *
   * @param idDatasetOrigin the id dataset origin
   * @param fieldSchemaVO the field schema VO
   */
  void addForeignRelation(Long idDatasetOrigin, FieldSchemaVO fieldSchemaVO);

  /**
   * Gets the field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param idFieldSchema the id field schema
   *
   * @return the field schema
   */
  FieldSchemaVO getFieldSchema(String datasetSchemaId, String idFieldSchema);

  /**
   * Delete from pk catalogue.
   *
   * @param fieldSchemaVO the field schema VO
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  void deleteFromPkCatalogue(FieldSchemaVO fieldSchemaVO, Long datasetId) throws EEAException;

  /**
   * Delete foreign relation.
   *
   * @param idDatasetOrigin the id dataset origin
   * @param fieldSchemaVO the field schema VO
   */
  void deleteForeignRelation(Long idDatasetOrigin, FieldSchemaVO fieldSchemaVO);

  /**
   * Update foreign relation.
   *
   * @param idDatasetOrigin the id dataset origin
   * @param fieldSchemaVO the field schema VO
   * @param datasetSchemaId the dataset schema id
   */
  void updateForeignRelation(Long idDatasetOrigin, FieldSchemaVO fieldSchemaVO,
      String datasetSchemaId);

  /**
   * Gets the referenced fields by schema.
   *
   * @param datasetSchemaId the dataset schema id
   *
   * @return the referenced fields by schema
   */
  List<ReferencedFieldSchema> getReferencedFieldsBySchema(String datasetSchemaId);


  /**
   * Checks if is schema allowed for deletion.
   *
   * @param idDatasetSchema the id dataset schema
   *
   * @return the boolean
   */
  Boolean isSchemaAllowedForDeletion(String idDatasetSchema);

  /**
   * Update pk catalogue deleting schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  void updatePkCatalogueDeletingSchema(String idDatasetSchema, Long datasetId) throws EEAException;


  /**
   * Delete from pk catalogue.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  void deleteFromPkCatalogue(String datasetSchemaId, String tableSchemaId, Long datasetId)
      throws EEAException;


  /**
   * Update PK catalogue and foreigns after snapshot.
   *
   * @param idDatasetSchema the id dataset schema
   * @param idDataset the id dataset
   *
   * @throws EEAException the EEA exception
   */
  void updatePKCatalogueAndForeignsAfterSnapshot(String idDatasetSchema, Long idDataset)
      throws EEAException;

  /**
   * Creates the unique constraint.
   *
   * @param uniqueConstraint the unique constraint
   */
  void createUniqueConstraint(UniqueConstraintVO uniqueConstraint);

  /**
   * Delete unique constraint.
   *
   * @param uniqueId the unique id
   * @throws EEAException the EEA exception
   */
  void deleteUniqueConstraint(String uniqueId) throws EEAException;

  /**
   * Update unique constraint.
   *
   * @param uniqueConstraint the unique constraint
   */
  void updateUniqueConstraint(UniqueConstraintVO uniqueConstraint);

  /**
   * Gets the unique constraints.
   *
   * @param schemaId the schema id
   * @return the unique constraints
   */
  List<UniqueConstraintVO> getUniqueConstraints(String schemaId);

  /**
   * Gets the unique constraint.
   *
   * @param uniqueId the unique id
   * @return the unique constraint
   * @throws EEAException the EEA exception
   */
  UniqueConstraintVO getUniqueConstraint(String uniqueId) throws EEAException;

  /**
   * Delete uniques constraint from table.
   *
   * @param tableSchemaId the table schema id
   * @throws EEAException the EEA exception
   */
  void deleteUniquesConstraintFromTable(String tableSchemaId) throws EEAException;

  /**
   * Delete uniques constraint from dataset.
   *
   * @param datasetSchemaId the dataset schema id
   * @throws EEAException the EEA exception
   */
  void deleteUniquesConstraintFromDataset(String datasetSchemaId) throws EEAException;

  /**
   * Delete uniques constraint from field.
   *
   * @param schemaId the schema id
   * @param fieldSchemaId the field schema id
   * @throws EEAException the EEA exception
   */
  void deleteUniquesConstraintFromField(String schemaId, String fieldSchemaId) throws EEAException;

  /**
   * Creates the unique constraint PK.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   */
  void createUniqueConstraintPK(String datasetSchemaId, FieldSchemaVO fieldSchemaVO);

  /**
   * Delete only unique constraint from field.
   *
   * @param schemaId the schema id
   * @param fieldSchemaId the field schema id
   * @throws EEAException the EEA exception
   */
  void deleteOnlyUniqueConstraintFromField(String schemaId, String fieldSchemaId)
      throws EEAException;

  /**
   * Copy unique constraints catalogue.
   *
   * @param originDatasetSchemaIds the origin dataset schema ids
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   */
  void copyUniqueConstraintsCatalogue(List<String> originDatasetSchemaIds,
      Map<String, String> dictionaryOriginTargetObjectId);

  /**
   * Gets the simple schema.
   *
   * @param datasetId the dataset id
   * @return the simple schema
   * @throws EEAException the EEA exception
   */
  SimpleDatasetSchemaVO getSimpleSchema(Long datasetId) throws EEAException;

  /**
   * Check clear attachments.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @return the boolean
   */
  Boolean checkClearAttachments(Long datasetId, String datasetSchemaId,
      FieldSchemaVO fieldSchemaVO);

  /**
   * Update webform.
   *
   * @param datasetSchemaId the dataset schema id
   * @param webformVO the webform VO
   */
  void updateWebform(String datasetSchemaId, WebformVO webformVO);

  /**
   * Gets the table schema.
   *
   * @param tableSchemaId the table schema id
   * @param datasetSchemaId the dataset schema id
   * @return the table schema
   */
  TableSchema getTableSchema(String tableSchemaId, String datasetSchemaId);


  /**
   * Release create update view.
   *
   * @param datasetId the dataset id
   * @param user the user
   * @param checkSQL the check SQL
   */
  void releaseCreateUpdateView(Long datasetId, String user, boolean checkSQL);


  /**
   * Export schemas.
   *
   * @param dataflowId the dataflow id
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  byte[] exportSchemas(Long dataflowId) throws IOException, EEAException;


  /**
   * Import schemas.
   *
   * @param dataflowId the dataflow id
   * @param in the in
   * @param fileName the file name
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  void importSchemas(Long dataflowId, InputStream in, String fileName)
      throws IOException, EEAException;


  /**
   * Update dataset schema exportable.
   *
   * @param datasetSchemaId the dataset schema id
   * @param availableInPublic the available in public
   */
  void updateDatasetSchemaExportable(String datasetSchemaId, boolean availableInPublic);


  /**
   * Gets the table schemas ids.
   *
   * @param datasetId the dataset id
   * @return the table schemas ids
   * @throws EEAException the EEA exception
   */
  List<TableSchemaIdNameVO> getTableSchemasIds(Long datasetId) throws EEAException;


  /**
   * Update reference dataset.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param referenceDataset the reference dataset
   * @param updateTables the update tables
   */
  void updateReferenceDataset(Long datasetId, String datasetSchemaId, boolean referenceDataset,
      boolean updateTables);



  /**
   * Export fields schema.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @return the byte[]
   * @throws EEAException the EEA exception
   */
  byte[] exportFieldsSchema(final Long datasetId, final String datasetSchemaId,
      final String tableSchemaId) throws EEAException;


  /**
   * Import fields schema.
   *
   * @param tableSchemaId the table schema id
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   * @param file the file
   * @param replace the replace
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void importFieldsSchema(String tableSchemaId, String datasetSchemaId, Long datasetId,
      InputStream file, boolean replace);

}
