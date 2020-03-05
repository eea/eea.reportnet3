package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.util.SortField;
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
   * @param idTableSchema the id table schema
   * @param levelErrorList the level error list
   * @param pageable the pageable
   * @param sortFields the sort fields
   * @return the list
   */
  TableVO findByTableValueWithOrder(String idTableSchema, List<ErrorTypeEnum> levelErrorList,
      Pageable pageable, SortField... sortFields);



  /**
   * Find by table value no order.
   *
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @return the list
   */
  List<RecordValue> findByTableValueNoOrder(String idTableSchema, Pageable pageable);

}
