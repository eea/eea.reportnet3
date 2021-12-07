package org.eea.validation.persistence.data.repository;

import java.util.List;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.validation.exception.EEAInvalidSQLException;
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
   * @param idTable the id table
   * @return the table value
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  TableValue queryRSExecution(String query, EntityTypeEnum entityTypeEnum, String entityName,
      Long datasetId, Long idTable) throws EEAInvalidSQLException;

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
   * Gets the table id.
   *
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @return the table id
   */
  Long getTableId(String idTableSchema, Long datasetId);

  /**
   * Validate query.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  void validateQuery(String query, Long datasetId) throws EEAInvalidSQLException;

  /**
   * Run sql rule.
   *
   * @param datasetId the dataset id
   * @param sqlRule the sql rule
   * @return the string formatted as JSON
   * @throws EEAInvalidSQLException the EEA invalid SQL exception
   */
  String runSqlRule(Long datasetId, String sqlRule) throws EEAInvalidSQLException;

}
