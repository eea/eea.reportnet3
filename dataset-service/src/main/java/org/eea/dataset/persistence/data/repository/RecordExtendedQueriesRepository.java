package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.springframework.data.domain.Pageable;

/**
 * The Interface RecordExtendedQueriesRepository.
 */
public interface RecordExtendedQueriesRepository {



  /**
   * Find by table value with order.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param levelErrorList the level error list
   * @param pageable the pageable
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   * @param sortFields the sort fields
   * @return the list
   */
  TableVO findByTableValueWithOrder(Long datasetId, String idTableSchema,
      List<ErrorTypeEnum> levelErrorList, Pageable pageable, List<String> idRules,
      String fieldSchema, String fieldValue, SortField... sortFields);



  /**
   * Find by table value no order.
   *
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @return the list
   */
  List<RecordValue> findByTableValueNoOrder(String idTableSchema, Pageable pageable);



  /**
   * Find by table value all records.
   *
   * @param idTableSchema the id table schema
   * @return the list
   */
  List<RecordValue> findByTableValueAllRecords(String idTableSchema);

  /**
   * Find by table value no order optimized.
   *
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @return the list
   */
  List<RecordValue> findByTableValueNoOrderOptimized(String idTableSchema, Pageable pageable);

  /**
   * Find and generate ETL json.
   *
   * @param stringQuery the string query
   * @return the stream
   */
  String findAndGenerateETLJson(String stringQuery);

  /**
   * Find ordered native record.
   *
   * @param idTable the id table
   * @param datasetId the dataset id
   * @return the list
   * @throws EEAException the EEA exception
   */
  List<RecordValue> findOrderedNativeRecord(Long idTable, Long datasetId);
}
