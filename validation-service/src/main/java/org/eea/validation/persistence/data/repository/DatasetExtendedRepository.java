package org.eea.validation.persistence.data.repository;

import java.sql.SQLException;
import java.util.List;
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
   * @return the table value
   * @throws SQLException
   */
  TableValue queryRSExecution(String query) throws SQLException;

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

}
