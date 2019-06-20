package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.util.SortField;

public interface RecordExtendedQueriesRepository {

  /**
   * Find by table value with order list.
   *
   * @param idTableSchema the id table schema
   * @param sortFields the sort fields
   *
   * @return the list
   */
  List<RecordValue> findByTableValueWithOrder(String idTableSchema, SortField... sortFields);

  /**
   * Find by table value no order list.
   *
   * @param idTableSchema the id table schema
   *
   * @return the list
   */
  List<RecordValue> findByTableValueNoOrder(String idTableSchema);

}
