package org.eea.validation.persistence.data.repository;

import java.sql.SQLException;
import java.util.List;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.TableValue;

/**
 * The Interface DatasetExtendedRepository.
 */
public interface DatasetExtendedRepository {

  /**
   * Query RS execution.
   *
   * @param query the query
   * @param entityTypeEnum the entity type enum
   * @param entityName the entity name
   * @param datasetId the dataset id
   * @param idTable
   * @return the table value
   * @throws SQLException the SQL exception
   */
  TableValue queryRSExecution(String query, EntityTypeEnum entityTypeEnum, String entityName,
      Long datasetId, Long idTable);

  /**
   * Query record validation execution.
   *
   * @param query the query
   * @return the list
   */
  List<RecordValidation> queryRecordValidationExecution(String query);

  /**
   * Query field validation execution.
   *
   * @param query the query
   * @return the list
   */
  List<FieldValidation> queryFieldValidationExecution(String query);

  /**
   * Query unique result execution.
   *
   * @param stringQuery the string query
   * @return the object
   */
  List<Object> queryUniqueResultExecution(String stringQuery);

  /**
   * Gets the table id.
   *
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @return the table id
   */
  Long getTableId(String idTableSchema, Long datasetId);



}
